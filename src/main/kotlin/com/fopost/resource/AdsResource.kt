package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.Ad
import com.fopost.model.AdActivity
import com.fopost.model.AdActivityResult
import com.fopost.model.AdLabel
import com.fopost.model.AdLibraryPage
import com.fopost.model.AdStudy
import com.fopost.model.CatalogBatchResult
import com.fopost.model.CatalogProductsPage
import com.fopost.model.HighDemandPeriod
import com.fopost.model.IosCampaignLimits
import com.fopost.model.PartnershipCreator
import com.fopost.model.ProductCatalog
import com.fopost.model.ProductCatalogsResult
import com.fopost.model.ProductFeed
import com.fopost.model.ProductFeedUpload
import com.fopost.model.ProductSet
import com.fopost.model.ReachFrequencyPrediction
import com.fopost.model.ReachFrequencyResult
import com.fopost.model.ValueRuleSet
import com.fopost.param.AdLabelParams
import com.fopost.param.ApplyAdLabelParams
import com.fopost.param.CatalogProductBatchParams
import com.fopost.param.CreateAdStudyParams
import com.fopost.param.CreateCatalogParams
import com.fopost.param.CreateHighDemandPeriodParams
import com.fopost.param.CreateProductFeedParams
import com.fopost.param.CreateReachFrequencyParams
import com.fopost.param.CreateValueRuleSetParams
import com.fopost.param.PartnershipParams
import com.fopost.param.ProductSetParams
import com.fopost.param.ReachFrequencyActionParams
import com.fopost.param.StartFeedUploadParams
import com.fopost.param.UpdateCatalogParams
import com.fopost.model.AdAccountTree
import com.fopost.model.AdCampaign
import com.fopost.model.AdConnection
import com.fopost.model.AdCreative
import com.fopost.model.AdCreativesResult
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
import com.fopost.model.TargetingOption
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
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
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

    // ─── Goals ──────────────────────────────────────────────────────

    /**
     * The goals this connection's ad platform can run right now. Ask rather than assume: a goal
     * the deployment is not set up for is absent here and is refused if you send it anyway.
     */
    public suspend fun goals(connectionId: String, workspaceId: String? = null): List<String> =
        http.callList("GET", "/ads/goals", String.serializer(), query = connection(workspaceId, connectionId))

    // ─── Product catalogs ───────────────────────────────────────────

    /** Catalogs the connection's business portfolios reach. Read live, never stored. */
    public suspend fun catalogs(connectionId: String, workspaceId: String? = null): List<ProductCatalog> =
        http.call(
            "GET",
            "/ads/catalogs",
            ProductCatalogsResult.serializer(),
            query = connection(workspaceId, connectionId),
        ).catalogs

    /** Created on the connection's business portfolio. Also needs `publish`. */
    public suspend fun createCatalog(params: CreateCatalogParams): ProductCatalog =
        http.call(
            "POST",
            "/ads/catalogs",
            ProductCatalog.serializer(),
            http.jsonBody(params, CreateCatalogParams.serializer()),
        )

    public suspend fun catalog(catalogId: String, connectionId: String, workspaceId: String? = null): ProductCatalog =
        http.call(
            "GET",
            "/ads/catalogs/$catalogId",
            ProductCatalog.serializer(),
            query = connection(workspaceId, connectionId),
        )

    /** Also needs `publish`. */
    public suspend fun updateCatalog(
        catalogId: String,
        workspaceId: String,
        connectionId: String,
        params: UpdateCatalogParams,
    ): ProductCatalog =
        http.call(
            "PATCH",
            "/ads/catalogs/$catalogId",
            ProductCatalog.serializer(),
            http.jsonBody(params, UpdateCatalogParams.serializer()),
            query = connection(workspaceId, connectionId),
        )

    /** Deletes every product, feed and set in it. Also needs `publish`. */
    public suspend fun deleteCatalog(catalogId: String, workspaceId: String, connectionId: String) {
        http.send("DELETE", "/ads/catalogs/$catalogId", query = connection(workspaceId, connectionId))
    }

    /** One page of products; pass `nextCursor` back as [after]. */
    public suspend fun catalogProducts(
        catalogId: String,
        connectionId: String,
        workspaceId: String? = null,
        after: String? = null,
    ): CatalogProductsPage =
        http.call(
            "GET",
            "/ads/catalogs/$catalogId/products",
            CatalogProductsPage.serializer(),
            query = connection(workspaceId, connectionId) + ("after" to after),
        )

    /**
     * Up to 500 upserts and deletes in one batch, keyed by your own retailer id. Also needs
     * `publish`.
     */
    public suspend fun writeCatalogProducts(
        catalogId: String,
        params: CatalogProductBatchParams,
    ): CatalogBatchResult =
        http.call(
            "POST",
            "/ads/catalogs/$catalogId/products",
            CatalogBatchResult.serializer(),
            http.jsonBody(params, CatalogProductBatchParams.serializer()),
        )

    public suspend fun productFeeds(
        catalogId: String,
        connectionId: String,
        workspaceId: String? = null,
    ): List<ProductFeed> =
        http.callList(
            "GET",
            "/ads/catalogs/$catalogId/feeds",
            ProductFeed.serializer(),
            query = connection(workspaceId, connectionId),
        )

    /** Also needs `publish`. */
    public suspend fun createProductFeed(catalogId: String, params: CreateProductFeedParams): ProductFeed =
        http.call(
            "POST",
            "/ads/catalogs/$catalogId/feeds",
            ProductFeed.serializer(),
            http.jsonBody(params, CreateProductFeedParams.serializer()),
        )

    /** Also needs `publish`. */
    public suspend fun deleteProductFeed(
        catalogId: String,
        feedId: String,
        workspaceId: String,
        connectionId: String,
    ) {
        http.send(
            "DELETE",
            "/ads/catalogs/$catalogId/feeds/$feedId",
            query = connection(workspaceId, connectionId),
        )
    }

    /** Each run the ad platform made of the feed. */
    public suspend fun feedUploads(
        catalogId: String,
        feedId: String,
        connectionId: String,
        workspaceId: String? = null,
    ): List<ProductFeedUpload> =
        http.callList(
            "GET",
            "/ads/catalogs/$catalogId/feeds/$feedId/uploads",
            ProductFeedUpload.serializer(),
            query = connection(workspaceId, connectionId),
        )

    /** Fetches the feed now; the id of the run. Also needs `publish`. */
    public suspend fun startFeedUpload(
        catalogId: String,
        feedId: String,
        params: StartFeedUploadParams,
    ): String {
        val data = http.call(
            "POST",
            "/ads/catalogs/$catalogId/feeds/$feedId/uploads",
            JsonObject.serializer(),
            http.jsonBody(params, StartFeedUploadParams.serializer()),
        )
        return data["id"]?.jsonPrimitive?.contentOrNull.orEmpty()
    }

    /** A catalog ad runs from a product set, not the whole catalog. */
    public suspend fun productSets(
        catalogId: String,
        connectionId: String,
        workspaceId: String? = null,
    ): List<ProductSet> =
        http.callList(
            "GET",
            "/ads/catalogs/$catalogId/product-sets",
            ProductSet.serializer(),
            query = connection(workspaceId, connectionId),
        )

    /** Also needs `publish`. */
    public suspend fun createProductSet(catalogId: String, params: ProductSetParams): ProductSet =
        http.call(
            "POST",
            "/ads/catalogs/$catalogId/product-sets",
            ProductSet.serializer(),
            http.jsonBody(params, ProductSetParams.serializer()),
        )

    /** Also needs `publish`. */
    public suspend fun updateProductSet(
        catalogId: String,
        setId: String,
        workspaceId: String,
        connectionId: String,
        params: ProductSetParams,
    ): ProductSet =
        http.call(
            "PATCH",
            "/ads/catalogs/$catalogId/product-sets/$setId",
            ProductSet.serializer(),
            http.jsonBody(params, ProductSetParams.serializer()),
            query = connection(workspaceId, connectionId),
        )

    /** Also needs `publish`. */
    public suspend fun deleteProductSet(
        catalogId: String,
        setId: String,
        workspaceId: String,
        connectionId: String,
    ) {
        http.send(
            "DELETE",
            "/ads/catalogs/$catalogId/product-sets/$setId",
            query = connection(workspaceId, connectionId),
        )
    }

    // ─── Reach and frequency ────────────────────────────────────────

    public suspend fun reachFrequency(
        connectionId: String,
        adAccountId: String,
        workspaceId: String? = null,
    ): List<ReachFrequencyPrediction> =
        http.call(
            "GET",
            "/ads/reach-frequency",
            ReachFrequencyResult.serializer(),
            query = account(workspaceId, connectionId, adAccountId),
        ).predictions

    /** Prices a flight. Nothing is bought until you reserve it. */
    public suspend fun createReachFrequency(params: CreateReachFrequencyParams): ReachFrequencyPrediction =
        http.call(
            "POST",
            "/ads/reach-frequency",
            ReachFrequencyPrediction.serializer(),
            http.jsonBody(params, CreateReachFrequencyParams.serializer()),
        )

    public suspend fun reachFrequencyPrediction(
        predictionId: String,
        connectionId: String,
        adAccountId: String,
        workspaceId: String? = null,
    ): ReachFrequencyPrediction =
        http.call(
            "GET",
            "/ads/reach-frequency/$predictionId",
            ReachFrequencyPrediction.serializer(),
            query = account(workspaceId, connectionId, adAccountId),
        )

    /** Holds the inventory the prediction priced. Also needs `publish`. */
    public suspend fun reserveReachFrequency(
        predictionId: String,
        params: ReachFrequencyActionParams,
    ): ReachFrequencyPrediction = reachFrequencyAction(predictionId, "reserve", params)

    /** Also needs `publish`. */
    public suspend fun cancelReachFrequency(
        predictionId: String,
        params: ReachFrequencyActionParams,
    ): ReachFrequencyPrediction = reachFrequencyAction(predictionId, "cancel", params)

    // ─── Ad Library ─────────────────────────────────────────────────

    /**
     * The public ad archive: ads anyone is running, by keyword or by Page. Read live on every call
     * and stored nowhere, so an ad that stops running is simply absent from the next search.
     * [countries] are two-letter codes the ad reached.
     */
    public suspend fun library(
        connectionId: String,
        countries: List<String>,
        q: String? = null,
        pageIds: List<String>? = null,
        activeStatus: String? = null,
        limit: Int? = null,
        after: String? = null,
        workspaceId: String? = null,
    ): AdLibraryPage =
        http.call(
            "GET",
            "/ads/library",
            AdLibraryPage.serializer(),
            query = connection(workspaceId, connectionId) + mapOf(
                "countries" to countries.joinToString(","),
                "q" to q,
                "page_ids" to pageIds?.joinToString(","),
                "active_status" to activeStatus,
                "limit" to limit,
                "after" to after,
            ),
        )

    // ─── Partnership ads ────────────────────────────────────────────

    /** Creators who allowlisted this Page to run partnership ads on their posts. */
    public suspend fun partnershipCreators(
        connectionId: String,
        pageId: String,
        workspaceId: String? = null,
    ): List<PartnershipCreator> =
        http.callList(
            "GET",
            "/ads/partnership/creators",
            PartnershipCreator.serializer(),
            query = connection(workspaceId, connectionId) + ("page_id" to pageId),
        )

    /** Asks a creator for permission; the list as it now stands. */
    public suspend fun requestPartnership(params: PartnershipParams): List<PartnershipCreator> =
        http.callList(
            "POST",
            "/ads/partnership/creators",
            PartnershipCreator.serializer(),
            http.jsonBody(params, PartnershipParams.serializer()),
        )

    public suspend fun revokePartnership(
        creatorId: String,
        workspaceId: String,
        connectionId: String,
        pageId: String,
    ) {
        http.send(
            "DELETE",
            "/ads/partnership/creators/$creatorId",
            query = connection(workspaceId, connectionId) + ("page_id" to pageId),
        )
    }

    // ─── Ad account settings ────────────────────────────────────────

    /** Who changed what on the ad account, and when. Dates are `YYYY-MM-DD`. */
    public suspend fun accountActivity(
        connectionId: String,
        adAccountId: String,
        since: String? = null,
        until: String? = null,
        workspaceId: String? = null,
    ): List<AdActivity> =
        http.call(
            "GET",
            "/ads/account/activity",
            AdActivityResult.serializer(),
            query = account(workspaceId, connectionId, adAccountId) +
                mapOf("since" to since, "until" to until),
        ).activity

    public suspend fun labels(
        connectionId: String,
        adAccountId: String,
        workspaceId: String? = null,
    ): List<AdLabel> =
        http.callList(
            "GET",
            "/ads/account/labels",
            AdLabel.serializer(),
            query = account(workspaceId, connectionId, adAccountId),
        )

    public suspend fun createLabel(params: AdLabelParams): AdLabel =
        http.call(
            "POST",
            "/ads/account/labels",
            AdLabel.serializer(),
            http.jsonBody(params, AdLabelParams.serializer()),
        )

    public suspend fun updateLabel(
        labelId: String,
        workspaceId: String,
        connectionId: String,
        params: AdLabelParams,
    ): AdLabel =
        http.call(
            "PATCH",
            "/ads/account/labels/$labelId",
            AdLabel.serializer(),
            http.jsonBody(params, AdLabelParams.serializer()),
            query = connection(workspaceId, connectionId),
        )

    public suspend fun deleteLabel(
        labelId: String,
        workspaceId: String,
        connectionId: String,
        adAccountId: String,
    ) {
        http.send(
            "DELETE",
            "/ads/account/labels/$labelId",
            query = account(workspaceId, connectionId, adAccountId),
        )
    }

    /** Keeps whatever labels the object already carries. */
    public suspend fun applyLabel(labelId: String, params: ApplyAdLabelParams) {
        http.send(
            "POST",
            "/ads/account/labels/$labelId/apply",
            http.jsonBody(params, ApplyAdLabelParams.serializer()),
        )
    }

    public suspend fun studies(
        connectionId: String,
        adAccountId: String,
        workspaceId: String? = null,
    ): List<AdStudy> =
        http.callList(
            "GET",
            "/ads/account/studies",
            AdStudy.serializer(),
            query = account(workspaceId, connectionId, adAccountId),
        )

    /** Splits traffic evenly across the cells for the length of the flight. */
    public suspend fun createStudy(params: CreateAdStudyParams): AdStudy =
        http.call(
            "POST",
            "/ads/account/studies",
            AdStudy.serializer(),
            http.jsonBody(params, CreateAdStudyParams.serializer()),
        )

    public suspend fun study(
        studyId: String,
        connectionId: String,
        adAccountId: String,
        workspaceId: String? = null,
    ): AdStudy =
        http.call(
            "GET",
            "/ads/account/studies/$studyId",
            AdStudy.serializer(),
            query = account(workspaceId, connectionId, adAccountId),
        )

    public suspend fun deleteStudy(
        studyId: String,
        workspaceId: String,
        connectionId: String,
        adAccountId: String,
    ) {
        http.send(
            "DELETE",
            "/ads/account/studies/$studyId",
            query = account(workspaceId, connectionId, adAccountId),
        )
    }

    /** How many iOS 14 campaigns the account may run at once, per app. */
    public suspend fun iosCampaignLimits(
        connectionId: String,
        adAccountId: String,
        workspaceId: String? = null,
    ): List<IosCampaignLimits> =
        http.callList(
            "GET",
            "/ads/account/ios-limits",
            IosCampaignLimits.serializer(),
            query = account(workspaceId, connectionId, adAccountId),
        )

    public suspend fun highDemandPeriods(
        connectionId: String,
        adAccountId: String,
        workspaceId: String? = null,
    ): List<HighDemandPeriod> =
        http.callList(
            "GET",
            "/ads/account/high-demand-periods",
            HighDemandPeriod.serializer(),
            query = account(workspaceId, connectionId, adAccountId),
        )

    /** Tells the ad platform to expect heavier spend over a window, so pacing allows for it. */
    public suspend fun createHighDemandPeriod(params: CreateHighDemandPeriodParams): HighDemandPeriod =
        http.call(
            "POST",
            "/ads/account/high-demand-periods",
            HighDemandPeriod.serializer(),
            http.jsonBody(params, CreateHighDemandPeriodParams.serializer()),
        )

    public suspend fun deleteHighDemandPeriod(
        periodId: String,
        workspaceId: String,
        connectionId: String,
        adAccountId: String,
    ) {
        http.send(
            "DELETE",
            "/ads/account/high-demand-periods/$periodId",
            query = account(workspaceId, connectionId, adAccountId),
        )
    }

    public suspend fun valueRuleSets(
        connectionId: String,
        adAccountId: String,
        workspaceId: String? = null,
    ): List<ValueRuleSet> =
        http.callList(
            "GET",
            "/ads/account/value-rule-sets",
            ValueRuleSet.serializer(),
            query = account(workspaceId, connectionId, adAccountId),
        )

    /** Weights conversions so some audiences count for more than others. */
    public suspend fun createValueRuleSet(params: CreateValueRuleSetParams): ValueRuleSet =
        http.call(
            "POST",
            "/ads/account/value-rule-sets",
            ValueRuleSet.serializer(),
            http.jsonBody(params, CreateValueRuleSetParams.serializer()),
        )

    public suspend fun deleteValueRuleSet(
        ruleSetId: String,
        workspaceId: String,
        connectionId: String,
        adAccountId: String,
    ) {
        http.send(
            "DELETE",
            "/ads/account/value-rule-sets/$ruleSetId",
            query = account(workspaceId, connectionId, adAccountId),
        )
    }

    private suspend fun reachFrequencyAction(
        predictionId: String,
        action: String,
        params: ReachFrequencyActionParams,
    ): ReachFrequencyPrediction =
        http.call(
            "POST",
            "/ads/reach-frequency/$predictionId/$action",
            ReachFrequencyPrediction.serializer(),
            http.jsonBody(params, ReachFrequencyActionParams.serializer()),
        )

    private fun account(workspaceId: String?, connectionId: String, adAccountId: String): Map<String, Any?> =
        connection(workspaceId, connectionId) + ("ad_account_id" to adAccountId)

    private fun connection(workspaceId: String?, connectionId: String): Map<String, Any?> =
        mapOf("workspace_id" to workspaceId, "connection_id" to connectionId)
}
