package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.Ad
import com.fopost.model.AdAccountTree
import com.fopost.model.AdBusinessCenter
import com.fopost.model.AdCampaign
import com.fopost.model.AdComment
import com.fopost.model.AdCommentsPage
import com.fopost.model.AdConnection
import com.fopost.model.AdCreative
import com.fopost.model.AdCreativesResult
import com.fopost.model.AdIdentity
import com.fopost.model.AdInsightsReport
import com.fopost.model.AdSet
import com.fopost.model.AdSource
import com.fopost.model.Audience
import com.fopost.model.AudiencesResult
import com.fopost.model.BoostablePost
import com.fopost.model.BulkAdStatusResult
import com.fopost.model.CreatedAudience
import com.fopost.model.ExternalAd
import com.fopost.model.LeadFormDetail
import com.fopost.model.LeadFormSource
import com.fopost.model.LeadPage
import com.fopost.model.LeadPageSubscription
import com.fopost.model.LeadsFeed
import com.fopost.model.LeadsPage
import com.fopost.model.NetworkAd
import com.fopost.model.ReachEstimate
import com.fopost.model.SparkPost
import com.fopost.model.TargetingOption
import com.fopost.param.AdCommentParams
import com.fopost.param.AddAudienceUsersBody
import com.fopost.param.BoostPostParams
import com.fopost.param.BulkAdStatusParams
import com.fopost.param.CreateAdCampaignParams
import com.fopost.param.CreateAdCreativeParams
import com.fopost.param.CreateAdParams
import com.fopost.param.CreateAdSetParams
import com.fopost.param.CreateAudienceParams
import com.fopost.param.CreateLeadFormParams
import com.fopost.param.CreateNetworkAdParams
import com.fopost.param.DuplicateAdObjectBody
import com.fopost.param.LeadPageBody
import com.fopost.param.MetaAuthorizeParams
import com.fopost.param.ReachEstimateParams
import com.fopost.param.SetAdStatusBody
import com.fopost.param.UpdateAdCampaignParams
import com.fopost.param.UpdateAdSetParams
import com.fopost.param.UpdateAudienceParams
import com.fopost.param.UpdateNetworkAdParams
import com.fopost.param.UploadConversionsParams
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Meta ads, campaigns, creatives, audiences, insights and lead forms.
 *
 * Every method needs the `ads` scope. [boost], [create], [setStatus] and [delete] spend money and
 * also need `publish`, as do creating, updating, deleting and duplicating campaigns, ad sets and
 * network ads, and [bulkSetStatus].
 */
public class AdsResource internal constructor(private val http: ApiClient) {

    /** Boosts and ads created through FoPost, with insights from their last refresh. */
    public suspend fun list(workspaceId: String? = null): List<Ad> =
        http.callList("GET", "/ads", Ad.serializer(), query = mapOf("workspace_id" to workspaceId))

    /** Ads on the connected ad accounts that were made elsewhere. Read live, never stored. */
    public suspend fun external(workspaceId: String? = null): List<ExternalAd> =
        http.callList("GET", "/ads/external", ExternalAd.serializer(), query = mapOf("workspace_id" to workspaceId))

    /** Published posts that can be boosted. */
    public suspend fun boostable(workspaceId: String? = null): List<BoostablePost> =
        http.callList("GET", "/ads/boostable", BoostablePost.serializer(), query = mapOf("workspace_id" to workspaceId))

    public suspend fun connections(workspaceId: String? = null): List<AdConnection> =
        http.callList(
            "GET",
            "/ads/connections",
            AdConnection.serializer(),
            query = mapOf("workspace_id" to workspaceId),
        )

    /** Each connection with the ad accounts and Pages its grant reaches. */
    public suspend fun sources(workspaceId: String? = null): List<AdSource> =
        http.callList("GET", "/ads/sources", AdSource.serializer(), query = mapOf("workspace_id" to workspaceId))

    /** The Meta login URL. The caller finishes the login in a browser. */
    public suspend fun authorizeMeta(params: MetaAuthorizeParams): String {
        val data = http.call(
            "POST",
            "/ads/connections/meta/authorize",
            JsonObject.serializer(),
            http.jsonBody(params, MetaAuthorizeParams.serializer()),
        )
        return data["url"]?.jsonPrimitive?.contentOrNull.orEmpty()
    }

