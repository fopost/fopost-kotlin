package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.GoogleAdScheduleSlot
import com.fopost.model.GoogleAssetGroup
import com.fopost.model.GoogleAssetsResult
import com.fopost.model.GoogleBidStrategy
import com.fopost.model.GoogleConversionAction
import com.fopost.model.GoogleKeyword
import com.fopost.model.GoogleKeywordIdea
import com.fopost.model.GoogleLocalServicesLead
import com.fopost.model.GoogleQueryResult
import com.fopost.model.GoogleSearchTerm
import com.fopost.model.GoogleSharedSet
import com.fopost.param.AddGoogleNegativeKeywordsParams
import com.fopost.param.AttachGoogleAssetParams
import com.fopost.param.AttachGoogleNegativeKeywordListParams
import com.fopost.param.CreateGoogleAssetGroupParams
import com.fopost.param.CreateGoogleAssetParams
import com.fopost.param.CreateGoogleBidStrategyParams
import com.fopost.param.CreateGoogleConversionActionParams
import com.fopost.param.CreateGoogleKeywordParams
import com.fopost.param.CreateGoogleNegativeKeywordListParams
import com.fopost.param.GoogleAdsScope
import com.fopost.param.GoogleAdsScopeBody
import com.fopost.param.GoogleKeywordIdeasParams
import com.fopost.param.GoogleKeywordMetricsParams
import com.fopost.param.GoogleQueryParams
import com.fopost.param.SetGoogleAdScheduleParams
import com.fopost.param.UpdateGoogleAssetGroupParams
import com.fopost.param.UpdateGoogleKeywordParams
import com.fopost.param.UploadGoogleConversionAdjustmentsParams
import com.fopost.param.UploadGoogleConversionsParams
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * The Google Ads surface no other network has: keywords, assets, Performance Max asset groups,
 * Local Services leads, conversions and raw GAQL.
 *
 * Campaigns, ad groups, ads, audiences and insights are on [AdsResource] and dispatch by
 * connection; a connection on another network answers 400 here. Every method needs the `ads` scope,
 * and anything that changes what a live account serves or bids also needs `publish`. Amounts are in
 * the account's currency, in minor units.
 */
public class GoogleAdsResource internal constructor(private val http: ApiClient) {

    // ── Keywords ──

    /** Keywords on the account, or on one ad group. */
    public suspend fun keywords(scope: GoogleAdsScope, adGroupId: String? = null): List<GoogleKeyword> =
        http.callList("GET", "/ads/google/keywords", GoogleKeyword.serializer(), query = query(scope, "ad_group_id" to adGroupId))

    /** Add a keyword. Needs `publish` as well as `ads`. */
    public suspend fun createKeyword(params: CreateGoogleKeywordParams): String =
        id("POST", "/ads/google/keywords", params, CreateGoogleKeywordParams.serializer())

    /** Pause, resume or rebid a keyword. Needs `publish` as well as `ads`. */
    public suspend fun updateKeyword(keywordId: String, params: UpdateGoogleKeywordParams): String =
        id("PATCH", "/ads/google/keywords/$keywordId", params, UpdateGoogleKeywordParams.serializer())

    /** Remove a keyword. Needs `publish` as well as `ads`. */
    public suspend fun deleteKeyword(keywordId: String, scope: GoogleAdsScope) {
        http.send("DELETE", "/ads/google/keywords/$keywordId", scopeBody(scope))
    }

    /** Ideas from seed keywords, a landing page, or both. */
    public suspend fun keywordIdeas(params: GoogleKeywordIdeasParams): List<GoogleKeywordIdea> =
        http.callList(
            "POST",
            "/ads/google/keyword-ideas",
            GoogleKeywordIdea.serializer(),
            http.jsonBody(params, GoogleKeywordIdeasParams.serializer()),
        )

    /** Historical metrics for keywords you already have. */
    public suspend fun keywordMetrics(params: GoogleKeywordMetricsParams): List<GoogleKeywordIdea> =
        http.callList(
            "POST",
            "/ads/google/keyword-metrics",
            GoogleKeywordIdea.serializer(),
            http.jsonBody(params, GoogleKeywordMetricsParams.serializer()),
        )

