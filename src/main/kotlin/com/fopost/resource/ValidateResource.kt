package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.LengthValidation
import com.fopost.model.MediaValidation
import com.fopost.model.PostValidation
import com.fopost.model.SubredditValidation
import com.fopost.param.ValidateLengthParams
import com.fopost.param.ValidateMediaParams
import com.fopost.param.ValidatePostParams

/**
 * Check a draft, a text, a file or a subreddit against platform rules without creating anything.
 *
 * Every method needs the `posts` scope. Nothing is stored server-side.
 */
public class ValidateResource internal constructor(private val http: ApiClient) {

    /** Per-platform blockers and advisory signals for a draft that is not a post yet. */
    public suspend fun post(params: ValidatePostParams): PostValidation =
        http.call(
            "POST",
            "/validate/post",
            PostValidation.serializer(),
            http.jsonBody(params, ValidatePostParams.serializer()),
        )

    /** How each platform counts the text, and whether it fits. */
    public suspend fun length(params: ValidateLengthParams): LengthValidation =
        http.call(
            "POST",
            "/validate/length",
            LengthValidation.serializer(),
            http.jsonBody(params, ValidateLengthParams.serializer()),
        )

    /** Fetch a public file and check it. Answers 200 with [MediaValidation.ok] false when a check fails. */
    public suspend fun media(url: String): MediaValidation =
        http.call(
            "POST",
            "/validate/media",
            MediaValidation.serializer(),
            http.jsonBody(ValidateMediaParams(url), ValidateMediaParams.serializer()),
        )

    /**
     * Whether the subreddit [name] exists and takes a post from [accountId].
     *
     * The check runs with that account's own token, so the account has to be one the key can see.
     * A private, banned or missing subreddit still answers 200, with [SubredditValidation.exists]
     * false.
     */
    public suspend fun subreddit(accountId: String, name: String): SubredditValidation =
        http.call(
            "GET",
            "/validate/subreddit",
            SubredditValidation.serializer(),
            query = mapOf("account_id" to accountId, "name" to name),
        )
}
