package com.fopost.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/**
 * A Discord text channel the bot can post to; [isCurrent] marks this account's.
 *
 * [type] is Discord's channel type: 0 text, 5 announcement, 15 forum.
 */
@Serializable
public data class DiscordChannel(
    val id: String? = null,
    val name: String? = null,
    val type: Int = 0,
    @SerialName("parent_id") @JsonNames("parentId") val parentId: String? = null,
    val nsfw: Boolean = false,
    @SerialName("is_current") @JsonNames("isCurrent") val isCurrent: Boolean = false,
)

/** The nickname and avatar the bot wears in the server; null means its own. */
@Serializable
public data class DiscordIdentity(
    val username: String? = null,
    @SerialName("avatar_url") @JsonNames("avatarUrl") val avatarUrl: String? = null,
)

/** A message in the connected channel. */
@Serializable
public data class DiscordMessage(
    val id: String? = null,
    @SerialName("channel_id") @JsonNames("channelId") val channelId: String? = null,
    val content: String = "",
    @SerialName("author_id") @JsonNames("authorId") val authorId: String? = null,
    @SerialName("author_name") @JsonNames("authorName") val authorName: String? = null,
    val pinned: Boolean = false,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: String? = null,
)

/** A message the bot put somewhere. */
@Serializable
public data class DiscordMessageRef(
    val id: String? = null,
    @SerialName("channel_id") @JsonNames("channelId") val channelId: String? = null,
)

/** A thread started on a message. */
@Serializable
public data class DiscordThread(
    val id: String? = null,
    val name: String? = null,
    @SerialName("parent_id") @JsonNames("parentId") val parentId: String? = null,
)

/**
 * An event on the server's calendar. [channelId] names a voice or stage channel; otherwise
 * [location] says where it happens. [status] is scheduled, active, completed or canceled.
 */
@Serializable
public data class DiscordScheduledEvent(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    @SerialName("channel_id") @JsonNames("channelId") val channelId: String? = null,
    val location: String? = null,
    @SerialName("start_time") @JsonNames("startTime") val startTime: String? = null,
    @SerialName("end_time") @JsonNames("endTime") val endTime: String? = null,
    val status: String = "scheduled",
    @SerialName("user_count") @JsonNames("userCount") val userCount: Int? = null,
)

/** A person in the connected server; pass [id] as the member id for a DM or a role. */
@Serializable
public data class DiscordMember(
    val id: String? = null,
    val username: String? = null,
    @SerialName("display_name") @JsonNames("displayName") val displayName: String? = null,
    /** Nickname in this server. */
    val nick: String? = null,
    val avatar: String? = null,
    @SerialName("is_bot") @JsonNames("isBot") val isBot: Boolean = false,
    val roles: List<String> = emptyList(),
    @SerialName("joined_at") @JsonNames("joinedAt") val joinedAt: String? = null,
)

/**
 * A role in the connected server. A [managed] role belongs to an integration and cannot be edited;
 * [permissions] is Discord's bitfield as a decimal string.
 */
@Serializable
public data class DiscordRole(
    val id: String? = null,
    val name: String? = null,
    val color: Int = 0,
    val hoist: Boolean = false,
    val mentionable: Boolean = false,
    val managed: Boolean = false,
    val position: Int = 0,
    val permissions: String = "0",
)

/** What a Discord delete, pin or role assignment answers. */
@Serializable
public data class DiscordAck(
    val deleted: Boolean? = null,
    val pinned: Boolean? = null,
    val assigned: Boolean? = null,
)
