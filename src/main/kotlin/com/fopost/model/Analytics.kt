@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonObject

/** Headline totals for the requested window, with the per-platform and per-account breakdown. */
@Serializable
public data class AnalyticsOverview(
    @SerialName("total_accounts") @JsonNames("totalAccounts") val totalAccounts: Int? = null,
    @SerialName("total_followers") @JsonNames("totalFollowers") val totalFollowers: Long? = null,
    @SerialName("total_posts") @JsonNames("totalPosts") val totalPosts: Long? = null,
    @SerialName("total_engagement") @JsonNames("totalEngagement") val totalEngagement: Long? = null,
    @SerialName("total_impressions") @JsonNames("totalImpressions") val totalImpressions: Long? = null,
    @SerialName("total_reach") @JsonNames("totalReach") val totalReach: Long? = null,
    @SerialName("total_likes") @JsonNames("totalLikes") val totalLikes: Long? = null,
    @SerialName("total_comments") @JsonNames("totalComments") val totalComments: Long? = null,
    @SerialName("total_shares") @JsonNames("totalShares") val totalShares: Long? = null,
    @SerialName("total_reposts") @JsonNames("totalReposts") val totalReposts: Long? = null,
    @SerialName("total_saves") @JsonNames("totalSaves") val totalSaves: Long? = null,
    @SerialName("total_clicks") @JsonNames("totalClicks") val totalClicks: Long? = null,
    @SerialName("total_video_views") @JsonNames("totalVideoViews") val totalVideoViews: Long? = null,
    @SerialName("total_profile_views") @JsonNames("totalProfileViews") val totalProfileViews: Long? = null,
    @SerialName("engagement_rate") @JsonNames("engagementRate") val engagementRate: Double? = null,
    val deltas: AnalyticsDeltas? = null,
    @SerialName("today_stats") @JsonNames("todayStats") val todayStats: TodayStats? = null,
    val platforms: List<PlatformTotals> = emptyList(),
    val accounts: List<AccountTotals> = emptyList(),
)

/** Percentage change against the previous window of the same length. */
@Serializable
public data class AnalyticsDeltas(
    val followers: Double? = null,
    val posts: Double? = null,
    val engagement: Double? = null,
    val impressions: Double? = null,
    val likes: Double? = null,
    val comments: Double? = null,
    val shares: Double? = null,
    @SerialName("profile_views") @JsonNames("profileViews") val profileViews: Double? = null,
)

/** Activity since midnight in the workspace's timezone. */
@Serializable
public data class TodayStats(
    val posts: Int? = null,
    @SerialName("follower_change") @JsonNames("followerChange") val followerChange: Int? = null,
    val engagement: Int? = null,
)

/** Account and follower counts for one platform. */
@Serializable
public data class PlatformTotals(
    val platform: String? = null,
    val accounts: Int? = null,
    val followers: Long? = null,
)

/** Headline figures for one connected account. */
@Serializable
public data class AccountTotals(
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    val platform: String? = null,
    val username: String? = null,
    val name: String? = null,
    val avatar: String? = null,
    val followers: Long? = null,
    @SerialName("total_posts") @JsonNames("totalPosts") val totalPosts: Long? = null,
    @SerialName("fetched_at") @JsonNames("fetchedAt") val fetchedAt: Instant? = null,
)

/** Daily activity over the requested window. */
@Serializable
public data class TimeSeries(
    val days: Int? = null,
    val series: List<TimeSeriesPoint> = emptyList(),
)

/** One day of the time series. */
@Serializable
public data class TimeSeriesPoint(
    val date: String? = null,
    val engagements: Long? = null,
    val impressions: Long? = null,
    val likes: Long? = null,
    val comments: Long? = null,
    val shares: Long? = null,
    val followers: Long? = null,
    val posts: Long? = null,
)

