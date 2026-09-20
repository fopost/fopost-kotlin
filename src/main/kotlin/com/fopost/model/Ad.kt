@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonObject

// AdBudget and AdTargeting travel in both directions, so they keep the request spelling
// (camelCase) as the serial name and accept snake_case on the way in.

/** How much an ad may spend. [minor] is in the ad account currency, minor units. */
@Serializable
public data class AdBudget(
    val minor: Long,
    /** `daily` or `lifetime`. */
    val type: String,
    /** When a `lifetime` budget stops delivering. */
    val endAt: Instant? = null,
)

/** A location below country level, from `ads.searchTargeting`. `type` is region, city, zip or geo_market. */
@Serializable
public data class AdTargetingLocation(
    val key: String,
    val name: String,
    val type: String,
)

/** An interest, behaviour or income bracket as Meta names it. */
@Serializable
public data class AdTargetingOption(
    val id: String,
    val name: String? = null,
)

/** Who an ad is shown to. At least one country or one location is required. */
@Serializable
public data class AdTargeting(
    /** ISO 3166-1 alpha-2 codes. */
    val countries: List<String>? = null,
    @JsonNames("age_min") val ageMin: Int? = null,
    @JsonNames("age_max") val ageMax: Int? = null,
    /** `all`, `male` or `female`. */
    val gender: String? = null,
    @JsonNames("audience_ids") val audienceIds: List<String>? = null,
    val locations: List<AdTargetingLocation>? = null,
    val interests: List<AdTargetingOption>? = null,
    val behaviors: List<AdTargetingOption>? = null,
    val income: List<AdTargetingOption>? = null,
    /**
     * Facets the network defines for itself, keyed by the targeting search type they were found
     * with. `ads.providers()` reports which a network accepts.
     */
    val facets: Map<String, List<AdTargetingOption>>? = null,
)

/** Lifetime delivery numbers from the last refresh. [spendMinor] is in the ad account currency. */
@Serializable
public data class AdInsights(
    val impressions: Long? = null,
    val reach: Long? = null,
    val clicks: Long? = null,
    @SerialName("spend_minor") @JsonNames("spendMinor") val spendMinor: Long? = null,
)

/**
 * A boost or standalone ad created through FoPost.
 *
 * [kind] is `boost` or `ad`; [goal] is `engagement`, `traffic`, `awareness` or `video_views`.
 */
@Serializable
public data class Ad(
    val id: String? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
    val kind: String? = null,
    val name: String? = null,
    val goal: String? = null,
    val status: String? = null,
    @SerialName("effective_status") @JsonNames("effectiveStatus") val effectiveStatus: String? = null,
    @SerialName("connection_id") @JsonNames("connectionId") val connectionId: String? = null,
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    val platform: String? = null,
    @SerialName("ad_account_id") @JsonNames("adAccountId") val adAccountId: String? = null,
    @SerialName("source_post_id") @JsonNames("sourcePostId") val sourcePostId: String? = null,
    @SerialName("budget_minor") @JsonNames("budgetMinor") val budgetMinor: Long? = null,
    @SerialName("budget_type") @JsonNames("budgetType") val budgetType: String? = null,
    val currency: String? = null,
    @SerialName("end_at") @JsonNames("endAt") val endAt: Instant? = null,
    val targeting: AdTargeting? = null,
    val creative: JsonObject? = null,
    val insights: AdInsights? = null,
    @SerialName("insights_at") @JsonNames("insightsAt") val insightsAt: Instant? = null,
    @SerialName("last_error") @JsonNames("lastError") val lastError: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
)

/** An ad on a connected ad account that was made outside FoPost. Read live, never stored. */
@Serializable
public data class ExternalAd(
    val id: String? = null,
    val name: String? = null,
    @SerialName("effective_status") @JsonNames("effectiveStatus") val effectiveStatus: String? = null,
    @SerialName("campaign_id") @JsonNames("campaignId") val campaignId: String? = null,
    @SerialName("campaign_name") @JsonNames("campaignName") val campaignName: String? = null,
    val objective: String? = null,
    @SerialName("budget_minor") @JsonNames("budgetMinor") val budgetMinor: Long? = null,
    @SerialName("budget_type") @JsonNames("budgetType") val budgetType: String? = null,
    @SerialName("end_at") @JsonNames("endAt") val endAt: Instant? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    @SerialName("connection_id") @JsonNames("connectionId") val connectionId: String? = null,
    @SerialName("ad_account_id") @JsonNames("adAccountId") val adAccountId: String? = null,
    val currency: String? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
)

