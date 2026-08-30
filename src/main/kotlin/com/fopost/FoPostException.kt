package com.fopost

import java.time.Instant
import kotlin.time.Duration
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** The per-key, per-minute budget the API reports on every response. */
public data class RateLimit(
    val limit: Int? = null,
    val remaining: Int? = null,
    val reset: Instant? = null,
)

/**
 * Base class for every error the SDK raises.
 *
 * The API answers a failure with `{"error": "<code>", "message": "<explanation>"}`, which maps
 * onto [code] and `message`. [body] is the decoded response, for the extra fields an individual
 * error carries.
 */
public sealed class FoPostException(
    message: String,
    public val status: Int,
    public val code: String?,
    public val body: JsonElement?,
    public val rateLimit: RateLimit? = null,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {

    /** An extra field the error body carries alongside `error` and `message`. */
    public fun field(name: String): JsonElement? = (body as? JsonObject)?.get(name)

    protected fun stringField(name: String): String? =
        (field(name) as? JsonPrimitive)?.takeIf { it.isString }?.content

    override fun toString(): String {
        val suffix = code?.let { " ($it)" } ?: ""
        return "[$status$suffix] $message"
    }
}

/** 400 or 422 — the request was rejected before anything happened. */
public class ValidationException internal constructor(
    message: String,
    status: Int,
    code: String?,
    body: JsonElement?,
    rateLimit: RateLimit? = null,
) : FoPostException(message, status, code, body, rateLimit)

/** 401 — the API key is missing, invalid, or expired. */
public class AuthenticationException internal constructor(
    message: String,
    status: Int,
    code: String?,
    body: JsonElement?,
    rateLimit: RateLimit? = null,
) : FoPostException(message, status, code, body, rateLimit)

/** 402 — no active subscription, or the AI credit balance ran out. */
public class PaymentRequiredException internal constructor(
    message: String,
    status: Int,
    code: String?,
    body: JsonElement?,
    rateLimit: RateLimit? = null,
) : FoPostException(message, status, code, body, rateLimit) {

    /** Where the API suggests sending the user to restore access, when it says. */
    public val upgradeUrl: String? get() = stringField("upgrade_url")
}

/** 403 — the key is valid but lacks the scope or the workspace. */
public class PermissionDeniedException internal constructor(
    message: String,
    status: Int,
    code: String?,
    body: JsonElement?,
    rateLimit: RateLimit? = null,
) : FoPostException(message, status, code, body, rateLimit)

/** 404 — no such resource, or it is outside the key's reach. */
public class NotFoundException internal constructor(
    message: String,
    status: Int,
    code: String?,
    body: JsonElement?,
    rateLimit: RateLimit? = null,
) : FoPostException(message, status, code, body, rateLimit)

/** 429 — the rate limit was exceeded. Raised only once the automatic retries are spent. */
public class RateLimitException internal constructor(
    message: String,
    status: Int,
    code: String?,
    body: JsonElement?,
    rateLimit: RateLimit? = null,
    /** How long the API asked the caller to wait, when it sent `Retry-After`. */
    public val retryAfter: Duration? = null,
) : FoPostException(message, status, code, body, rateLimit)

/** 5xx — the API failed. Retried automatically before it reaches you. */
public class ServerException internal constructor(
    message: String,
    status: Int,
    code: String?,
    body: JsonElement?,
    rateLimit: RateLimit? = null,
) : FoPostException(message, status, code, body, rateLimit)

/** Any other non-2xx status, so a new one still arrives as a typed error. */
public class ApiException internal constructor(
    message: String,
    status: Int,
    code: String?,
    body: JsonElement?,
    rateLimit: RateLimit? = null,
) : FoPostException(message, status, code, body, rateLimit)

/** The request never got an answer: a connection failure, a timeout, or an unreadable body. */
public class TransportException internal constructor(
    message: String,
    cause: Throwable? = null,
) : FoPostException(message, 0, null, null, null, cause)
