package com.fopost.param

import java.time.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Fields of a workspace being created or updated.
 *
 * `name` and `slug` are required on create; an update sends only what you set.
 */
@Serializable
public data class WorkspaceParams(
    val name: String? = null,
    val slug: String? = null,
    val type: String? = null,
    val logo: String? = null,
    val website: String? = null,
    /** An IANA zone, e.g. `Europe/Berlin`. Scheduled times are read against it. */
    val timezone: String? = null,
    val country: String? = null,
    val description: String? = null,
    val language: String? = null,
    /** Update only: hold every post for approval before it can be scheduled. */
    val requireApproval: Boolean? = null,
    /** Update only. */
    val aiAltTextEnabled: Boolean? = null,
    /** Update only. */
    val brandColor: String? = null,
)

/**
 * An account to connect with credentials you already hold.
 *
 * Most accounts are connected through the OAuth flow in the dashboard instead; this covers the
 * platforms where you bring your own token.
 */
@Serializable
public data class CreateAccountParams(
    val workspaceId: String,
    val platform: String,
    val username: String,
    val name: String,
    val avatar: String? = null,
    /** Platform credentials, in the shape that platform expects. */
    val credentials: JsonObject? = null,
)

/** A label being created. `color` is a hex value, e.g. `#4F46E5`. */
@Serializable
public data class CreateLabelParams(
    @kotlinx.serialization.SerialName("workspace_id") val workspaceId: String,
    val name: String,
    val color: String,
)

/** A label being renamed or recoloured. */
@Serializable
public data class UpdateLabelParams(val name: String, val color: String)

/** An account group being created. */
@Serializable
public data class CreateAccountGroupParams(
    @kotlinx.serialization.SerialName("workspace_id") val workspaceId: String,
    val name: String,
    @kotlinx.serialization.SerialName("account_ids") val accountIds: List<String>? = null,
)

/** An account group being renamed. */
@Serializable
public data class UpdateAccountGroupParams(val name: String)

/** The full member list for an account group. */
@Serializable
public data class SetAccountGroupMembersParams(
    @kotlinx.serialization.SerialName("account_ids") val accountIds: List<String>,
)

/** A webhook being registered. Events come from [com.fopost.model.WebhookEvents]. */
@Serializable
public data class CreateWebhookParams(
    val workspaceId: String,
    val url: String,
    val events: List<String>,
)

/** A partial update to a webhook. */
@Serializable
public data class UpdateWebhookParams(
    val url: String? = null,
    /** Replaces the current list. */
    val events: List<String>? = null,
    /** Pause or resume delivery without deleting the endpoint. */
    val active: Boolean? = null,
)

/** One step of an automation. `actionType` is publish, delay or transform. */
@Serializable
public data class AutomationStepInput(
    val actionType: String,
    val actionConfig: JsonObject? = null,
)

/**
 * An automation being created or updated.
 *
 * `workspaceId`, `name`, `triggerType` and `steps` are required on create; an update sends only
 * what you set. `triggerType` is cross_post, rss_feed, api_webhook or schedule.
 */
@Serializable
public data class AutomationParams(
    val workspaceId: String? = null,
    val name: String? = null,
    val triggerType: String? = null,
    val triggerConfig: JsonObject? = null,
    val steps: List<AutomationStepInput>? = null,
    val active: Boolean? = null,
)

/** A community added by its id on X, for one search cannot reach. */
@Serializable
public data class AddCommunityParams(
    val communityId: String,
    val name: String? = null,
)

/**
 * Filters shared by the analytics endpoints. Every field is optional, and each endpoint reads
 * the ones that apply to it.
 *
 * Give either [days] or a [from]/[to] pair; a range wins where both are sent.
 */
public data class AnalyticsParams(
    val accountId: String? = null,
    val workspaceId: String? = null,
    val days: Int? = null,
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val limit: Int? = null,
    val page: Int? = null,
    /** Top posts only: which metric to rank on. */
    val sort: String? = null,
    val label: String? = null,
    /** Demographics only: `followers`, `engaged` or `reached`. */
    val audience: String? = null,
) {
    public fun toQuery(): Map<String, Any?> = mapOf(
        "accountId" to accountId,
        "workspace_id" to workspaceId,
        "days" to days,
        "from" to from?.toString(),
        "to" to to?.toString(),
        "limit" to limit,
        "page" to page,
        "sort" to sort,
        "label" to label,
        "audience" to audience,
    )
}

/** A direct upload being reserved. `size` is the exact byte count that will be PUT. */
@Serializable
public data class PresignUploadParams(
    val workspaceId: String,
    val filename: String,
    val mimeType: String,
    val size: Long,
)
