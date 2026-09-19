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
