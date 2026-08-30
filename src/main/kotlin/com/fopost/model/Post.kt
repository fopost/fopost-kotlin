@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonObject

/** A post: its content blocks, the accounts it targets, and its scheduling state. */
@Serializable
public data class Post(
    val id: String? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
    val status: String? = null,
    @SerialName("content_type") @JsonNames("contentType") val contentType: String? = null,
    @SerialName("schedule_at") @JsonNames("scheduleAt") val scheduleAt: Instant? = null,
    val repeatable: Boolean? = null,
    @SerialName("repeatable_times") @JsonNames("repeatableTimes") val repeatableTimes: Int? = null,
    @SerialName("repeatable_gap") @JsonNames("repeatableGap") val repeatableGap: Int? = null,
    @SerialName("repeatable_gap_unit") @JsonNames("repeatableGapUnit") val repeatableGapUnit: String? = null,
    @SerialName("remaining_posts") @JsonNames("remainingPosts") val remainingPosts: Int? = null,
    val title: String? = null,
    val summary: String? = null,
    @SerialName("auto_plug") @JsonNames("autoPlug") val autoPlug: Boolean? = null,
    @SerialName("auto_plug_content") @JsonNames("autoPlugContent") val autoPlugContent: String? = null,
    val content: List<ContentBlock> = emptyList(),
    val accounts: List<PostAccount> = emptyList(),
    val labels: List<LabelRef> = emptyList(),
    val settings: JsonObject? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    @SerialName("updated_at") @JsonNames("updatedAt") val updatedAt: Instant? = null,
)

/** An account a post targets, plus that account's delivery state. */
@Serializable
public data class PostAccount(
    val id: String? = null,
    val platform: String? = null,
    val username: String? = null,
    val name: String? = null,
    val avatar: String? = null,
    @SerialName("publish_status") @JsonNames("publishStatus") val publishStatus: String? = null,
    @SerialName("posted_at") @JsonNames("postedAt") val postedAt: Instant? = null,
    @SerialName("platform_post_id") @JsonNames("platformPostId") val platformPostId: String? = null,
    @SerialName("external_url") @JsonNames("externalUrl") val externalUrl: String? = null,
    @SerialName("error_code") @JsonNames("errorCode") val errorCode: String? = null,
    @SerialName("error_message") @JsonNames("errorMessage") val errorMessage: String? = null,
    @SerialName("raw_error_message") @JsonNames("rawErrorMessage") val rawErrorMessage: String? = null,
    val attempts: Int? = null,
    @SerialName("max_attempts") @JsonNames("maxAttempts") val maxAttempts: Int? = null,
)

/**
 * The answer to a publish request.
 *
 * A dry run fills [dryRun] and [post] and queues nothing; a real publish fills [postStatus] and
 * [deliveries]. Publishing returns when delivery is queued, not when it is live.
 */
@Serializable
public data class PublishResult(
    @SerialName("post_status") @JsonNames("postStatus") val postStatus: String? = null,
    val deliveries: List<Delivery> = emptyList(),
    @SerialName("health_warnings") @JsonNames("healthWarnings") val healthWarnings: List<HealthWarning> = emptyList(),
    @SerialName("dry_run") @JsonNames("dryRun") val dryRun: Boolean? = null,
    val post: JsonObject? = null,
)

/** The answer to a retry: what was re-queued, and what has run out of attempts. */
@Serializable
public data class RetryResult(
    @SerialName("post_status") @JsonNames("postStatus") val postStatus: String? = null,
    val deliveries: List<Delivery> = emptyList(),
    val exceeded: List<ExceededDelivery> = emptyList(),
)

/** A delivery skipped by a retry because it is out of attempts. */
@Serializable
public data class ExceededDelivery(
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    val platform: String? = null,
    val attempts: Int? = null,
)

/** The answer to a cancel: the deliveries that were pending and are now cancelled. */
@Serializable
public data class CancelResult(
    @SerialName("post_status") @JsonNames("postStatus") val postStatus: String? = null,
    val deliveries: List<Delivery> = emptyList(),
)

/**
 * A preflight check: per-account blockers and advisory signals, without publishing anything.
 *
 * [ready] is false when any account has a hard blocker in its `issues`.
 */
@Serializable
public data class PreflightResult(
    val ready: Boolean? = null,
    val post: JsonObject? = null,
    val accounts: List<PreflightAccount> = emptyList(),
)

/** One account's preflight verdict. */
@Serializable
public data class PreflightAccount(
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    val platform: String? = null,
    val username: String? = null,
    val ready: Boolean? = null,
    val issues: List<String> = emptyList(),
    val score: Double? = null,
    val signals: List<ContentSignal> = emptyList(),
)

