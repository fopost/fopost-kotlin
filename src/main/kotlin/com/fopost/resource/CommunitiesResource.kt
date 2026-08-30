package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.Community
import com.fopost.model.CommunitySearchResult
import com.fopost.param.AddCommunityParams

/** The X communities an account can post into. */
public class CommunitiesResource internal constructor(private val http: ApiClient) {

    public suspend fun list(accountId: String): List<Community> =
        http.callList("GET", "/accounts/$accountId/communities", Community.serializer())

    /** Re-read the account's communities from X and store what came back. */
    public suspend fun sync(accountId: String): List<Community> =
        http.callList("POST", "/accounts/$accountId/communities/sync", Community.serializer())

    /** Search X directly, for a community the account has not joined. */
    public suspend fun search(accountId: String, query: String): List<CommunitySearchResult> =
        http.callList(
            "GET",
            "/accounts/$accountId/communities/search",
            CommunitySearchResult.serializer(),
            query = mapOf("q" to query),
        )

    /** Add a community by its id on X, for one search cannot reach. */
    public suspend fun add(accountId: String, communityId: String, name: String? = null): Community =
        http.call(
            "POST",
            "/accounts/$accountId/communities/manual",
            Community.serializer(),
            http.jsonBody(AddCommunityParams(communityId, name), AddCommunityParams.serializer()),
        )

    public suspend fun remove(accountId: String, communityId: Long) {
        http.send("DELETE", "/accounts/$accountId/communities/$communityId")
    }
}
