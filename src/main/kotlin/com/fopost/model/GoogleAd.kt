package com.fopost.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

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

/** Google portfolio bid strategy types. */
public object GoogleBidStrategyTypes {
    public const val TARGET_SPEND: String = "TARGET_SPEND"
    public const val MAXIMIZE_CONVERSIONS: String = "MAXIMIZE_CONVERSIONS"
    public const val MAXIMIZE_CONVERSION_VALUE: String = "MAXIMIZE_CONVERSION_VALUE"
    public const val TARGET_CPA: String = "TARGET_CPA"
    public const val TARGET_ROAS: String = "TARGET_ROAS"
}
