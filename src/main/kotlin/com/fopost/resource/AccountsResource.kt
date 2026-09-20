package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.Account
import com.fopost.model.AccountAnalyticsHistory
import com.fopost.model.AccountHealth
import com.fopost.model.AccountValidation
import com.fopost.model.AccountsHealthSummary
import com.fopost.model.DiscordAck
import com.fopost.model.DiscordChannel
import com.fopost.model.DiscordIdentity
import com.fopost.model.DiscordMember
import com.fopost.model.DiscordMessage
import com.fopost.model.DiscordMessageRef
import com.fopost.model.DiscordRole
import com.fopost.model.DiscordScheduledEvent
import com.fopost.model.DiscordThread
import com.fopost.model.MetaGreeting
import com.fopost.model.MetaGreetingText
import com.fopost.model.MetaIceBreaker
import com.fopost.model.MetaIceBreakers
import com.fopost.model.MetaPersistentMenu
import com.fopost.model.MetaPersistentMenuEntry
import com.fopost.model.BlueskyLanguages
import com.fopost.model.InstagramAudio
import com.fopost.model.InstagramPublishingLimit
import com.fopost.model.InstagramStory
import com.fopost.model.InstagramStoryInsights
import com.fopost.model.LinkedInMention
import com.fopost.model.PinterestBoard
import com.fopost.model.SlackChannel
import com.fopost.model.SlackIdentity
import com.fopost.model.SlackMember
import com.fopost.model.TelegramBotCommand
import com.fopost.model.TelegramBotCommands
import com.fopost.model.TelegramConnectCode
import com.fopost.model.TelegramConnectStatus
import com.fopost.model.TikTokCreatorInfo
import com.fopost.model.TikTokMusic
import com.fopost.model.TikTokPlace
import com.fopost.model.TikTokVideoSource
import com.fopost.model.TokenRefresh
import com.fopost.model.WebhookSubscription
import com.fopost.model.YouTubeCaptionTrack
import com.fopost.model.YouTubePlaylist
import com.fopost.model.YouTubeTranscript
import com.fopost.param.CreateAccountParams
import com.fopost.param.CreateTelegramConnectCodeParams
import com.fopost.param.DiscordDirectMessageParams
import com.fopost.param.DiscordEventParams
import com.fopost.param.DiscordRoleParams
import com.fopost.param.DiscordThreadParams
import com.fopost.param.MoveAccountParams
import com.fopost.param.SetGreetingParams
import com.fopost.param.SetIceBreakersParams
import com.fopost.param.SetPersistentMenuParams
import com.fopost.param.SetTelegramBotCommandsParams
import com.fopost.param.SwitchDiscordChannelParams
import com.fopost.param.UpdateDiscordIdentityParams
import com.fopost.param.UpdateSlackIdentityParams
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
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

    // ── Discord (bot connections) ───────────────────────────────────────

    /**
     * Text channels the bot can post to in the connected server.
     *
     * A 409 `webhook_connection` means the account posts through a webhook; upgrade it to the bot
     * first. The same applies to every other Discord call here.
     */
    public suspend fun listDiscordChannels(accountId: String): List<DiscordChannel> =
        http.callList("GET", discordPath(accountId, "/channels"), DiscordChannel.serializer())

    /** Move the account to another channel in the same server. */
    public suspend fun switchDiscordChannel(accountId: String, channelId: String): DiscordChannel =
        http.call(
            "PATCH",
            discordPath(accountId, "/channels/current"),
            DiscordChannel.serializer(),
            http.jsonBody(SwitchDiscordChannelParams(channelId), SwitchDiscordChannelParams.serializer()),
        )

    /** The nickname and avatar the bot wears in the server. */
    public suspend fun getDiscordIdentity(accountId: String): DiscordIdentity =
        http.call("GET", discordPath(accountId, "/identity"), DiscordIdentity.serializer())

    /** Change the nickname or avatar the bot wears in the server. */
    public suspend fun updateDiscordIdentity(
        accountId: String,
        params: UpdateDiscordIdentityParams,
    ): DiscordIdentity =
        http.call(
            "PATCH",
            discordPath(accountId, "/identity"),
            DiscordIdentity.serializer(),
            // Built by hand so a null is sent, not dropped.
            http.jsonBody(buildJsonObject { params.fields.forEach { (key, value) -> put(key, value) } }),
        )

    /** Pinned messages in the account's channel. */
    public suspend fun listDiscordPins(accountId: String): List<DiscordMessage> =
        http.callList("GET", discordPath(accountId, "/messages/pinned"), DiscordMessage.serializer())

    /** Remove a message from the account's channel. */
    public suspend fun deleteDiscordMessage(accountId: String, messageId: String): DiscordAck =
        http.call("DELETE", discordPath(accountId, "/messages/$messageId"), DiscordAck.serializer())

    /** Pin a message in the account's channel. */
    public suspend fun pinDiscordMessage(accountId: String, messageId: String): DiscordAck =
        http.call("POST", discordPath(accountId, "/messages/$messageId/pin"), DiscordAck.serializer())

    /** Unpin a message in the account's channel. */
    public suspend fun unpinDiscordMessage(accountId: String, messageId: String): DiscordAck =
        http.call("DELETE", discordPath(accountId, "/messages/$messageId/pin"), DiscordAck.serializer())

    /** Publish an announcement-channel message to every server following the channel. */
    public suspend fun crosspostDiscordMessage(accountId: String, messageId: String): DiscordMessageRef =
        http.call(
            "POST",
            discordPath(accountId, "/messages/$messageId/crosspost"),
            DiscordMessageRef.serializer(),
        )

    /** Start a thread on a message. */
    public suspend fun createDiscordThread(
        accountId: String,
        messageId: String,
        params: DiscordThreadParams,
    ): DiscordThread =
        http.call(
            "POST",
            discordPath(accountId, "/messages/$messageId/thread"),
            DiscordThread.serializer(),
            http.jsonBody(params, DiscordThreadParams.serializer()),
        )

    /** Send one message to a member of the server. */
    public suspend fun sendDiscordDirectMessage(
        accountId: String,
        memberId: String,
        content: String,
    ): DiscordMessageRef =
        http.call(
            "POST",
            discordPath(accountId, "/dm"),
            DiscordMessageRef.serializer(),
            http.jsonBody(
                DiscordDirectMessageParams(memberId, content),
                DiscordDirectMessageParams.serializer(),
            ),
        )

    /** The server's scheduled events. */
    public suspend fun listDiscordEvents(accountId: String): List<DiscordScheduledEvent> =
        http.callList("GET", discordPath(accountId, "/events"), DiscordScheduledEvent.serializer())

    /** One scheduled event. */
    public suspend fun getDiscordEvent(accountId: String, eventId: String): DiscordScheduledEvent =
        http.call("GET", discordPath(accountId, "/events/$eventId"), DiscordScheduledEvent.serializer())

    /** Add an event to the server's calendar. */
    public suspend fun createDiscordEvent(accountId: String, params: DiscordEventParams): DiscordScheduledEvent =
        http.call(
            "POST",
            discordPath(accountId, "/events"),
            DiscordScheduledEvent.serializer(),
            http.jsonBody(params, DiscordEventParams.serializer()),
        )

    /** Change a scheduled event; a null field is left as it is. */
    public suspend fun updateDiscordEvent(
        accountId: String,
        eventId: String,
        params: DiscordEventParams,
    ): DiscordScheduledEvent =
        http.call(
            "PATCH",
            discordPath(accountId, "/events/$eventId"),
            DiscordScheduledEvent.serializer(),
            http.jsonBody(params, DiscordEventParams.serializer()),
        )

    /** Remove a scheduled event. */
    public suspend fun deleteDiscordEvent(accountId: String, eventId: String): DiscordAck =
        http.call("DELETE", discordPath(accountId, "/events/$eventId"), DiscordAck.serializer())

    /** The server's roster, or the members whose name starts with [query]. */
    public suspend fun listDiscordMembers(
        accountId: String,
        query: String? = null,
        limit: Int? = null,
    ): List<DiscordMember> =
        http.callList(
            "GET",
            discordPath(accountId, "/members"),
            DiscordMember.serializer(),
            query = mapOf("q" to query, "limit" to limit),
        )

    /** One member of the server. */
    public suspend fun getDiscordMember(accountId: String, memberId: String): DiscordMember =
        http.call("GET", discordPath(accountId, "/members/$memberId"), DiscordMember.serializer())

    /** The server's roles, highest first. */
    public suspend fun listDiscordRoles(accountId: String): List<DiscordRole> =
        http.callList("GET", discordPath(accountId, "/roles"), DiscordRole.serializer())

    /** Add a role to the server. */
    public suspend fun createDiscordRole(accountId: String, params: DiscordRoleParams): DiscordRole =
        http.call(
            "POST",
            discordPath(accountId, "/roles"),
            DiscordRole.serializer(),
            http.jsonBody(params, DiscordRoleParams.serializer()),
        )

    /** Change a role on the server; a null field is left as it is. */
    public suspend fun updateDiscordRole(
        accountId: String,
        roleId: String,
        params: DiscordRoleParams,
    ): DiscordRole =
        http.call(
            "PATCH",
            discordPath(accountId, "/roles/$roleId"),
            DiscordRole.serializer(),
            http.jsonBody(params, DiscordRoleParams.serializer()),
        )

    /** Remove a role from the server. */
    public suspend fun deleteDiscordRole(accountId: String, roleId: String): DiscordAck =
        http.call("DELETE", discordPath(accountId, "/roles/$roleId"), DiscordAck.serializer())

    /** Give a member a role. */
    public suspend fun addDiscordMemberRole(accountId: String, roleId: String, memberId: String): DiscordAck =
        http.call("PUT", memberRolePath(accountId, roleId, memberId), DiscordAck.serializer())

    /** Take a role from a member. */
    public suspend fun removeDiscordMemberRole(accountId: String, roleId: String, memberId: String): DiscordAck =
        http.call("DELETE", memberRolePath(accountId, roleId, memberId), DiscordAck.serializer())

    private fun discordPath(accountId: String, suffix: String): String =
        "/accounts/$accountId/discord$suffix"

    private fun memberRolePath(accountId: String, roleId: String, memberId: String): String =
        discordPath(accountId, "/roles/$roleId/members/$memberId")
    // --- Per-network extras ------------------------------------------------

    /** Boards this Pinterest connection can pin to. */
    public suspend fun listPinterestBoards(accountId: String): List<PinterestBoard> =
        http.callList("GET", "/accounts/$accountId/pinterest/boards", PinterestBoard.serializer())

    /** Create a board on the connected Pinterest account. `privacy` is PUBLIC, PROTECTED or SECRET. */
    public suspend fun createPinterestBoard(
        accountId: String,
        name: String,
        description: String? = null,
        privacy: String? = null,
    ): PinterestBoard =
        http.call(
            "POST",
            "/accounts/$accountId/pinterest/boards",
            PinterestBoard.serializer(),
            http.jsonBody(
                buildJsonObject {
                    put("name", JsonPrimitive(name))
                    description?.let { put("description", JsonPrimitive(it)) }
                    privacy?.let { put("privacy", JsonPrimitive(it)) }
                },
            ),
        )

    /** The channel's own playlists, with the stored default marked. */
    public suspend fun listYouTubePlaylists(accountId: String): List<YouTubePlaylist> =
        http.callList("GET", "/accounts/$accountId/youtube/playlists", YouTubePlaylist.serializer())

    /** Create a playlist on the connected channel. `privacy` is public, unlisted or private. */
    public suspend fun createYouTubePlaylist(
        accountId: String,
        title: String,
        description: String? = null,
        privacy: String? = null,
    ): YouTubePlaylist =
        http.call(
            "POST",
            "/accounts/$accountId/youtube/playlists",
            YouTubePlaylist.serializer(),
            http.jsonBody(
                buildJsonObject {
                    put("title", JsonPrimitive(title))
                    description?.let { put("description", JsonPrimitive(it)) }
                    privacy?.let { put("privacy", JsonPrimitive(it)) }
                },
            ),
        )

    /**
     * The playlist a new video joins when the post picks none. A null [playlistId] clears it, and
     * the stored value comes back.
     */
    public suspend fun setDefaultYouTubePlaylist(accountId: String, playlistId: String?): String? {
        val stored: JsonObject =
            http.call(
                "PUT",
                "/accounts/$accountId/youtube/playlists/default",
                JsonObject.serializer(),
                // Built by hand so an explicit null is sent, not dropped.
                http.jsonBody(
                    buildJsonObject {
                        put("playlist_id", playlistId?.let { JsonPrimitive(it) } ?: JsonNull)
                    },
                ),
            )
        return stored["playlist_id"]?.jsonPrimitive?.contentOrNull
    }

    /** Caption tracks on one of the channel's videos. */
    public suspend fun listYouTubeCaptions(accountId: String, videoId: String): List<YouTubeCaptionTrack> =
        http.callList(
            "GET",
            "/accounts/$accountId/youtube/videos/$videoId/captions",
            YouTubeCaptionTrack.serializer(),
        )

    /** Upload a caption track. [body] is the subtitle file; YouTube reads SRT and WebVTT. */
    public suspend fun uploadYouTubeCaptions(
        accountId: String,
        videoId: String,
        language: String,
        body: String,
        name: String? = null,
        isDraft: Boolean? = null,
    ): YouTubeCaptionTrack =
        http.call(
            "POST",
            "/accounts/$accountId/youtube/videos/$videoId/captions",
            YouTubeCaptionTrack.serializer(),
            http.jsonBody(
                buildJsonObject {
                    put("language", JsonPrimitive(language))
                    put("body", JsonPrimitive(body))
                    name?.let { put("name", JsonPrimitive(it)) }
                    isDraft?.let { put("is_draft", JsonPrimitive(it)) }
                },
            ),
        )

    /** One caption track read back as text. */
    public suspend fun readYouTubeTranscript(accountId: String, captionId: String): YouTubeTranscript =
        http.call("GET", "/accounts/$accountId/youtube/captions/$captionId", YouTubeTranscript.serializer())

    /** What a post from this Bluesky connection is written in when it does not say. */
    public suspend fun getBlueskyLanguages(accountId: String): BlueskyLanguages =
        http.call("GET", "/accounts/$accountId/bluesky/languages", BlueskyLanguages.serializer())

    /** Store up to three BCP-47 tags. An empty list clears the default. */
    public suspend fun setBlueskyLanguages(accountId: String, languages: List<String>): BlueskyLanguages =
        http.call(
            "PUT",
            "/accounts/$accountId/bluesky/languages",
            BlueskyLanguages.serializer(),
            http.jsonBody(
                buildJsonObject {
                    put("languages", JsonArray(languages.map { JsonPrimitive(it) }))
                },
            ),
        )

    /** The switches TikTok enforces at publish time, changed in the TikTok app. */
    public suspend fun getTikTokCreatorInfo(accountId: String): TikTokCreatorInfo =
        http.call("GET", "/accounts/$accountId/tiktok/creator-info", TikTokCreatorInfo.serializer())

    /**
     * TikTok's Commercial Music Library. Needs the Marketing API product on the TikTok app;
     * without it the call fails with 403 rather than answering an empty list.
     */
    public suspend fun searchTikTokMusic(
        accountId: String,
        query: String,
        limit: Int? = null,
    ): List<TikTokMusic> =
        http.callList(
            "GET",
            "/accounts/$accountId/tiktok/music",
            TikTokMusic.serializer(),
            query = mapOf("q" to query, "limit" to limit?.toString()),
        )

    /** Places a post can be tagged with. Same TikTok product as the music library. */
    public suspend fun searchTikTokLocations(
        accountId: String,
        query: String,
        limit: Int? = null,
    ): List<TikTokPlace> =
        http.callList(
            "GET",
            "/accounts/$accountId/tiktok/locations",
            TikTokPlace.serializer(),
            query = mapOf("q" to query, "limit" to limit?.toString()),
        )

    /** Resolves a share link to one of this account's own videos, for repurposing. */
    public suspend fun lookupTikTokVideo(accountId: String, url: String): TikTokVideoSource =
        http.call(
            "POST",
            "/accounts/$accountId/tiktok/video-download",
            TikTokVideoSource.serializer(),
            http.jsonBody(buildJsonObject { put("url", url) }),
        )

    /** Tracks a Reel can carry. With no query Instagram answers with what is trending. */
    public suspend fun searchInstagramAudio(
        accountId: String,
        query: String? = null,
        audioType: String? = null,
    ): List<InstagramAudio> =
        http.callList(
            "GET",
            "/accounts/$accountId/instagram/audio",
            InstagramAudio.serializer(),
            query = mapOf("q" to query, "audio_type" to audioType),
        )

    /** How many posts are left before Instagram refuses the next one. */
    public suspend fun getInstagramPublishingLimit(accountId: String): InstagramPublishingLimit =
        http.call(
            "GET",
            "/accounts/$accountId/instagram/publishing-limit",
            InstagramPublishingLimit.serializer(),
        )

    /**
     * Stories still inside their 24 hours, posted through FoPost or not. Asking for [insights]
     * costs one extra call per story.
     */
    public suspend fun listInstagramStories(accountId: String, insights: Boolean = false): List<InstagramStory> =
        http.callList(
            "GET",
            "/accounts/$accountId/instagram/stories",
            InstagramStory.serializer(),
            query = if (insights) mapOf("insights" to true) else null,
        )

    /** The insight set for one story. */
    public suspend fun getInstagramStoryInsights(accountId: String, storyId: String): InstagramStoryInsights =
        http.call(
            "GET",
            "/accounts/$accountId/instagram/stories/$storyId/insights",
            InstagramStoryInsights.serializer(),
        )

    /**
     * Organizations a LinkedIn post can mention. People are not searchable: LinkedIn has no public
     * person search, so a member mention needs a URN you already hold.
     */
    public suspend fun searchLinkedInMentions(accountId: String, query: String): List<LinkedInMention> =
        http.callList(
            "GET",
            "/accounts/$accountId/linkedin/mentions",
            LinkedInMention.serializer(),
            query = mapOf("q" to query),
        )
}

