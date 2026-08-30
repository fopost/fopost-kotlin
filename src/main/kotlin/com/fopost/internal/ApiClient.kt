package com.fopost.internal

import com.fopost.ApiException
import com.fopost.AuthenticationException
import com.fopost.FoPostException
import com.fopost.NotFoundException
import com.fopost.PaymentRequiredException
import com.fopost.PermissionDeniedException
import com.fopost.RateLimit
import com.fopost.RateLimitException
import com.fopost.ServerException
import com.fopost.TransportException
import com.fopost.ValidationException
import com.fopost.model.Page
import com.fopost.model.PageMeta
import java.io.IOException
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

/**
 * The HTTP layer: auth headers, query encoding, the `{"data": ...}` envelope unwrap, retries,
 * and the mapping from a failed response to a typed exception.
 */
internal class ApiClient(
    private val apiKey: String,
    val baseUrl: String,
    private val maxRetries: Int,
    private val userAgent: String,
    val http: OkHttpClient,
    val json: Json,
) {

    /** Test seam: how the retry loop waits. */
    internal var sleeper: suspend (Duration) -> Unit = { delay(it) }

    suspend fun send(
        method: String,
        path: String,
        body: RequestBody? = null,
        query: Map<String, Any?>? = null,
    ): JsonElement {
        val request = Request.Builder()
            .url(url(path, query))
            .header("Accept", "application/json")
            .header("X-API-Key", apiKey)
            .header("User-Agent", userAgent)
            .method(method, body ?: emptyBodyFor(method))
            .build()

        var attempt = 1
        while (true) {
            val response = try {
                http.newCall(request).await()
            } catch (e: IOException) {
                if (attempt >= maxRetries) {
                    throw TransportException("fopost: $method $path failed: ${e.message}", e)
                }
                sleeper(backoff(attempt))
                attempt++
                continue
            }

            val status: Int
            val text: String
            val limit: RateLimit?
            val retryAfter: Duration?
            response.use {
                status = it.code
                limit = rateLimitOf(it)
                retryAfter = retryAfterOf(it)
                text = try {
                    it.body?.string().orEmpty()
                } catch (e: IOException) {
                    throw TransportException("fopost: reading the response to $method $path failed", e)
                }
            }

            if (status in 200..299) {
                return parse(text, status)
            }
            if (attempt < maxRetries && (status == 429 || status >= 500)) {
                val wait = if (status == 429 && retryAfter != null) minOf(retryAfter, MAX_RETRY_WAIT) else backoff(attempt)
                sleeper(wait)
                attempt++
                continue
            }
            throw errorFor(status, text, limit, retryAfter)
        }
    }

    suspend fun <T> call(
        method: String,
        path: String,
        deserializer: KSerializer<T>,
        body: RequestBody? = null,
        query: Map<String, Any?>? = null,
        unwrap: Boolean = true,
    ): T {
        val element = send(method, path, body, query)
        val payload = if (unwrap) unwrapEnvelope(element) else element
        return decode(deserializer, payload)
    }

    suspend fun <T> callList(
        method: String,
        path: String,
        deserializer: KSerializer<T>,
        body: RequestBody? = null,
        query: Map<String, Any?>? = null,
    ): List<T> = call(method, path, ListSerializer(deserializer), body, query)

    /** A list endpoint's page: its items plus the `meta` block that sits beside them. */
    suspend fun <T> page(
        path: String,
        deserializer: KSerializer<T>,
        query: Map<String, Any?>? = null,
    ): Page<T> {
        val element = send("GET", path, null, query) as? JsonObject ?: return Page(emptyList(), null)
        val items = element["data"]
            ?.takeUnless { it is JsonNull }
            ?.let { decode(ListSerializer(deserializer), it) }
            ?: emptyList()
        val meta = (element["meta"] as? JsonObject)?.let { decode(PageMeta.serializer(), it) }
        return Page(items, meta)
    }

    fun <T> jsonBody(value: T, serializer: SerializationStrategy<T>): RequestBody =
        json.encodeToString(serializer, value).toRequestBody(JSON_MEDIA_TYPE)

    fun jsonBody(element: JsonElement): RequestBody = element.toString().toRequestBody(JSON_MEDIA_TYPE)

    private fun <T> decode(deserializer: KSerializer<T>, payload: JsonElement): T =
        try {
            json.decodeFromJsonElement(deserializer, payload)
        } catch (e: SerializationException) {
            throw TransportException("fopost: could not decode the response: ${e.message}", e)
        }

    private fun url(path: String, query: Map<String, Any?>?): okhttp3.HttpUrl {
        val absolute = if (path.startsWith("http://") || path.startsWith("https://")) {
            path
        } else {
            baseUrl + if (path.startsWith("/")) path else "/$path"
        }
        val builder = absolute.toHttpUrl().newBuilder()
        query?.forEach { (key, value) ->
            when (value) {
                null -> Unit
                is Iterable<*> -> value.filterNotNull().forEach { builder.addQueryParameter(key, it.toString()) }
                else -> builder.addQueryParameter(key, value.toString())
            }
        }
        return builder.build()
    }

    private fun parse(text: String, status: Int): JsonElement {
        if (status == 204 || text.isBlank()) return JsonNull
        return try {
            json.parseToJsonElement(text)
        } catch (e: SerializationException) {
            throw TransportException("fopost: expected a JSON response, got ${text.take(120)}", e)
        }
    }

    private fun errorFor(status: Int, text: String, limit: RateLimit?, retryAfter: Duration?): FoPostException {
        val body = runCatching { json.parseToJsonElement(text) }.getOrNull()?.takeUnless { it is JsonNull }
        val obj = body as? JsonObject
        val code = obj?.stringOf("error")
        val message = obj?.stringOf("message") ?: code ?: "HTTP $status"

        return when {
            status == 400 || status == 422 -> ValidationException(message, status, code, body, limit)
            status == 401 -> AuthenticationException(message, status, code, body, limit)
            status == 402 -> PaymentRequiredException(message, status, code, body, limit)
            status == 403 -> PermissionDeniedException(message, status, code, body, limit)
            status == 404 -> NotFoundException(message, status, code, body, limit)
            status == 429 -> RateLimitException(message, status, code, body, limit, retryAfter)
            status >= 500 -> ServerException(message, status, code, body, limit)
            else -> ApiException(message, status, code, body, limit)
        }
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json".toMediaType()
        val MAX_RETRY_WAIT = 60.seconds
        val BASE_RETRY_WAIT = 500.milliseconds
        val EMPTY_BODY: RequestBody = ByteArray(0).toRequestBody(null)

        fun emptyBodyFor(method: String): RequestBody? =
            if (method.uppercase() in setOf("POST", "PUT", "PATCH")) EMPTY_BODY else null

        fun backoff(attempt: Int): Duration =
            minOf(BASE_RETRY_WAIT * (1 shl (attempt - 1)), MAX_RETRY_WAIT)

        fun JsonObject.stringOf(key: String): String? =
            (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content?.takeIf { it.isNotEmpty() }

        /** Peels the `{"data": ...}` wrapper, which some endpoints send and some do not. */
        fun unwrapEnvelope(element: JsonElement): JsonElement =
            (element as? JsonObject)?.get("data") ?: element

        fun rateLimitOf(response: Response): RateLimit? {
            val limit = response.header("X-RateLimit-Limit")?.toIntOrNull()
            val remaining = response.header("X-RateLimit-Remaining")?.toIntOrNull()
            val resetRaw = response.header("X-RateLimit-Reset")?.toLongOrNull()
            if (limit == null && remaining == null && resetRaw == null) return null
            // The API sends a unix timestamp; tolerate a delta from a proxy in front of it.
            val reset = resetRaw?.let {
                if (it > 1_000_000_000L) Instant.ofEpochSecond(it) else Instant.now().plusSeconds(it)
            }
            return RateLimit(limit, remaining, reset)
        }

        /** `Retry-After` is either delta-seconds or an HTTP date. */
        fun retryAfterOf(response: Response): Duration? {
            val raw = response.header("Retry-After")?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            raw.toDoubleOrNull()?.let { return maxOf(it, 0.0).seconds }
            return runCatching {
                val target = ZonedDateTime.parse(raw, DateTimeFormatter.RFC_1123_DATE_TIME)
                val delta = java.time.Duration.between(ZonedDateTime.now(target.zone), target)
                if (delta.isNegative) Duration.ZERO else delta.toMillis().milliseconds
            }.getOrNull()
        }
    }
}