/** One attempt at publishing a post, with a delivery row per targeted account. */
@Serializable
public data class PublishRun(
    val id: String? = null,
    @SerialName("run_number") @JsonNames("runNumber") val runNumber: Int? = null,
    val status: String? = null,
    @SerialName("started_at") @JsonNames("startedAt") val startedAt: Instant? = null,
    @SerialName("completed_at") @JsonNames("completedAt") val completedAt: Instant? = null,
    val deliveries: List<PublishRunDelivery> = emptyList(),
)

/** One account's row inside a publish run. */
@Serializable
public data class PublishRunDelivery(
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    @SerialName("account_name") @JsonNames("accountName") val accountName: String? = null,
    val username: String? = null,
    val platform: String? = null,
    val status: String? = null,
    @SerialName("attempt_number") @JsonNames("attemptNumber") val attemptNumber: Int? = null,
    @SerialName("error_code") @JsonNames("errorCode") val errorCode: String? = null,
    @SerialName("error_message") @JsonNames("errorMessage") val errorMessage: String? = null,
    @SerialName("platform_post_id") @JsonNames("platformPostId") val platformPostId: String? = null,
    @SerialName("external_url") @JsonNames("externalUrl") val externalUrl: String? = null,
    @SerialName("started_at") @JsonNames("startedAt") val startedAt: Instant? = null,
    @SerialName("completed_at") @JsonNames("completedAt") val completedAt: Instant? = null,
    @SerialName("duration_ms") @JsonNames("durationMs") val durationMs: Long? = null,
)

/** Reach and engagement for one post, totalled and split per platform. */
@Serializable
public data class PostAnalytics(
    @SerialName("post_id") @JsonNames("postId") val postId: String? = null,
    val totals: PostAnalyticsTotals? = null,
    val platforms: List<PostPlatformMetrics> = emptyList(),
    @SerialName("last_fetched_at") @JsonNames("lastFetchedAt") val lastFetchedAt: Instant? = null,
)

/** A post's metrics summed over every platform it reached. */
@Serializable
public data class PostAnalyticsTotals(
    val impressions: Long? = null,
    val reach: Long? = null,
    val engagements: Long? = null,
    val likes: Long? = null,
    val comments: Long? = null,
    val shares: Long? = null,
    val reposts: Long? = null,
    val clicks: Long? = null,
    val saves: Long? = null,
    @SerialName("video_views") @JsonNames("videoViews") val videoViews: Long? = null,
    val follows: Long? = null,
)

/** One platform's copy of a post, and what it did there. */
@Serializable
public data class PostPlatformMetrics(
    val platform: String? = null,
    val username: String? = null,
    @SerialName("external_post_id") @JsonNames("externalPostId") val externalPostId: String? = null,
    val permalink: String? = null,
    @SerialName("thumbnail_url") @JsonNames("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerialName("media_type") @JsonNames("mediaType") val mediaType: String? = null,
    @SerialName("posted_at") @JsonNames("postedAt") val postedAt: Instant? = null,
    val metrics: JsonObject? = null,
)

/** The copy a duplicate call created, always as a fresh draft. */
@Serializable
public data class DuplicatedPost(
    val id: String? = null,
    val status: String? = null,
)

/** How many posts a bulk shift, relabel, or delete touched. */
@Serializable
public data class BulkActionResult(
    val updated: Int? = null,
    val action: String? = null,
    val mode: String? = null,
)

/** The dry-run result of a bulk-import CSV: every row, with the reasons any cannot be created. */
@Serializable
public data class BulkImportValidation(
    @SerialName("total_rows") @JsonNames("totalRows") val totalRows: Int? = null,
    @SerialName("valid_rows") @JsonNames("validRows") val validRows: Int? = null,
    @SerialName("invalid_rows") @JsonNames("invalidRows") val invalidRows: Int? = null,
    val rows: List<BulkImportRow> = emptyList(),
)

/** One row of a bulk-import CSV, as validation read it. */
@Serializable
public data class BulkImportRow(
    val row: Int? = null,
    @SerialName("content_preview") @JsonNames("contentPreview") val contentPreview: String? = null,
    @SerialName("schedule_at") @JsonNames("scheduleAt") val scheduleAt: String? = null,
    val accounts: List<String> = emptyList(),
    val labels: Int? = null,
    @SerialName("has_media") @JsonNames("hasMedia") val hasMedia: Boolean? = null,
    val errors: List<String> = emptyList(),
)

/** What a committed bulk-import batch created. Keep [batchId] to roll the batch back. */
@Serializable
public data class BulkImportResult(
    @SerialName("batch_id") @JsonNames("batchId") val batchId: String? = null,
    val created: Int? = null,
    val posts: List<BulkImportCreatedPost> = emptyList(),
)

/** One post a bulk import created. */
@Serializable
public data class BulkImportCreatedPost(
    val id: String? = null,
    @SerialName("schedule_at") @JsonNames("scheduleAt") val scheduleAt: Instant? = null,
)