/** A Meta Ads login granted to a workspace. */
@Serializable
public data class AdConnection(
    val id: String? = null,
    val provider: String? = null,
    @SerialName("auth_type") @JsonNames("authType") val authType: String? = null,
    val name: String? = null,
    @SerialName("business_id") @JsonNames("businessId") val businessId: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
)

/** A connection with the ad accounts and Pages its grant reaches. */
@Serializable
public data class AdSource(
    @SerialName("connection_id") @JsonNames("connectionId") val connectionId: String? = null,
    val name: String? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
    @SerialName("ad_accounts") @JsonNames("adAccounts") val adAccounts: List<JsonObject> = emptyList(),
    val pages: List<JsonObject> = emptyList(),
    val error: String? = null,
)

/** A published post that can be boosted. */
@Serializable
public data class BoostablePost(
    val id: String? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
    val text: String? = null,
    @SerialName("thumbnail_url") @JsonNames("thumbnailUrl") val thumbnailUrl: String? = null,
    val deliveries: List<JsonObject> = emptyList(),
)

/** A saved audience on an ad account. */
@Serializable
public data class Audience(
    val id: String? = null,
    val name: String? = null,
    val subtype: String? = null,
    val description: String? = null,
    @SerialName("size_lower") @JsonNames("sizeLower") val sizeLower: Long? = null,
    @SerialName("size_upper") @JsonNames("sizeUpper") val sizeUpper: Long? = null,
    @SerialName("delivery_status") @JsonNames("deliveryStatus") val deliveryStatus: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: String? = null,
)

/** The audiences and pixels on an ad account. */
@Serializable
public data class AudiencesResult(
    val audiences: List<Audience> = emptyList(),
    val pixels: List<JsonObject> = emptyList(),
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
)

/** The audience just created, and how many emails Meta accepted. */
@Serializable
public data class CreatedAudience(
    val id: String? = null,
    val added: Int? = null,
)

/** A location, interest, behaviour or income bracket as Meta names it. */
@Serializable
public data class TargetingOption(
    val id: String? = null,
    val name: String? = null,
    val detail: String? = null,
)

/** An Instant Form on a Page. */
@Serializable
public data class LeadForm(
    val id: String? = null,
    val name: String? = null,
    val status: String? = null,
    @SerialName("leads_count") @JsonNames("leadsCount") val leadsCount: Int? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: String? = null,
    val questions: List<String> = emptyList(),
)

/** A connection and Page with the forms on it. */
@Serializable
public data class LeadFormSource(
    @SerialName("connection_id") @JsonNames("connectionId") val connectionId: String? = null,
    @SerialName("connection_name") @JsonNames("connectionName") val connectionName: String? = null,
    @SerialName("page_id") @JsonNames("pageId") val pageId: String? = null,
    @SerialName("page_name") @JsonNames("pageName") val pageName: String? = null,
    val forms: List<LeadForm> = emptyList(),
    val error: String? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
)

/** One lead submitted through an Instant Form. */
@Serializable
public data class Lead(
    val id: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: String? = null,
    val fields: List<JsonObject> = emptyList(),
    @SerialName("ad_name") @JsonNames("adName") val adName: String? = null,
    @SerialName("campaign_name") @JsonNames("campaignName") val campaignName: String? = null,
    val platform: String? = null,
    @SerialName("is_organic") @JsonNames("isOrganic") val isOrganic: Boolean? = null,
)

/** One page of leads. Pass [nextCursor] back as `after` for the next. */
@Serializable
public data class LeadsPage(
    val leads: List<Lead> = emptyList(),
    @SerialName("next_cursor") @JsonNames("nextCursor") val nextCursor: String? = null,
)

/** A campaign on a Meta ad account, read live. [status] is `ACTIVE`, `PAUSED`, `DELETED` or `ARCHIVED`. */
@Serializable
public data class AdCampaign(
    val id: String? = null,
    val name: String? = null,
    val status: String? = null,
    @SerialName("effective_status") @JsonNames("effectiveStatus") val effectiveStatus: String? = null,
    val objective: String? = null,
    /** Null when the budget lives on the ad sets. */
    @SerialName("budget_minor") @JsonNames("budgetMinor") val budgetMinor: Long? = null,
    @SerialName("budget_type") @JsonNames("budgetType") val budgetType: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: String? = null,
)

