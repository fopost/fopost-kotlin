package com.fopost.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A file to check alongside the text. [url] must be public http(s). */
@Serializable
public data class ValidateMediaInput(
    val url: String,
    @SerialName("mime_type") val mimeType: String,
    /** Bytes, when known. */
    val size: Long? = null,
)

/** A draft to check against one or more platforms before it exists as a post. */
@Serializable
public data class ValidatePostParams(
    /** Platform slugs, at least one. */
    val platforms: List<String>,
    val content: String? = null,
    val media: List<ValidateMediaInput>? = null,
)

/** Text to measure against each platform's limit. */
@Serializable
public data class ValidateLengthParams(
    val text: String,
    /** Platform slugs, at least one. */
    val platforms: List<String>,
)

/** A public http(s) URL of a file to check. */
@Serializable
public data class ValidateMediaParams(
    val url: String,
)
