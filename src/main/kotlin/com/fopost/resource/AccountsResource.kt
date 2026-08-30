package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.Account
import com.fopost.model.AccountAnalyticsHistory
import com.fopost.model.AccountHealth
import com.fopost.model.AccountValidation
import com.fopost.model.AccountsHealthSummary
import com.fopost.model.TokenRefresh
import com.fopost.param.CreateAccountParams
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive

/** The social accounts connected to a workspace. */
public class AccountsResource internal constructor(private val http: ApiClient) {

    /** Connected accounts, across every workspace the key can reach unless one is named. */
    public suspend fun list(workspaceId: String? = null): List<Account> =
        http.callList("GET", "/accounts", Account.serializer(), query = mapOf("workspaceId" to workspaceId))

    public suspend fun get(accountId: String): Account =
        http.call("GET", "/accounts/$accountId", Account.serializer())

    /** Connect an account with credentials you already hold, instead of the dashboard OAuth flow. */
    public suspend fun create(params: CreateAccountParams): Account =
        http.call("POST", "/accounts", Account.serializer(), http.jsonBody(params, CreateAccountParams.serializer()))

    /** Disconnect the account. Posts already published stay where they are. */
    public suspend fun delete(accountId: String) {
        http.send("DELETE", "/accounts/$accountId")
    }

    /** Token validity for every account, with the counts rolled up. */
    public suspend fun healthSummary(workspaceId: String? = null): AccountsHealthSummary =
        http.call(
            "GET",
            "/accounts/health",
            AccountsHealthSummary.serializer(),
            query = mapOf("workspaceId" to workspaceId),
        )

    /** Health for one account. [refresh] re-checks the credentials instead of reading the cache. */
    public suspend fun health(accountId: String, refresh: Boolean = false): AccountHealth =
        http.call(
            "GET",
            "/accounts/$accountId/health",
            AccountHealth.serializer(),
            query = if (refresh) mapOf("refresh" to "true") else null,
        )

    /** Make this the account a post targets by default, or clear the flag. Returns the new state. */
    public suspend fun togglePrimary(accountId: String): Boolean {
        val data = http.call("POST", "/accounts/$accountId/primary", JsonObject.serializer())
        return data["isPrimary"]?.jsonPrimitive?.booleanOrNull ?: false
    }

    /** Check the stored credentials against the platform, now. */
    public suspend fun validate(accountId: String): AccountValidation =
        http.call("POST", "/accounts/$accountId/validate", AccountValidation.serializer())

    /** Force an OAuth token refresh, ahead of the automatic one. */
    public suspend fun refreshToken(accountId: String): TokenRefresh =
        http.call("POST", "/accounts/$accountId/refresh-token", TokenRefresh.serializer())

    /** Follower and reach history for the account, newest first. */
    public suspend fun analytics(accountId: String, limit: Int? = null): AccountAnalyticsHistory =
        http.call(
            "GET",
            "/accounts/$accountId/analytics",
            AccountAnalyticsHistory.serializer(),
            query = mapOf("limit" to limit),
        )
}
