package com.fopost.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/** What a pixel-level conversions upload stored and sent. */
@Serializable
public data class ConversionUploadResult(
    /** The stored batch. */
    val id: String? = null,
    /** How many events the network received. */
    val accepted: Int = 0,
    @SerialName("event_count") @JsonNames("eventCount") val eventCount: Int = 0,
    /** True when the `Idempotency-Key` had been used before and nothing was sent. */
    val replayed: Boolean = false,
)

/** One stored conversions batch. Identifiers are kept only as hashes. */
@Serializable
public data class ConversionBatch(
    val id: String,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
    @SerialName("connection_id") @JsonNames("connectionId") val connectionId: String? = null,
    @SerialName("ad_account_id") @JsonNames("adAccountId") val adAccountId: String? = null,
    @SerialName("pixel_id") @JsonNames("pixelId") val pixelId: String? = null,
    @SerialName("idempotency_key") @JsonNames("idempotencyKey") val idempotencyKey: String? = null,
    @SerialName("event_count") @JsonNames("eventCount") val eventCount: Int = 0,
    val accepted: Int = 0,
    val events: List<HashedConversionEvent> = emptyList(),
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: String? = null,
)

/** A conversion as stored: every identifier already hashed, the raw value gone. */
@Serializable
public data class HashedConversionEvent(
    @SerialName("event_name") @JsonNames("eventName") val eventName: String? = null,
    @SerialName("occurred_at") @JsonNames("occurredAt") val occurredAt: String? = null,
    @SerialName("event_id") @JsonNames("eventId") val eventId: String? = null,
    @SerialName("action_source") @JsonNames("actionSource") val actionSource: String? = null,
    @SerialName("email_hash") @JsonNames("emailHash") val emailHash: String? = null,
    @SerialName("phone_hash") @JsonNames("phoneHash") val phoneHash: String? = null,
    @SerialName("external_id_hash") @JsonNames("externalIdHash") val externalIdHash: String? = null,
    @SerialName("value_minor") @JsonNames("valueMinor") val valueMinor: Long? = null,
    val currency: String? = null,
    @SerialName("order_id") @JsonNames("orderId") val orderId: String? = null,
)

/** A pixel the ad account owns. */
@Serializable
public data class AdPixel(
    val id: String,
    val name: String? = null,
)

/** A pixel with its install script and settings. */
@Serializable
public data class AdPixelDetail(
    val id: String,
    val name: String? = null,
    /** The script to place on the site. */
    val code: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: String? = null,
    @SerialName("last_fired_at") @JsonNames("lastFiredAt") val lastFiredAt: String? = null,
    @SerialName("is_unavailable") @JsonNames("isUnavailable") val isUnavailable: Boolean = false,
    /** One of [PixelDataUseSettings]. */
    @SerialName("data_use_setting") @JsonNames("dataUseSetting") val dataUseSetting: String? = null,
)

/** Events a pixel fired over the last 28 days. */
@Serializable
public data class AdPixelStats(
    val since: String? = null,
    val until: String? = null,
    val events: List<PixelEventCount> = emptyList(),
)

@Serializable
public data class PixelEventCount(
    @SerialName("event_name") @JsonNames("eventName") val eventName: String? = null,
    val count: Long = 0,
)

/** How well the network matches one event's identifiers to people. */
@Serializable
public data class EventMatchQuality(
    @SerialName("event_name") @JsonNames("eventName") val eventName: String? = null,
    /** The network's match score out of 10. */
    val score: Double? = null,
    @SerialName("match_keys") @JsonNames("matchKeys") val matchKeys: List<MatchKeyCoverage> = emptyList(),
)

@Serializable
public data class MatchKeyCoverage(
    val identifier: String? = null,
    @SerialName("coverage_percent") @JsonNames("coveragePercent") val coveragePercent: Double = 0.0,
)

/** A custom conversion read from a pixel's events. */
@Serializable
public data class CustomConversion(
    val id: String,
    val name: String? = null,
    @SerialName("pixel_id") @JsonNames("pixelId") val pixelId: String? = null,
    /** One of [CustomConversionEventTypes]. */
    @SerialName("event_type") @JsonNames("eventType") val eventType: String? = null,
    val rule: CustomConversionRule? = null,
    val description: String? = null,
    @SerialName("default_value_minor") @JsonNames("defaultValueMinor") val defaultValueMinor: Long? = null,
    @SerialName("is_archived") @JsonNames("isArchived") val isArchived: Boolean = false,
    @SerialName("is_unavailable") @JsonNames("isUnavailable") val isUnavailable: Boolean = false,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: String? = null,
    @SerialName("last_fired_at") @JsonNames("lastFiredAt") val lastFiredAt: String? = null,
)

// CustomConversionRule travels in both directions, so it keeps the request spelling (camelCase)
// as the serial name and accepts snake_case on the way in.

/** Which pixel event, and which URL, counts as the conversion. Pick one of [urlContains] and [urlEquals]. */
@Serializable
public data class CustomConversionRule(
    /** The pixel event the rule reads; `PageView` when omitted. */
    @JsonNames("event_name") val eventName: String? = null,
    @JsonNames("url_contains") val urlContains: String? = null,
    @JsonNames("url_equals") val urlEquals: String? = null,
)

/** Where a conversion happened. */
public object ConversionActionSources {
    public const val WEBSITE: String = "website"
    public const val APP: String = "app"
    public const val EMAIL: String = "email"
    public const val PHONE_CALL: String = "phone_call"
    public const val CHAT: String = "chat"
    public const val PHYSICAL_STORE: String = "physical_store"
    public const val SYSTEM_GENERATED: String = "system_generated"
    public const val BUSINESS_MESSAGING: String = "business_messaging"
    public const val OTHER: String = "other"
}

/** What a pixel's data may be used for. */
public object PixelDataUseSettings {
    public const val ADVERTISING_AND_ANALYTICS: String = "advertising_and_analytics"
    public const val ANALYTICS_ONLY: String = "analytics_only"
}

/** The standard event a custom conversion is optimised as. */
public object CustomConversionEventTypes {
    public const val ADD_PAYMENT_INFO: String = "ADD_PAYMENT_INFO"
    public const val ADD_TO_CART: String = "ADD_TO_CART"
    public const val ADD_TO_WISHLIST: String = "ADD_TO_WISHLIST"
    public const val COMPLETE_REGISTRATION: String = "COMPLETE_REGISTRATION"
    public const val CONTENT_VIEW: String = "CONTENT_VIEW"
    public const val INITIATED_CHECKOUT: String = "INITIATED_CHECKOUT"
    public const val LEAD: String = "LEAD"
    public const val PURCHASE: String = "PURCHASE"
    public const val SEARCH: String = "SEARCH"
    public const val CONTACT: String = "CONTACT"
    public const val CUSTOMIZE_PRODUCT: String = "CUSTOMIZE_PRODUCT"
    public const val DONATE: String = "DONATE"
    public const val FIND_LOCATION: String = "FIND_LOCATION"
    public const val SCHEDULE: String = "SCHEDULE"
    public const val START_TRIAL: String = "START_TRIAL"
    public const val SUBMIT_APPLICATION: String = "SUBMIT_APPLICATION"
    public const val SUBSCRIBE: String = "SUBSCRIBE"
    public const val OTHER: String = "OTHER"
}