/** A high-performing post, ranked by the metric the request sorted on. */
@Serializable
public data class TopPost(
    val rank: Int? = null,
    @SerialName("post_id") @JsonNames("postId") val postId: String? = null,
    @SerialName("external_post_id") @JsonNames("externalPostId") val externalPostId: String? = null,
    val source: String? = null,
    val preview: String? = null,
    val permalink: String? = null,
    @SerialName("thumbnail_url") @JsonNames("thumbnailUrl") val thumbnailUrl: String? = null,
    val status: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    val platforms: List<TopPostPlatform> = emptyList(),
    val labels: List<LabelRef> = emptyList(),
    val metrics: TopPostMetrics? = null,
)

/** Where a top post went live. */
@Serializable
public data class TopPostPlatform(
    val platform: String? = null,
    val username: String? = null,
    val url: String? = null,
)

/** What a top post achieved. */
@Serializable
public data class TopPostMetrics(
    val engagements: Long? = null,
    val impressions: Long? = null,
    val reach: Long? = null,
    val likes: Long? = null,
    val comments: Long? = null,
    val shares: Long? = null,
    val reposts: Long? = null,
    val clicks: Long? = null,
    val saves: Long? = null,
    @SerialName("video_views") @JsonNames("videoViews") val videoViews: Long? = null,
)

/** Posts with their per-platform delivery breakdown, as the analytics table renders them. */
@Serializable
public data class PostsTable(
    val posts: List<PostsTableRow> = emptyList(),
    val total: Int? = null,
    val page: Int? = null,
    val limit: Int? = null,
    @SerialName("status_summary") @JsonNames("statusSummary") val statusSummary: PostsTableStatusSummary? = null,
)

/** One row of the analytics posts table. */
@Serializable
public data class PostsTableRow(
    @SerialName("post_id") @JsonNames("postId") val postId: String? = null,
    val preview: String? = null,
    val status: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    @SerialName("scheduled_at") @JsonNames("scheduledAt") val scheduledAt: Instant? = null,
    val platforms: List<JsonObject> = emptyList(),
    @SerialName("delivery_summary") @JsonNames("deliverySummary") val deliverySummary: JsonObject? = null,
)

/** How many posts sit in each status. */
@Serializable
public data class PostsTableStatusSummary(
    val draft: Int? = null,
    val scheduled: Int? = null,
    val published: Int? = null,
    val failed: Int? = null,
    val pending: Int? = null,
    val total: Int? = null,
)

/** One year of posting activity, a row per day. */
@Serializable
public data class PostingStreak(
    val streak: List<PostingStreakDay> = emptyList(),
)

/** One day of the posting streak. */
@Serializable
public data class PostingStreakDay(
    val date: String? = null,
    val count: Int? = null,
    @SerialName("published_count") @JsonNames("publishedCount") val publishedCount: Int? = null,
    @SerialName("failed_count") @JsonNames("failedCount") val failedCount: Int? = null,
    @SerialName("scheduled_count") @JsonNames("scheduledCount") val scheduledCount: Int? = null,
)

/**
 * Audience demographics, aggregated over the accounts that report them.
 *
 * [unsupportedAccounts] names the accounts whose platform returns no demographics, so a thin
 * result is explainable rather than surprising.
 */
@Serializable
public data class Demographics(
    val audience: String? = null,
    val dimensions: DemographicDimensions? = null,
    @SerialName("contributing_accounts") @JsonNames("contributingAccounts")
    val contributingAccounts: List<DemographicAccountRef> = emptyList(),
    @SerialName("unsupported_accounts") @JsonNames("unsupportedAccounts")
    val unsupportedAccounts: List<DemographicAccountRef> = emptyList(),
)

/** The dimensions demographics are split across. */
@Serializable
public data class DemographicDimensions(
    val age: List<DemographicBucket> = emptyList(),
    val gender: List<DemographicBucket> = emptyList(),
    val country: List<DemographicBucket> = emptyList(),
    val city: List<DemographicBucket> = emptyList(),
)

