@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames

/**
 * One custom-field clause in an audience filter.
 *
 * [op] is `is`, `is_not`, `contains`, `is_set` or `is_not_set`; null means `is`.
 */
@Serializable
public data class AudienceField(
    val key: String,
    val op: String? = null,
    val value: String? = null,
)

/**
 * Who a broadcast or an enrollment resolves to, expressed over contacts.
 *
 * Every clause narrows: a contact has to match all of them. An unset clause is not sent, so an
 * empty filter is everyone in the workspace.
 */
@Serializable
public data class AudienceFilter(
    /** Contacts with a handle on at least one of these networks. */
    val platforms: List<String>? = null,
    @SerialName("label_ids") @JsonNames("labelIds") val labelIds: List<String>? = null,
    /** `inbox`, `radar` or `import`. */
    val source: String? = null,
    val fields: List<AudienceField>? = null,
)

/**
 * What became of a broadcast's recipients, by status.
 *
 * [skipped] is usually the messaging window doing its job.
 */
@Serializable
public data class BroadcastCounts(
    val total: Int = 0,
    val sent: Int = 0,
    val skipped: Int = 0,
    val failed: Int = 0,
    val pending: Int = 0,
)

/**
 * One message, sent into conversations the workspace already has.
 *
 * [name] is internal only and is never sent to anyone. [status] is `draft`, `scheduled`,
 * `sending`, `sent` or `cancelled`.
 */
@Serializable
public data class Broadcast(
    val id: String? = null,
    val name: String? = null,
    val text: String? = null,
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    val audience: AudienceFilter? = null,
    val status: String? = null,
    @SerialName("scheduled_at") @JsonNames("scheduledAt") val scheduledAt: Instant? = null,
    @SerialName("sent_at") @JsonNames("sentAt") val sentAt: Instant? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    val counts: BroadcastCounts? = null,
    /** Set only on a list that spans more than one workspace. */
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
)

/**
 * One contact on one broadcast, and what became of their message.
 *
 * [status] is `pending`, `sent`, `skipped` or `failed`. [skipReason] is set when the status is
 * skipped: `window_closed`, `no_conversation` or `unsupported_platform`. `window_closed` means
 * the network's messaging window had shut, so nothing was attempted.
 */
@Serializable
public data class BroadcastRecipient(
    @SerialName("contact_id") @JsonNames("contactId") val contactId: String? = null,
    @SerialName("display_name") @JsonNames("displayName") val displayName: String? = null,
    val status: String? = null,
    @SerialName("skip_reason") @JsonNames("skipReason") val skipReason: String? = null,
    @SerialName("sent_at") @JsonNames("sentAt") val sentAt: Instant? = null,
    val error: String? = null,
)

/**
 * What a send started.
 *
 * [recipients] is how many contacts matched, not how many will be messaged — the messaging
 * window decides that.
 */
@Serializable
public data class BroadcastSent(
    val id: String? = null,
    val status: String? = null,
    val recipients: Int = 0,
)

/**
 * One message and how long after the previous step it goes out.
 *
 * [delayHours] on the first step is measured from the enrollment, so 0 means straight away.
 */
@Serializable
public data class SequenceStep(
    @SerialName("delay_hours") @JsonNames("delayHours") val delayHours: Double,
    val text: String,
    @SerialName("media_id") @JsonNames("mediaId") val mediaId: String? = null,
)

/** Where a sequence's enrollments stand, by status. */
@Serializable
public data class EnrollmentCounts(
    val total: Int = 0,
    val active: Int = 0,
    val completed: Int = 0,
    val stopped: Int = 0,
    val failed: Int = 0,
)

/**
 * A series of messages, each a delay after the one before.
 *
 * [status] is `active` or `paused`; a paused sequence fires nothing.
 */
@Serializable
public data class Sequence(
    val id: String? = null,
    val name: String? = null,
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    val steps: List<SequenceStep> = emptyList(),
    val status: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    val enrollments: EnrollmentCounts? = null,
    /** Set only on a list that spans more than one workspace. */
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
)

/**
 * One contact walking one sequence.
 *
 * [step] counts the steps already sent, so it is also the index of the next one. [status] is
 * `active`, `completed`, `stopped` or `failed`, and [error] carries the reason when a step was
 * skipped rather than sent.
 */
@Serializable
public data class Enrollment(
    val id: String? = null,
    @SerialName("contact_id") @JsonNames("contactId") val contactId: String? = null,
    @SerialName("display_name") @JsonNames("displayName") val displayName: String? = null,
    val step: Int = 0,
    @SerialName("next_at") @JsonNames("nextAt") val nextAt: Instant? = null,
    val status: String? = null,
    @SerialName("last_sent_at") @JsonNames("lastSentAt") val lastSentAt: Instant? = null,
    val error: String? = null,
)

/** How many contacts a call put on the sequence. */
@Serializable
public data class Enrolled(
    val id: String? = null,
    val enrolled: Int = 0,
)

/** How many enrollments a call stopped. */
@Serializable
public data class Unenrolled(
    val id: String? = null,
    val stopped: Int = 0,
)
