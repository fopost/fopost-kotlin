package com.fopost.param

import com.fopost.internal.InstantSerializer
import com.fopost.model.AdBudget
import com.fopost.model.AdTargeting
import java.time.Instant
import kotlinx.serialization.Serializable

/** Where to send the caller after the Meta login. [method] is `business` or `user`. */
@Serializable
public data class MetaAuthorizeParams(
    val workspaceId: String,
    val method: String? = null,
    val returnTo: String? = null,
)

/**
 * A post FoPost already published, to promote.
 *
 * [goal] is `engagement`, `traffic`, `awareness` or `video_views`. The boost starts paused unless
 * [paused] is `false`.
 */
@Serializable
public data class BoostPostParams(
    val workspaceId: String,
    /** A Meta Ads connection in the workspace. */
    val connectionId: String,
    /** Meta ad account id, `act_…`. */
    val adAccountId: String,
    val postId: String,
    /** The connected account the post went out on. */
    val accountId: String,
    val name: String,
    val goal: String,
    val budget: AdBudget,
    val targeting: AdTargeting,
    val paused: Boolean? = null,
)

/**
 * A standalone ad, built from a creative.
 *
 * [goal] is `engagement`, `traffic`, `awareness` or `video_views`. The ad starts paused unless
 * [paused] is `false`.
 */
@Serializable
public data class CreateAdParams(
    val workspaceId: String,
    /** A Meta Ads connection in the workspace. */
    val connectionId: String,
    /** Meta ad account id, `act_…`. */
    val adAccountId: String,
    /** The Facebook Page the ad is published as. */
    val pageId: String,
    val name: String,
    val goal: String,
    val budget: AdBudget,
    val targeting: AdTargeting,
    val text: String,
    val headline: String? = null,
    val destinationUrl: String? = null,
    val mediaUrl: String? = null,
    val paused: Boolean? = null,
    /** Query string appended to every link in the ad, e.g. `utm_source=meta&utm_medium=paid`. */
    val urlTags: String? = null,
)

/**
 * What an audience is built from. Use [custom], [lookalike] or [website]; each sends only the
 * fields its subtype reads.
 */
@Serializable
public data class AudienceSpec(
    /** `CUSTOM`, `LOOKALIKE` or `WEBSITE`. */
    val subtype: String,
    /** Customer list; hashed before it leaves the API. */
    val emails: List<String>? = null,
    val originAudienceId: String? = null,
    /** ISO 3166-1 alpha-2. */
    val country: String? = null,
    /** 0.01 to 0.2. */
    val ratio: Double? = null,
    val pixelId: String? = null,
    /** 1 to 180. */
    val retentionDays: Int? = null,
    val urlContains: String? = null,
) {
    public companion object {
        public fun custom(emails: List<String> = emptyList()): AudienceSpec =
            AudienceSpec(subtype = "CUSTOM", emails = emails)

        public fun lookalike(originAudienceId: String, country: String, ratio: Double? = null): AudienceSpec =
            AudienceSpec(subtype = "LOOKALIKE", originAudienceId = originAudienceId, country = country, ratio = ratio)

        public fun website(pixelId: String, retentionDays: Int? = null, urlContains: String? = null): AudienceSpec =
            AudienceSpec(subtype = "WEBSITE", pixelId = pixelId, retentionDays = retentionDays, urlContains = urlContains)
    }
}

/** An audience to create on an ad account. */
@Serializable
public data class CreateAudienceParams(
    val workspaceId: String,
    val connectionId: String,
    /** Meta ad account id, `act_…`. */
    val adAccountId: String,
    val name: String,
    val spec: AudienceSpec,
    val description: String? = null,
)

/** An Instant Form to create on a Page. [questions] are `EMAIL`, `FULL_NAME` and `PHONE`, one to three of them. */
@Serializable
public data class CreateLeadFormParams(
    val workspaceId: String,
    val connectionId: String,
    val pageId: String,
    val name: String,
    val questions: List<String>,
    val privacyPolicyUrl: String,
    val thankYouMessage: String,
    val followUpUrl: String? = null,
)

