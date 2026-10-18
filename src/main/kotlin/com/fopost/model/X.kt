@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames

// X pass-through reads and follow actions. Every call is an X API call metered to the workspace.

/** A post on X, read with the connected account's own token. */
@Serializable
public data class XPost(
    val id: String,
    val text: String? = null,
    val permalink: String? = null,
    @SerialName("author_name") @JsonNames("authorName") val authorName: String? = null,
    @SerialName("author_handle") @JsonNames("authorHandle") val authorHandle: String? = null,
    @SerialName("author_avatar_url") @JsonNames("authorAvatarUrl") val authorAvatarUrl: String? = null,
    @SerialName("author_followers") @JsonNames("authorFollowers") val authorFollowers: Int? = null,
    /** Thread root id. */
    @SerialName("conversation_id") @JsonNames("conversationId") val conversationId: String? = null,
    /** The post this one replied to. */
    @SerialName("parent_external_id") @JsonNames("parentExternalId") val parentExternalId: String? = null,
    val likes: Int? = null,
    val replies: Int? = null,
    val reposts: Int? = null,
    val quotes: Int? = null,
    @SerialName("platform_created_at") @JsonNames("platformCreatedAt") val platformCreatedAt: Instant? = null,
)

/** One page of X search results. Pass [nextToken] back as `nextToken`; null means the end. */
@Serializable
public data class XSearchPage(
    val data: List<XPost> = emptyList(),
    val meta: XSearchMeta? = null,
) : Iterable<XPost> {
    override fun iterator(): Iterator<XPost> = data.iterator()
    public val nextToken: String? get() = meta?.nextToken
}

@Serializable
public data class XSearchMeta(
    @SerialName("next_token") @JsonNames("nextToken") val nextToken: String? = null,
)

/** The outcome of following or unfollowing an X user from a connected account. */
@Serializable
public data class XFollowResult(
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    /** The X user id that was followed or unfollowed. */
    @SerialName("user_id") @JsonNames("userId") val userId: String? = null,
    val following: Boolean = false,
)
