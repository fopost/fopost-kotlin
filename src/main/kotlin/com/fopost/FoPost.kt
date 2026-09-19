package com.fopost

import com.fopost.internal.ApiClient
import com.fopost.internal.SDK_VERSION
import com.fopost.internal.defaultJson
import com.fopost.resource.AccountGroupsResource
import com.fopost.resource.AccountsResource
import com.fopost.resource.AdsResource
import com.fopost.resource.AnalyticsResource
import com.fopost.resource.AutomationsResource
import com.fopost.resource.CommunitiesResource
import com.fopost.resource.InboxResource
import com.fopost.resource.LabelsResource
import com.fopost.resource.MediaResource
import com.fopost.resource.PostsResource
import com.fopost.resource.ValidateResource
import com.fopost.resource.WebhooksResource
import com.fopost.resource.WorkspacesResource
import java.io.Closeable
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.OkHttpClient

/**
 * Client for the FoPost API.
 *
 * ```
 * FoPost("fp_...").use { client ->           // or set FOPOST_API_KEY
 *     val workspace = client.workspaces.list().first()
 *     val accounts = client.accounts.list(workspace.id)
 *
 *     val post = client.posts.create(
 *         CreatePostParams(
 *             workspaceId = workspace.id!!,
 *             accounts = accounts.mapNotNull { it.id },
 *             content = contentOf("Hello from Kotlin"),
 *         ),
 *     )
 *     client.posts.publish(post.id!!)
 * }
 * ```
 *
 * Every call is a `suspend` function, so it never blocks the thread it was started on. A 429, a
 * 5xx, and a connection failure are retried automatically; a cancelled coroutine cancels the
 * in-flight call and is never retried.
 *
 * The client owns a connection pool, so [close] it when you are done — unless you passed your own
 * [OkHttpClient], which stays yours to shut down. Instances are safe to share across coroutines.
 *
 * @param apiKey the key created in the dashboard under Settings → API Keys. Falls back to the
 *   `FOPOST_API_KEY` environment variable.
 * @param baseUrl another deployment to talk to. Falls back to `FOPOST_BASE_URL`, then to
 *   [DEFAULT_BASE_URL].
 * @param maxRetries total attempts per request, so the default of 3 means two retries.
 * @param timeout bounds one request, including its body. Ignored when [httpClient] is given.
 * @param httpClient your own transport, e.g. one carrying an interceptor or a shared pool.
 * @param userAgent prefixed to the SDK's own `User-Agent`, to identify your application.
 */
public class FoPost @JvmOverloads constructor(
    apiKey: String? = null,
    baseUrl: String? = null,
    maxRetries: Int = DEFAULT_MAX_RETRIES,
    timeout: Duration = DEFAULT_TIMEOUT,
    httpClient: OkHttpClient? = null,
    userAgent: String? = null,
) : Closeable {

    private val ownsHttpClient = httpClient == null

    private val okHttpClient: OkHttpClient = httpClient ?: OkHttpClient.Builder()
        .callTimeout(timeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        .connectTimeout(timeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        .readTimeout(timeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        .writeTimeout(timeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        .build()

    /** The JSON the SDK decodes and encodes with, exposed for the typed escape hatch. */
    public val json: Json = defaultJson()

    internal val api: ApiClient

    init {
        val key = apiKey?.takeIf { it.isNotBlank() } ?: System.getenv(API_KEY_ENV)?.takeIf { it.isNotBlank() }
        require(!key.isNullOrBlank()) {
            "fopost: an API key is required — pass one to FoPost(...) or set $API_KEY_ENV"
        }
        require(maxRetries >= 1) { "fopost: maxRetries must be at least 1" }

        val resolvedBaseUrl = (
            baseUrl?.takeIf { it.isNotBlank() }
                ?: System.getenv(BASE_URL_ENV)?.takeIf { it.isNotBlank() }
                ?: DEFAULT_BASE_URL
            ).trimEnd('/')

        val agent = "fopost-kotlin/$SDK_VERSION".let { if (userAgent.isNullOrBlank()) it else "$userAgent $it" }
        api = ApiClient(key, resolvedBaseUrl, maxRetries, agent, okHttpClient, json)
    }

    /** The API root every request is sent to. */
    public val baseUrl: String get() = api.baseUrl

    public val posts: PostsResource = PostsResource(api)
    public val workspaces: WorkspacesResource = WorkspacesResource(api)
    public val accounts: AccountsResource = AccountsResource(api)
    public val accountGroups: AccountGroupsResource = AccountGroupsResource(api)
    public val communities: CommunitiesResource = CommunitiesResource(api)
    public val labels: LabelsResource = LabelsResource(api)
    public val webhooks: WebhooksResource = WebhooksResource(api)
    public val analytics: AnalyticsResource = AnalyticsResource(api)
    public val automations: AutomationsResource = AutomationsResource(api)
    public val media: MediaResource = MediaResource(api)
    public val inbox: InboxResource = InboxResource(api)
    public val ads: AdsResource = AdsResource(api)
    public val validate: ValidateResource = ValidateResource(api)

    /**
     * Call an endpoint this SDK does not wrap yet, authenticated and retried like any other call.
     *
     * ```
     * val body = client.request("GET", "/analytics/overview", query = mapOf("days" to 30))
     * ```
     *
     * The `{"data": ...}` envelope is left in place — see [requestAs] to decode a payload.
     */
    public suspend fun request(
        method: String,
        path: String,
        body: JsonElement? = null,
        query: Map<String, Any?>? = null,
    ): JsonElement = api.send(method, path, body?.let { api.jsonBody(it) }, query)

    /** Release the connection pool. A caller-supplied [OkHttpClient] is left alone. */
    override fun close() {
        if (!ownsHttpClient) return
        okHttpClient.dispatcher.executorService.shutdown()
        okHttpClient.connectionPool.evictAll()
        okHttpClient.cache?.close()
    }

    public companion object {
        /** The version of this SDK, as reported in the `User-Agent`. */
        public const val VERSION: String = SDK_VERSION

        /** Where the API lives, unless a caller or the environment says otherwise. */
        public const val DEFAULT_BASE_URL: String = "https://api.fopost.com/v1"

        /** The environment variable read when no key is passed. */
        public const val API_KEY_ENV: String = "FOPOST_API_KEY"

        /** The environment variable read when no base url is passed. */
        public const val BASE_URL_ENV: String = "FOPOST_BASE_URL"

        /** Total attempts per request, so 3 means two retries. */
        public const val DEFAULT_MAX_RETRIES: Int = 3

        /** How long one request may take, including its body. */
        public val DEFAULT_TIMEOUT: Duration = 30.seconds
    }
}

/**
 * The typed half of the escape hatch: calls an unwrapped endpoint and decodes the `data` payload.
 *
 * ```
 * val platforms: List<String> = client.requestAs("GET", "/platforms")
 * ```
 */
public suspend inline fun <reified T> FoPost.requestAs(
    method: String,
    path: String,
    body: JsonElement? = null,
    query: Map<String, Any?>? = null,
): T {
    val element = request(method, path, body, query)
    val payload = (element as? JsonObject)?.get("data") ?: element
    return json.decodeFromJsonElement<T>(payload)
}
