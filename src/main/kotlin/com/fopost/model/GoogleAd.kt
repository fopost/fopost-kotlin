package com.fopost.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A keyword on an ad group.
 *
 * [id] is `<customerId>~keyword~<adGroupId>~<criterionId>`: a Google resource name has slashes and
 * cannot ride in a URL path segment, so every id here carries the account it belongs to.
 */
@Serializable
public data class GoogleKeyword(
    val id: String,
    val adGroupId: String? = null,
    val text: String? = null,
    val matchType: String? = null,
    val status: String? = null,
    /** The account's currency, in minor units. */
    val cpcBidMinor: Long? = null,
    val negative: Boolean = false,
)

/** A keyword idea, or the historical metrics of one. */
@Serializable
public data class GoogleKeywordIdea(
    val text: String? = null,
    val avgMonthlySearches: Long = 0,
    val competition: String? = null,
    val lowTopOfPageBidMinor: Long? = null,
    val highTopOfPageBidMinor: Long? = null,
)

/** What someone actually searched, with the metrics it earned. */
@Serializable
public data class GoogleSearchTerm(
    val term: String? = null,
    val adGroupId: String? = null,
    val status: String? = null,
    val metrics: InsightsMetrics? = null,
)

/** A portfolio bid strategy on the account. */
@Serializable
public data class GoogleBidStrategy(
    val id: String,
    val name: String? = null,
    val type: String? = null,
    val status: String? = null,
    val campaignCount: Int = 0,
)

/** One slot of a campaign's ad schedule. */
@Serializable
public data class GoogleAdScheduleSlot(
    val id: String,
    val dayOfWeek: String? = null,
    val startHour: Int = 0,
    val endHour: Int = 0,
    val bidModifier: Double? = null,
)

/** A negative keyword list. */
@Serializable
public data class GoogleSharedSet(
    val id: String,
    val name: String? = null,
    val type: String? = null,
    val memberCount: Int = 0,
)

/** A sitelink, callout or structured snippet. */
@Serializable
public data class GoogleAsset(
    val id: String,
    val name: String? = null,
    val type: String? = null,
    /** What a sitelink, callout or snippet renders. */
    val text: String? = null,
    val finalUrl: String? = null,
)

/** Where an asset is attached; one with no links serves nowhere. */
@Serializable
public data class GoogleAssetLink(
    val id: String,
    val assetId: String? = null,
    val level: String? = null,
    val ownerId: String? = null,
    val fieldType: String? = null,
    val status: String? = null,
)

/** The account's assets with the links that place them. */
@Serializable
public data class GoogleAssetsResult(
    val assets: List<GoogleAsset> = emptyList(),
    val links: List<GoogleAssetLink> = emptyList(),
)

/** A Performance Max asset group. */
@Serializable
public data class GoogleAssetGroup(
    val id: String,
    val campaignId: String? = null,
    val name: String? = null,
    val status: String? = null,
    val finalUrls: List<String> = emptyList(),
)

/** A lead from Local Services Ads, read live and never stored. */
@Serializable
public data class GoogleLocalServicesLead(
    val id: String,
    val category: String? = null,
    val service: String? = null,
    val contactName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val status: String? = null,
    val type: String? = null,
    val createdAt: String? = null,
)

/** A conversion action on the account. */
@Serializable
public data class GoogleConversionAction(
    val id: String,
    val name: String? = null,
    val category: String? = null,
    val status: String? = null,
    val type: String? = null,
    val countingType: String? = null,
    val valueMinor: Long? = null,
)

/** Rows exactly as Google returns them. */
@Serializable
public data class GoogleQueryResult(val rows: List<JsonElement> = emptyList())

/**
 * What Google projects applying a recommendation would change. A null field is
 * one Google does not estimate for that recommendation.
 */
@Serializable
public data class GoogleRecommendationImpact(
    val baseClicks: Double? = null,
    val potentialClicks: Double? = null,
    /** The account's currency, in minor units. */
    val baseCostMinor: Long? = null,
    val potentialCostMinor: Long? = null,
    val baseConversions: Double? = null,
    val potentialConversions: Double? = null,
)

/**
 * One of Google's own recommendations for the account. [id] is the Google
 * resource name rather than the `~` form other objects use, because a
 * recommendation is not an object you address again: it is what apply and
 * dismiss take.
 */
@Serializable
public data class GoogleRecommendation(
    val id: String,
    val type: String = "",
    val campaignId: String? = null,
    val adGroupId: String? = null,
    val dismissed: Boolean = false,
    val impact: GoogleRecommendationImpact? = null,
)

/** One campaign's optimization score. */
@Serializable
public data class GoogleOptimizationScoreCampaign(
    val id: String,
    val name: String = "",
    val score: Double? = null,
)

/** Google's estimate of how well the account is set up, from 0 to 1. */
@Serializable
public data class GoogleOptimizationScore(
    val score: Double? = null,
    /** How much this account's score counts against others under the same manager. */
    val weight: Double? = null,
    val campaigns: List<GoogleOptimizationScoreCampaign> = emptyList(),
)

/** Google keyword match types. */
public object GoogleMatchTypes {
    public const val EXACT: String = "EXACT"
    public const val PHRASE: String = "PHRASE"
    public const val BROAD: String = "BROAD"
}

/** Google portfolio bid strategy types. */
public object GoogleBidStrategyTypes {
    public const val TARGET_SPEND: String = "TARGET_SPEND"
    public const val MAXIMIZE_CONVERSIONS: String = "MAXIMIZE_CONVERSIONS"
    public const val MAXIMIZE_CONVERSION_VALUE: String = "MAXIMIZE_CONVERSION_VALUE"
    public const val TARGET_CPA: String = "TARGET_CPA"
    public const val TARGET_ROAS: String = "TARGET_ROAS"
}

/** Where an asset renders. */
public object GoogleAssetFieldTypes {
    public const val SITELINK: String = "SITELINK"
    public const val CALLOUT: String = "CALLOUT"
    public const val STRUCTURED_SNIPPET: String = "STRUCTURED_SNIPPET"
}