/** An ad set on a Meta ad account, read live. [budgetType] is `daily` or `lifetime`. */
@Serializable
public data class AdSet(
    val id: String? = null,
    val name: String? = null,
    @SerialName("campaign_id") @JsonNames("campaignId") val campaignId: String? = null,
    val status: String? = null,
    @SerialName("effective_status") @JsonNames("effectiveStatus") val effectiveStatus: String? = null,
    @SerialName("budget_minor") @JsonNames("budgetMinor") val budgetMinor: Long? = null,
    @SerialName("budget_type") @JsonNames("budgetType") val budgetType: String? = null,
    @SerialName("end_at") @JsonNames("endAt") val endAt: String? = null,
    @SerialName("optimization_goal") @JsonNames("optimizationGoal") val optimizationGoal: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: String? = null,
)

/** An ad inside an ad set on a Meta ad account, read live. */
@Serializable
public data class NetworkAd(
    val id: String? = null,
    val name: String? = null,
    @SerialName("campaign_id") @JsonNames("campaignId") val campaignId: String? = null,
    @SerialName("ad_set_id") @JsonNames("adSetId") val adSetId: String? = null,
    @SerialName("creative_id") @JsonNames("creativeId") val creativeId: String? = null,
    val status: String? = null,
    @SerialName("effective_status") @JsonNames("effectiveStatus") val effectiveStatus: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: String? = null,
)

/** An ad set in an [AdAccountTree], with its ads. */
@Serializable
public data class AdSetNode(
    val id: String? = null,
    val name: String? = null,
    @SerialName("campaign_id") @JsonNames("campaignId") val campaignId: String? = null,
    val status: String? = null,
    @SerialName("effective_status") @JsonNames("effectiveStatus") val effectiveStatus: String? = null,
    @SerialName("budget_minor") @JsonNames("budgetMinor") val budgetMinor: Long? = null,
    @SerialName("budget_type") @JsonNames("budgetType") val budgetType: String? = null,
    @SerialName("end_at") @JsonNames("endAt") val endAt: String? = null,
    @SerialName("optimization_goal") @JsonNames("optimizationGoal") val optimizationGoal: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: String? = null,
    val ads: List<NetworkAd> = emptyList(),
)

/** A campaign in an [AdAccountTree], with its ad sets. */
@Serializable
public data class CampaignNode(
    val id: String? = null,
    val name: String? = null,
    val status: String? = null,
    @SerialName("effective_status") @JsonNames("effectiveStatus") val effectiveStatus: String? = null,
    val objective: String? = null,
    @SerialName("budget_minor") @JsonNames("budgetMinor") val budgetMinor: Long? = null,
    @SerialName("budget_type") @JsonNames("budgetType") val budgetType: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: String? = null,
    @SerialName("ad_sets") @JsonNames("adSets") val adSets: List<AdSetNode> = emptyList(),
)

/** Every campaign on an ad account, with its ad sets and their ads. */
@Serializable
public data class AdAccountTree(
    @SerialName("ad_account_id") @JsonNames("adAccountId") val adAccountId: String? = null,
    val currency: String? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
    val campaigns: List<CampaignNode> = emptyList(),
)

/** The outcome for one object of a bulk status change. [error] is null when it worked. */
@Serializable
public data class BulkAdStatusResult(
    val id: String? = null,
    /** `campaign`, `ad_set` or `ad`. */
    val level: String? = null,
    val ok: Boolean? = null,
    val error: String? = null,
)

