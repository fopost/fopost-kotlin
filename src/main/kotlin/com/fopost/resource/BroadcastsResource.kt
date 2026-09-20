package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.Broadcast
import com.fopost.model.BroadcastRecipient
import com.fopost.model.BroadcastSent
import com.fopost.model.Page
import com.fopost.model.PageMeta
import com.fopost.param.BroadcastListParams
import com.fopost.param.CreateBroadcastParams
import com.fopost.param.RecipientListParams
import com.fopost.param.UpdateBroadcastParams
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonObject

/**
 * Broadcasts: one message into every conversation the workspace already has with a segment of
 * its contacts.
 *
 * A broadcast is not a post and not a cold DM — every message lands in a direct-message thread
 * the contact already started.
 *
 * Nothing is sent into a closed messaging window. Messenger and Instagram take a
 * business-initiated message only within 24 hours of the contact's last one, so recipients
 * outside it come back skipped with `window_closed` and nothing is attempted — which is why the
 * number sent is often lower than the audience. Telegram, Slack, Bluesky and Reddit have no
 * window.
 *
 * Reading needs the `inbox` scope; [send] and [cancel] also need `publish`.
 */
public class BroadcastsResource internal constructor(private val http: ApiClient) {

    /** One page of broadcasts, newest first. */
    public suspend fun list(params: BroadcastListParams = BroadcastListParams()): Page<Broadcast> =
        paged("/broadcasts", params.toQuery(), Broadcast.serializer())

    /**
     * One broadcast. A broadcast in a workspace the key cannot reach answers `404`, exactly as an
     * id that never existed does.
     */
    public suspend fun get(broadcastId: String): Broadcast =
        http.call("GET", "/broadcasts/$broadcastId", Broadcast.serializer())

    /**
     * Writes a broadcast without sending it. Set `scheduledAt` to have it go out on its own at
     * that time; otherwise call [send].
     */
    public suspend fun create(params: CreateBroadcastParams): Broadcast =
        http.call(
            "POST",
            "/broadcasts",
            Broadcast.serializer(),
            http.jsonBody(params, CreateBroadcastParams.serializer()),
        )

    /** Changes a broadcast. Only a draft or scheduled broadcast can be edited. */
    public suspend fun update(broadcastId: String, params: UpdateBroadcastParams): Broadcast =
        http.call(
            "PATCH",
            "/broadcasts/$broadcastId",
            Broadcast.serializer(),
            http.jsonBody(params, UpdateBroadcastParams.serializer()),
        )

    /**
     * Freezes the audience into a recipient list and starts sending.
     *
     * The returned `recipients` is how many contacts matched, not how many will be messaged —
     * the messaging window decides that. Needs the `publish` scope as well as `inbox`.
     */
    public suspend fun send(broadcastId: String): BroadcastSent =
        http.call("POST", "/broadcasts/$broadcastId/send", BroadcastSent.serializer())

    /**
     * Stops a broadcast where it stands. Anyone not yet written to stays unsent; messages already
     * delivered are not recalled. Needs the `publish` scope.
     */
    public suspend fun cancel(broadcastId: String): Broadcast =
        http.call("POST", "/broadcasts/$broadcastId/cancel", Broadcast.serializer())

    /**
     * One row per contact, with what became of their message. A skipped row carries its reason.
     */
    public suspend fun recipients(
        broadcastId: String,
        params: RecipientListParams = RecipientListParams(),
    ): Page<BroadcastRecipient> =
        paged("/broadcasts/$broadcastId/recipients", params.toQuery(), BroadcastRecipient.serializer())

    /**
     * Removes a broadcast and its recipient records. Messages already sent stay in the
     * conversations they went to.
     */
    public suspend fun delete(broadcastId: String) {
        http.send("DELETE", "/broadcasts/$broadcastId")
    }

    /** These lists name their counters `pagination` rather than `meta`, like contacts. */
    private suspend fun <T> paged(
        path: String,
        query: Map<String, Any?>,
        serializer: KSerializer<T>,
    ): Page<T> {
        val element = http.send("GET", path, null, query) as? JsonObject
            ?: return Page(emptyList(), null)
        val items = element["data"]
            ?.let { http.decodeElement(ListSerializer(serializer), it) }
            ?: emptyList()
        val meta = (element["pagination"] as? JsonObject)
            ?.let { http.decodeElement(PageMeta.serializer(), it) }
        return Page(items, meta)
    }
}
