package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.internal.InstantSerializer
import com.fopost.model.QueueNext
import com.fopost.model.QueueSlot
import com.fopost.model.QueueSlotDeleted
import com.fopost.param.CreateQueueSlotParams
import com.fopost.param.UpdateQueueSlotParams
import java.time.Instant

/**
 * The posting queue: weekly slots a post lands in when it is created with
 * `schedule = "queue"` instead of a `scheduleAt`.
 */
public class QueueResource internal constructor(private val http: ApiClient) {

    /** Every slot in the workspace. */
    public suspend fun slots(workspaceId: String): List<QueueSlot> =
        http.callList("GET", "/queue/slots", QueueSlot.serializer(), query = mapOf("workspace_id" to workspaceId))

    public suspend fun createSlot(params: CreateQueueSlotParams): QueueSlot =
        http.call(
            "POST",
            "/queue/slots",
            QueueSlot.serializer(),
            http.jsonBody(params, CreateQueueSlotParams.serializer()),
        )

    /** Posts already queued into the slot move with it; they come back in [QueueSlot.moved]. */
    public suspend fun updateSlot(slotId: String, params: UpdateQueueSlotParams): QueueSlot =
        http.call(
            "PUT",
            "/queue/slots/$slotId",
            QueueSlot.serializer(),
            http.jsonBody(params, UpdateQueueSlotParams.serializer()),
        )

    /** Posts queued into the slot move to the next one; they come back in [QueueSlotDeleted.moved]. */
    public suspend fun deleteSlot(slotId: String): QueueSlotDeleted =
        http.call("DELETE", "/queue/slots/$slotId", QueueSlotDeleted.serializer())

    /** The next free time, or null in [QueueNext.next] when no slot applies. */
    public suspend fun next(workspaceId: String, accountId: String? = null): QueueNext =
        http.call(
            "GET",
            "/queue/next",
            QueueNext.serializer(),
            query = mapOf("workspace_id" to workspaceId, "account_id" to accountId),
        )

    /** The next [count] times the queue would fill, in order. */
    public suspend fun preview(workspaceId: String, accountId: String? = null, count: Int? = null): List<Instant> =
        http.callList(
            "GET",
            "/queue/preview",
            InstantSerializer,
            query = mapOf("workspace_id" to workspaceId, "account_id" to accountId, "count" to count),
        )
}
