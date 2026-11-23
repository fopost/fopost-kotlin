package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.GoogleAdScheduleSlot
import com.fopost.model.GoogleBidStrategy
import com.fopost.model.GoogleConversionAction
import com.fopost.model.GoogleLocalServicesLead
import com.fopost.model.GoogleQueryResult
import com.fopost.param.CreateGoogleBidStrategyParams
import com.fopost.param.CreateGoogleConversionActionParams
import com.fopost.param.GoogleAdsScope
import com.fopost.param.GoogleQueryParams
import com.fopost.param.SetGoogleAdScheduleParams
import com.fopost.param.UploadGoogleConversionAdjustmentsParams
import com.fopost.param.UploadGoogleConversionsParams
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * The Google Ads surface no other network has: bid strategies, the ad schedule, Local Services
 * leads, conversions and raw GAQL.
 *
 * Ads, audiences and insights are on [AdsResource] and dispatch by connection; a connection on another network answers 400 here. Every method needs the `ads` scope,
 * and anything that changes what a live account serves or bids also needs `publish`. Amounts are in
 * the account's currency, in minor units.
 */
public class GoogleAdsResource internal constructor(private val http: ApiClient) {

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

    private suspend fun uploaded(path: String, body: okhttp3.RequestBody): Int = counted(path, "uploaded", body)

    private suspend fun counted(path: String, key: String, body: okhttp3.RequestBody): Int {
        val data = http.call("POST", path, JsonObject.serializer(), body)
        return data[key]?.jsonPrimitive?.intOrNull ?: 0
    }

    private fun query(scope: GoogleAdsScope, vararg extra: Pair<String, String?>): Map<String, Any?> =
        buildMap {
            put("workspace_id", scope.workspaceId)
            put("connection_id", scope.connectionId)
            put("customer_id", scope.customerId)
            extra.forEach { (key, value) -> put(key, value) }
        }
}
