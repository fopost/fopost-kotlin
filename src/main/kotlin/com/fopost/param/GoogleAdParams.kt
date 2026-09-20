package com.fopost.param

import kotlinx.serialization.Serializable

/**
 * The connection and the Google Ads account a call runs against.
 *
 * [customerId] is digits only and has to name an account the connection's grant reaches: any other
 * answers 404. [workspaceId] may be left out on a read, and is required on a write.
 */
@Serializable
public data class GoogleAdsScope(
    val workspaceId: String? = null,
    val connectionId: String,
    val customerId: String,
)

/** Start a Google Ads connection. */
@Serializable
public data class GoogleAuthorizeParams(
    val workspaceId: String,
    /** Dashboard path to land on after Google redirects back. */
    val returnTo: String? = null,
)

/** Add a keyword to an ad group. */
@Serializable
public data class CreateGoogleKeywordParams(
    val workspaceId: String,
    val connectionId: String,
    val customerId: String,
    val adGroupId: String,
    val text: String,
    /** One of [com.fopost.model.GoogleMatchTypes]. */
    val matchType: String,
    val cpcBidMinor: Long? = null,
)

/** Pause, resume or rebid a keyword. */
@Serializable
public data class UpdateGoogleKeywordParams(
    val workspaceId: String,
    val connectionId: String,
    val customerId: String,
    /** `active` or `paused`. */
    val status: String? = null,
    val cpcBidMinor: Long? = null,
)

/** The scope alone, for a delete that carries it in the body. */
@Serializable
public data class GoogleAdsScopeBody(
    val workspaceId: String,
    val connectionId: String,
    val customerId: String,
)

/** Ask for keyword ideas from seeds, a landing page, or both. */
@Serializable
public data class GoogleKeywordIdeasParams(
    val workspaceId: String,
    val connectionId: String,
    val customerId: String,
    val seeds: List<String>? = null,
    val url: String? = null,
    val languageId: String? = null,
    val geoTargetIds: List<String>? = null,
)

/** Read the historical metrics of keywords you already have. */
@Serializable
public data class GoogleKeywordMetricsParams(
    val workspaceId: String,
    val connectionId: String,
    val customerId: String,
    val keywords: List<String>,
)

/** Add a portfolio bid strategy. */
@Serializable
public data class CreateGoogleBidStrategyParams(
    val workspaceId: String,
    val connectionId: String,
    val customerId: String,
    val name: String,
    /** One of [com.fopost.model.GoogleBidStrategyTypes]. */
    val type: String,
    val targetMinor: Long? = null,
)

/** One slot to put on a campaign's schedule. */
@Serializable
public data class GoogleAdScheduleInput(
    val dayOfWeek: String,
    val startHour: Int,
    val endHour: Int,
    val bidModifier: Double? = null,
)

/** Replace a campaign's schedule; Google has no partial edit for one. */
@Serializable
public data class SetGoogleAdScheduleParams(
    val workspaceId: String,
    val connectionId: String,
    val customerId: String,
    val campaignId: String,
    val slots: List<GoogleAdScheduleInput>,
)

/** Create a negative keyword list. */
@Serializable
public data class CreateGoogleNegativeKeywordListParams(
    val workspaceId: String,
    val connectionId: String,
    val customerId: String,
    val name: String,
)

/** One keyword in a negative list. */
@Serializable
public data class GoogleNegativeKeyword(val text: String, val matchType: String)

/** Add keywords to a negative list. */
@Serializable
public data class AddGoogleNegativeKeywordsParams(
    val workspaceId: String,
    val connectionId: String,
    val customerId: String,
    val sharedSetId: String,
    val keywords: List<GoogleNegativeKeyword>,
)

/** Put a negative keyword list on a campaign. */
@Serializable
public data class AttachGoogleNegativeKeywordListParams(
    val workspaceId: String,
    val connectionId: String,
    val customerId: String,
    val sharedSetId: String,
    val campaignId: String,
)

/**
 * The asset to create. [kind] picks which other fields apply: [text] and [finalUrl] for
 * `sitelink`, [text] alone for `callout`, [header] and [values] for `snippet`.
 */
@Serializable
public data class GoogleAssetSpec(
    val kind: String,
    val text: String? = null,
    val description1: String? = null,
    val description2: String? = null,
    val finalUrl: String? = null,
    val header: String? = null,
    val values: List<String>? = null,
)

/** Add an asset to the library. */
@Serializable
public data class CreateGoogleAssetParams(
    val workspaceId: String,
    val connectionId: String,
    val customerId: String,
    val spec: GoogleAssetSpec,
)

/** Attach an asset to the account, or to one campaign. */
@Serializable
public data class AttachGoogleAssetParams(
    val workspaceId: String,
    val connectionId: String,
    val customerId: String,
    val assetId: String,
    /** One of [com.fopost.model.GoogleAssetFieldTypes]. */
    val fieldType: String,
    /** Attaches to the account when left out. */
    val campaignId: String? = null,
)

/** Create a Performance Max asset group. */
@Serializable
public data class CreateGoogleAssetGroupParams(
    val workspaceId: String,
    val connectionId: String,
    val customerId: String,
    val campaignId: String,
    val name: String,
    val finalUrls: List<String>,
    /** `active` or `paused`; starts paused when left out. */
    val status: String? = null,
)

/** Rename, pause or resume an asset group. */
@Serializable
public data class UpdateGoogleAssetGroupParams(
    val workspaceId: String,
    val connectionId: String,
    val customerId: String,
    val name: String? = null,
    val status: String? = null,
)

/** Create a conversion action. */
@Serializable
public data class CreateGoogleConversionActionParams(
    val workspaceId: String,
    val connectionId: String,
    val customerId: String,
    val name: String,
    val category: String,
    val valueMinor: Long? = null,
    val countingType: String? = null,
)

/**
 * One offline conversion. One of [gclid], [gbraid] or [wbraid] is required: it is what matches the
 * click.
 */
@Serializable
public data class GoogleClickConversion(
    val conversionActionId: String,
    /** `yyyy-MM-dd HH:mm:ss+|-HH:mm`, the only shape Google accepts. */
    val conversionDateTime: String,
    val gclid: String? = null,
    val gbraid: String? = null,
    val wbraid: String? = null,
    val valueMinor: Long? = null,
    val currencyCode: String? = null,
    val orderId: String? = null,
)

/** Send offline conversions. */
@Serializable
public data class UploadGoogleConversionsParams(
    val workspaceId: String,
    val connectionId: String,
    val customerId: String,
    val conversions: List<GoogleClickConversion>,
)

/** Restate, retract or enhance a conversion already counted. */
@Serializable
public data class GoogleConversionAdjustment(
    val conversionActionId: String,
    /** `RESTATEMENT`, `RETRACTION` or `ENHANCEMENT`. */
    val adjustmentType: String,
    val adjustmentDateTime: String,
    val orderId: String? = null,
    val gclid: String? = null,
    val conversionDateTime: String? = null,
    val restatementValueMinor: Long? = null,
    val currencyCode: String? = null,
)

/** Send conversion adjustments. */
@Serializable
public data class UploadGoogleConversionAdjustmentsParams(
    val workspaceId: String,
    val connectionId: String,
    val customerId: String,
    val adjustments: List<GoogleConversionAdjustment>,
)

/**
 * A raw read-only GAQL SELECT. The account read is [customerId], never anything named inside
 * [query].
 */
@Serializable
public data class GoogleQueryParams(
    val connectionId: String,
    val customerId: String,
    val query: String,
    val workspaceId: String? = null,
)
