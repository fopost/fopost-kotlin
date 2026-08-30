@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames

/**
 * A webhook endpoint.
 *
 * [secret] is returned only by the create call — store it then, because it is never shown again.
 */
@Serializable
public data class Webhook(
    val id: String? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
    val url: String? = null,
    val events: List<String> = emptyList(),
    val active: Boolean? = null,
    val secret: String? = null,
    @SerialName("last_triggered_at") @JsonNames("lastTriggeredAt") val lastTriggeredAt: Instant? = null,
    @SerialName("failure_count") @JsonNames("failureCount") val failureCount: Int? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
)
