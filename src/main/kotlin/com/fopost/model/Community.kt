@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames

/** An X community an account can post into. */
@Serializable
public data class Community(
    val id: Long? = null,
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    @SerialName("community_id") @JsonNames("communityId") val communityId: String? = null,
    val name: String? = null,
    @SerialName("member_count") @JsonNames("memberCount") val memberCount: Long? = null,
    val description: String? = null,
    @SerialName("image_url") @JsonNames("imageUrl") val imageUrl: String? = null,
    @SerialName("last_synced_at") @JsonNames("lastSyncedAt") val lastSyncedAt: Instant? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
)

/** A community as returned by a live search against X. */
@Serializable
public data class CommunitySearchResult(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    @SerialName("member_count") @JsonNames("memberCount") val memberCount: Long? = null,
)
