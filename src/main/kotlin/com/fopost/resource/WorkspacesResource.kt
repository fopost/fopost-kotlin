package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.Workspace
import com.fopost.model.WorkspaceAnalytics
import com.fopost.param.WorkspaceParams

/** The workspaces the key can reach. */
public class WorkspacesResource internal constructor(private val http: ApiClient) {

    /** Every workspace the key can reach, each with its connected accounts. */
    public suspend fun list(): List<Workspace> =
        http.callList("GET", "/workspaces", Workspace.serializer())

    public suspend fun get(workspaceId: String): Workspace =
        http.call("GET", "/workspaces/$workspaceId", Workspace.serializer())

    public suspend fun create(params: WorkspaceParams): Workspace =
        http.call("POST", "/workspaces", Workspace.serializer(), http.jsonBody(params, WorkspaceParams.serializer()))

    public suspend fun update(workspaceId: String, params: WorkspaceParams): Workspace =
        http.call(
            "PUT",
            "/workspaces/$workspaceId",
            Workspace.serializer(),
            http.jsonBody(params, WorkspaceParams.serializer()),
        )

    /** Deletes the workspace and everything in it. This cannot be undone. */
    public suspend fun delete(workspaceId: String) {
        http.send("DELETE", "/workspaces/$workspaceId")
    }

    /** Latest follower and post figures for every account in the workspace. */
    public suspend fun analytics(workspaceId: String): WorkspaceAnalytics =
        http.call("GET", "/workspaces/$workspaceId/analytics", WorkspaceAnalytics.serializer())
}
