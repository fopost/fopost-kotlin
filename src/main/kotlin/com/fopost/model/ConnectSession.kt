@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames

/**
 * A hosted connect link: one of your users connects their own account through it.
 *
 * [url] is present on creation only; hand it to the user. [status] moves `pending` → `used`
 * (then [accountId] names the connected account) or `expired`.
 */
@Serializable
public data class ConnectSession(
    val id: String? = null,
    val status: String? = null,
    @SerialName("external_user_id") @JsonNames("externalUserId") val externalUserId: String? = null,
    val platforms: List<String> = emptyList(),
    @SerialName("redirect_url") @JsonNames("redirectUrl") val redirectUrl: String? = null,
    val branding: ConnectSessionBranding? = null,
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    val url: String? = null,
    @SerialName("expires_at") @JsonNames("expiresAt") val expiresAt: Instant? = null,
    @SerialName("used_at") @JsonNames("usedAt") val usedAt: Instant? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
)

/** The name and logo shown on the hosted page. */
@Serializable
public data class ConnectSessionBranding(
    val name: String? = null,
    @SerialName("logo_url") @JsonNames("logoUrl") val logoUrl: String? = null,
)
