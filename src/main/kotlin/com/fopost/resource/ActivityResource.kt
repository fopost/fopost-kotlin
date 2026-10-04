package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.ActivityPage

/** What happened in a workspace, including the security audit log. */
public class ActivityResource internal constructor(private val http: ApiClient) {

    /**
     * Activity newest first. Leave [workspaceId] null to read every workspace the key can reach.
     *
     * [kind] of `security` is the audit log: members joining, leaving or changing role and access,
     * and changes to two-step verification, passkeys, single sign-on and signed-in devices. Those
     * rows are append-only and never expire.
     */
    public suspend fun list(
        workspaceId: String? = null,
        kind: String? = null,
        from: String? = null,
        to: String? = null,
        cursor: String? = null,
        limit: Int? = null,
    ): ActivityPage =
        // The response carries meta beside data, so it is read whole rather than unwrapped.
        http.call(
            "GET",
            "/activity",
            ActivityPage.serializer(),
            query = mapOf(
                "workspace_id" to workspaceId,
                "kind" to kind,
                "from" to from,
                "to" to to,
                "cursor" to cursor,
                "limit" to limit,
            ),
            unwrap = false,
        )
}
