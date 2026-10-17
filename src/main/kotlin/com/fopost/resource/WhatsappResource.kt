package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.WhatsappBlockResult
import com.fopost.model.WhatsappCommerceSettings
import com.fopost.model.WhatsappEncryptionKeyStatus
import com.fopost.model.WhatsappFlow
import com.fopost.model.WhatsappFlowJsonResult
import com.fopost.model.WhatsappFlowResponse
import com.fopost.model.WhatsappGroup
import com.fopost.model.WhatsappProfile
import com.fopost.model.WhatsappSandboxSession
import com.fopost.model.WhatsappTemplate
import com.fopost.param.CreateWhatsappFlowParams
import com.fopost.param.CreateWhatsappTemplateParams
import com.fopost.param.ImportWhatsappTemplateParams
import com.fopost.param.UpdateWhatsappCommerceParams
import com.fopost.param.UpdateWhatsappFlowParams
import com.fopost.param.UpdateWhatsappProfileParams
import com.fopost.param.UpdateWhatsappTemplateParams
import com.fopost.param.WhatsappGroupParams
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * A WhatsApp Business connection: a number the customer already owns.
 *
 * The platform owns templates, flows, the business profile and the commerce
 * settings, so every method here is a live read or write against the customer's
 * own WhatsApp Business Account. Nothing is cached, and all of it answers 503
 * until WhatsApp is set up on the deployment. Every method needs the `accounts`
 * scope, except the sandbox, which sends a template and needs `publish`.
 */
public class WhatsappResource internal constructor(private val http: ApiClient) {

    private fun base(accountId: String) = "/accounts/$accountId/whatsapp"

    // ─── Profile ──────────────────────────────────────────────────────────

    /** The profile on the number, plus its quality rating and limit tier. */
    public suspend fun profile(accountId: String): WhatsappProfile =
        http.call("GET", "${base(accountId)}/profile", WhatsappProfile.serializer())

    /** A partial update: omitted fields keep their value. */
    public suspend fun updateProfile(
        accountId: String,
        params: UpdateWhatsappProfileParams,
    ): WhatsappProfile =
        http.call(
            "PATCH",
            "${base(accountId)}/profile",
            WhatsappProfile.serializer(),
            http.jsonBody(params, UpdateWhatsappProfileParams.serializer()),
        )

    /** A review, not a write: the number keeps its old name until it passes. */
    public suspend fun requestDisplayName(accountId: String, displayName: String) {
        http.send(
            "POST",
            "${base(accountId)}/profile/display-name",
            http.jsonBody(buildJsonObject { put("display_name", displayName) }),
        )
    }

    /** Sets the public username on the number. */
    public suspend fun setUsername(accountId: String, username: String): WhatsappProfile =
        http.call(
            "PUT",
            "${base(accountId)}/profile/username",
            WhatsappProfile.serializer(),
            http.jsonBody(buildJsonObject { put("username", username) }),
        )

    // ─── Templates ────────────────────────────────────────────────────────

    /** Every template on the account, with its review status. */
    public suspend fun templates(accountId: String, after: String? = null): List<WhatsappTemplate> =
        http.callList(
            "GET",
            "${base(accountId)}/templates",
            WhatsappTemplate.serializer(),
            query = mapOf("after" to after),
        )

    /** The pre-written templates the platform offers, for adapting. */
    public suspend fun templateLibrary(accountId: String, search: String? = null): List<JsonElement> =
        http.callList(
            "GET",
            "${base(accountId)}/templates/library",
            JsonElement.serializer(),
            query = mapOf("search" to search),
        )

    /** One template and the review status it currently has. */
    public suspend fun template(accountId: String, templateId: String): WhatsappTemplate =
        http.call("GET", "${base(accountId)}/templates/$templateId", WhatsappTemplate.serializer())

    /**
     * Files a template for review. The result carries the status the platform
     * assigned, which is `PENDING` on a normal submission.
     */
    public suspend fun createTemplate(
        accountId: String,
        params: CreateWhatsappTemplateParams,
    ): WhatsappTemplate =
        http.call(
            "POST",
            "${base(accountId)}/templates",
            WhatsappTemplate.serializer(),
            http.jsonBody(params, CreateWhatsappTemplateParams.serializer()),
        )

    /** Creates a template from one of the platform's library entries. */
    public suspend fun importTemplate(
        accountId: String,
        params: ImportWhatsappTemplateParams,
    ): WhatsappTemplate =
        http.call(
            "POST",
            "${base(accountId)}/templates/import",
            WhatsappTemplate.serializer(),
            http.jsonBody(params, ImportWhatsappTemplateParams.serializer()),
        )

    /** Edits a template. The name cannot change. */
    public suspend fun updateTemplate(
        accountId: String,
        templateId: String,
        params: UpdateWhatsappTemplateParams,
    ): WhatsappTemplate =
        http.call(
            "PATCH",
            "${base(accountId)}/templates/$templateId",
            WhatsappTemplate.serializer(),
            http.jsonBody(params, UpdateWhatsappTemplateParams.serializer()),
        )

