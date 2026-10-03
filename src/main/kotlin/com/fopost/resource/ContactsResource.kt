package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.Contact
import com.fopost.model.ContactConversation
import com.fopost.model.ContactField
import com.fopost.model.ContactImportResult
import com.fopost.model.ConversationAnalytics
import com.fopost.model.Page
import com.fopost.model.PageMeta
import com.fopost.param.ContactListParams
import com.fopost.param.ConversationAnalyticsParams
import com.fopost.param.CreateContactFieldParams
import com.fopost.param.CreateContactParams
import com.fopost.param.ImportContactsParams
import com.fopost.param.UpdateContactFieldParams
import com.fopost.param.UpdateContactParams
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonObject

/**
 * Contacts: the people behind the inbox.
 *
 * One row per human, however many handles they write from. Contacts are built for you: an inbound
 * inbox item files its author, a reply files whoever you answered, and both fold into whatever is
 * already on file.
 *
 * Everything here needs the `inbox` scope — a key that may read a message may read who sent it —
 * except [conversationAnalytics], which answers counts and reads under `analytics`.
 */
public class ContactsResource internal constructor(private val http: ApiClient) {

    /** One page of contacts, most recently active first. */
    public suspend fun list(params: ContactListParams = ContactListParams()): Page<Contact> {
        // Not `http.page`: this list names its counters `pagination` rather than `meta`.
        val element = http.send("GET", "/contacts", null, params.toQuery()) as? JsonObject
            ?: return Page(emptyList(), null)
        val items = element["data"]
            ?.let { http.decodeElement(ListSerializer(Contact.serializer()), it) }
            ?: emptyList()
        val meta = (element["pagination"] as? JsonObject)
            ?.let { http.decodeElement(PageMeta.serializer(), it) }
        return Page(items, meta)
    }

    /**
     * One contact. A contact in a workspace the key cannot reach answers `404`, exactly as an id
     * that never existed does.
     */
    public suspend fun get(contactId: String): Contact =
        http.call("GET", "/contacts/$contactId", Contact.serializer())

    /**
     * Files a person by hand. It folds into the contact that already holds the first channel, so
     * it cannot duplicate someone the inbox has already met.
     */
    public suspend fun create(params: CreateContactParams): Contact =
        http.call(
            "POST",
            "/contacts",
            Contact.serializer(),
            http.jsonBody(params, CreateContactParams.serializer()),
        )

    /** Changes a contact. Only what is set on [params] is sent. */
    public suspend fun update(contactId: String, params: UpdateContactParams): Contact =
        http.call(
            "PATCH",
            "/contacts/$contactId",
            Contact.serializer(),
            http.jsonBody(params, UpdateContactParams.serializer()),
        )

    /**
     * Removes a contact. The messages stay in the inbox, and a later one files the person again.
     */
    public suspend fun delete(contactId: String) {
        http.send("DELETE", "/contacts/$contactId")
    }

    /** The inbox threads one contact appears in, newest first. */
    public suspend fun conversations(contactId: String, limit: Int? = null): List<ContactConversation> =
        http.callList(
            "GET",
            "/contacts/$contactId/conversations",
            ContactConversation.serializer(),
            query = mapOf("limit" to limit),
        )

    /**
     * Imports contacts from CSV text. `platform` and `handle` are required columns; `external_id`,
     * `display_name` and `note` are optional, and every other column is read as a custom field
     * key. A column matching no field is reported back rather than stored.
     */
    public suspend fun import(workspaceId: String, csv: String): ContactImportResult =
        http.call(
            "POST",
            "/contacts/import",
            ContactImportResult.serializer(),
            http.jsonBody(ImportContactsParams(workspaceId, csv), ImportContactsParams.serializer()),
        )

    /** The columns this workspace keeps about a contact, in display order. */
    public suspend fun listFields(workspaceId: String): List<ContactField> =
        http.callList(
            "GET",
            "/contacts/fields",
            ContactField.serializer(),
            query = mapOf("workspace_id" to workspaceId),
        )

    /** Adds a column. A duplicate key answers `409`. */
    public suspend fun createField(params: CreateContactFieldParams): ContactField =
        http.call(
            "POST",
            "/contacts/fields",
            ContactField.serializer(),
            http.jsonBody(params, CreateContactFieldParams.serializer()),
            query = mapOf("workspace_id" to params.workspaceId),
        )

    /** Renames a field, changes its options, or moves it. */
    public suspend fun updateField(fieldId: String, params: UpdateContactFieldParams): ContactField =
        http.call(
            "PATCH",
            "/contacts/fields/$fieldId",
            ContactField.serializer(),
            http.jsonBody(params, UpdateContactFieldParams.serializer()),
        )

    /** Removes a field and every contact answer to it. */
    public suspend fun deleteField(fieldId: String) {
        http.send("DELETE", "/contacts/fields/$fieldId")
    }

    /** Inbox volume and reply time per thread. Needs the `analytics` scope rather than `inbox`. */
    public suspend fun conversationAnalytics(
        params: ConversationAnalyticsParams = ConversationAnalyticsParams(),
    ): ConversationAnalytics =
        http.call(
            "GET",
            "/analytics/inbox/conversations",
            ConversationAnalytics.serializer(),
            query = params.toQuery(),
        )
}