@Serializable
internal data class SetAdStatusBody(val status: String)

/**
 * A campaign on a Meta ad account. [goal] is `engagement`, `traffic`, `awareness` or
 * `video_views`. It starts paused unless [paused] is `false`.
 */
@Serializable
public data class CreateAdCampaignParams(
    val workspaceId: String,
    val connectionId: String,
    /** Meta ad account id, `act_…`. */
    val adAccountId: String,
    val name: String,
    val goal: String,
    val paused: Boolean? = null,
)

/** Changes to a campaign. [status] is `active` or `paused`. */
@Serializable
public data class UpdateAdCampaignParams(
    val name: String? = null,
    val status: String? = null,
)

/**
 * An ad set inside a campaign. [pageId] is the Page its ads run as. It starts paused unless
 * [paused] is `false`.
 */
@Serializable
public data class CreateAdSetParams(
    val workspaceId: String,
    val connectionId: String,
    val campaignId: String,
    val pageId: String,
    val name: String,
    val goal: String,
    val budget: AdBudget,
    val targeting: AdTargeting,
    val paused: Boolean? = null,
)

/** Changes to an ad set. [budgetMinor] keeps the budget type set at creation. */
@Serializable
public data class UpdateAdSetParams(
    val name: String? = null,
    /** `active` or `paused`. */
    val status: String? = null,
    val budgetMinor: Long? = null,
    @Serializable(with = InstantSerializer::class) val endAt: Instant? = null,
    val targeting: AdTargeting? = null,
)

/**
 * An ad inside an ad set, from a creative. Unlike [CreateAdParams] it builds no campaign. It
 * starts paused unless [paused] is `false`.
 */
@Serializable
public data class CreateNetworkAdParams(
    val workspaceId: String,
    val connectionId: String,
    val adSetId: String,
    val creativeId: String,
    val name: String,
    val paused: Boolean? = null,
)

/** Changes to an ad inside an ad set. [status] is `active` or `paused`. */
@Serializable
public data class UpdateNetworkAdParams(
    val name: String? = null,
    val status: String? = null,
    val creativeId: String? = null,
)

@Serializable
internal data class DuplicateAdObjectBody(val paused: Boolean? = null)

/** One object of a bulk status change. [level] is `campaign`, `ad_set` or `ad`. */
@Serializable
public data class AdObjectRef(
    val id: String,
    val level: String,
)

/** Pause or resume up to 50 campaigns, ad sets and ads at once. [status] is `active` or `paused`. */
@Serializable
public data class BulkAdStatusParams(
    val workspaceId: String,
    val connectionId: String,
    val status: String,
    val objects: List<AdObjectRef>,
)

/** One carousel card. [mediaUrl] is a library image. */
@Serializable
public data class AdCreativeCard(
    val mediaUrl: String,
    val destinationUrl: String? = null,
    val headline: String? = null,
    val description: String? = null,
)

/**
 * A creative on a Meta ad account. [format] is `image`, `video` or `carousel`; a video needs
 * [mediaUrl], a carousel two to ten [cards].
 */
@Serializable
public data class CreateAdCreativeParams(
    val workspaceId: String,
    val connectionId: String,
    /** Meta ad account id, `act_…`. */
    val adAccountId: String,
    val pageId: String,
    val name: String,
    val format: String,
    /** Primary text. */
    val text: String,
    val headline: String? = null,
    val destinationUrl: String? = null,
    /** `LEARN_MORE` (the default), `SHOP_NOW`, `SIGN_UP`, `SUBSCRIBE`, `CONTACT_US`, and so on. */
    val callToAction: String? = null,
    /** Query string appended to every link in the ad, e.g. `utm_source=meta&utm_medium=paid`. */
    val urlTags: String? = null,
    /** A media library asset url: the image, or the video. */
    val mediaUrl: String? = null,
    /** A video's poster frame, as a library image. */
    val thumbnailMediaUrl: String? = null,
    val cards: List<AdCreativeCard>? = null,
)

