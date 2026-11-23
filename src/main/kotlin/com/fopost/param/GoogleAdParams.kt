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
