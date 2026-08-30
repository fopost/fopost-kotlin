package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.Webhook
import com.fopost.param.CreateWebhookParams
import com.fopost.param.UpdateWebhookParams

/** Webhook endpoints, for publishing and account-health events. */
public class WebhooksResource internal constructor(private val http: ApiClient) {

    public suspend fun list(): List<Webhook> =
        http.callList("GET", "/webhooks", Webhook.serializer())

    /**
     * Register an endpoint. Events come from [com.fopost.model.WebhookEvents].
     *
     * The signing secret is on the returned object and is never shown again — store it now.
     */
    public suspend fun create(workspaceId: String, url: String, events: List<String>): Webhook =
        http.call(
            "POST",
            "/webhooks",
            Webhook.serializer(),
            http.jsonBody(CreateWebhookParams(workspaceId, url, events), CreateWebhookParams.serializer()),
        )

    public suspend fun update(webhookId: String, params: UpdateWebhookParams): Webhook =
        http.call(
            "PUT",
            "/webhooks/$webhookId",
            Webhook.serializer(),
            http.jsonBody(params, UpdateWebhookParams.serializer()),
        )

    public suspend fun delete(webhookId: String) {
        http.send("DELETE", "/webhooks/$webhookId")
    }

    /** Send a sample event to the endpoint, to check it is reachable and verifies the signature. */
    public suspend fun test(webhookId: String) {
        http.send("POST", "/webhooks/$webhookId/test")
    }
}