/** One demographic bucket: its label, its absolute value, and its share of the total. */
@Serializable
public data class DemographicBucket(
    val key: String? = null,
    val value: Double? = null,
    val share: Double? = null,
)

/** An account that did, or could not, contribute to a demographics roll-up. */
@Serializable
public data class DemographicAccountRef(
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    val platform: String? = null,
    val username: String? = null,
)

/** What a triggered analytics collection managed to fetch. */
@Serializable
public data class CollectSummary(
    val accounts: Int? = null,
    val posts: Int? = null,
    val demographics: Int? = null,
    val errors: Int? = null,
    @SerialName("error_details") @JsonNames("errorDetails") val errorDetails: List<CollectError> = emptyList(),
)

/** One account a collection pass could not read. */
@Serializable
public data class CollectError(
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    val platform: String? = null,
    val username: String? = null,
    val stage: String? = null,
    val message: String? = null,
)

// ─── Deeper analytics ────────────────────────────────────────────

/** One age band of the content decay report. */
@Serializable
public data class DecayBand(
    val bucket: String? = null,
    val label: String? = null,
    /** Posts with at least one reading in this band. */
    val posts: Int = 0,
    @SerialName("avg_engagements") @JsonNames("avgEngagements") val avgEngagements: Double = 0.0,
    @SerialName("avg_impressions") @JsonNames("avgImpressions") val avgImpressions: Double = 0.0,
    /**
     * Mean share of the post's final engagement reached by this age, 0-1. Null when nothing in the
     * band had earned anything yet.
     */
    @SerialName("share_of_final") @JsonNames("shareOfFinal") val shareOfFinal: Double? = null,
)

/** How engagement accumulates as a post ages. */
@Serializable
public data class ContentDecay(
    val days: Int? = null,
    /** Posts with a publish time and at least one later reading. */
    @SerialName("posts_measured") @JsonNames("postsMeasured") val postsMeasured: Int = 0,
    /** First band where the average post had passed half its final engagement. */
    @SerialName("half_life_bucket") @JsonNames("halfLifeBucket") val halfLifeBucket: String? = null,
    val bands: List<DecayBand> = emptyList(),
)

/** One week of posting. `weekStart` is the Monday, UTC, as YYYY-MM-DD. */
@Serializable
public data class FrequencyWeek(
    @SerialName("week_start") @JsonNames("weekStart") val weekStart: String? = null,
    val posts: Int = 0,
    val engagements: Long = 0,
    @SerialName("avg_engagements_per_post")
    @JsonNames("avgEngagementsPerPost")
    val avgEngagementsPerPost: Double = 0.0,
)

/** The weeks that shared a cadence, folded together. */
@Serializable
public data class FrequencyBand(
    val band: String? = null,
    val label: String? = null,
    val weeks: Int = 0,
    val posts: Int = 0,
    @SerialName("avg_posts_per_week") @JsonNames("avgPostsPerWeek") val avgPostsPerWeek: Double = 0.0,
    @SerialName("avg_engagements_per_post")
    @JsonNames("avgEngagementsPerPost")
    val avgEngagementsPerPost: Double = 0.0,
    /** Engagements over reach, impressions as the stand-in; null with neither. */
    @SerialName("engagement_rate") @JsonNames("engagementRate") val engagementRate: Double? = null,
)

/** Weekly cadence set against what each cadence earned per post. */
@Serializable
public data class PostingFrequency(
    val days: Int? = null,
    val weeks: List<FrequencyWeek> = emptyList(),
    val bands: List<FrequencyBand> = emptyList(),
    /** The cadence that earned the most per post; null without posts. */
    val best: FrequencyBand? = null,
)

/** What moved between one reading and the one before it. */
@Serializable
public data class TimelineDelta(
    val impressions: Long = 0,
    val reach: Long = 0,
    val engagements: Long = 0,
    val likes: Long = 0,
    val comments: Long = 0,
    val shares: Long = 0,
)

