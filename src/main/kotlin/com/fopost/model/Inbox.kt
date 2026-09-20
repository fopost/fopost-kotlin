@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonObject

/** The connected account an inbox item arrived on. */
@Serializable
public data class InboxAccountRef(
    val id: String? = null,
    val platform: String? = null,
    val username: String? = null,
    val name: String? = null,
    val avatar: String? = null,
)

/** A file attached to an inbox item. [url] is served by the API, never a platform URL. */
@Serializable
public data class InboxAttachment(
    val kind: String? = null,
    val name: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val link: String? = null,
    val url: String? = null,
    @SerialName("preview_url") @JsonNames("previewUrl") val previewUrl: String? = null,
)

/** The platform post an item sits under, whoever published it. */
@Serializable
public data class InboxPostContext(
    @SerialName("external_id") @JsonNames("externalId") val externalId: String? = null,
    @SerialName("is_own") @JsonNames("isOwn") val isOwn: Boolean? = null,
    val text: String? = null,
    @SerialName("author_name") @JsonNames("authorName") val authorName: String? = null,
    @SerialName("author_handle") @JsonNames("authorHandle") val authorHandle: String? = null,
    @SerialName("author_avatar_url") @JsonNames("authorAvatarUrl") val authorAvatarUrl: String? = null,
    @SerialName("thumbnail_url") @JsonNames("thumbnailUrl") val thumbnailUrl: String? = null,
    val permalink: String? = null,
    @SerialName("published_at") @JsonNames("publishedAt") val publishedAt: Instant? = null,
    /** The FoPost post this was published from, when it was. */
    val published: JsonObject? = null,
)

/**
 * A comment, mention, review or direct message on a connected account.
 *
 * [type] is `comment`, `mention`, `review` or `dm`; [state] is `unread`, `read`, `resolved` or
 * `snoozed`; [direction] is `inbound` or `outbound`. [rating] is the stars on a review, 1-5, and
 * null on every other type.
 */
@Serializable
public data class InboxItem(
    val id: String? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
    val platform: String? = null,
    val type: String? = null,
    val state: String? = null,
    val direction: String? = null,
    @SerialName("conversation_id") @JsonNames("conversationId") val conversationId: String? = null,
    @SerialName("author_name") @JsonNames("authorName") val authorName: String? = null,
    @SerialName("author_handle") @JsonNames("authorHandle") val authorHandle: String? = null,
    @SerialName("author_avatar_url") @JsonNames("authorAvatarUrl") val authorAvatarUrl: String? = null,
    val text: String? = null,
    val rating: Int? = null,
    val attachments: List<InboxAttachment> = emptyList(),
    val permalink: String? = null,
    @SerialName("post_external_id") @JsonNames("postExternalId") val postExternalId: String? = null,
    @SerialName("parent_external_id") @JsonNames("parentExternalId") val parentExternalId: String? = null,
    @SerialName("platform_created_at") @JsonNames("platformCreatedAt") val platformCreatedAt: Instant? = null,
    @SerialName("snoozed_until") @JsonNames("snoozedUntil") val snoozedUntil: Instant? = null,
    @SerialName("replied_at") @JsonNames("repliedAt") val repliedAt: Instant? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    @SerialName("can_reply") @JsonNames("canReply") val canReply: Boolean? = null,
    val hidden: Boolean? = null,
    val liked: Boolean? = null,
    val pinned: Boolean? = null,
    /** Our reaction on a DM. */
    val reaction: String? = null,
    @SerialName("edited_at") @JsonNames("editedAt") val editedAt: Instant? = null,
    @SerialName("can_hide") @JsonNames("canHide") val canHide: Boolean? = null,
    /** A comment someone left, or our own reply. */
    @SerialName("can_delete") @JsonNames("canDelete") val canDelete: Boolean? = null,
    @SerialName("can_like") @JsonNames("canLike") val canLike: Boolean? = null,
    /** Our own comment only. */
    @SerialName("can_pin") @JsonNames("canPin") val canPin: Boolean? = null,
    /** Our own comment only. */
    @SerialName("can_edit") @JsonNames("canEdit") val canEdit: Boolean? = null,
    @SerialName("can_react") @JsonNames("canReact") val canReact: Boolean? = null,
    @SerialName("can_send_media") @JsonNames("canSendMedia") val canSendMedia: Boolean? = null,
    @SerialName("can_quick_reply") @JsonNames("canQuickReply") val canQuickReply: Boolean? = null,
    /** A DM can be opened with [com.fopost.resource.InboxResource.startConversation] and `commentId`. */
    @SerialName("can_private_reply") @JsonNames("canPrivateReply") val canPrivateReply: Boolean? = null,
    val post: JsonObject? = null,
    @SerialName("post_context") @JsonNames("postContext") val postContext: InboxPostContext? = null,
    val account: InboxAccountRef? = null,
)

