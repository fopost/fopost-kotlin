package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.Automation
import com.fopost.model.AutomationRun
import com.fopost.model.AutomationStats
import com.fopost.model.AutomationTrigger
import com.fopost.model.AutomationVersion
import com.fopost.model.AutomationVersionRestore
import com.fopost.model.Page
import com.fopost.param.AutomationParams
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive

/** Automations: a trigger, and the steps it runs. */
public class AutomationsResource internal constructor(private val http: ApiClient) {

    public suspend fun list(): List<Automation> =
        http.callList("GET", "/automations", Automation.serializer())

    /** The full automation, including its steps. */
    public suspend fun get(automationId: String): Automation =
        http.call("GET", "/automations/$automationId", Automation.serializer())

    /** The webhook signing secret is on the created object and is never shown again. */
    public suspend fun create(params: AutomationParams): Automation =
        http.call(
            "POST",
            "/automations",
            Automation.serializer(),
            http.jsonBody(params, AutomationParams.serializer()),
        )

    public suspend fun update(automationId: String, params: AutomationParams): Automation =
        http.call(
            "PUT",
            "/automations/$automationId",
            Automation.serializer(),
            http.jsonBody(params, AutomationParams.serializer()),
        )

    public suspend fun delete(automationId: String) {
        http.send("DELETE", "/automations/$automationId")
    }

    /** Flip the automation on or off. Returns its new state. */
    public suspend fun toggle(automationId: String): Boolean {
        val data = http.call("POST", "/automations/$automationId/toggle", JsonObject.serializer())
        return data["active"]?.jsonPrimitive?.booleanOrNull ?: false
    }

    public suspend fun runs(automationId: String, page: Int? = null, perPage: Int? = null): Page<AutomationRun> =
        http.page(
            "/automations/$automationId/runs",
            AutomationRun.serializer(),
            mapOf("page" to page, "per_page" to perPage),
        )

    /** One run, with the per-step log of what it did. */
    public suspend fun run(automationId: String, runId: Long): AutomationRun =
        http.call("GET", "/automations/$automationId/runs/$runId", AutomationRun.serializer())

    /**
     * Fire an `api_webhook` automation by hand.
     *
     * [payload] becomes the run's trigger event, and the steps read it from there.
     */
    public suspend fun trigger(automationId: String, payload: JsonObject? = null): AutomationTrigger =
        http.call(
            "POST",
            "/automations/$automationId/trigger",
            AutomationTrigger.serializer(),
            http.jsonBody(payload ?: JsonObject(emptyMap())),
        )

    /** Saved versions of the graph, newest first, with what each one added or removed. */
    public suspend fun versions(automationId: String, page: Int? = null, perPage: Int? = null): Page<AutomationVersion> =
        http.page(
            "/automations/$automationId/versions",
            AutomationVersion.serializer(),
            mapOf("page" to page, "per_page" to perPage),
        )

    /** One version, with its full graph. */
    public suspend fun version(automationId: String, version: Int): AutomationVersion =
        http.call("GET", "/automations/$automationId/versions/$version", AutomationVersion.serializer())

    /** Make an old graph current again. It is saved as a new version, so nothing is lost. */
    public suspend fun restoreVersion(automationId: String, version: Int): AutomationVersionRestore =
        http.call(
            "POST",
            "/automations/$automationId/versions/$version/restore",
            AutomationVersionRestore.serializer(),
        )

    public suspend fun stats(): AutomationStats =
        http.call("GET", "/automations/stats", AutomationStats.serializer())
}