    /** Disconnect. Also deletes every ad record created through the connection. */
    public suspend fun deleteConnection(connectionId: String, workspaceId: String) {
        http.send("DELETE", "/ads/connections/$connectionId", query = mapOf("workspace_id" to workspaceId))
    }

    /**
     * Promote a post FoPost already published.
     *
     * Needs the `publish` scope as well as `ads`. The boost starts paused unless `paused` is `false`.
     */
    public suspend fun boost(params: BoostPostParams): Ad =
        http.call("POST", "/ads/boost", Ad.serializer(), http.jsonBody(params, BoostPostParams.serializer()))

    /**
     * Create a standalone ad from a creative.
     *
     * Needs the `publish` scope as well as `ads`. The ad starts paused unless `paused` is `false`.
     */
    public suspend fun create(params: CreateAdParams): Ad =
        http.call("POST", "/ads", Ad.serializer(), http.jsonBody(params, CreateAdParams.serializer()))

    /** Read the delivery status and lifetime insights from Meta. */
    public suspend fun refresh(adId: String, workspaceId: String): Ad =
        http.call("POST", "/ads/$adId/refresh", Ad.serializer(), query = mapOf("workspace_id" to workspaceId))

    /** Set the ad `active` or `paused`. Needs the `publish` scope as well as `ads`. */
    public suspend fun setStatus(adId: String, workspaceId: String, status: String): Ad =
        http.call(
            "PATCH",
            "/ads/$adId",
            Ad.serializer(),
            http.jsonBody(SetAdStatusBody(status), SetAdStatusBody.serializer()),
            query = mapOf("workspace_id" to workspaceId),
        )

    /** End delivery and delete the ad on Meta as well as here. Needs the `publish` scope as well as `ads`. */
    public suspend fun delete(adId: String, workspaceId: String) {
        http.send("DELETE", "/ads/$adId", query = mapOf("workspace_id" to workspaceId))
    }

    // Campaigns, ad sets and ads on the ad account, read live from Meta by their Meta ids.

    /** Every campaign on the ad account with its ad sets and ads. */
    public suspend fun accountTree(adAccountId: String, connectionId: String, workspaceId: String? = null): AdAccountTree =
        http.call(
            "GET",
            "/ads/accounts/$adAccountId/tree",
            AdAccountTree.serializer(),
            query = connection(workspaceId, connectionId),
        )

    /** Needs the `publish` scope as well as `ads`. Starts paused unless `paused` is `false`. */
    public suspend fun createCampaign(params: CreateAdCampaignParams): AdCampaign =
        http.call(
            "POST",
            "/ads/campaigns",
            AdCampaign.serializer(),
            http.jsonBody(params, CreateAdCampaignParams.serializer()),
        )

    public suspend fun campaign(campaignId: String, connectionId: String, workspaceId: String? = null): AdCampaign =
        http.call(
            "GET",
            "/ads/campaigns/$campaignId",
            AdCampaign.serializer(),
            query = connection(workspaceId, connectionId),
        )

    /** Needs the `publish` scope as well as `ads`. */
    public suspend fun updateCampaign(
        campaignId: String,
        workspaceId: String,
        connectionId: String,
        params: UpdateAdCampaignParams,
    ): AdCampaign =
        http.call(
            "PATCH",
            "/ads/campaigns/$campaignId",
            AdCampaign.serializer(),
            http.jsonBody(params, UpdateAdCampaignParams.serializer()),
            query = connection(workspaceId, connectionId),
        )

    /** Deletes the campaign with its ad sets and ads. Needs the `publish` scope as well as `ads`. */
    public suspend fun deleteCampaign(campaignId: String, workspaceId: String, connectionId: String) {
        http.send("DELETE", "/ads/campaigns/$campaignId", query = connection(workspaceId, connectionId))
    }

    /**
     * Copy the campaign and everything in it. Returns the copy's Meta id. The copy starts paused
     * unless [paused] is `false`. Needs the `publish` scope as well as `ads`.
     */
    public suspend fun duplicateCampaign(
        campaignId: String,
        workspaceId: String,
        connectionId: String,
        paused: Boolean? = null,
    ): String = duplicate("/ads/campaigns/$campaignId", workspaceId, connectionId, paused)

