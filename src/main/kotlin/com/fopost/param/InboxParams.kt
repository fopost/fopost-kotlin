@file:UseSerializers(InstantSerializer::class)

package com.fopost.param

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * Filters for listing inbox items. Every field is optional.
 *
 * [type] is `comment`, `mention` or `dm`; [state] is `unread`, `read`, `resolved` or `snoozed`;
 * [direction] is `inbound` or `outbound`; [sort] is `newest`, `oldest` or `unanswered`.
 */
public data class InboxListParams(
    val workspaceId: String? = null,
    val type: String? = null,
    val state: String? = null,
    val platform: String? = null,
    val accountId: String? = null,
    val postId: String? = null,
    val postExternalId: String? = null,
    val conversationId: String? = null,
    val direction: String? = null,
    val q: String? = null,
    val sort: String? = null,
    val page: Int? = null,
    val perPage: Int? = null,
) {
    public fun toQuery(): Map<String, Any?> = mapOf(
        "workspace_id" to workspaceId,
        "type" to type,
        "state" to state,
        "platform" to platform,
        "account_id" to accountId,
        "post_id" to postId,
        "post_external_id" to postExternalId,
        "conversation_id" to conversationId,
        "direction" to direction,
        "q" to q,
        "sort" to sort,
        "page" to page,
        "per_page" to perPage,
    )
}

/** Filters for listing comment threads. [kind] is `comments` (the default) or `mentions`. */
public data class InboxThreadListParams(
    val workspaceId: String? = null,
    val kind: String? = null,
    val platform: String? = null,
    val accountId: String? = null,
    val state: String? = null,
    val q: String? = null,
    val sort: String? = null,
    val page: Int? = null,
    val perPage: Int? = null,
) {
    public fun toQuery(): Map<String, Any?> = mapOf(
        "workspace_id" to workspaceId,
        "kind" to kind,
        "platform" to platform,
        "account_id" to accountId,
        "state" to state,
        "q" to q,
        "sort" to sort,
        "page" to page,
        "per_page" to perPage,
    )
}

/** Filters for listing direct-message threads. */
public data class InboxConversationListParams(
    val workspaceId: String? = null,
    val platform: String? = null,
    val accountId: String? = null,
    val state: String? = null,
    val q: String? = null,
    val sort: String? = null,
    val page: Int? = null,
    val perPage: Int? = null,
) {
    public fun toQuery(): Map<String, Any?> = mapOf(
        "workspace_id" to workspaceId,
        "platform" to platform,
        "account_id" to accountId,
        "state" to state,
        "q" to q,
        "sort" to sort,
        "page" to page,
        "per_page" to perPage,
    )
}

/** A whole thread to mark read: a comment thread by [postExternalId], or a DM thread by [conversationId]. */
@Serializable
public data class MarkInboxReadParams(
    @SerialName("workspace_id") val workspaceId: String,
    @SerialName("account_id") val accountId: String,
    @SerialName("post_external_id") val postExternalId: String? = null,
    @SerialName("conversation_id") val conversationId: String? = null,
)

/** A state change for an item. [state] is `unread`, `read`, `resolved` or `snoozed`. */
@Serializable
public data class UpdateInboxItemParams(
    val state: String,
    /** When a `snoozed` item comes back. */
    val snoozedUntil: Instant? = null,
)

/**
 * A new DM: either [handle] with [accountId], or [commentId] to answer an inbox comment privately.
 */
@Serializable
public data class StartInboxConversationParams(
    val text: String,
    @SerialName("account_id") val accountId: String? = null,
    val handle: String? = null,
    @SerialName("comment_id") val commentId: String? = null,
    /** Media library ids to attach, at most 10. */
    @SerialName("media_ids") val mediaIds: List<String>? = null,
)

@Serializable
internal data class RefreshInboxBody(@SerialName("workspace_id") val workspaceId: String)

@Serializable
internal data class InboxTextBody(val text: String? = null)

@Serializable
internal data class InboxVoteBody(val direction: String)

@Serializable
internal data class InboxReplyBody(
    val text: String? = null,
    @SerialName("media_ids") val mediaIds: List<String>? = null,
    @SerialName("quick_replies") val quickReplies: List<String>? = null,
)

@Serializable
internal data class InboxTypingBody(
    @SerialName("account_id") val accountId: String,
    val on: Boolean? = null,
)
