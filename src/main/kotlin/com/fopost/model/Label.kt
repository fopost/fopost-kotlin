@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNames

/** A workspace label. [workspace] is either the workspace id or a short reference object. */
@Serializable
public data class Label(
    val id: String? = null,
    val name: String? = null,
    val color: String? = null,
    val workspace: JsonElement? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    @SerialName("updated_at") @JsonNames("updatedAt") val updatedAt: Instant? = null,
)

/** Campaign roll-up for one label. */
@Serializable
public data class LabelAnalytics(
    @SerialName("label_id") @JsonNames("labelId") val labelId: String? = null,
    val name: String? = null,
    val color: String? = null,
    @SerialName("post_count") @JsonNames("postCount") val postCount: Int? = null,
    val impressions: Long? = null,
    val reach: Long? = null,
    val engagements: Long? = null,
    val likes: Long? = null,
    val comments: Long? = null,
    val shares: Long? = null,
    @SerialName("engagement_rate") @JsonNames("engagementRate") val engagementRate: Double? = null,
    @SerialName("follower_delta") @JsonNames("followerDelta") val followerDelta: Long? = null,
)
