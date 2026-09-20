package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.InboxAccount
import com.fopost.model.InboxApproval
import com.fopost.model.InboxApprovalDecision
import com.fopost.model.InboxConversation
import com.fopost.model.InboxConversationStart
import com.fopost.model.InboxItem
import com.fopost.model.InboxPlatform
import com.fopost.model.InboxRefreshResult
import com.fopost.model.InboxReplyResult
import com.fopost.model.InboxThread
import com.fopost.model.Page
import com.fopost.param.InboxConversationListParams
import com.fopost.param.InboxListParams
import com.fopost.param.InboxReplyBody
import com.fopost.param.InboxTextBody
import com.fopost.param.InboxThreadListParams
import com.fopost.param.InboxTypingBody
import com.fopost.param.MarkInboxReadParams
import com.fopost.param.RefreshInboxBody
import com.fopost.param.StartInboxConversationParams
import com.fopost.param.UpdateInboxItemParams
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Comments, mentions, reviews and direct messages on connected accounts. Needs the `inbox` scope.
 */
public class InboxResource internal constructor(private val http: ApiClient) {

    /** One page of items, newest first. `meta.perPage` and `meta.total` are set. */
    public suspend fun list(params: InboxListParams = InboxListParams()): Page<InboxItem> =
        http.page("/inbox", InboxItem.serializer(), params.toQuery())

    /**
     * One row per post with comments; `kind = "mentions"` for posts the account was tagged in,
     * `kind = "reviews"` for one row per review left on the business.
     */
    public suspend fun threads(params: InboxThreadListParams = InboxThreadListParams()): Page<InboxThread> =
        http.page("/inbox/posts", InboxThread.serializer(), params.toQuery())

    /** One row per DM thread, latest first. */
    public suspend fun conversations(
        params: InboxConversationListParams = InboxConversationListParams(),
    ): Page<InboxConversation> =
        http.page("/inbox/conversations", InboxConversation.serializer(), params.toQuery())

    public suspend fun unreadCount(workspaceId: String? = null): Int {
        val body = http.send("GET", "/inbox/unread-count", query = mapOf("workspace_id" to workspaceId))
        return (body as? JsonObject)?.get("count")?.jsonPrimitive?.intOrNull ?: 0
    }

    /** Every active account, flagged with whether comments and DMs can be read for it. */
    public suspend fun accounts(workspaceId: String? = null): List<InboxAccount> =
        http.callList("GET", "/inbox/accounts", InboxAccount.serializer(), query = mapOf("workspace_id" to workspaceId))

    public suspend fun platforms(): List<InboxPlatform> =
        http.callList("GET", "/inbox/platforms", InboxPlatform.serializer())

    /** Mark a whole comment thread or DM thread read. Returns how many items changed. */
    public suspend fun markThreadRead(params: MarkInboxReadParams): Int {
        val data = http.call(
            "POST",
            "/inbox/read",
            JsonObject.serializer(),
            http.jsonBody(params, MarkInboxReadParams.serializer()),
        )
        return data["updated"]?.jsonPrimitive?.intOrNull ?: 0
    }

    /** Poll every inbox-capable account in the workspace now. */
    public suspend fun refresh(workspaceId: String): InboxRefreshResult =
        http.call(
            "POST",
            "/inbox/refresh",
            InboxRefreshResult.serializer(),
            http.jsonBody(RefreshInboxBody(workspaceId), RefreshInboxBody.serializer()),
        )

    /** Mark the item `unread`, `read`, `resolved` or `snoozed`. */
    public suspend fun update(itemId: String, params: UpdateInboxItemParams): InboxItem =
        http.call(
            "PATCH",
            "/inbox/$itemId",
            InboxItem.serializer(),
            http.jsonBody(params, UpdateInboxItemParams.serializer()),
        )

    /**
     * Edit our own comment on the platform. Needs the `publish` scope; only where
     * [InboxItem.canEdit] is true.
     */
    public suspend fun editComment(itemId: String, text: String): InboxItem =
        http.call(
            "PATCH",
            "/inbox/$itemId",
            InboxItem.serializer(),
            http.jsonBody(InboxTextBody(text), InboxTextBody.serializer()),
        )

