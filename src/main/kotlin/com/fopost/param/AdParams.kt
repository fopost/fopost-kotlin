package com.fopost.param

import com.fopost.model.AdBudget
import com.fopost.model.AdTargeting
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
