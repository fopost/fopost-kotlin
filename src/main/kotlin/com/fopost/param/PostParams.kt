@file:UseSerializers(InstantSerializer::class)

package com.fopost.param

import com.fopost.internal.InstantSerializer
import com.fopost.model.MediaItem
import com.fopost.model.PostStatus
import java.time.Instant
import java.time.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonObject

/**
 * One block of a post being written.
 *
 * A single block is a plain update; several make a thread.
 */
@Serializable
public data class ContentBlockInput(
    val text: String,
    val media: List<MediaItem>? = null,
)

/** Shorthand for a post made of plain text blocks. */
public fun contentOf(vararg text: String): List<ContentBlockInput> = text.map { ContentBlockInput(it) }

/**
 * A post to create.
 *
 * [status] is `draft` or `scheduled`, and a scheduled post needs [scheduleAt]. To send something
 * out now, create it and call `posts.publish(id)`.
 */
@Serializable
public data class CreatePostParams(
    @SerialName("workspace_id") val workspaceId: String,
    val accounts: List<String>,
    val content: List<ContentBlockInput>,
    val status: String = PostStatus.DRAFT,
    @SerialName("content_type") val contentType: String? = null,
    @SerialName("artifact_type") val artifactType: String? = null,
    @SerialName("schedule_at") val scheduleAt: Instant? = null,
    val repeatable: Boolean? = null,
    @SerialName("repeatable_times") val repeatableTimes: Int? = null,
    @SerialName("repeatable_gap") val repeatableGap: Int? = null,
    @SerialName("repeatable_gap_unit") val repeatableGapUnit: String? = null,
    val labels: List<String>? = null,
    val title: String? = null,
    @SerialName("internal_title") val internalTitle: String? = null,
    val summary: String? = null,
    @SerialName("auto_plug") val autoPlug: Boolean? = null,
    @SerialName("auto_plug_content") val autoPlugContent: String? = null,
    val settings: JsonObject? = null,
    @SerialName("source_ids") val sourceIds: List<String>? = null,
    @SerialName("companion_of") val companionOf: String? = null,
) {
    /** The same post, scheduled instead of held as a draft. */
    public fun scheduledAt(at: Instant): CreatePostParams =
        copy(status = PostStatus.SCHEDULED, scheduleAt = at)
}

/**
 * A partial update to a post. Only the fields you set are sent, so everything else is left alone.
 *
 * Setting [content] or [accounts] replaces the whole list.
 */
@Serializable
public data class UpdatePostParams(
    val accounts: List<String>? = null,
    val content: List<ContentBlockInput>? = null,
    val status: String? = null,
    @SerialName("content_type") val contentType: String? = null,
    @SerialName("artifact_type") val artifactType: String? = null,
    @SerialName("schedule_at") val scheduleAt: Instant? = null,
    val repeatable: Boolean? = null,
    @SerialName("repeatable_times") val repeatableTimes: Int? = null,
    @SerialName("repeatable_gap") val repeatableGap: Int? = null,
    @SerialName("repeatable_gap_unit") val repeatableGapUnit: String? = null,
    val labels: List<String>? = null,
    val title: String? = null,
    @SerialName("internal_title") val internalTitle: String? = null,
    val summary: String? = null,
    @SerialName("auto_plug") val autoPlug: Boolean? = null,
    @SerialName("auto_plug_content") val autoPlugContent: String? = null,
    val settings: JsonObject? = null,
)

/** Rehearsal switch for a publish. */
@Serializable
public data class PublishOptions(val dryRun: Boolean)

/** Options for a publish: a subset of the post's accounts, and whether to only rehearse it. */
@Serializable
public data class PublishParams(
    val accountIds: List<String>? = null,
    val options: PublishOptions? = null,
) {
    public constructor(dryRun: Boolean) : this(null, PublishOptions(dryRun))
}

/** Options for retrying a post's failed deliveries. */
@Serializable
public data class RetryParams(
    val accountIds: List<String>? = null,
    /** Also re-send to accounts that already published. Off by default, and rarely what you want. */
    val includePublished: Boolean? = null,
)

/** Which of a post's pending deliveries to cancel. Empty cancels them all. */
@Serializable
public data class CancelParams(val accountIds: List<String>? = null)

/** Filters for listing posts. Every field is optional. */
public data class PostListParams(
    val workspaceId: String? = null,
    val status: String? = null,
    val search: String? = null,
    val platform: String? = null,
    val label: String? = null,
    val accountId: String? = null,
    val date: LocalDate? = null,
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val sort: String? = null,
    val page: Int? = null,
    val perPage: Int? = null,
) {
    public fun toQuery(): Map<String, Any?> = mapOf(
        "workspace_id" to workspaceId,
        "status" to status,
        "search" to search,
        "platform" to platform,
        "label" to label,
        "account_id" to accountId,
        "date" to date?.toString(),
        "from" to from?.toString(),
        "to" to to?.toString(),
        "sort" to sort,
        "page" to page,
        "per_page" to perPage,
    )
}
