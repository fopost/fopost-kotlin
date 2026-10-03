@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames

/** Who did it: `user`, `api_key`, `agent` or `system`. [name] is absent for a system event. */
@Serializable
public data class ActivityActor(
    val type: String? = null,
    val name: String? = null,
)

/** One thing that happened in a workspace. A `security` [kind] is an audit row. */
@Serializable
public data class ActivityEvent(
    val id: String? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
    val kind: String? = null,
    @SerialName("ref_type") @JsonNames("refType") val refType: String? = null,
    @SerialName("ref_id") @JsonNames("refId") val refId: String? = null,
    val summary: String? = null,
    val actor: ActivityActor? = null,
    val time: Instant? = null,
)

/** The cursor for the next page; null at the end of the list. */
@Serializable
public data class ActivityMeta(
    @SerialName("next_cursor") @JsonNames("nextCursor") val nextCursor: String? = null,
)

/** One page of activity, newest first. Iterates over its events. */
@Serializable
public data class ActivityPage(
    val data: List<ActivityEvent> = emptyList(),
    val meta: ActivityMeta = ActivityMeta(),
) : List<ActivityEvent> by data

/** Activity kinds. [SECURITY] is the audit log: append-only, and it never expires. */
public object ActivityKinds {
    public const val PUBLISH: String = "publish"
    public const val CONNECTION: String = "connection"
    public const val WEBHOOK: String = "webhook"
    public const val INBOX: String = "inbox"
    public const val AUTOMATION: String = "automation"
    public const val BILLING: String = "billing"
    public const val SECURITY: String = "security"
}
