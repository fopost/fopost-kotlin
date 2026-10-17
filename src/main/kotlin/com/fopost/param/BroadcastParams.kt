package com.fopost.param

import com.fopost.model.AudienceFilter
import com.fopost.model.SequenceStep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Filters for listing broadcasts. Every field is optional. */
public data class BroadcastListParams(
    val workspaceId: String? = null,
    /** `draft`, `scheduled`, `sending`, `sent` or `cancelled`. */
    val status: String? = null,
    val page: Int? = null,
    val perPage: Int? = null,
) {
    public fun toQuery(): Map<String, Any?> = mapOf(
        "workspace_id" to workspaceId,
        "status" to status,
        "page" to page,
        "per_page" to perPage,
    )
}

/** Filters for listing a broadcast's recipients. */
public data class RecipientListParams(
    /** `pending`, `sent`, `skipped` or `failed`. */
    val status: String? = null,
    val page: Int? = null,
    val perPage: Int? = null,
) {
    public fun toQuery(): Map<String, Any?> = mapOf(
        "status" to status,
        "page" to page,
        "per_page" to perPage,
    )
}

/**
 * The body of `broadcasts.create`. Creating never sends: set [scheduledAt] to have it go out on
 * its own, or call `broadcasts.send`.
 */
@Serializable
public data class CreateBroadcastParams(
    @SerialName("workspace_id") val workspaceId: String,
    /** The connected account the messages go out from. */
    @SerialName("account_id") val accountId: String,
    /** Internal only; never sent to anyone. */
    val name: String,
    val text: String,
    @SerialName("media_id") val mediaId: String? = null,
    /** Unset means every contact in the workspace. */
    val audience: AudienceFilter? = null,
    @SerialName("scheduled_at") val scheduledAt: String? = null,
)

/**
 * The body of `broadcasts.update`. Only what is set is sent, and only a draft or scheduled
 * broadcast can be edited.
 */
@Serializable
public data class UpdateBroadcastParams(
    val name: String? = null,
    val text: String? = null,
    @SerialName("media_id") val mediaId: String? = null,
    val audience: AudienceFilter? = null,
    @SerialName("scheduled_at") val scheduledAt: String? = null,
)

/** Filters for listing sequences. */
public data class SequenceListParams(
    val workspaceId: String? = null,
    val page: Int? = null,
    val perPage: Int? = null,
) {
    public fun toQuery(): Map<String, Any?> = mapOf(
        "workspace_id" to workspaceId,
        "page" to page,
        "per_page" to perPage,
    )
}

/** The body of `sequences.create`. Creating one enrolls nobody. */
@Serializable
public data class CreateSequenceParams(
    @SerialName("workspace_id") val workspaceId: String,
    @SerialName("account_id") val accountId: String,
    val name: String,
    /** In order; each step's delay is counted from the one before. */
    val steps: List<SequenceStep>,
    /** `active` (the default) or `paused`. */
    val status: String? = null,
)

/**
 * The body of `sequences.update`. Pausing stops every enrollment from firing without ending any
 * of them; resuming picks them up where they stood.
 */
@Serializable
public data class UpdateSequenceParams(
    val name: String? = null,
    val steps: List<SequenceStep>? = null,
    val status: String? = null,
)

/** Who to enroll: named contacts, or the audience they are drawn from. */
@Serializable
public data class EnrollParams(
    @SerialName("contact_ids") val contactIds: List<String>? = null,
    val audience: AudienceFilter? = null,
)

/** The contacts to take off a sequence. */
@Serializable
public data class UnenrollParams(
    @SerialName("contact_ids") val contactIds: List<String>,
)

/** Pagination for a sequence's enrollments. */
public data class EnrollmentListParams(
    val page: Int? = null,
    val perPage: Int? = null,
) {
    public fun toQuery(): Map<String, Any?> = mapOf(
        "page" to page,
        "per_page" to perPage,
    )
}
