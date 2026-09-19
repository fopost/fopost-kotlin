@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames

/** A named set of accounts in one workspace, for posting to all of them at once. */
@Serializable
public data class AccountGroup(
    val id: String? = null,
    val name: String? = null,
    @SerialName("account_ids") @JsonNames("accountIds") val accountIds: List<String> = emptyList(),
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    @SerialName("updated_at") @JsonNames("updatedAt") val updatedAt: Instant? = null,
)