    /** Needs the `publish` scope as well as `ads`. Starts paused unless `paused` is `false`. */
    public suspend fun createAdSet(params: CreateAdSetParams): AdSet =
        http.call("POST", "/ads/ad-sets", AdSet.serializer(), http.jsonBody(params, CreateAdSetParams.serializer()))

    public suspend fun adSet(adSetId: String, connectionId: String, workspaceId: String? = null): AdSet =
        http.call("GET", "/ads/ad-sets/$adSetId", AdSet.serializer(), query = connection(workspaceId, connectionId))

    /** Needs the `publish` scope as well as `ads`. */
    public suspend fun updateAdSet(
        adSetId: String,
        workspaceId: String,
        connectionId: String,
        params: UpdateAdSetParams,
    ): AdSet =
        http.call(
            "PATCH",
            "/ads/ad-sets/$adSetId",
            AdSet.serializer(),
            http.jsonBody(params, UpdateAdSetParams.serializer()),
            query = connection(workspaceId, connectionId),
        )

    /** Needs the `publish` scope as well as `ads`. */
    public suspend fun deleteAdSet(adSetId: String, workspaceId: String, connectionId: String) {
        http.send("DELETE", "/ads/ad-sets/$adSetId", query = connection(workspaceId, connectionId))
    }

    /** Returns the copy's Meta id. Needs the `publish` scope as well as `ads`. */
    public suspend fun duplicateAdSet(
        adSetId: String,
        workspaceId: String,
        connectionId: String,
        paused: Boolean? = null,
    ): String = duplicate("/ads/ad-sets/$adSetId", workspaceId, connectionId, paused)

    /**
     * An ad inside an ad set, unlike [create], which builds a whole campaign. Needs the `publish`
     * scope as well as `ads`. Starts paused unless `paused` is `false`.
     */
    public suspend fun createNetworkAd(params: CreateNetworkAdParams): NetworkAd =
        http.call("POST", "/ads/ads", NetworkAd.serializer(), http.jsonBody(params, CreateNetworkAdParams.serializer()))

    /** [adId] is Meta's ad id. */
    public suspend fun networkAd(adId: String, connectionId: String, workspaceId: String? = null): NetworkAd =
        http.call("GET", "/ads/ads/$adId", NetworkAd.serializer(), query = connection(workspaceId, connectionId))

    /** Needs the `publish` scope as well as `ads`. */
    public suspend fun updateNetworkAd(
        adId: String,
        workspaceId: String,
        connectionId: String,
        params: UpdateNetworkAdParams,
    ): NetworkAd =
        http.call(
            "PATCH",
            "/ads/ads/$adId",
            NetworkAd.serializer(),
            http.jsonBody(params, UpdateNetworkAdParams.serializer()),
            query = connection(workspaceId, connectionId),
        )

    /** Needs the `publish` scope as well as `ads`. */
    public suspend fun deleteNetworkAd(adId: String, workspaceId: String, connectionId: String) {
        http.send("DELETE", "/ads/ads/$adId", query = connection(workspaceId, connectionId))
    }

    /** Returns the copy's Meta id. Needs the `publish` scope as well as `ads`. */
    public suspend fun duplicateNetworkAd(
        adId: String,
        workspaceId: String,
        connectionId: String,
        paused: Boolean? = null,
    ): String = duplicate("/ads/ads/$adId", workspaceId, connectionId, paused)

    /**
     * Pause or resume campaigns, ad sets and ads in one call; each object reports on its own.
     * Needs the `publish` scope as well as `ads`.
     */
    public suspend fun bulkSetStatus(params: BulkAdStatusParams): List<BulkAdStatusResult> =
        http.callList(
            "POST",
            "/ads/status",
            BulkAdStatusResult.serializer(),
            http.jsonBody(params, BulkAdStatusParams.serializer()),
        )

    /** The creatives on one ad account. */
    public suspend fun creatives(connectionId: String, adAccountId: String, workspaceId: String? = null): List<AdCreative> =
        http.call(
            "GET",
            "/ads/creatives",
            AdCreativesResult.serializer(),
            query = connection(workspaceId, connectionId) + ("ad_account_id" to adAccountId),
        ).creatives

    /** An image, video or carousel creative. */
    public suspend fun createCreative(params: CreateAdCreativeParams): AdCreative =
        http.call(
            "POST",
            "/ads/creatives",
            AdCreative.serializer(),
            http.jsonBody(params, CreateAdCreativeParams.serializer()),
        )

