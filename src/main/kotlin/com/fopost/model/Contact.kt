@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames

/**
 * One handle on one network.
 *
 * [handle] is lower-cased with no leading `@`. [externalId] is the platform's own id for that
 * person when it gave one, and a merge prefers it: a handle can be changed, an id cannot.
 */
@Serializable
public data class ContactChannel(
    val platform: String,
    val handle: String,
    @SerialName("externalId") @JsonNames("external_id") val externalId: String? = null,
)

/** A workspace label put on a contact. */
@Serializable
public data class ContactLabel(
    val id: String? = null,
    val name: String? = null,
    val color: String? = null,
)

/**
 * One person behind the inbox, however many handles they write from.
 *
 * Built from what already reached the workspace: an inbound item files its author, a reply files
 * whoever you answered, an import files a row. [source] is `inbox`, `radar` or `import`.
 */
@Serializable
public data class Contact(
    val id: String? = null,
    @SerialName("display_name") @JsonNames("displayName") val displayName: String? = null,
    val channels: List<ContactChannel> = emptyList(),
    val source: String? = null,
    val note: String? = null,
    @SerialName("first_seen_at") @JsonNames("firstSeenAt") val firstSeenAt: Instant? = null,
    @SerialName("last_seen_at") @JsonNames("lastSeenAt") val lastSeenAt: Instant? = null,
    /** Custom field answers, keyed by field key. */
    val fields: Map<String, String> = emptyMap(),
    val labels: List<ContactLabel> = emptyList(),
    /** Set only on a list that spans more than one workspace. */
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
)

/**
 * A column the workspace invented.
 *
 * [key] is the machine name and also the CSV column header, fixed once created; [name] is what
 * people read. [type] is `text`, `number`, `date`, `select` or `boolean`.
 */
@Serializable
public data class ContactField(
    val id: String? = null,
    val key: String? = null,
    val name: String? = null,
    val type: String? = null,
    val options: List<String> = emptyList(),
    val position: Int? = null,
)

/**
 * One inbox thread a contact appears in.
 *
 * [key] is how the inbox groups it: the DM thread id, else the post the comments hang off, else
 * the handle.
 */
@Serializable
public data class ContactConversation(
    val key: String? = null,
    @SerialName("account_id") @JsonNames("accountId") val accountId: String? = null,
    @SerialName("account_username") @JsonNames("accountUsername") val accountUsername: String? = null,
    val platform: String? = null,
    val messages: Int? = null,
    val received: Int? = null,
    val sent: Int? = null,
    @SerialName("last_message_at") @JsonNames("lastMessageAt") val lastMessageAt: Instant? = null,
    /** An inbox item id, so the thread can be read through the inbox. */
    @SerialName("last_item_id") @JsonNames("lastItemId") val lastItemId: String? = null,
)

/** One CSV row that was not stored, and why. */
@Serializable
public data class ContactImportSkip(
    val row: Int? = null,
    val reason: String? = null,
)

/**
 * What an import did.
 *
 * [unknownColumns] are headers matching no custom field. They are reported rather than stored, so
 * a typo in a column name is visible.
 */
@Serializable
public data class ContactImportResult(
    val created: Int? = null,
    val merged: Int? = null,
    val skipped: List<ContactImportSkip> = emptyList(),
    @SerialName("unknownColumns") @JsonNames("unknown_columns") val unknownColumns: List<String> = emptyList(),
)

/** How one thread performed over the period. */
@Serializable
public data class ConversationAnalyticsRow(
    /**
     * An opaque, stable handle for the thread, not the id or handle the inbox groups on: that
     * would be a person, and this reads under the `analytics` scope. Use it to line the same
     * thread up between two calls.
     */
    val key: String? = null,
    @SerialName("accountId") @JsonNames("account_id") val accountId: String? = null,
    val platform: String? = null,
    val received: Int? = null,
    val sent: Int? = null,
    val answered: Int? = null,
    val open: Int? = null,
    /** Null when the thread was never answered. */
    @SerialName("medianResponseMinutes")
    @JsonNames("median_response_minutes")
    val medianResponseMinutes: Int? = null,
    @SerialName("firstMessageAt") @JsonNames("first_message_at") val firstMessageAt: Instant? = null,
    @SerialName("lastMessageAt") @JsonNames("last_message_at") val lastMessageAt: Instant? = null,
)

/** One page of per-thread inbox numbers. */
@Serializable
public data class ConversationAnalytics(
    val conversations: List<ConversationAnalyticsRow> = emptyList(),
    val total: Int? = null,
    val page: Int? = null,
    @SerialName("perPage") @JsonNames("per_page") val perPage: Int? = null,
)
