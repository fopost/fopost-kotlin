package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.Account
import com.fopost.model.AccountAnalyticsHistory
import com.fopost.model.AccountHealth
import com.fopost.model.AccountValidation
import com.fopost.model.AccountsHealthSummary
import com.fopost.model.RedditDefaultSubreddit
import com.fopost.model.RedditFlair
import com.fopost.model.RedditFlairs
import com.fopost.model.RedditSubreddit
import com.fopost.model.RedditSubredditRule
import com.fopost.model.RedditSubredditRules
import com.fopost.model.SlackChannel
import com.fopost.model.SlackIdentity
import com.fopost.model.SlackMember
import com.fopost.model.TelegramBotCommand
import com.fopost.model.TelegramBotCommands
import com.fopost.model.TelegramConnectCode
import com.fopost.model.TelegramConnectStatus
import com.fopost.model.TokenRefresh
import com.fopost.param.CreateAccountParams
import com.fopost.param.CreateTelegramConnectCodeParams
import com.fopost.param.MoveAccountParams
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

    /**
     * Subreddits this Reddit account is subscribed to, busiest first, plus its own profile page.
     *
     * [RedditSubreddit.canPost] is false where the account may read but not submit, and
     * [RedditSubreddit.isDefault] marks the subreddit posts go to when a post names none. A 409
     * `reconnect_required` means the account has to be reconnected first. The same applies to the
     * other Reddit calls.
     */
    public suspend fun listRedditSubreddits(accountId: String): List<RedditSubreddit> =
        http.callList("GET", "/accounts/$accountId/reddit/subreddits", RedditSubreddit.serializer())

    /** The rules a subreddit publishes, in its own order. [subreddit] carries no `r/` prefix. */
    public suspend fun listRedditSubredditRules(accountId: String, subreddit: String): List<RedditSubredditRule> =
        http.call(
            "GET",
            "/accounts/$accountId/reddit/subreddits/$subreddit/rules",
            RedditSubredditRules.serializer(),
        ).rules

    /**
     * Post flairs one subreddit offers.
     *
     * A flair id is valid only in the subreddit it came from: pass it as `flair_id` in the post's
     * Reddit platform settings, and preflight rejects an id from anywhere else.
     */
    public suspend fun listRedditFlairs(accountId: String, subreddit: String): List<RedditFlair> =
        http.call(
            "GET",
            "/accounts/$accountId/reddit/flairs",
            RedditFlairs.serializer(),
            query = mapOf("subreddit" to subreddit),
        ).flairs

    /**
     * Set where posts from this Reddit account go when a post names no subreddit.
     *
     * `null` falls back to the account's own profile page, which always takes a post. Returns the
     * subreddit that is now in effect.
     */
    public suspend fun setRedditDefaultSubreddit(accountId: String, subreddit: String?): String? =
        http.call(
            "PUT",
            "/accounts/$accountId/reddit/default-subreddit",
            RedditDefaultSubreddit.serializer(),
            // Built by hand so a null is sent, not dropped.
            http.jsonBody(buildJsonObject { put("subreddit", subreddit) }),
        ).subreddit

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
}
