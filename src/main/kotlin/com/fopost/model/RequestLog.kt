@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames

/** One API request made with your keys. [message] carries the error text of a failed one. */
@Serializable
public data class RequestLog(
    val id: Long? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
    val time: Instant? = null,
    @SerialName("auth_method") @JsonNames("authMethod") val authMethod: String? = null,
    val method: String? = null,
    val route: String? = null,
    val path: String? = null,
    @SerialName("status_code") @JsonNames("statusCode") val statusCode: Int? = null,
    @SerialName("duration_ms") @JsonNames("durationMs") val durationMs: Int? = null,
    val platform: String? = null,
    val message: String? = null,
)

/** A page of the request log with its cursor and the period's totals. Iterates over its items. */
@Serializable
public data class RequestLogPage(
    val data: List<RequestLog> = emptyList(),
    val meta: RequestLogMeta? = null,
    val summary: RequestLogSummary? = null,
) : Iterable<RequestLog> {
    override fun iterator(): Iterator<RequestLog> = data.iterator()
}

/** [nextCursor] is null on the last page. */
@Serializable
public data class RequestLogMeta(
    @SerialName("next_cursor") @JsonNames("nextCursor") val nextCursor: String? = null,
    @SerialName("has_more") @JsonNames("hasMore") val hasMore: Boolean? = null,
)

/** Request and error counts since [since], bucketed over time. */
@Serializable
public data class RequestLogSummary(
    val since: Instant? = null,
    val total: Int? = null,
    val errors: Int? = null,
    val buckets: List<RequestLogBucket> = emptyList(),
)

/** Requests and errors in one time bucket starting at [at]. */
@Serializable
public data class RequestLogBucket(
    val at: Instant? = null,
    val total: Int? = null,
    val errors: Int? = null,
)