    public suspend fun creative(creativeId: String, connectionId: String, workspaceId: String? = null): AdCreative =
        http.call(
            "GET",
            "/ads/creatives/$creativeId",
            AdCreative.serializer(),
            query = connection(workspaceId, connectionId),
        )

    public suspend fun deleteCreative(creativeId: String, workspaceId: String, connectionId: String) {
        http.send("DELETE", "/ads/creatives/$creativeId", query = connection(workspaceId, connectionId))
    }

    /** The audience size a targeting would reach. */
    public suspend fun estimateReach(params: ReachEstimateParams): ReachEstimate =
        http.call(
            "POST",
            "/ads/reach-estimate",
            ReachEstimate.serializer(),
            http.jsonBody(params, ReachEstimateParams.serializer()),
        )

    /**
     * Insights for any campaign, ad set or ad on the ad account, by its Meta id. Dates are
     * `YYYY-MM-DD`; [breakdown] is `age`, `gender`, `placement` or `country`; [daily] adds a timeline.
     */
    public suspend fun insights(
        connectionId: String,
        objectId: String,
        since: String,
        until: String,
        breakdown: String? = null,
        daily: Boolean? = null,
        workspaceId: String? = null,
    ): AdInsightsReport =
        http.call(
            "GET",
            "/ads/insights",
            AdInsightsReport.serializer(),
            query = connection(workspaceId, connectionId) + mapOf(
                "object_id" to objectId,
                "since" to since,
                "until" to until,
                "breakdown" to breakdown,
                "daily" to daily,
            ),
        )

    /** Insights for a boost or ad created through FoPost, by its FoPost id. */
    public suspend fun adInsights(
        adId: String,
        workspaceId: String,
        since: String,
        until: String,
        breakdown: String? = null,
        daily: Boolean? = null,
    ): AdInsightsReport =
        http.call(
            "GET",
            "/ads/$adId/insights",
            AdInsightsReport.serializer(),
            query = mapOf(
                "workspace_id" to workspaceId,
                "since" to since,
                "until" to until,
                "breakdown" to breakdown,
                "daily" to daily,
            ),
        )

    /** The saved audiences and pixels on an ad account. */
    public suspend fun audiences(connectionId: String, adAccountId: String, workspaceId: String? = null): AudiencesResult =
        http.call(
            "GET",
            "/ads/audiences",
            AudiencesResult.serializer(),
            query = mapOf(
                "workspace_id" to workspaceId,
                "connection_id" to connectionId,
                "ad_account_id" to adAccountId,
            ),
        )

    /** Create an audience. The spec's subtype is `CUSTOM`, `LOOKALIKE` or `WEBSITE`. */
    public suspend fun createAudience(params: CreateAudienceParams): CreatedAudience =
        http.call(
            "POST",
            "/ads/audiences",
            CreatedAudience.serializer(),
            http.jsonBody(params, CreateAudienceParams.serializer()),
        )

    public suspend fun audience(audienceId: String, connectionId: String, workspaceId: String? = null): Audience =
        http.call(
            "GET",
            "/ads/audiences/$audienceId",
            Audience.serializer(),
            query = connection(workspaceId, connectionId),
        )

    public suspend fun updateAudience(
        audienceId: String,
        workspaceId: String,
        connectionId: String,
        params: UpdateAudienceParams,
    ): Audience =
        http.call(
            "PATCH",
            "/ads/audiences/$audienceId",
            Audience.serializer(),
            http.jsonBody(params, UpdateAudienceParams.serializer()),
            query = connection(workspaceId, connectionId),
        )

    public suspend fun deleteAudience(audienceId: String, workspaceId: String, connectionId: String) {
        http.send("DELETE", "/ads/audiences/$audienceId", query = connection(workspaceId, connectionId))
    }

    /** Add people to a custom audience; the emails are hashed before they leave the API. Returns the count sent. */
    public suspend fun addAudienceUsers(
        audienceId: String,
        workspaceId: String,
        connectionId: String,
        emails: List<String>,
    ): Int {
        val data = http.call(
            "POST",
            "/ads/audiences/$audienceId/users",
            JsonObject.serializer(),
            http.jsonBody(AddAudienceUsersBody(emails), AddAudienceUsersBody.serializer()),
            query = connection(workspaceId, connectionId),
        )
        return data["added"]?.jsonPrimitive?.intOrNull ?: 0
    }

