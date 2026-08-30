package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.Label
import com.fopost.param.CreateLabelParams
import com.fopost.param.UpdateLabelParams

/** Workspace labels, for grouping posts into campaigns. */
public class LabelsResource internal constructor(private val http: ApiClient) {

    public suspend fun list(workspaceId: String? = null): List<Label> =
        http.callList("GET", "/labels", Label.serializer(), query = mapOf("workspace_id" to workspaceId))

    public suspend fun get(labelId: String): Label =
        http.call("GET", "/labels/$labelId", Label.serializer())

    /** [color] is a hex value, e.g. `#4F46E5`. */
    public suspend fun create(workspaceId: String, name: String, color: String): Label =
        http.call(
            "POST",
            "/labels",
            Label.serializer(),
            http.jsonBody(CreateLabelParams(workspaceId, name, color), CreateLabelParams.serializer()),
        )

    public suspend fun update(labelId: String, name: String, color: String): Label =
        http.call(
            "PUT",
            "/labels/$labelId",
            Label.serializer(),
            http.jsonBody(UpdateLabelParams(name, color), UpdateLabelParams.serializer()),
        )

    /** Deletes the label and detaches it from every post that carried it. */
    public suspend fun delete(labelId: String) {
        http.send("DELETE", "/labels/$labelId")
    }
}
