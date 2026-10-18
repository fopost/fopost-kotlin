@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonObject

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

/**
 * One attempt to deliver an event to a webhook endpoint.
 *
 * Every attempt of one event shares [deliveryKey] (the `X-FoPost-Delivery` id); [attempt] is 1
 * for the first send and counts up through retries and redeliveries. [payload], the body exactly
 * as sent, arrives from the single-delivery call only.
 */
@Serializable
public data class WebhookDelivery(
    val id: String? = null,
    @SerialName("webhook_id") @JsonNames("webhookId") val webhookId: String? = null,
    val event: String? = null,
    @SerialName("delivery_key") @JsonNames("deliveryKey") val deliveryKey: String? = null,
    val attempt: Int? = null,
    val status: String? = null,
    @SerialName("response_status") @JsonNames("responseStatus") val responseStatus: Int? = null,
    @SerialName("response_snippet") @JsonNames("responseSnippet") val responseSnippet: String? = null,
    @SerialName("duration_ms") @JsonNames("durationMs") val durationMs: Int? = null,
    @SerialName("payload_hash") @JsonNames("payloadHash") val payloadHash: String? = null,
    @SerialName("next_retry_at") @JsonNames("nextRetryAt") val nextRetryAt: Instant? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    val payload: JsonObject? = null,
)