    /**
     * Locations, interests, behaviours and income brackets as Meta names them.
     *
     * [type] is `country`, `region`, `city`, `zip`, `metro`, `interest`, `behavior` or `income`.
     */
    public suspend fun searchTargeting(
        connectionId: String,
        type: String,
        q: String? = null,
        workspaceId: String? = null,
    ): List<TargetingOption> =
        http.callList(
            "GET",
            "/ads/targeting/search",
            TargetingOption.serializer(),
            query = mapOf(
                "workspace_id" to workspaceId,
                "connection_id" to connectionId,
                "type" to type,
                "q" to q,
            ),
        )

    /**
     * TikTok's Business Centers. The one network-named read on this resource, because no other
     * network groups ad accounts this way.
     */
    public suspend fun tiktokBusinessCenters(
        connectionId: String,
        workspaceId: String? = null,
    ): List<AdBusinessCenter> =
        http.callList(
            "GET",
            "/ads/tiktok/business-centers",
            AdBusinessCenter.serializer(),
            query = connection(workspaceId, connectionId),
        )

    /** The accounts an ad can run as; an identity id is a `pageId`. */
    public suspend fun tiktokIdentities(
        connectionId: String,
        adAccountId: String,
        workspaceId: String? = null,
    ): List<AdIdentity> =
        http.callList(
            "GET",
            "/ads/tiktok/identities",
            AdIdentity.serializer(),
            query = connection(workspaceId, connectionId) + ("ad_account_id" to adAccountId),
        )

    /** Posts already live under an identity, each a candidate Spark ad. */
    public suspend fun sparkPosts(
        connectionId: String,
        adAccountId: String,
        identityId: String,
        workspaceId: String? = null,
    ): List<SparkPost> =
        http.callList(
            "GET",
            "/ads/spark-posts",
            SparkPost.serializer(),
            query = connection(workspaceId, connectionId) +
                mapOf("ad_account_id" to adAccountId, "identity_id" to identityId),
        )

    /**
     * Offline conversions against a pixel the ad account owns. Identifiers are hashed before
     * anything leaves FoPost; returns how many the network accepted.
     */
    public suspend fun uploadConversions(params: UploadConversionsParams): Long {
        val data = http.call(
            "POST",
            "/ads/conversions",
            JsonObject.serializer(),
            http.jsonBody(params, UploadConversionsParams.serializer()),
        )
        return data["accepted"]?.jsonPrimitive?.longOrNull ?: 0
    }

    /** One page of an ad's comments; pass `nextCursor` back as [after]. */
    public suspend fun comments(
        connectionId: String,
        adId: String,
        after: String? = null,
        workspaceId: String? = null,
    ): AdCommentsPage =
        http.call(
            "GET",
            "/ads/comments",
            AdCommentsPage.serializer(),
            query = connection(workspaceId, connectionId) +
                mapOf("ad_id" to adId, "after" to after),
        )

    /**
     * Answer a comment on an ad; returns the reply's id on the network. Needs the `publish` scope
     * as well as `ads`.
     */
    public suspend fun replyToComment(commentId: String, params: AdCommentParams): String {
        val data = http.call(
            "POST",
            "/ads/comments/$commentId/reply",
            JsonObject.serializer(),
            http.jsonBody(params, AdCommentParams.serializer()),
        )
        return data["replyId"]?.jsonPrimitive?.contentOrNull.orEmpty()
    }

    /** Hide or show a comment on an ad. Needs the `publish` scope as well as `ads`. */
    public suspend fun setCommentHidden(commentId: String, params: AdCommentParams) {
        http.send(
            "POST",
            "/ads/comments/$commentId/hide",
            http.jsonBody(params, AdCommentParams.serializer()),
        )
    }

    /**
     * Remove a comment from the ad on the network. One already gone succeeds. Needs the `publish`
     * scope as well as `ads`.
     */
    public suspend fun deleteComment(commentId: String, params: AdCommentParams) {
        http.send(
            "DELETE",
            "/ads/comments/$commentId",
            http.jsonBody(params, AdCommentParams.serializer()),
        )
    }