    /** The name is required: it is what the platform deletes by. */
    public suspend fun deleteTemplate(accountId: String, templateId: String, name: String) {
        http.send(
            "DELETE",
            "${base(accountId)}/templates/$templateId",
            query = mapOf("name" to name),
        )
    }

    // ─── Groups ───────────────────────────────────────────────────────────

    /** The groups this number created. */
    public suspend fun groups(accountId: String): List<WhatsappGroup> =
        http.callList("GET", "${base(accountId)}/groups", WhatsappGroup.serializer())

    /** Participation is invite-only: send the invite link, there is no add. */
    public suspend fun createGroup(accountId: String, params: WhatsappGroupParams): WhatsappGroup =
        http.call(
            "POST",
            "${base(accountId)}/groups",
            WhatsappGroup.serializer(),
            http.jsonBody(params, WhatsappGroupParams.serializer()),
        )

    /** One group and its participant count. */
    public suspend fun group(accountId: String, groupId: String): WhatsappGroup =
        http.call("GET", "${base(accountId)}/groups/$groupId", WhatsappGroup.serializer())

    /** Changes a group's subject or description. */
    public suspend fun updateGroup(
        accountId: String,
        groupId: String,
        params: WhatsappGroupParams,
    ): WhatsappGroup =
        http.call(
            "PATCH",
            "${base(accountId)}/groups/$groupId",
            WhatsappGroup.serializer(),
            http.jsonBody(params, WhatsappGroupParams.serializer()),
        )

    /** Removes the group. */
    public suspend fun deleteGroup(accountId: String, groupId: String) {
        http.send("DELETE", "${base(accountId)}/groups/$groupId")
    }

    /** The link someone joins the group with. */
    public suspend fun groupInviteLink(accountId: String, groupId: String): String? =
        inviteLinkOf(http.send("GET", "${base(accountId)}/groups/$groupId/invite-link"))

    /** Issues a new link and invalidates the old one. */
    public suspend fun resetGroupInviteLink(accountId: String, groupId: String): String? =
        inviteLinkOf(http.send("POST", "${base(accountId)}/groups/$groupId/invite-link"))

    private fun inviteLinkOf(element: JsonElement): String? {
        val data = (element as? JsonObject)?.get("data") as? JsonObject ?: return null
        return data["inviteLink"]?.jsonPrimitive?.contentOrNullSafe()
    }

    private fun JsonPrimitive.contentOrNullSafe(): String? =
        if (this.isString) this.content else null

    /** Removes people from the group. There is no matching add. */
    public suspend fun removeGroupParticipants(
        accountId: String,
        groupId: String,
        users: List<String>,
    ) {
        http.send(
            "DELETE",
            "${base(accountId)}/groups/$groupId/participants",
            http.jsonBody(usersBody(users)),
        )
    }

    private fun usersBody(users: List<String>): JsonObject = buildJsonObject {
        putJsonArray("users") { users.forEach { add(JsonPrimitive(it)) } }
    }

    // ─── Blocking ─────────────────────────────────────────────────────────

    /** The numbers this account has blocked. */
    public suspend fun blocked(accountId: String, after: String? = null): List<String> =
        http.callList(
            "GET",
            "${base(accountId)}/block",
            String.serializer(),
            query = mapOf("after" to after),
        )

    /** Blocks up to 100 numbers, and names the ones the platform refused. */
    public suspend fun blockUsers(accountId: String, users: List<String>): WhatsappBlockResult =
        http.call(
            "POST",
            "${base(accountId)}/block",
            WhatsappBlockResult.serializer(),
            http.jsonBody(usersBody(users)),
        )

    /** Unblocks up to 100 numbers. */
    public suspend fun unblockUsers(accountId: String, users: List<String>): WhatsappBlockResult =
        http.call(
            "DELETE",
            "${base(accountId)}/block",
            WhatsappBlockResult.serializer(),
            http.jsonBody(usersBody(users)),
        )

    // ─── Commerce ─────────────────────────────────────────────────────────

    /** Whether the cart and catalog show on the number. */
    public suspend fun commerceSettings(accountId: String): WhatsappCommerceSettings =
        http.call("GET", "${base(accountId)}/commerce", WhatsappCommerceSettings.serializer())

    /** Turns the cart or the catalog on or off. */
    public suspend fun updateCommerceSettings(
        accountId: String,
        params: UpdateWhatsappCommerceParams,
    ): WhatsappCommerceSettings =
        http.call(
            "PATCH",
            "${base(accountId)}/commerce",
            WhatsappCommerceSettings.serializer(),
            http.jsonBody(params, UpdateWhatsappCommerceParams.serializer()),
        )

    /** Points the number at a catalog the customer already owns. */
    public suspend fun linkCatalog(accountId: String, catalogId: String): WhatsappCommerceSettings =
        http.call(
            "POST",
            "${base(accountId)}/commerce/catalog",
            WhatsappCommerceSettings.serializer(),
            http.jsonBody(buildJsonObject { put("catalog_id", catalogId) }),
        )