    /** What people actually searched, with the metrics each term earned. */
    public suspend fun searchTerms(scope: GoogleAdsScope, since: String, until: String): List<GoogleSearchTerm> =
        http.callList(
            "GET",
            "/ads/google/search-terms",
            GoogleSearchTerm.serializer(),
            query = query(scope, "since" to since, "until" to until),
        )

    // ── Bid strategies and ad schedule ──

    /** The account's portfolio bid strategies. */
    public suspend fun bidStrategies(scope: GoogleAdsScope): List<GoogleBidStrategy> =
        http.callList("GET", "/ads/google/bid-strategies", GoogleBidStrategy.serializer(), query = query(scope))

    /** Add a bid strategy. Needs `publish` as well as `ads`. */
    public suspend fun createBidStrategy(params: CreateGoogleBidStrategyParams): String =
        id("POST", "/ads/google/bid-strategies", params, CreateGoogleBidStrategyParams.serializer())

    /** A campaign's ad schedule. */
    public suspend fun adSchedule(scope: GoogleAdsScope, campaignId: String): List<GoogleAdScheduleSlot> =
        http.callList(
            "GET",
            "/ads/google/ad-schedule",
            GoogleAdScheduleSlot.serializer(),
            query = query(scope, "campaign_id" to campaignId),
        )

    /**
     * Replace a campaign's schedule; the slots given replace every slot on it. Needs `publish` as
     * well as `ads`.
     */
    public suspend fun setAdSchedule(params: SetGoogleAdScheduleParams): Int {
        val data = http.call(
            "PUT",
            "/ads/google/ad-schedule",
            JsonObject.serializer(),
            http.jsonBody(params, SetGoogleAdScheduleParams.serializer()),
        )
        return data["slots"]?.jsonPrimitive?.intOrNull ?: 0
    }

    // ── Negative keyword lists ──

    /** The account's negative keyword lists. */
    public suspend fun negativeKeywordLists(scope: GoogleAdsScope): List<GoogleSharedSet> =
        http.callList("GET", "/ads/google/negative-keywords", GoogleSharedSet.serializer(), query = query(scope))

    /** Create a negative keyword list. Needs `publish` as well as `ads`. */
    public suspend fun createNegativeKeywordList(params: CreateGoogleNegativeKeywordListParams): String =
        id("POST", "/ads/google/negative-keywords", params, CreateGoogleNegativeKeywordListParams.serializer())

    /** Add keywords to a list; answers how many landed. Needs `publish`. */
    public suspend fun addNegativeKeywords(params: AddGoogleNegativeKeywordsParams): Int {
        val data = http.call(
            "POST",
            "/ads/google/negative-keywords/keywords",
            JsonObject.serializer(),
            http.jsonBody(params, AddGoogleNegativeKeywordsParams.serializer()),
        )
        return data["added"]?.jsonPrimitive?.intOrNull ?: 0
    }

    /** Put a list on a campaign. Needs `publish` as well as `ads`. */
    public suspend fun attachNegativeKeywordList(params: AttachGoogleNegativeKeywordListParams) {
        http.send(
            "POST",
            "/ads/google/negative-keywords/attach",
            http.jsonBody(params, AttachGoogleNegativeKeywordListParams.serializer()),
        )
    }

    // ── Assets ──

    /** Sitelinks, callouts and snippets, with the links that place each one. */
    public suspend fun assets(scope: GoogleAdsScope): GoogleAssetsResult =
        http.call("GET", "/ads/google/assets", GoogleAssetsResult.serializer(), query = query(scope))

    /** Add an asset to the library. Needs `publish` as well as `ads`. */
    public suspend fun createAsset(params: CreateGoogleAssetParams): String =
        id("POST", "/ads/google/assets", params, CreateGoogleAssetParams.serializer())

    /** Put an asset under the ads it belongs to. Needs `publish`. */
    public suspend fun attachAsset(params: AttachGoogleAssetParams) {
        http.send("POST", "/ads/google/assets/attach", http.jsonBody(params, AttachGoogleAssetParams.serializer()))
    }

    /**
     * Remove the links that put an asset under an ad; on Google the asset itself is permanent.
     * Needs `publish` as well as `ads`.
     */
    public suspend fun deleteAsset(assetId: String, scope: GoogleAdsScope) {
        http.send("DELETE", "/ads/google/assets/$assetId", scopeBody(scope))
    }

    // ── Performance Max asset groups ──

