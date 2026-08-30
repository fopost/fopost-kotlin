package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.AnalyticsOverview
import com.fopost.model.CollectSummary
import com.fopost.model.Demographics
import com.fopost.model.LabelAnalytics
import com.fopost.model.PostingStreak
import com.fopost.model.PostsTable
import com.fopost.model.TimeSeries
import com.fopost.model.TopPost
import com.fopost.param.AnalyticsParams

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
}
