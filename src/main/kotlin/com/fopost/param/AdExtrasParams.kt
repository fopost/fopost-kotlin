package com.fopost.param

import com.fopost.internal.InstantSerializer
import com.fopost.model.AdTargeting
import com.fopost.model.ValueRule
import java.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** A product catalog on the connection's business portfolio. Also needs `publish`. */
@Serializable
public data class CreateCatalogParams(
    val workspaceId: String,
    val connectionId: String,
    val name: String,
    /** The ad platform's catalog vertical; `commerce` when unset. */
    val vertical: String? = null,
)

/** Renames a catalog. Also needs `publish`. */
@Serializable
public data class UpdateCatalogParams(
    val workspaceId: String,
    val connectionId: String,
    val name: String,
)

/**
 * One upsert or delete in a catalog batch, keyed by your own [retailerId]. A delete needs only
 * [op] and [retailerId]; use [upsert] and [delete] rather than filling it in by hand.
 */
@Serializable
public data class CatalogProductWrite(
    /** `upsert` or `delete`. */
    val op: String,
    val retailerId: String,
    val name: String? = null,
    val description: String? = null,
    val url: String? = null,
    val imageUrl: String? = null,
    /** Minor units of [currency]: 12900 with USD is $129.00. */
    val priceMinor: Long? = null,
    val currency: String? = null,
    /** `in stock`, `out of stock`, `preorder`, and so on. */
    val availability: String? = null,
    /** `new`, `refurbished` or `used`. */
    val condition: String? = null,
    val brand: String? = null,
) {
    public companion object {
        /** Adds or replaces a product. */
        public fun upsert(
            retailerId: String,
            name: String,
            url: String,
            imageUrl: String,
            priceMinor: Long,
            currency: String,
            description: String? = null,
            availability: String? = null,
            condition: String? = null,
            brand: String? = null,
        ): CatalogProductWrite = CatalogProductWrite(
            op = "upsert",
            retailerId = retailerId,
            name = name,
            description = description,
            url = url,
            imageUrl = imageUrl,
            priceMinor = priceMinor,
            currency = currency,
            availability = availability,
            condition = condition,
            brand = brand,
        )

        /** Removes a product from the catalog. */
        public fun delete(retailerId: String): CatalogProductWrite =
            CatalogProductWrite(op = "delete", retailerId = retailerId)
    }
}

/** Up to 500 product upserts and deletes in one batch. Also needs `publish`. */
@Serializable
public data class CatalogProductBatchParams(
    val workspaceId: String,
    val connectionId: String,
    val products: List<CatalogProductWrite>,
)

/** A product feed. A [schedule] needs a [url]. Also needs `publish`. */
@Serializable
public data class CreateProductFeedParams(
    val workspaceId: String,
    val connectionId: String,
    val name: String,
    /** Where the ad platform fetches the file; omit for manual uploads. */
    val url: String? = null,
    /** `HOURLY`, `DAILY` or `WEEKLY`. */
    val schedule: String? = null,
)

/** Fetches a feed now. Also needs `publish`. */
@Serializable
public data class StartFeedUploadParams(
    val workspaceId: String,
    val connectionId: String,
    /** Overrides the feed's own url for this run. */
    val url: String? = null,
)

/**
 * A product set: the slice of a catalog one catalog ad runs from. Without a [filter] the set is
 * the whole catalog. Also needs `publish`.
 */
@Serializable
public data class ProductSetParams(
    val workspaceId: String,
    val connectionId: String,
    val name: String,
    val filter: JsonObject? = null,
)

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
