package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.Account
import com.fopost.model.AccountAnalyticsHistory
import com.fopost.model.AccountHealth
import com.fopost.model.AccountValidation
import com.fopost.model.AccountsHealthSummary
import com.fopost.model.MetaGreeting
import com.fopost.model.MetaGreetingText
import com.fopost.model.MetaIceBreaker
import com.fopost.model.MetaIceBreakers
import com.fopost.model.MetaPersistentMenu
import com.fopost.model.MetaPersistentMenuEntry
import com.fopost.model.SlackChannel
import com.fopost.model.SlackIdentity
import com.fopost.model.SlackMember
import com.fopost.model.TelegramBotCommand
import com.fopost.model.TelegramBotCommands
import com.fopost.model.TelegramConnectCode
import com.fopost.model.TelegramConnectStatus
import com.fopost.model.TokenRefresh
import com.fopost.model.WebhookSubscription
import com.fopost.param.CreateAccountParams
import com.fopost.param.CreateTelegramConnectCodeParams
import com.fopost.param.MoveAccountParams
import com.fopost.param.SetGreetingParams
import com.fopost.param.SetIceBreakersParams
import com.fopost.param.SetPersistentMenuParams
import com.fopost.param.SetTelegramBotCommandsParams
import com.fopost.param.UpdateSlackIdentityParams
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

    /**
     * Mint a one-time code, valid for 15 minutes. Sending `/connect <code>` to the bot in a Telegram
     * chat connects that chat. [workspaceId] may be omitted for a key bound to one workspace.
     */
    public suspend fun createTelegramConnectCode(workspaceId: String? = null): TelegramConnectCode =
        http.call(
            "POST",
            "/accounts/telegram/connect-code",
            TelegramConnectCode.serializer(),
            http.jsonBody(CreateTelegramConnectCodeParams(workspaceId), CreateTelegramConnectCodeParams.serializer()),
        )

    /** Whether a connect code has been used yet, and the account it connected. */
    public suspend fun getTelegramConnectStatus(code: String): TelegramConnectStatus =
        http.call(
            "GET",
            "/accounts/telegram/connect-code/status",
            TelegramConnectStatus.serializer(),
            query = mapOf("code" to code),
        )

    /** The command menu the bot shows in this Telegram chat. */
    public suspend fun getTelegramBotCommands(accountId: String): TelegramBotCommands =
        http.call("GET", "/accounts/$accountId/telegram/commands", TelegramBotCommands.serializer())

    /** Replace the command menu for this Telegram chat, 1-100 commands. */
    public suspend fun setTelegramBotCommands(
        accountId: String,
        commands: List<TelegramBotCommand>,
    ): TelegramBotCommands =
        http.call(
            "PUT",
            "/accounts/$accountId/telegram/commands",
            TelegramBotCommands.serializer(),
            http.jsonBody(SetTelegramBotCommandsParams(commands), SetTelegramBotCommandsParams.serializer()),
        )

    /** Clear the command menu for this Telegram chat. */
    public suspend fun deleteTelegramBotCommands(accountId: String): TelegramBotCommands =
        http.call("DELETE", "/accounts/$accountId/telegram/commands", TelegramBotCommands.serializer())

    /**
     * Channels the Slack app can post to: every public channel, and private ones it was invited to.
     *
     * A 409 `webhook_connection` means the account posts through a webhook; reconnect it with the
     * Slack app. The same applies to the other Slack calls.
     */
    public suspend fun listSlackChannels(accountId: String): List<SlackChannel> =
        http.callList("GET", "/accounts/$accountId/slack/channels", SlackChannel.serializer())

    /** People in the connected Slack workspace, for addressing a DM. */
    public suspend fun listSlackMembers(accountId: String): List<SlackMember> =
        http.callList("GET", "/accounts/$accountId/slack/members", SlackMember.serializer())

    /** The name and icon this Slack account posts under. */
    public suspend fun getSlackIdentity(accountId: String): SlackIdentity =
        http.call("GET", "/accounts/$accountId/slack/identity", SlackIdentity.serializer())

    /** Change the name or icon this Slack account posts under. */
    public suspend fun updateSlackIdentity(accountId: String, params: UpdateSlackIdentityParams): SlackIdentity =
        http.call(
            "PATCH",
            "/accounts/$accountId/slack/identity",
            SlackIdentity.serializer(),
            // Built by hand so a null is sent, not dropped.
            http.jsonBody(buildJsonObject { params.fields.forEach { (key, value) -> put(key, value) } }),
        )

    // ─── Meta messaging settings (Facebook Pages, Instagram) ─────────

    /** The prompts shown before the first message. A network without them answers 400. */
    public suspend fun getIceBreakers(accountId: String): MetaIceBreakers =
        http.call("GET", "/accounts/$accountId/messaging/ice-breakers", MetaIceBreakers.serializer())

    /** Replace the ice breakers, up to four. */
    public suspend fun setIceBreakers(accountId: String, iceBreakers: List<MetaIceBreaker>): MetaIceBreakers =
        http.call(
            "PUT",
            "/accounts/$accountId/messaging/ice-breakers",
            MetaIceBreakers.serializer(),
            http.jsonBody(SetIceBreakersParams(iceBreakers), SetIceBreakersParams.serializer()),
        )

    /** Clear the ice breakers. */
    public suspend fun deleteIceBreakers(accountId: String): MetaIceBreakers =
        http.call("DELETE", "/accounts/$accountId/messaging/ice-breakers", MetaIceBreakers.serializer())

    /** The always-visible Messenger menu. Facebook Pages only; other networks answer 400. */
    public suspend fun getPersistentMenu(accountId: String): MetaPersistentMenu =
        http.call("GET", "/accounts/$accountId/messaging/persistent-menu", MetaPersistentMenu.serializer())

    /** Replace the menu, one entry per locale, up to three items each. */
    public suspend fun setPersistentMenu(
        accountId: String,
        menu: List<MetaPersistentMenuEntry>,
    ): MetaPersistentMenu =
        http.call(
            "PUT",
            "/accounts/$accountId/messaging/persistent-menu",
            MetaPersistentMenu.serializer(),
            http.jsonBody(SetPersistentMenuParams(menu), SetPersistentMenuParams.serializer()),
        )

    /** Clear the menu. */
    public suspend fun deletePersistentMenu(accountId: String): MetaPersistentMenu =
        http.call("DELETE", "/accounts/$accountId/messaging/persistent-menu", MetaPersistentMenu.serializer())

    /** The text shown before a Messenger conversation starts. Facebook Pages only. */
    public suspend fun getGreeting(accountId: String): MetaGreeting =
        http.call("GET", "/accounts/$accountId/messaging/greeting", MetaGreeting.serializer())

    /** Replace the greeting, one entry per locale, each up to 160 characters. */
    public suspend fun setGreeting(accountId: String, greeting: List<MetaGreetingText>): MetaGreeting =
        http.call(
            "PUT",
            "/accounts/$accountId/messaging/greeting",
            MetaGreeting.serializer(),
            http.jsonBody(SetGreetingParams(greeting), SetGreetingParams.serializer()),
        )

    /** Clear the greeting. */
    public suspend fun deleteGreeting(accountId: String): MetaGreeting =
        http.call("DELETE", "/accounts/$accountId/messaging/greeting", MetaGreeting.serializer())

    /** What the network is delivering to the FoPost webhook for this account. */
    public suspend fun getWebhookSubscription(accountId: String): WebhookSubscription =
        http.call("GET", "/accounts/$accountId/webhook-subscription", WebhookSubscription.serializer())

    /** Subscribe to every field this account needs, lapsed or not. */
    public suspend fun resubscribeWebhook(accountId: String): WebhookSubscription =
        http.call("POST", "/accounts/$accountId/webhook-subscription", WebhookSubscription.serializer())
}
