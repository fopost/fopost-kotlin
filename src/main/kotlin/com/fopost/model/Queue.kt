@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames

/**
 * A posting-queue slot: a weekday and a time of day, in [timezone].
 *
 * [accountId] is null when the slot applies to every account in the workspace.
 * [weekday] runs 0 (Sunday) to 6 (Saturday); [time] is `HH:MM`, 24-hour.
 */
@Serializable
public data class QueueSlot(
    val id: String? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    val weekday: Int? = null,
    val time: String? = null,
    val timezone: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    /** Queued posts moved to a new slot because of this change. Update and delete only. */
    val moved: List<QueueMovedPost> = emptyList(),
)

/** A queued post that moved when a slot changed. [id] is the post id. */
@Serializable
public data class QueueMovedPost(
    val id: String? = null,
    val from: Instant? = null,
    val to: Instant? = null,
)

/** The answer to deleting a slot: whether it went, and the posts that moved. */
@Serializable
public data class QueueSlotDeleted(
    val deleted: Boolean? = null,
    val moved: List<QueueMovedPost> = emptyList(),
)

/** The next free queue time. [next] is null when no slot applies. */
@Serializable
public data class QueueNext(
    val next: Instant? = null,
)