/** Changes to a saved audience. */
@Serializable
public data class UpdateAudienceParams(
    val name: String? = null,
    val description: String? = null,
)

@Serializable
internal data class AddAudienceUsersBody(val emails: List<String>)

/** The targeting to size, on one ad account and Page. */
@Serializable
public data class ReachEstimateParams(
    val workspaceId: String,
    val connectionId: String,
    /** Meta ad account id, `act_…`. */
    val adAccountId: String,
    val pageId: String,
    val targeting: AdTargeting,
)

@Serializable
internal data class LeadPageBody(
    val workspaceId: String,
    val connectionId: String,
    val pageId: String,
)

/**
 * One row of a company-list upload. At least one of [name], [domain], [pageUrl] or [ticker] is
 * required; the rows travel with the request and are never stored.
 */
@Serializable
public data class AdCompany(
    val name: String? = null,
    val domain: String? = null,
    /** The company's page on the network. */
    val pageUrl: String? = null,
    /** Stock ticker, where the network matches on one. */
    val ticker: String? = null,
    val country: String? = null,
)

@Serializable
internal data class AddAudienceCompaniesBody(val companies: List<AdCompany>)

/**
 * The body of a bid-pricing or supply-forecast request. [bidType] applies to bid pricing only,
 * [budgetMinor] to the supply forecast only.
 */
@Serializable
public data class AdForecastParams(
    val workspaceId: String,
    val connectionId: String,
    /** The ad account as the network addresses it. */
    val adAccountId: String,
    /** `engagement`, `traffic`, `awareness` or `video_views`. */
    val goal: String,
    val targeting: AdTargeting,
    val placements: List<String>? = null,
    /** `CPC`, `CPM` or `CPV`. */
    val bidType: String? = null,
    /** The budget for the forecast window, minor units. */
    val budgetMinor: Long? = null,
)

/** A new conversion rule on one ad account. */
@Serializable
public data class CreateConversionRuleParams(
    val workspaceId: String,
    val connectionId: String,
    val adAccountId: String,
    val name: String,
    /** `purchase`, `lead`, `sign_up`, `add_to_cart`, `download`, `install`, `key_page_view` or `other`. */
    val type: String,
    /** `last_touch` or `each_campaign`. */
    val attribution: String,
    val postClickWindowDays: Int? = null,
    val viewThroughWindowDays: Int? = null,
    /** What one conversion is worth, minor units. */
    val valueMinor: Long? = null,
    val currency: String? = null,
)

/** Changes to a conversion rule. Only the fields you set move. */
@Serializable
public data class UpdateConversionRuleParams(
    val name: String? = null,
    val type: String? = null,
    val attribution: String? = null,
    val postClickWindowDays: Int? = null,
    val viewThroughWindowDays: Int? = null,
    val valueMinor: Long? = null,
    val currency: String? = null,
    val enabled: Boolean? = null,
)

@Serializable
internal data class ConversionAssociationBody(val campaignId: String)

/**
 * One conversion sent back to the network. It needs an [email] or a [clickId]; the address is
 * hashed inside the API and nothing about an event is stored.
 */
@Serializable
public data class ConversionEvent(
    /** Epoch milliseconds. */
    val happenedAt: Long,
    val valueMinor: Long? = null,
    val currency: String? = null,
    /** Your own id for the event, so a replay is counted once. */
    val eventId: String? = null,
    val email: String? = null,
    /** The network's click id, as the landing page received it. */
    val clickId: String? = null,
)

@Serializable
internal data class ConversionEventsBody(val events: List<ConversionEvent>)

/** What an ad-library search narrows on. Dates are `YYYY-MM-DD`. */
public data class AdLibraryParams(
    val connectionId: String,
    val workspaceId: String? = null,
    val keyword: String? = null,
    val advertiser: String? = null,
    /** ISO 3166-1 alpha-2 codes. */
    val countries: List<String>? = null,
    val since: String? = null,
    val until: String? = null,
    /** The `nextCursor` from the previous page. */
    val cursor: String? = null,
)