    /**
     * Send the reply on the platform as the connected account. [text] may be omitted when
     * [mediaIds] (media library ids, at most 10) is given; [mediaIds] and [quickReplies] (at most
     * 13, each up to 20 characters) apply to DMs and need the `publish` scope.
     */
    public suspend fun reply(
        itemId: String,
        text: String? = null,
        mediaIds: List<String>? = null,
        quickReplies: List<String>? = null,
    ): InboxReplyResult =
        http.call(
            "POST",
            "/inbox/$itemId/reply",
            InboxReplyResult.serializer(),
            http.jsonBody(InboxReplyBody(text, mediaIds, quickReplies), InboxReplyBody.serializer()),
        )

    /** Hide the comment on the platform. */
    public suspend fun hide(itemId: String): InboxItem =
        http.call("POST", "/inbox/$itemId/hide", InboxItem.serializer())

    public suspend fun unhide(itemId: String): InboxItem =
        http.call("POST", "/inbox/$itemId/unhide", InboxItem.serializer())

    /**
     * Delete the comment, or our own reply, on the platform. Deleting our own reply needs the
     * `publish` scope. Returns whether it was removed.
     */
    public suspend fun delete(itemId: String): Boolean {
        val data = http.call("DELETE", "/inbox/$itemId", JsonObject.serializer())
        return data["deleted"]?.jsonPrimitive?.booleanOrNull ?: false
    }

    /** Like the item on the platform. Needs the `publish` scope. */
    public suspend fun like(itemId: String): InboxItem =
        http.call("POST", "/inbox/$itemId/like", InboxItem.serializer())

    public suspend fun unlike(itemId: String): InboxItem =
        http.call("POST", "/inbox/$itemId/unlike", InboxItem.serializer())

    /** Pin our own comment on the platform. Needs the `publish` scope. */
    public suspend fun pin(itemId: String): InboxItem =
        http.call("POST", "/inbox/$itemId/pin", InboxItem.serializer())

    public suspend fun unpin(itemId: String): InboxItem =
        http.call("POST", "/inbox/$itemId/unpin", InboxItem.serializer())

    /**
     * React to a DM with an emoji (at most 32 characters), or pass `null` to remove ours. Needs
     * the `publish` scope.
     */
    public suspend fun react(itemId: String, reaction: String?): InboxItem =
        http.call(
            "POST",
            "/inbox/$itemId/react",
            InboxItem.serializer(),
            // Built by hand: the configured Json drops nulls, and a null here means remove.
            http.jsonBody(buildJsonObject { put("reaction", reaction) }),
        )

    /** Open a DM by handle, or answer an inbox comment privately. Needs the `publish` scope. */
    public suspend fun startConversation(params: StartInboxConversationParams): InboxConversationStart =
        http.call(
            "POST",
            "/inbox/conversations",
            InboxConversationStart.serializer(),
            http.jsonBody(params, StartInboxConversationParams.serializer()),
        )

    /**
     * Show the typing indicator in a DM conversation, or clear it with [on] set to false. Needs
     * the `publish` scope. Returns whether the indicator is now on.
     */
    public suspend fun setTyping(conversationId: String, accountId: String, on: Boolean? = null): Boolean {
        val data = http.call(
            "POST",
            "/inbox/conversations/$conversationId/typing",
            JsonObject.serializer(),
            http.jsonBody(InboxTypingBody(accountId, on), InboxTypingBody.serializer()),
        )
        return data["typing"]?.jsonPrimitive?.booleanOrNull ?: false
    }

    /** Replies an automation or the agent drafted that a person still has to send. */
    public suspend fun listApprovals(workspaceId: String? = null): List<InboxApproval> =
        http.callList(
            "GET",
            "/inbox/approvals",
            InboxApproval.serializer(),
            query = mapOf("workspace_id" to workspaceId),
        )

    /** Send the draft, or [text] in its place. */
    public suspend fun approveReply(approvalId: Long, text: String? = null): InboxApprovalDecision =
        http.call(
            "POST",
            "/inbox/approvals/$approvalId/approve",
            InboxApprovalDecision.serializer(),
            http.jsonBody(InboxTextBody(text), InboxTextBody.serializer()),
        )

    public suspend fun rejectReply(approvalId: Long): InboxApprovalDecision =
        http.call("POST", "/inbox/approvals/$approvalId/reject", InboxApprovalDecision.serializer())
}