    /** Performance Max asset groups on the account, or on one campaign. */
    public suspend fun assetGroups(scope: GoogleAdsScope, campaignId: String? = null): List<GoogleAssetGroup> =
        http.callList(
            "GET",
            "/ads/google/asset-groups",
            GoogleAssetGroup.serializer(),
            query = query(scope, "campaign_id" to campaignId),
        )

    /** Create an asset group. Needs `publish` as well as `ads`. */
    public suspend fun createAssetGroup(params: CreateGoogleAssetGroupParams): String =
        id("POST", "/ads/google/asset-groups", params, CreateGoogleAssetGroupParams.serializer())

    /** Rename, pause or resume an asset group. Needs `publish`. */
    public suspend fun updateAssetGroup(assetGroupId: String, params: UpdateGoogleAssetGroupParams): String =
        id("PATCH", "/ads/google/asset-groups/$assetGroupId", params, UpdateGoogleAssetGroupParams.serializer())

    /** Remove an asset group. Needs `publish` as well as `ads`. */
    public suspend fun deleteAssetGroup(assetGroupId: String, scope: GoogleAdsScope) {
        http.send("DELETE", "/ads/google/asset-groups/$assetGroupId", scopeBody(scope))
    }

    // ── Local Services leads ──

    /** Leads from Local Services Ads, read live and never stored. */
    public suspend fun localServicesLeads(
        scope: GoogleAdsScope,
        since: String,
        until: String,
    ): List<GoogleLocalServicesLead> =
        http.callList(
            "GET",
            "/ads/google/local-services",
            GoogleLocalServicesLead.serializer(),
            query = query(scope, "since" to since, "until" to until),
        )

    // ── Conversions ──

    /** The account's conversion actions. */
    public suspend fun conversionActions(scope: GoogleAdsScope): List<GoogleConversionAction> =
        http.callList("GET", "/ads/google/conversions", GoogleConversionAction.serializer(), query = query(scope))

    /** Add a conversion action. Needs `publish` as well as `ads`. */
    public suspend fun createConversionAction(params: CreateGoogleConversionActionParams): String =
        id("POST", "/ads/google/conversions", params, CreateGoogleConversionActionParams.serializer())

    /** Send offline conversions; answers how many landed. Needs `publish`. */
    public suspend fun uploadConversions(params: UploadGoogleConversionsParams): Int =
        uploaded(
            "/ads/google/conversions/upload",
            http.jsonBody(params, UploadGoogleConversionsParams.serializer()),
        )

    /** Send conversion adjustments; answers how many landed. Needs `publish`. */
    public suspend fun uploadConversionAdjustments(params: UploadGoogleConversionAdjustmentsParams): Int =
        uploaded(
            "/ads/google/conversions/adjustments",
            http.jsonBody(params, UploadGoogleConversionAdjustmentsParams.serializer()),
        )

    // ── GAQL ──

    /** Run a read-only GAQL SELECT; rows come back as Google sends them. */
    public suspend fun query(params: GoogleQueryParams): GoogleQueryResult =
        http.call(
            "POST",
            "/ads/insights/query",
            GoogleQueryResult.serializer(),
            http.jsonBody(params, GoogleQueryParams.serializer()),
        )

    private suspend fun <T> id(
        method: String,
        path: String,
        params: T,
        serializer: SerializationStrategy<T>,
    ): String {
        val data = http.call(method, path, JsonObject.serializer(), http.jsonBody(params, serializer))
        return data["id"]?.jsonPrimitive?.contentOrNull.orEmpty()
    }

    private suspend fun uploaded(path: String, body: okhttp3.RequestBody): Int {
        val data = http.call("POST", path, JsonObject.serializer(), body)
        return data["uploaded"]?.jsonPrimitive?.intOrNull ?: 0
    }

    private fun scopeBody(scope: GoogleAdsScope) =
        http.jsonBody(
            GoogleAdsScopeBody(scope.workspaceId.orEmpty(), scope.connectionId, scope.customerId),
            GoogleAdsScopeBody.serializer(),
        )

    private fun query(scope: GoogleAdsScope, vararg extra: Pair<String, String?>): Map<String, Any?> =
        buildMap {
            put("workspace_id", scope.workspaceId)
            put("connection_id", scope.connectionId)
            put("customer_id", scope.customerId)
            extra.forEach { (key, value) -> put(key, value) }
        }
}
