package com.fopost.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.fopost.model.ContactChannel

/**
 * Filters for listing contacts. Every field is optional.
 *
 * [search] matches a display name or any of their handles; [platform] narrows to contacts with a
 * handle on that network; [source] is `inbox`, `radar` or `import`.
 */
public data class ContactListParams(
    val workspaceId: String? = null,
    val search: String? = null,
    val platform: String? = null,
    val source: String? = null,
    val page: Int? = null,
    val perPage: Int? = null,
) {
    public fun toQuery(): Map<String, Any?> = mapOf(
        "workspace_id" to workspaceId,
        "search" to search,
        "platform" to platform,
        "source" to source,
        "page" to page,
        "per_page" to perPage,
    )
}

/** The body of `contacts.create`. */
@Serializable
public data class CreateContactParams(
    @SerialName("workspace_id") val workspaceId: String,
    val channels: List<ContactChannel>,
    @SerialName("display_name") val displayName: String? = null,
    val note: String? = null,
    val fields: Map<String, String>? = null,
)

/**
 * The body of `contacts.update`. Only what is set is sent; a value in [fields] set to null clears
 * it, and passing [channels] replaces the list.
 */
@Serializable
public data class UpdateContactParams(
    @SerialName("display_name") val displayName: String? = null,
    val channels: List<ContactChannel>? = null,
    val note: String? = null,
    val fields: Map<String, String?>? = null,
)

/** The body of `contacts.import`. */
@Serializable
public data class ImportContactsParams(
    @SerialName("workspace_id") val workspaceId: String,
    val csv: String,
)

/**
 * The body of `contacts.createField`. [key] is lower-case letters, digits and underscores,
 * starting with a letter, and is fixed once created. A `select` field needs at least one option.
 */
@Serializable
public data class CreateContactFieldParams(
    @SerialName("workspace_id") val workspaceId: String,
    val key: String,
    val name: String,
    val type: String? = null,
    val options: List<String>? = null,
)

/** The body of `contacts.updateField`. The key and the type are fixed once created. */
@Serializable
public data class UpdateContactFieldParams(
    val name: String? = null,
    val options: List<String>? = null,
    val position: Int? = null,
)

/**
 * Filters for per-thread inbox numbers. [days] is the reporting period, 1 to 365, and the API
 * defaults to 7; [sort] is `volume`, `slowest` or `recent`.
 */
public data class ConversationAnalyticsParams(
    val workspaceId: String? = null,
    val accountId: String? = null,
    val days: Int? = null,
    val sort: String? = null,
    val page: Int? = null,
    val perPage: Int? = null,
) {
    public fun toQuery(): Map<String, Any?> = mapOf(
        "workspace_id" to workspaceId,
        "accountId" to accountId,
        "days" to days,
        "sort" to sort,
        "page" to page,
        "per_page" to perPage,
    )
}
