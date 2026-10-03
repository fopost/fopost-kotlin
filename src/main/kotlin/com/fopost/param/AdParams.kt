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
    /**
     * A post already live on the network, from `ads.sparkPosts(...)`. Runs it as a Spark ad, so
     * [text], [headline] and [mediaUrl] are ignored. Needs the network's `sparkAds` capability.
     */
    val sparkPostId: String? = null,
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
    /**
     * Hands targeting and creative rotation to the network. Needs its `smartPlus` capability.
     */
    val smartPlus: Boolean? = null,
)

/** One offline conversion. Identifiers are hashed before anything leaves FoPost. */
@Serializable
public data class ConversionEvent(
    val eventName: String,
    /** ISO 8601. */
    val occurredAt: String,
    val email: String? = null,
    val phone: String? = null,
    /** Account currency, minor units. */
    val valueMinor: Long? = null,
    val currency: String? = null,
    val orderId: String? = null,
)

/** Offline conversions against a pixel the ad account owns. */
@Serializable
public data class UploadConversionsParams(
    val workspaceId: String,
    val connectionId: String,
    val adAccountId: String,
    /** A pixel the ad account owns, from `ads.audiences(...)`. */
    val pixelId: String,
    /** Up to 1000 per call. */
    val events: List<ConversionEvent>,
)

/** Scopes a comment write; the comment id travels in the path. */
@Serializable
public data class AdCommentParams(
    val workspaceId: String,
    val connectionId: String,
    val adId: String,
    /** The reply, on `replyToComment` only. */
    val text: String? = null,
    /** The new state, on `setCommentHidden` only. */
    val hidden: Boolean? = null,
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
