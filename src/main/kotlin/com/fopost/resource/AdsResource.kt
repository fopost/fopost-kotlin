package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.Ad
import com.fopost.model.AdConnection
import com.fopost.model.AdSource
import com.fopost.model.AudiencesResult
import com.fopost.model.BoostablePost
import com.fopost.model.CreatedAudience
import com.fopost.model.ExternalAd
import com.fopost.model.LeadFormSource
import com.fopost.model.LeadsPage
import com.fopost.model.TargetingOption
import com.fopost.param.BoostPostParams
import com.fopost.param.CreateAdParams
import com.fopost.param.CreateAudienceParams
import com.fopost.param.CreateLeadFormParams
import com.fopost.param.MetaAuthorizeParams
import com.fopost.param.SetAdStatusBody
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Meta ads, audiences and lead forms.
 *
 * Every method needs the `ads` scope. [boost], [create], [setStatus] and [delete] spend money and
 * also need `publish`.
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
        http.callList("GET", "/ads/connections", AdConnection.serializer(), query = mapOf("workspace_id" to workspaceId))

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
        http.callList("GET", "/ads/lead-forms", LeadFormSource.serializer(), query = mapOf("workspace_id" to workspaceId))

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
}
