package com.fopost

import com.fopost.internal.defaultJson
import java.security.MessageDigest
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * A verified webhook delivery: the event name, its payload and when it was sent.
 *
 * [data] is the event-specific body; decode it with [dataAs], e.g.
 * `envelope.dataAs<BlogArticlePublished>()` for `article.published`.
 */
public data class WebhookEnvelope(
    val event: String,
    val data: JsonElement,
    /** ISO 8601, as the API wrote it. */
    val timestamp: String,
) {
    /** Decodes [data] with an explicit serializer. */
    public fun <T> dataAs(serializer: KSerializer<T>): T = Webhooks.json.decodeFromJsonElement(serializer, data)
}

/** Decodes the envelope's [WebhookEnvelope.data] into a type of yours. */
public inline fun <reified T> WebhookEnvelope.dataAs(): T = Webhooks.json.decodeFromJsonElement<T>(data)

/**
 * Verifies the signature on a webhook delivery, for the endpoints created with
 * `client.webhooks` and for the blog's website target alike.
 *
 * ```
 * val envelope = Webhooks.verify(rawBody, headers, secret)
 * if (envelope.event == WebhookEvents.ARTICLE_PUBLISHED) {
 *     val article = envelope.dataAs<BlogArticlePublished>()
 * }
 * ```
 *
 * Pass the body exactly as received. Parsing it and serialising it again changes the bytes, and
 * the signature no longer matches.
 */
public object Webhooks {

    public const val SIGNATURE_HEADER: String = "X-FoPost-Signature"
    public const val TIMESTAMPED_SIGNATURE_HEADER: String = "X-FoPost-Signature-256"
    public const val TIMESTAMP_HEADER: String = "X-FoPost-Timestamp"
    public const val EVENT_HEADER: String = "X-FoPost-Event"
    public const val DELIVERY_HEADER: String = "X-FoPost-Delivery"

    /** How far a delivery's timestamp may sit from now before it is refused as a replay. */
    public val DEFAULT_TOLERANCE: Duration = 300.seconds

    /** The JSON the envelope and its payload are decoded with. */
    public val json: Json = defaultJson()

    /**
     * Verifies [rawBody] against the signing [headers] and returns the parsed envelope.
     *
     * The timestamped signature (`X-FoPost-Signature-256` over `<timestamp>.<body>`) is checked
     * whenever `X-FoPost-Timestamp` is present, and refused when the timestamp is further than
     * [tolerance] from [now]. Without a timestamp header the plain `X-FoPost-Signature` over the
     * body is accepted. Header names are matched case-insensitively.
     *
     * @throws WebhookVerificationException when the signature is missing, wrong or stale, or the
     *   body is not a webhook envelope.
     */
    @JvmStatic
    @JvmOverloads
    public fun verify(
        rawBody: ByteArray,
        headers: Map<String, String>,
        secret: String,
        tolerance: Duration = DEFAULT_TOLERANCE,
        now: Instant = Instant.now(),
    ): WebhookEnvelope {
        val timestamp = header(headers, TIMESTAMP_HEADER)
        if (timestamp != null) {
            val signature = header(headers, TIMESTAMPED_SIGNATURE_HEADER)
                ?: throw WebhookVerificationException("fopost: $TIMESTAMPED_SIGNATURE_HEADER is missing", "missing_signature")
            val sent = timestamp.toLongOrNull()
                ?: throw WebhookVerificationException("fopost: $TIMESTAMP_HEADER is not unix seconds", "stale_timestamp")
            if (abs(now.epochSecond - sent) > tolerance.inWholeSeconds) {
                throw WebhookVerificationException("fopost: the delivery timestamp is outside the tolerance", "stale_timestamp")
            }
            val signed = "$timestamp.".toByteArray(Charsets.UTF_8) + rawBody
            if (!matches(signature, hmac(secret, signed))) {
                throw WebhookVerificationException("fopost: $TIMESTAMPED_SIGNATURE_HEADER does not match the body", "invalid_signature")
            }
        } else {
            val signature = header(headers, SIGNATURE_HEADER)
                ?: throw WebhookVerificationException("fopost: $SIGNATURE_HEADER is missing", "missing_signature")
            if (!matches(signature, hmac(secret, rawBody))) {
                throw WebhookVerificationException("fopost: $SIGNATURE_HEADER does not match the body", "invalid_signature")
            }
        }
        return parse(rawBody)
    }

    /** [verify] for a body held as text; it is signed as UTF-8. */
    @JvmStatic
    @JvmOverloads
    public fun verify(
        rawBody: String,
        headers: Map<String, String>,
        secret: String,
        tolerance: Duration = DEFAULT_TOLERANCE,
        now: Instant = Instant.now(),
    ): WebhookEnvelope = verify(rawBody.toByteArray(Charsets.UTF_8), headers, secret, tolerance, now)

    /** The `sha256=<hex>` value the API sends for these exact bytes, for tests and fixtures. */
    @JvmStatic
    public fun sign(rawBody: ByteArray, secret: String, timestamp: Long? = null): String {
        val signed = if (timestamp == null) rawBody else "$timestamp.".toByteArray(Charsets.UTF_8) + rawBody
        return "sha256=${hmac(secret, signed)}"
    }

    private fun header(headers: Map<String, String>, name: String): String? =
        headers.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value?.trim()?.takeIf { it.isNotEmpty() }

    private fun hmac(secret: String, bytes: ByteArray): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return mac.doFinal(bytes).joinToString("") { "%02x".format(it) }
    }

    private fun matches(header: String, expectedHex: String): Boolean {
        val received = header.removePrefix("sha256=").lowercase().toByteArray(Charsets.US_ASCII)
        return MessageDigest.isEqual(received, expectedHex.toByteArray(Charsets.US_ASCII))
    }

    private fun parse(rawBody: ByteArray): WebhookEnvelope {
        val element = try {
            json.parseToJsonElement(rawBody.toString(Charsets.UTF_8))
        } catch (e: SerializationException) {
            throw WebhookVerificationException("fopost: the body is not JSON", "invalid_payload")
        }
        val obj = element as? JsonObject
            ?: throw WebhookVerificationException("fopost: the body is not a webhook envelope", "invalid_payload")
        val event = (obj["event"] as? JsonPrimitive)?.takeIf { it.isString }?.content
            ?: throw WebhookVerificationException("fopost: the envelope has no event", "invalid_payload")
        val timestamp = (obj["timestamp"] as? JsonPrimitive)?.content ?: ""
        return WebhookEnvelope(event, obj["data"] ?: JsonNull, timestamp)
    }
}
