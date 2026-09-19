@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames

/** Pagination counters returned alongside a page of results. */
@Serializable
public data class PageMeta(
    @SerialName("current_page") @JsonNames("currentPage", "page") val currentPage: Int? = null,
    @SerialName("per_page") @JsonNames("perPage") val perPage: Int? = null,
    val total: Int? = null,
    @SerialName("last_page") @JsonNames("lastPage") val lastPage: Int? = null,
    val from: Int? = null,
    val to: Int? = null,
)

/** One page of a list endpoint: its items plus the pagination meta. Iterates over its items. */
public data class Page<out T>(
    val data: List<T> = emptyList(),
    val meta: PageMeta? = null,
) : Iterable<T> {
    override fun iterator(): Iterator<T> = data.iterator()
    public val size: Int get() = data.size
    public fun isEmpty(): Boolean = data.isEmpty()
}

/**
 * A media attachment on a content block.
 *
 * `type` is `image`, `video` or `gif`. The same shape is used to attach a file to a post.
 */
@Serializable
public data class MediaItem(
    val type: String? = null,
    val name: String? = null,
    val url: String? = null,
    val size: Long? = null,
    val alt: String? = null,
    val thumbnail: String? = null,
)

/** One block of a post. A single block is a plain update; several make a thread. */
@Serializable
public data class ContentBlock(
    val id: String? = null,
    val text: String? = null,
    val media: List<MediaItem> = emptyList(),
    val position: Int? = null,
)

/** A label as it appears attached to a post. */
@Serializable
public data class LabelRef(
    val id: String? = null,
    val name: String? = null,
    val color: String? = null,
)

/** A short reference to a workspace, as embedded in another resource. */
@Serializable
public data class WorkspaceRef(
    val id: String? = null,
    val name: String? = null,
    val slug: String? = null,
    val type: String? = null,
)

/** One post-to-account delivery, and how far it got. */
@Serializable
public data class Delivery(
    val id: String? = null,
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    val status: String? = null,
    val platform: String? = null,
    val username: String? = null,
    @SerialName("account_name") @JsonNames("accountName") val accountName: String? = null,
    @SerialName("error_code") @JsonNames("errorCode") val errorCode: String? = null,
    @SerialName("error_message") @JsonNames("errorMessage") val errorMessage: String? = null,
    val attempts: Int? = null,
    @SerialName("max_attempts") @JsonNames("maxAttempts") val maxAttempts: Int? = null,
    @SerialName("scheduled_publish_at") @JsonNames("scheduledPublishAt") val scheduledPublishAt: Instant? = null,
    @SerialName("delay_reason") @JsonNames("delayReason") val delayReason: String? = null,
    @SerialName("delay_message") @JsonNames("delayMessage") val delayMessage: String? = null,
    @SerialName("posted_at") @JsonNames("postedAt") val postedAt: Instant? = null,
    @SerialName("last_attempt_at") @JsonNames("lastAttemptAt") val lastAttemptAt: Instant? = null,
    @SerialName("platform_post_id") @JsonNames("platformPostId") val platformPostId: String? = null,
    @SerialName("external_url") @JsonNames("externalUrl") val externalUrl: String? = null,
)

/** An advisory content signal from a preflight check. `level` is `info` or `warn`. */
@Serializable
public data class ContentSignal(
    val level: String? = null,
    val code: String? = null,
    val message: String? = null,
)

/** An account whose credentials look shaky, reported alongside a publish. */
@Serializable
public data class HealthWarning(
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    val platform: String? = null,
    @SerialName("health_status") @JsonNames("healthStatus") val healthStatus: String? = null,
    val message: String? = null,
)