    // ─── Flows ────────────────────────────────────────────────────────────

    /** The in-chat forms on this account, with their validation errors. */
    public suspend fun flows(accountId: String): List<WhatsappFlow> =
        http.callList("GET", "${base(accountId)}/flows", WhatsappFlow.serializer())

    /** One flow and what the platform found wrong with it. */
    public suspend fun flow(accountId: String, flowId: String): WhatsappFlow =
        http.call("GET", "${base(accountId)}/flows/$flowId", WhatsappFlow.serializer())

    /** Creates a draft flow; its screens are uploaded separately. */
    public suspend fun createFlow(
        accountId: String,
        params: CreateWhatsappFlowParams,
    ): WhatsappFlow =
        http.call(
            "POST",
            "${base(accountId)}/flows",
            WhatsappFlow.serializer(),
            http.jsonBody(params, CreateWhatsappFlowParams.serializer()),
        )

    /** Changes a flow's name, categories or endpoint. */
    public suspend fun updateFlow(
        accountId: String,
        flowId: String,
        params: UpdateWhatsappFlowParams,
    ): WhatsappFlow =
        http.call(
            "PATCH",
            "${base(accountId)}/flows/$flowId",
            WhatsappFlow.serializer(),
            http.jsonBody(params, UpdateWhatsappFlowParams.serializer()),
        )

    /** Drafts only; a published flow is deprecated instead. */
    public suspend fun deleteFlow(accountId: String, flowId: String) {
        http.send("DELETE", "${base(accountId)}/flows/$flowId")
    }

    /**
     * Replaces the flow's screens. The platform answers with its validation
     * errors rather than refusing, so they come back as data.
     */
    public suspend fun uploadFlowJson(
        accountId: String,
        flowId: String,
        flowJson: JsonObject,
    ): WhatsappFlowJsonResult =
        http.call(
            "PUT",
            "${base(accountId)}/flows/$flowId/json",
            WhatsappFlowJsonResult.serializer(),
            http.jsonBody(buildJsonObject { put("flow_json", flowJson) }),
        )

    /** Makes the flow sendable. A published flow can no longer be deleted. */
    public suspend fun publishFlow(accountId: String, flowId: String): WhatsappFlow =
        http.call("POST", "${base(accountId)}/flows/$flowId/publish", WhatsappFlow.serializer())

    /** Retires a published flow. */
    public suspend fun deprecateFlow(accountId: String, flowId: String): WhatsappFlow =
        http.call("POST", "${base(accountId)}/flows/$flowId/deprecate", WhatsappFlow.serializer())

    /** What people submitted through this account's flows. */
    public suspend fun flowResponses(accountId: String): List<WhatsappFlowResponse> =
        http.callList(
            "GET",
            "${base(accountId)}/flows/responses",
            WhatsappFlowResponse.serializer(),
        )

    /** Whether a business public key is registered, and how the platform judged it. */
    public suspend fun encryptionKeyStatus(accountId: String): WhatsappEncryptionKeyStatus =
        http.call(
            "GET",
            "${base(accountId)}/flows/encryption-key",
            WhatsappEncryptionKeyStatus.serializer(),
        )

    /**
     * Registers the public half of the key the platform encrypts a flow
     * endpoint's payloads with. The private half stays with the customer.
     */
    public suspend fun setEncryptionKey(
        accountId: String,
        businessPublicKey: String,
    ): WhatsappEncryptionKeyStatus =
        http.call(
            "PUT",
            "${base(accountId)}/flows/encryption-key",
            WhatsappEncryptionKeyStatus.serializer(),
            http.jsonBody(buildJsonObject { put("business_public_key", businessPublicKey) }),
        )

    // ─── Account state and sandbox ────────────────────────────────────────

    /** The account review state and the number's quality and limit tier. */
    public suspend fun accountEvents(accountId: String): JsonElement =
        http.call("GET", "${base(accountId)}/events", JsonElement.serializer())

    /** Sandbox invitations for a workspace. */
    public suspend fun sandboxSessions(workspaceId: String): List<WhatsappSandboxSession> =
        http.callList(
            "GET",
            "/whatsapp/sandbox/sessions",
            WhatsappSandboxSession.serializer(),
            query = mapOf("workspaceId" to workspaceId),
        )

    /**
     * Invites one tester to the platform-owned test number. Inviting sends a
     * template, so it needs the `publish` scope.
     */
    public suspend fun createSandboxSession(
        workspaceId: String,
        phoneNumber: String,
    ): WhatsappSandboxSession =
        http.call(
            "POST",
            "/whatsapp/sandbox/sessions",
            WhatsappSandboxSession.serializer(),
            http.jsonBody(
                buildJsonObject {
                    put("workspaceId", workspaceId)
                    put("phoneNumber", phoneNumber)
                }
            ),
        )
}
