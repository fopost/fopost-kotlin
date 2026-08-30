@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames

/** A workspace. [accounts] is populated by the list and get endpoints. */
@Serializable
public data class Workspace(
    val id: String? = null,
    val name: String? = null,
    val slug: String? = null,
    val type: String? = null,
    val logo: String? = null,
    val website: String? = null,
    val timezone: String? = null,
    val country: String? = null,
    val description: String? = null,
    val language: String? = null,
    @SerialName("require_approval") @JsonNames("requireApproval") val requireApproval: Boolean? = null,
    @SerialName("ai_alt_text_enabled") @JsonNames("aiAltTextEnabled") val aiAltTextEnabled: Boolean? = null,
    @SerialName("brand_color") @JsonNames("brandColor") val brandColor: String? = null,
    val accounts: List<Account> = emptyList(),
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    @SerialName("updated_at") @JsonNames("updatedAt") val updatedAt: Instant? = null,
)

/** Latest per-account figures for a workspace, plus the workspace totals. */
@Serializable
public data class WorkspaceAnalytics(
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
    val accounts: List<WorkspaceAccountSnapshot> = emptyList(),
    val totals: WorkspaceTotals? = null,
)

/** One account's latest figures inside a workspace roll-up. */
@Serializable
public data class WorkspaceAccountSnapshot(
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    val platform: String? = null,
    val username: String? = null,
    val followers: Long? = null,
    val following: Long? = null,
    @SerialName("total_posts") @JsonNames("totalPosts") val totalPosts: Long? = null,
    @SerialName("fetched_at") @JsonNames("fetchedAt") val fetchedAt: Instant? = null,
)

/** A workspace's totals across every account in it. */
@Serializable
public data class WorkspaceTotals(
    val followers: Long? = null,
    @SerialName("total_posts") @JsonNames("totalPosts") val totalPosts: Long? = null,
)