/** A creative on a Meta ad account. [format] is `image`, `video`, `carousel`, `post` or `other`. */
@Serializable
public data class AdCreative(
    val id: String? = null,
    val name: String? = null,
    val format: String? = null,
    val status: String? = null,
    val title: String? = null,
    val body: String? = null,
    val link: String? = null,
    @SerialName("thumbnail_url") @JsonNames("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerialName("call_to_action") @JsonNames("callToAction") val callToAction: String? = null,
    @SerialName("url_tags") @JsonNames("urlTags") val urlTags: String? = null,
)

/** The creatives on an ad account. */
@Serializable
internal data class AdCreativesResult(
    val creatives: List<AdCreative> = emptyList(),
)

/** The audience size range for a targeting. [ready] is false while Meta is still sizing it. */
@Serializable
public data class ReachEstimate(
    val lower: Long? = null,
    val upper: Long? = null,
    val ready: Boolean? = null,
)

/** Delivery numbers. [spendMinor] is in the account currency, minor units; [ctr] is a percentage. */
@Serializable
public data class InsightsMetrics(
    val impressions: Long? = null,
    val reach: Long? = null,
    val clicks: Long? = null,
    @SerialName("spend_minor") @JsonNames("spendMinor") val spendMinor: Long? = null,
    val ctr: Double? = null,
    val leads: Long? = null,
)

/** One row of an insights breakdown, keyed by age band, gender, placement or country. */
@Serializable
public data class InsightsBreakdownRow(
    val key: String? = null,
    val metrics: InsightsMetrics? = null,
)

/** One day of an insights timeline. */
@Serializable
public data class InsightsTimelineRow(
    val date: String? = null,
    val metrics: InsightsMetrics? = null,
)

/**
 * Delivery numbers for one object over a date range. [breakdown] is filled when a breakdown was
 * asked for, [timeline] when daily numbers were; [totals] is null without data.
 */
@Serializable
public data class AdInsightsReport(
    @SerialName("object_id") @JsonNames("objectId") val objectId: String? = null,
    val currency: String? = null,
    val since: String? = null,
    val until: String? = null,
    @SerialName("breakdown_by") @JsonNames("breakdownBy") val breakdownBy: String? = null,
    val totals: InsightsMetrics? = null,
    val breakdown: List<InsightsBreakdownRow> = emptyList(),
    val timeline: List<InsightsTimelineRow> = emptyList(),
)

/** An Instant Form with its Page, privacy policy and locale. */
@Serializable
public data class LeadFormDetail(
    val id: String? = null,
    val name: String? = null,
    val status: String? = null,
    @SerialName("leads_count") @JsonNames("leadsCount") val leadsCount: Int? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: String? = null,
    val questions: List<String> = emptyList(),
    @SerialName("page_id") @JsonNames("pageId") val pageId: String? = null,
    @SerialName("privacy_policy_url") @JsonNames("privacyPolicyUrl") val privacyPolicyUrl: String? = null,
    val locale: String? = null,
)

/** One answer on a stored lead. */
@Serializable
public data class LeadField(
    val name: String? = null,
    val values: List<String> = emptyList(),
)

/** A lead stored from a subscribed Page. [leadId] is Meta's id. */
@Serializable
public data class FeedLead(
    val id: String? = null,
    @SerialName("lead_id") @JsonNames("leadId") val leadId: String? = null,
    @SerialName("connection_id") @JsonNames("connectionId") val connectionId: String? = null,
    @SerialName("page_id") @JsonNames("pageId") val pageId: String? = null,
    @SerialName("form_id") @JsonNames("formId") val formId: String? = null,
    @SerialName("ad_id") @JsonNames("adId") val adId: String? = null,
    @SerialName("ad_name") @JsonNames("adName") val adName: String? = null,
    @SerialName("campaign_name") @JsonNames("campaignName") val campaignName: String? = null,
    val platform: String? = null,
    @SerialName("is_organic") @JsonNames("isOrganic") val isOrganic: Boolean? = null,
    val fields: List<LeadField> = emptyList(),
    @SerialName("submitted_at") @JsonNames("submittedAt") val submittedAt: Instant? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
)

/** One page of the leads feed. Pass [nextCursor] back as `cursor`; null means the end. */
@Serializable
public data class LeadsFeed(
    val leads: List<FeedLead> = emptyList(),
    @SerialName("next_cursor") @JsonNames("nextCursor") val nextCursor: String? = null,
)

/** A Page whose new leads FoPost stores as they arrive. */
@Serializable
public data class LeadPage(
    @SerialName("connection_id") @JsonNames("connectionId") val connectionId: String? = null,
    @SerialName("page_id") @JsonNames("pageId") val pageId: String? = null,
    @SerialName("page_name") @JsonNames("pageName") val pageName: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
)

/** A new lead Page subscription. [backfilled] counts the existing leads stored with it. */
@Serializable
public data class LeadPageSubscription(
    @SerialName("page_id") @JsonNames("pageId") val pageId: String? = null,
    val backfilled: Int? = null,
)

/** A token a network expands in a link's tracking parameters at delivery time. */
@Serializable
public data class AdTrackingMacro(
    val token: String? = null,
    val description: String? = null,
)

/** An ad network from the API's registry. [configured] false cannot be connected yet. */
@Serializable
public data class AdProvider(
    val id: String? = null,
    val name: String? = null,
    /** Logo slug. */
    val logo: String? = null,
    val configured: Boolean? = null,
    @SerialName("connect_methods") @JsonNames("connectMethods") val connectMethods: List<String> = emptyList(),
    /** What the network supports: campaigns, audiences, conversions, forecasts, and so on. */
    val capabilities: Map<String, Boolean> = emptyMap(),
    /** What `searchTargeting` accepts here, in picker order. */
    @SerialName("targeting_facets") @JsonNames("targetingFacets") val targetingFacets: List<String> = emptyList(),
    @SerialName("tracking_macros")
    @JsonNames("trackingMacros")
    val trackingMacros: List<AdTrackingMacro> = emptyList(),
)

/** What the auction costs, in minor units of the ad account currency. */
@Serializable
public data class BidPricing(
    val currency: String? = null,
    @SerialName("suggested_bid_minor") @JsonNames("suggestedBidMinor") val suggestedBidMinor: Long? = null,
    @SerialName("min_bid_minor") @JsonNames("minBidMinor") val minBidMinor: Long? = null,
    @SerialName("max_bid_minor") @JsonNames("maxBidMinor") val maxBidMinor: Long? = null,
    @SerialName("daily_budget_floor_minor")
    @JsonNames("dailyBudgetFloorMinor")
    val dailyBudgetFloorMinor: Long? = null,
)

/**
 * What an audience would deliver at a budget, over the network's own window. [ready] is false
 * while the network has no answer for that audience.
 */
@Serializable
public data class SupplyForecast(
    val currency: String? = null,
    val impressions: Long? = null,
    val clicks: Long? = null,
    @SerialName("spend_minor") @JsonNames("spendMinor") val spendMinor: Long? = null,
    /** Days the numbers cover. */
    @SerialName("window_days") @JsonNames("windowDays") val windowDays: Long? = null,
    val ready: Boolean? = null,
)

/** How the network attributes a sale or a sign-up back to an ad set. */
@Serializable
public data class ConversionRule(
    val id: String? = null,
    val name: String? = null,
    /** `purchase`, `lead`, `sign_up`, `add_to_cart`, `download`, `install`, `key_page_view` or `other`. */
    val type: String? = null,
    /** `last_touch` or `each_campaign`. */
    val attribution: String? = null,
    @SerialName("post_click_window_days")
    @JsonNames("postClickWindowDays")
    val postClickWindowDays: Int? = null,
    @SerialName("view_through_window_days")
    @JsonNames("viewThroughWindowDays")
    val viewThroughWindowDays: Int? = null,
    @SerialName("value_minor") @JsonNames("valueMinor") val valueMinor: Long? = null,
    val currency: String? = null,
    val enabled: Boolean? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: String? = null,
    /** Ad sets this rule is attached to. */
    @SerialName("campaign_ids") @JsonNames("campaignIds") val campaignIds: List<String> = emptyList(),
)

/** What a conversion rule recorded over a date range. */
@Serializable
public data class ConversionMetrics(
    val conversions: Int? = null,
    @SerialName("post_click_conversions")
    @JsonNames("postClickConversions")
    val postClickConversions: Int? = null,
    @SerialName("view_through_conversions")
    @JsonNames("viewThroughConversions")
    val viewThroughConversions: Int? = null,
    @SerialName("value_minor") @JsonNames("valueMinor") val valueMinor: Long? = null,
    @SerialName("cost_per_conversion_minor")
    @JsonNames("costPerConversionMinor")
    val costPerConversionMinor: Long? = null,
)

/** A public ad from the network's own library, never a connection's own data. */
@Serializable
public data class AdLibraryAd(
    val id: String? = null,
    @SerialName("advertiser_name") @JsonNames("advertiserName") val advertiserName: String? = null,
    @SerialName("advertiser_url") @JsonNames("advertiserUrl") val advertiserUrl: String? = null,
    val headline: String? = null,
    val body: String? = null,
    val type: String? = null,
    @SerialName("thumbnail_url") @JsonNames("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerialName("first_impression_at")
    @JsonNames("firstImpressionAt")
    val firstImpressionAt: String? = null,
    @SerialName("last_impression_at") @JsonNames("lastImpressionAt") val lastImpressionAt: String? = null,
    val countries: List<String> = emptyList(),
    @SerialName("details_url") @JsonNames("detailsUrl") val detailsUrl: String? = null,
    /** The paying entity, where the network discloses one. */
    val payer: String? = null,
    @SerialName("impressions_range") @JsonNames("impressionsRange") val impressionsRange: String? = null,
)

/** One page of ad-library results; pass [nextCursor] back as the cursor. */
@Serializable
public data class AdLibraryPage(
    val ads: List<AdLibraryAd> = emptyList(),
    @SerialName("next_cursor") @JsonNames("nextCursor") val nextCursor: String? = null,
)
