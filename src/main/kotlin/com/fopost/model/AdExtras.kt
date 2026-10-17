@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonObject

/** A product catalog on the connection's business portfolio, read live. */
@Serializable
public data class ProductCatalog(
    val id: String? = null,
    val name: String? = null,
    val vertical: String? = null,
    @SerialName("product_count") @JsonNames("productCount") val productCount: Long? = null,
)

/** The catalogs one connection reaches. */
@Serializable
public data class ProductCatalogsResult(
    val catalogs: List<ProductCatalog> = emptyList(),
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
)

/** One product in a catalog. [priceMinor] is minor units of [currency]. */
@Serializable
public data class CatalogProduct(
    val id: String? = null,
    /** Your own key for the product. */
    @SerialName("retailer_id") @JsonNames("retailerId") val retailerId: String? = null,
    val name: String? = null,
    val description: String? = null,
    val availability: String? = null,
    val condition: String? = null,
    @SerialName("price_minor") @JsonNames("priceMinor") val priceMinor: Long? = null,
    val currency: String? = null,
    @SerialName("image_url") @JsonNames("imageUrl") val imageUrl: String? = null,
    val url: String? = null,
)

/** One page of catalog products; pass [nextCursor] back as `after`. */
@Serializable
public data class CatalogProductsPage(
    val products: List<CatalogProduct> = emptyList(),
    @SerialName("next_cursor") @JsonNames("nextCursor") val nextCursor: String? = null,
)

/** What a catalog product batch was accepted as. */
@Serializable
public data class CatalogBatchResult(
    val handles: List<String> = emptyList(),
    /** Products sent in this batch. */
    val accepted: Long = 0,
)

/** Keeps a catalog in step with a product file you host. */
@Serializable
public data class ProductFeed(
    val id: String? = null,
    val name: String? = null,
    /** Set when the ad platform fetches the file on a schedule. */
    val url: String? = null,
    val schedule: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
)

/** One run the ad platform made of a product feed. */
@Serializable
public data class ProductFeedUpload(
    val id: String? = null,
    @SerialName("started_at") @JsonNames("startedAt") val startedAt: Instant? = null,
    @SerialName("ended_at") @JsonNames("endedAt") val endedAt: Instant? = null,
    val status: String? = null,
    @SerialName("error_count") @JsonNames("errorCount") val errorCount: Long? = null,
    @SerialName("warning_count") @JsonNames("warningCount") val warningCount: Long? = null,
)

/** The slice of a catalog one catalog ad runs from. */
@Serializable
public data class ProductSet(
    val id: String? = null,
    val name: String? = null,
    @SerialName("product_count") @JsonNames("productCount") val productCount: Long? = null,
    /** The ad platform's own product-set filter. */
    val filter: JsonObject? = null,
)

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

/** One public archive entry. Read live on every search and stored nowhere. */
@Serializable
public data class AdLibraryEntry(
    val id: String? = null,
    @SerialName("page_id") @JsonNames("pageId") val pageId: String? = null,
    @SerialName("page_name") @JsonNames("pageName") val pageName: String? = null,
    val bodies: List<String> = emptyList(),
    val titles: List<String> = emptyList(),
    @SerialName("link_urls") @JsonNames("linkUrls") val linkUrls: List<String> = emptyList(),
    @SerialName("snapshot_url") @JsonNames("snapshotUrl") val snapshotUrl: String? = null,
    @SerialName("publisher_platforms")
    @JsonNames("publisherPlatforms")
    val publisherPlatforms: List<String> = emptyList(),
    @SerialName("started_at") @JsonNames("startedAt") val startedAt: Instant? = null,
    @SerialName("ended_at") @JsonNames("endedAt") val endedAt: Instant? = null,
    /** Only on the archive's disclosure entries. */
    val currency: String? = null,
    @SerialName("spend_lower") @JsonNames("spendLower") val spendLower: Long? = null,
    @SerialName("spend_upper") @JsonNames("spendUpper") val spendUpper: Long? = null,
    @SerialName("impressions_lower")
    @JsonNames("impressionsLower")
    val impressionsLower: Long? = null,
    @SerialName("impressions_upper")
    @JsonNames("impressionsUpper")
    val impressionsUpper: Long? = null,
)

/** One page of archive results. */
@Serializable
public data class AdLibraryPage(
    val entries: List<AdLibraryEntry> = emptyList(),
    @SerialName("next_cursor") @JsonNames("nextCursor") val nextCursor: String? = null,
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
