@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames

/** A priced flight. Nothing is bought until it is reserved. */
@Serializable
public data class ReachFrequencyPrediction(
    val id: String? = null,
    val name: String? = null,
    val status: String? = null,
    val reach: Long? = null,
    val impressions: Long? = null,
    @SerialName("frequency_cap") @JsonNames("frequencyCap") val frequencyCap: Long? = null,
    /** Account currency, minor units. */
    @SerialName("budget_minor") @JsonNames("budgetMinor") val budgetMinor: Long? = null,
    @SerialName("start_at") @JsonNames("startAt") val startAt: Instant? = null,
    @SerialName("end_at") @JsonNames("endAt") val endAt: Instant? = null,
    /** True once the prediction holds inventory. */
    val reserved: Boolean = false,
)

/** The predictions on one ad account. */
@Serializable
public data class ReachFrequencyResult(
    val predictions: List<ReachFrequencyPrediction> = emptyList(),
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
)

/** A creator who allowlisted this advertiser for partnership ads. */
@Serializable
public data class PartnershipCreator(
    val id: String? = null,
    val username: String? = null,
    val name: String? = null,
    val status: String? = null,
    val permissions: List<String> = emptyList(),
)

/** One change recorded on an ad account. */
@Serializable
public data class AdActivity(
    val id: String? = null,
    @SerialName("event_type") @JsonNames("eventType") val eventType: String? = null,
    @SerialName("actor_name") @JsonNames("actorName") val actorName: String? = null,
    @SerialName("object_name") @JsonNames("objectName") val objectName: String? = null,
    @SerialName("object_type") @JsonNames("objectType") val objectType: String? = null,
    @SerialName("extra_data") @JsonNames("extraData") val extraData: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
)

/** The change log of one ad account. */
@Serializable
public data class AdActivityResult(
    val activity: List<AdActivity> = emptyList(),
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
)

/** Groups campaigns, ad sets and ads for reporting. */
@Serializable
public data class AdLabel(
    val id: String? = null,
    val name: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
)

/** An A/B study splitting traffic across its cells. */
@Serializable
public data class AdStudy(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val type: String? = null,
    val status: String? = null,
    @SerialName("start_at") @JsonNames("startAt") val startAt: Instant? = null,
    @SerialName("end_at") @JsonNames("endAt") val endAt: Instant? = null,
)

/** How many iOS 14 campaigns an ad account may run at once, per app. */
@Serializable
public data class IosCampaignLimits(
    val limit: Long? = null,
    val used: Long? = null,
    @SerialName("app_id") @JsonNames("appId") val appId: String? = null,
)

/** A window the ad platform should expect heavier spend over. */
@Serializable
public data class HighDemandPeriod(
    val id: String? = null,
    @SerialName("start_at") @JsonNames("startAt") val startAt: Instant? = null,
    @SerialName("end_at") @JsonNames("endAt") val endAt: Instant? = null,
    @SerialName("budget_value") @JsonNames("budgetValue") val budgetValue: Double? = null,
    @SerialName("budget_value_type")
    @JsonNames("budgetValueType")
    val budgetValueType: String? = null,
)

/** Weights one condition's conversions. */
@Serializable
public data class ValueRule(
    val condition: String? = null,
    val multiplier: Double? = null,
)

/** Weights conversions so some audiences count for more than others. */
@Serializable
public data class ValueRuleSet(
    val id: String? = null,
    val name: String? = null,
    val status: String? = null,
    val rules: List<ValueRule> = emptyList(),
)
