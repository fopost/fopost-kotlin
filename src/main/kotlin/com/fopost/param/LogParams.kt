package com.fopost.param

/**
 * Filters for the request log. [status] is `success` or `failed`; [q] searches the path and
 * message; [before] is the id to page back from.
 */
public data class LogListParams(
    val workspaceId: String? = null,
    val days: Int? = null,
    val status: String? = null,
    val method: String? = null,
    val statusCode: Int? = null,
    val platform: String? = null,
    val route: String? = null,
    val q: String? = null,
    val limit: Int? = null,
    val before: Long? = null,
) {
    public fun toQuery(): Map<String, Any?> = mapOf(
        "workspace_id" to workspaceId,
        "days" to days,
        "status" to status,
        "method" to method,
        "status_code" to statusCode,
        "platform" to platform,
        "route" to route,
        "q" to q,
        "limit" to limit,
        "before" to before,
    )
}
