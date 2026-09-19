package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.Account
import com.fopost.model.AccountAnalyticsHistory
import com.fopost.model.AccountHealth
import com.fopost.model.AccountValidation
import com.fopost.model.AccountsHealthSummary
import com.fopost.model.TokenRefresh
import com.fopost.param.CreateAccountParams
import com.fopost.param.MoveAccountParams
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** The social accounts connected to a workspace. */
public class AccountsResource internal constructor(private val http: ApiClient) {

    /**
     * Connected accounts, across every workspace the key can reach unless one is named. [groupId]
     * keeps only the accounts in that account group.
     */
    public suspend fun list(workspaceId: String? = null, groupId: String? = null): List<Account> =
        http.callList(
            "GET",
            "/accounts",
            Account.serializer(),
            query = mapOf("workspaceId" to workspaceId, "group_id" to groupId),
        )

    public suspend fun get(accountId: String): Account =
        http.call("GET", "/accounts/$accountId", Account.serializer())

    /** Connect an account with credentials you already hold, instead of the dashboard OAuth flow. */
    public suspend fun create(params: CreateAccountParams): Account =
        http.call("POST", "/accounts", Account.serializer(), http.jsonBody(params, CreateAccountParams.serializer()))

    /**
     * Set the name shown instead of the platform name. `null` or an empty string restores the
     * platform name. Returns `id`, `name` and `platformName`.
     */
    public suspend fun rename(accountId: String, displayName: String?): Account =
        http.call(
            "PATCH",
            "/accounts/$accountId",
            Account.serializer(),
            // Built by hand so a null is sent, not dropped.
            http.jsonBody(buildJsonObject { put("display_name", displayName) }),
        )

    /**
     * Move the account to another workspace the caller owns. Returns `id` and `workspaceId`.
     *
     * A 409 with code `move_blocked` lists `blocking_tables` in the exception body; any other 409
     * means the target already has an account on that network.
     */
    public suspend fun move(accountId: String, workspaceId: String): Account =
        http.call(
            "POST",
            "/accounts/$accountId/move",
            Account.serializer(),
            http.jsonBody(MoveAccountParams(workspaceId), MoveAccountParams.serializer()),
        )

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
