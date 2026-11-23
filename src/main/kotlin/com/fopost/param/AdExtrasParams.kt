package com.fopost.param

import com.fopost.internal.InstantSerializer
import com.fopost.model.AdTargeting
import com.fopost.model.ValueRule
import java.time.Instant
import kotlinx.serialization.Serializable

/** Prices a flight. Nothing is bought until you reserve it. */
@Serializable
public data class CreateReachFrequencyParams(
    val workspaceId: String,
    val connectionId: String,
    /** Meta ad account id, `act_…`. */
    val adAccountId: String,
    val name: String,
    val targeting: AdTargeting,
    val placements: List<String>,
    val budgetMinor: Long,
    @Serializable(with = InstantSerializer::class) val startAt: Instant,
    @Serializable(with = InstantSerializer::class) val endAt: Instant,
    /** How often one person should see the ad over the flight. */
    val frequencyCap: Int? = null,
)

/** Reserves or cancels a prediction. Reserving spends on the ad account, so it needs `publish`. */
@Serializable
public data class ReachFrequencyActionParams(
    val workspaceId: String,
    val connectionId: String,
    /** Meta ad account id, `act_…`. */
    val adAccountId: String,
)

/** Asks a creator for partnership permission. */
@Serializable
public data class PartnershipParams(
    val workspaceId: String,
    val connectionId: String,
    val pageId: String,
    /** The creator's account id. */
    val creatorId: String,
)

/** Creates or renames an ad label. */
@Serializable
public data class AdLabelParams(
    val workspaceId: String,
    val connectionId: String,
    /** Meta ad account id, `act_…`. */
    val adAccountId: String,
    val name: String,
)

/** Puts a label on a campaign, ad set or ad, keeping whatever labels it already carries. */
@Serializable
public data class ApplyAdLabelParams(
    val workspaceId: String,
    val connectionId: String,
    /** Meta ad account id, `act_…`. */
    val adAccountId: String,
    val objectId: String,
    /** `campaign`, `ad_set` or `ad`. */
    val level: String,
)

/** One arm of an A/B study: the campaigns it tests. */
@Serializable
public data class AdStudyCell(
    val name: String,
    val objectIds: List<String>,
)

/** An A/B study splitting traffic evenly across two to five cells. */
@Serializable
public data class CreateAdStudyParams(
    val workspaceId: String,
    val connectionId: String,
    /** Meta ad account id, `act_…`. */
    val adAccountId: String,
    val name: String,
    @Serializable(with = InstantSerializer::class) val startAt: Instant,
    @Serializable(with = InstantSerializer::class) val endAt: Instant,
    val cells: List<AdStudyCell>,
    val description: String? = null,
)

/**
 * Tells the ad platform to expect heavier spend over a window, so pacing allows for it.
 * [budgetValueType] is `ABSOLUTE` or `MULTIPLIER`.
 */
@Serializable
public data class CreateHighDemandPeriodParams(
    val workspaceId: String,
    val connectionId: String,
    /** Meta ad account id, `act_…`. */
    val adAccountId: String,
    @Serializable(with = InstantSerializer::class) val startAt: Instant,
    @Serializable(with = InstantSerializer::class) val endAt: Instant,
    val budgetValue: Double,
    val budgetValueType: String,
)

/** Weights conversions so some audiences count for more than others. */
@Serializable
public data class CreateValueRuleSetParams(
    val workspaceId: String,
    val connectionId: String,
    /** Meta ad account id, `act_…`. */
    val adAccountId: String,
    val name: String,
    val rules: List<ValueRule>,
)