    /** Every connection and Page with the Instant Forms on it. */
    public suspend fun leadForms(workspaceId: String? = null): List<LeadFormSource> =
        http.callList(
            "GET",
            "/ads/lead-forms",
            LeadFormSource.serializer(),
            query = mapOf("workspace_id" to workspaceId),
        )

    /** Create an Instant Form on the Page. Returns its id. */
    public suspend fun createLeadForm(params: CreateLeadFormParams): String {
        val data = http.call(
            "POST",
            "/ads/lead-forms",
            JsonObject.serializer(),
            http.jsonBody(params, CreateLeadFormParams.serializer()),
        )
        return data["id"]?.jsonPrimitive?.contentOrNull.orEmpty()
    }

    /** One page of leads. Pass `nextCursor` back as [after] for the next. */
    public suspend fun leads(
        formId: String,
        connectionId: String,
        pageId: String,
        after: String? = null,
        workspaceId: String? = null,
    ): LeadsPage =
        http.call(
            "GET",
            "/ads/lead-forms/$formId/leads",
            LeadsPage.serializer(),
            query = mapOf(
                "workspace_id" to workspaceId,
                "connection_id" to connectionId,
                "page_id" to pageId,
                "after" to after,
            ),
        )

    public suspend fun leadForm(
        formId: String,
        connectionId: String,
        pageId: String,
        workspaceId: String? = null,
    ): LeadFormDetail =
        http.call(
            "GET",
            "/ads/lead-forms/$formId",
            LeadFormDetail.serializer(),
            query = connection(workspaceId, connectionId) + ("page_id" to pageId),
        )

    /** Stop the form taking new leads. Returns it as archived. */
    public suspend fun archiveLeadForm(
        formId: String,
        workspaceId: String,
        connectionId: String,
        pageId: String,
    ): LeadFormDetail =
        http.call(
            "POST",
            "/ads/lead-forms/$formId/archive",
            LeadFormDetail.serializer(),
            http.jsonBody(LeadPageBody(workspaceId, connectionId, pageId), LeadPageBody.serializer()),
        )

    /**
     * Leads stored from the subscribed Pages, newest first. Pass `nextCursor` back as [cursor] for
     * the next page. [limit] is 1 to 100.
     */
    public suspend fun leadsFeed(
        workspaceId: String? = null,
        formId: String? = null,
        pageId: String? = null,
        cursor: String? = null,
        limit: Int? = null,
    ): LeadsFeed =
        http.call(
            "GET",
            "/ads/leads",
            LeadsFeed.serializer(),
            query = mapOf(
                "workspace_id" to workspaceId,
                "form_id" to formId,
                "page_id" to pageId,
                "cursor" to cursor,
                "limit" to limit,
            ),
        )

    /** The Pages whose new leads FoPost stores as they arrive. */
    public suspend fun leadPages(workspaceId: String? = null): List<LeadPage> =
        http.callList("GET", "/ads/lead-pages", LeadPage.serializer(), query = mapOf("workspace_id" to workspaceId))

    /** Start storing a Page's leads as they arrive; its existing leads are backfilled. */
    public suspend fun subscribeLeadPage(workspaceId: String, connectionId: String, pageId: String): LeadPageSubscription =
        http.call(
            "POST",
            "/ads/lead-pages",
            LeadPageSubscription.serializer(),
            http.jsonBody(LeadPageBody(workspaceId, connectionId, pageId), LeadPageBody.serializer()),
        )

    public suspend fun unsubscribeLeadPage(pageId: String, workspaceId: String, connectionId: String) {
        http.send("DELETE", "/ads/lead-pages/$pageId", query = connection(workspaceId, connectionId))
    }

    private suspend fun duplicate(path: String, workspaceId: String, connectionId: String, paused: Boolean?): String {
        val data = http.call(
            "POST",
            "$path/duplicate",
            JsonObject.serializer(),
            http.jsonBody(DuplicateAdObjectBody(paused), DuplicateAdObjectBody.serializer()),
            query = connection(workspaceId, connectionId),
        )
        return data["id"]?.jsonPrimitive?.contentOrNull.orEmpty()
    }

    private fun connection(workspaceId: String?, connectionId: String): Map<String, Any?> =
        mapOf("workspace_id" to workspaceId, "connection_id" to connectionId)
}
