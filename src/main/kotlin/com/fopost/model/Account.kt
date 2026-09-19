@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames

/**
 * A connected social account.
 *
 * Named for the platform account, not the billing account. Which fields arrive depends on the
 * endpoint: the list gives health and primary state, the detail gives timestamps and the
 * owning workspace. [name] is the display name when one is set; [platformName] is always the
 * name from the platform.
 */
@Serializable
public data class Account(
    val id: String? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
    val platform: String? = null,
    val username: String? = null,
    val name: String? = null,
    val avatar: String? = null,
    @SerialName("is_primary") @JsonNames("isPrimary") val isPrimary: Boolean? = null,
    val active: Boolean? = null,
    @SerialName("health_status") @JsonNames("healthStatus") val healthStatus: String? = null,
    @SerialName("last_health_check") @JsonNames("lastHealthCheck") val lastHealthCheck: Instant? = null,
    val workspace: WorkspaceRef? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    @SerialName("updated_at") @JsonNames("updatedAt") val updatedAt: Instant? = null,
    @SerialName("platform_name") @JsonNames("platformName") val platformName: String? = null,
)

/** Token validity for one account. `healthStatus` is healthy, degraded, expired, revoked or unknown. */
@Serializable
public data class AccountHealth(
    val id: String? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
    val platform: String? = null,
    val username: String? = null,
    val active: Boolean? = null,
    @SerialName("health_status") @JsonNames("healthStatus") val healthStatus: String? = null,
    @SerialName("last_health_check") @JsonNames("lastHealthCheck") val lastHealthCheck: Instant? = null,
)

/** Health for every account the key can reach, with the counts rolled up. */
@Serializable
public data class AccountsHealthSummary(
    val accounts: List<AccountHealth> = emptyList(),
    val summary: AccountHealthCounts? = null,
)

/** How many accounts sit in each health state. */
@Serializable
public data class AccountHealthCounts(
    val total: Int? = null,
    val healthy: Int? = null,
    val degraded: Int? = null,
    val expired: Int? = null,
    val revoked: Int? = null,
    val unknown: Int? = null,
)

/** The result of re-checking one account's stored credentials against its platform. */
@Serializable
public data class AccountValidation(
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    val platform: String? = null,
    val valid: Boolean? = null,
    @SerialName("health_status") @JsonNames("healthStatus") val healthStatus: String? = null,
)

/** The outcome of refreshing an account's OAuth token. */
@Serializable
public data class TokenRefresh(
    val message: String? = null,
    @SerialName("expires_at") @JsonNames("expiresAt") val expiresAt: Instant? = null,
)

/** Follower and reach history for one account, newest first. */
@Serializable
public data class AccountAnalyticsHistory(
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    val platform: String? = null,
    val username: String? = null,
    val history: List<AccountSnapshot> = emptyList(),
)

/** One point of an account's follower and reach history. */
@Serializable
public data class AccountSnapshot(
    val followers: Long? = null,
    val following: Long? = null,
    @SerialName("total_posts") @JsonNames("totalPosts") val totalPosts: Long? = null,
    val reach: Long? = null,
    @SerialName("profile_views") @JsonNames("profileViews") val profileViews: Long? = null,
    @SerialName("fetched_at") @JsonNames("fetchedAt") val fetchedAt: Instant? = null,
)
