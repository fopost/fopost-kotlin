package com.fopost.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/** A channel the Slack app can post to; [isCurrent] marks the one this account posts to. */
@Serializable
public data class SlackChannel(
    val id: String? = null,
    val name: String? = null,
    @SerialName("is_private") @JsonNames("isPrivate") val isPrivate: Boolean = false,
    @SerialName("is_member") @JsonNames("isMember") val isMember: Boolean = false,
    @SerialName("is_current") @JsonNames("isCurrent") val isCurrent: Boolean = false,
)

/** A person in the connected Slack workspace; pass [id] as the handle to start a DM. */
@Serializable
public data class SlackMember(
    val id: String? = null,
    val name: String? = null,
    @SerialName("real_name") @JsonNames("realName") val realName: String? = null,
    @SerialName("display_name") @JsonNames("displayName") val displayName: String? = null,
    val avatar: String? = null,
    @SerialName("is_bot") @JsonNames("isBot") val isBot: Boolean = false,
)

/** The name and icon a Slack account posts under; each is null when unset. */
@Serializable
public data class SlackIdentity(
    val username: String? = null,
    @SerialName("icon_url") @JsonNames("iconUrl") val iconUrl: String? = null,
    @SerialName("icon_emoji") @JsonNames("iconEmoji") val iconEmoji: String? = null,
)