/**
 * One platform post and the comments it has collected, or one review left on the business.
 *
 * [rating] is the stars on a review thread, and null on comments and mentions.
 */
@Serializable
public data class InboxThread(
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    @SerialName("post_external_id") @JsonNames("postExternalId") val postExternalId: String? = null,
    @SerialName("comment_count") @JsonNames("commentCount") val commentCount: Int? = null,
    @SerialName("unread_count") @JsonNames("unreadCount") val unreadCount: Int? = null,
    @SerialName("last_comment_at") @JsonNames("lastCommentAt") val lastCommentAt: Instant? = null,
    @SerialName("last_comment_text") @JsonNames("lastCommentText") val lastCommentText: String? = null,
    @SerialName("last_comment_author") @JsonNames("lastCommentAuthor") val lastCommentAuthor: String? = null,
    val rating: Int? = null,
    val post: InboxPostContext? = null,
    val account: InboxAccountRef? = null,
)

/** The other side of a direct-message thread. */
@Serializable
public data class InboxParticipant(
    val name: String? = null,
    val handle: String? = null,
    @SerialName("avatar_url") @JsonNames("avatarUrl") val avatarUrl: String? = null,
)

/** One direct-message thread. */
@Serializable
public data class InboxConversation(
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    @SerialName("conversation_id") @JsonNames("conversationId") val conversationId: String? = null,
    @SerialName("message_count") @JsonNames("messageCount") val messageCount: Int? = null,
    @SerialName("unread_count") @JsonNames("unreadCount") val unreadCount: Int? = null,
    @SerialName("last_message_at") @JsonNames("lastMessageAt") val lastMessageAt: Instant? = null,
    @SerialName("last_message_text") @JsonNames("lastMessageText") val lastMessageText: String? = null,
    @SerialName("last_message_outbound") @JsonNames("lastMessageOutbound") val lastMessageOutbound: Boolean? = null,
    val participant: InboxParticipant? = null,
    val account: InboxAccountRef? = null,
)

/** A connected account, flagged with whether comments and DMs can be read for it. */
@Serializable
public data class InboxAccount(
    val id: String? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
    val platform: String? = null,
    val username: String? = null,
    val name: String? = null,
    val avatar: String? = null,
    @SerialName("inbox_supported") @JsonNames("inboxSupported") val inboxSupported: Boolean? = null,
    @SerialName("pending_reason") @JsonNames("pendingReason") val pendingReason: String? = null,
    @SerialName("dm_supported") @JsonNames("dmSupported") val dmSupported: Boolean? = null,
    @SerialName("dm_pending_reason") @JsonNames("dmPendingReason") val dmPendingReason: String? = null,
    /** A new DM can be opened from this account by handle. */
    @SerialName("can_start_conversation") @JsonNames("canStartConversation") val canStartConversation: Boolean? = null,
)

/** Inbox support per platform. [comments] and [dms] are `live`, `soon` or `none`. */
@Serializable
public data class InboxPlatform(
    val platform: String? = null,
    val comments: String? = null,
    val dms: String? = null,
)

/** A drafted reply a person still has to send. */
@Serializable
public data class InboxApproval(
    val id: Long? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
    val source: String? = null,
    val reply: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    val item: JsonObject? = null,
)

/** What became of an approval after it was approved or rejected. */
@Serializable
public data class InboxApprovalDecision(
    val id: Long? = null,
    val outcome: String? = null,
)

/** Where the reply landed on the platform. */
@Serializable
public data class InboxReplyRef(
    @SerialName("external_id") @JsonNames("externalId") val externalId: String? = null,
    @SerialName("external_url") @JsonNames("externalUrl") val externalUrl: String? = null,
)

/** The item after a reply was sent, and the reply itself. */
@Serializable
public data class InboxReplyResult(
    val item: InboxItem? = null,
    val reply: InboxReplyRef? = null,
)

/** A DM that was sent to open a conversation. */
@Serializable
public data class InboxConversationStart(
    @SerialName("conversation_id") @JsonNames("conversationId") val conversationId: String? = null,
    val item: InboxItem? = null,
)

/** What a manual inbox refresh did. */
@Serializable
public data class InboxRefreshResult(
    @SerialName("accounts_polled") @JsonNames("accountsPolled") val accountsPolled: Int? = null,
    @SerialName("new_items") @JsonNames("newItems") val newItems: Int? = null,
    @SerialName("rate_limited") @JsonNames("rateLimited") val rateLimited: Int? = null,
    /** Accounts whose DM access has to be granted again. */
    @SerialName("dm_reconnect") @JsonNames("dmReconnect") val dmReconnect: List<JsonObject> = emptyList(),
)
