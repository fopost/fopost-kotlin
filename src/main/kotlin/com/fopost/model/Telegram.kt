@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames

/** A one-time code, valid for 15 minutes, that connects a Telegram chat when sent to the bot as [command]. */
@Serializable
public data class TelegramConnectCode(
    val code: String? = null,
    val command: String? = null,
    @SerialName("bot_username") @JsonNames("botUsername") val botUsername: String? = null,
    @SerialName("deep_link") @JsonNames("deepLink") val deepLink: String? = null,
    @SerialName("group_link") @JsonNames("groupLink") val groupLink: String? = null,
    @SerialName("expires_at") @JsonNames("expiresAt") val expiresAt: Instant? = null,
)

/**
 * Where a connect code stands: `pending`, `connected` (with [accountId]), `failed` (with [reason])
 * or `expired`.
 */
@Serializable
public data class TelegramConnectStatus(
    val status: String? = null,
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    val reason: String? = null,
)

/** One entry in a Telegram bot's command menu; [command] is 1-32 of `[a-z0-9_]`. */
@Serializable
public data class TelegramBotCommand(
    val command: String? = null,
    val description: String? = null,
)

/** The command menu the bot shows in a connected Telegram chat. */
@Serializable
public data class TelegramBotCommands(
    val commands: List<TelegramBotCommand> = emptyList(),
)