/** One reading of a post. */
@Serializable
public data class TimelinePoint(
    val at: Instant? = null,
    /** Minutes since publication; null when the network never said when. */
    @SerialName("age_minutes") @JsonNames("ageMinutes") val ageMinutes: Long? = null,
    val impressions: Long? = null,
    val reach: Long? = null,
    val engagements: Long? = null,
    val likes: Long? = null,
    val comments: Long? = null,
    val shares: Long? = null,
    @SerialName("video_views") @JsonNames("videoViews") val videoViews: Long? = null,
    val delta: TimelineDelta = TimelineDelta(),
)

/** One delivery's readings: the same post on two networks decays differently. */
@Serializable
public data class TimelineDelivery(
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    val platform: String? = null,
    val username: String? = null,
    @SerialName("external_post_id") @JsonNames("externalPostId") val externalPostId: String? = null,
    @SerialName("posted_at") @JsonNames("postedAt") val postedAt: Instant? = null,
    val points: List<TimelinePoint> = emptyList(),
)

/** Every reading held for one post, one timeline per delivery. */
@Serializable
public data class PostTimeline(
    /** Null when the post was made natively on the network. */
    @SerialName("post_id") @JsonNames("postId") val postId: String? = null,
    val deliveries: List<TimelineDelivery> = emptyList(),
)

/** One reading, as the changes feed reports it. */
@Serializable
public data class MetricChange(
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    val platform: String? = null,
    @SerialName("external_post_id") @JsonNames("externalPostId") val externalPostId: String? = null,
    /** Null for a post made natively on the network. */
    @SerialName("post_id") @JsonNames("postId") val postId: String? = null,
    @SerialName("posted_at") @JsonNames("postedAt") val postedAt: Instant? = null,
    @SerialName("fetched_at") @JsonNames("fetchedAt") val fetchedAt: Instant? = null,
    val impressions: Long? = null,
    val reach: Long? = null,
    val engagements: Long? = null,
    val likes: Long? = null,
    val comments: Long? = null,
    val shares: Long? = null,
)

/** One page of readings. Feed `cursor` back as the next `since`. */
@Serializable
public data class MetricChangePage(
    val since: Instant? = null,
    /** Null when nothing changed. */
    val cursor: Instant? = null,
    @SerialName("has_more") @JsonNames("hasMore") val hasMore: Boolean = false,
    val changes: List<MetricChange> = emptyList(),
)

/** What the on-demand refresh did for one delivery. */
@Serializable
public data class CollectPostDelivery(
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    val platform: String? = null,
    @SerialName("external_post_id") @JsonNames("externalPostId") val externalPostId: String? = null,
    val collected: Boolean = false,
    @SerialName("fetched_at") @JsonNames("fetchedAt") val fetchedAt: Instant? = null,
    /** Why the refresh did not happen. */
    val message: String? = null,
)

/** What one post's refresh managed. */
@Serializable
public data class CollectPostResult(
    val collected: Int = 0,
    val deliveries: List<CollectPostDelivery> = emptyList(),
)

/** The freshest reading held for a post made outside FoPost. */
@Serializable
public data class NativePostMetrics(
    val impressions: Long? = null,
    val reach: Long? = null,
    val engagements: Long? = null,
    val likes: Long? = null,
    val comments: Long? = null,
    val shares: Long? = null,
    @SerialName("video_views") @JsonNames("videoViews") val videoViews: Long? = null,
)

/** A post on the account that never went out through FoPost. */
@Serializable
public data class NativePost(
    @SerialName("external_post_id") @JsonNames("externalPostId") val externalPostId: String? = null,
    val text: String? = null,
    val permalink: String? = null,
    @SerialName("thumbnail_url") @JsonNames("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerialName("media_type") @JsonNames("mediaType") val mediaType: String? = null,
    @SerialName("posted_at") @JsonNames("postedAt") val postedAt: Instant? = null,
    @SerialName("fetched_at") @JsonNames("fetchedAt") val fetchedAt: Instant? = null,
    val metrics: NativePostMetrics = NativePostMetrics(),
)
