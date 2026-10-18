package com.fopost.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The numeric X user id to follow or unfollow, as X reports it. */
@Serializable
public data class XFollowParams(@SerialName("user_id") val userId: String)

/**
 * An X search. [q] is 1 to 512 characters, [maxResults] 10 to 100 (default 10), and [nextToken]
 * is the cursor from the previous page.
 */
public data class XSearchParams(
    val accountId: String,
    val q: String,
    val maxResults: Int? = null,
    val nextToken: String? = null,
) {
    public fun toQuery(): Map<String, Any?> = mapOf(
        "account_id" to accountId,
        "q" to q,
        "max_results" to maxResults,
        "next_token" to nextToken,
    )
}
