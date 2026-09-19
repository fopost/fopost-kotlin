package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.LengthValidation
import com.fopost.model.MediaValidation
import com.fopost.model.PostValidation
import com.fopost.param.ValidateLengthParams
import com.fopost.param.ValidateMediaParams
import com.fopost.param.ValidatePostParams

/**
 * Check a draft, a text or a file against platform rules without creating anything.
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
}
