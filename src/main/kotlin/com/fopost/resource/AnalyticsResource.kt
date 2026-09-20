package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.AnalyticsOverview
import com.fopost.model.CollectPostResult
import com.fopost.model.CollectSummary
import com.fopost.model.ContentDecay
import com.fopost.model.Demographics
import com.fopost.model.LabelAnalytics
import com.fopost.model.MetricChangePage
import com.fopost.model.NativePost
import com.fopost.model.Page
import com.fopost.model.PostTimeline
import com.fopost.model.PostingFrequency
import com.fopost.model.PostingStreak
import com.fopost.model.PostsTable
import com.fopost.model.TimeSeries
import com.fopost.model.TopPost
import com.fopost.param.AnalyticsParams
import com.fopost.param.MetricChangesParams
import com.fopost.param.NativePostsParams
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Reach and engagement across the accounts a key can see. */
public class AnalyticsResource internal constructor(private val http: ApiClient) {

    /** Headline totals, deltas against the previous window, and the per-platform split. */
    public suspend fun overview(params: AnalyticsParams = AnalyticsParams()): AnalyticsOverview =
        http.call("GET", "/analytics/overview", AnalyticsOverview.serializer(), query = params.toQuery())

    /** One point per day over the window. */
    public suspend fun timeSeries(params: AnalyticsParams = AnalyticsParams()): TimeSeries =
        http.call("GET", "/analytics/time-series", TimeSeries.serializer(), query = params.toQuery())

    public suspend fun topPosts(params: AnalyticsParams = AnalyticsParams()): List<TopPost> =
        http.callList("GET", "/analytics/top-posts", TopPost.serializer(), query = params.toQuery())

    /** Campaign roll-up, one row per label. */
    public suspend fun labels(params: AnalyticsParams = AnalyticsParams()): List<LabelAnalytics> =
        http.callList("GET", "/analytics/labels", LabelAnalytics.serializer(), query = params.toQuery())

    public suspend fun postsTable(params: AnalyticsParams = AnalyticsParams()): PostsTable =
        http.call("GET", "/analytics/posts-table", PostsTable.serializer(), query = params.toQuery())

    /** A year of posting activity, a row per day. */
    public suspend fun postingStreak(params: AnalyticsParams = AnalyticsParams()): PostingStreak =
        http.call("GET", "/analytics/posting-streak", PostingStreak.serializer(), query = params.toQuery())

    /** Audience demographics, aggregated over the accounts whose platform reports them. */
    public suspend fun demographics(params: AnalyticsParams = AnalyticsParams()): Demographics =
        http.call("GET", "/analytics/demographics", Demographics.serializer(), query = params.toQuery())

    /** Fetch fresh figures from the platforms now, rather than waiting for the next scheduled pass. */
    public suspend fun collect(accountId: String? = null): CollectSummary =
        http.call("POST", "/analytics/collect", CollectSummary.serializer(), query = mapOf("accountId" to accountId))

    /**
     * How long a post keeps earning: engagement grouped by the post's age at each reading.
     * `days` selects posts by publish time, not reading time.
     */
    public suspend fun decay(params: AnalyticsParams = AnalyticsParams()): ContentDecay =
        http.call("GET", "/analytics/decay", ContentDecay.serializer(), query = params.toQuery())

    /** Whether posting more earned more: weekly cadence against what each cadence earned per post. */
    public suspend fun frequency(params: AnalyticsParams = AnalyticsParams()): PostingFrequency =
        http.call("GET", "/analytics/frequency", PostingFrequency.serializer(), query = params.toQuery())

    /**
     * Every reading held for one post, oldest first, with what moved between them and one timeline
     * per delivery. [idOrPermalink] is a FoPost post id, or the permalink of a post made natively
     * on the network.
     */
    public suspend fun timeline(idOrPermalink: String): PostTimeline =
        http.call("GET", "/analytics/posts/${encodeSegment(idOrPermalink)}/timeline", PostTimeline.serializer())

    /**
     * Readings recorded after `since`, oldest first, with a cursor to continue. Poll it to mirror
     * the metrics into your own store instead of refetching the whole history.
     */
    public suspend fun changes(params: MetricChangesParams = MetricChangesParams()): MetricChangePage =
        http.call("GET", "/analytics/changes", MetricChangePage.serializer(), query = params.toQuery())

    /**
     * Re-reads one post from the network now. Spends the same per-user budget as [collect], so a
     * burst answers 429.
     */
    public suspend fun collectPost(idOrPermalink: String): CollectPostResult =
        http.call(
            "POST",
            "/posts/${encodeSegment(idOrPermalink)}/analytics/collect",
            CollectPostResult.serializer(),
        )

    /** Posts on the account that never went out through FoPost, newest first. */
    public suspend fun nativePosts(
        accountId: String,
        params: NativePostsParams = NativePostsParams(),
    ): Page<NativePost> =
        http.page("/accounts/$accountId/native-posts", NativePost.serializer(), params.toQuery())

    /** A post can be addressed by permalink, whose slashes would otherwise split the path. */
    private fun encodeSegment(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")
}
