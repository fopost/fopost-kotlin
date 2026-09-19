package com.fopost.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/** The verdict for a draft. [ready] is true only when every platform is. */
@Serializable
public data class PostValidation(
    val ready: Boolean? = null,
    val platforms: List<PlatformValidation> = emptyList(),
)

/** One platform's verdict: hard blockers in [issues], advisory [signals] that never block. */
@Serializable
public data class PlatformValidation(
    val platform: String? = null,
    val ready: Boolean? = null,
    val issues: List<String> = emptyList(),
    val score: Double? = null,
    val signals: List<ContentSignal> = emptyList(),
)

/** Text length measured against each platform. */
@Serializable
public data class LengthValidation(
    val ok: Boolean? = null,
    val platforms: List<PlatformLength> = emptyList(),
)

/** What one platform counts. [limit] is null when the platform has no text limit; [unit] is `chars` or `bytes`. */
@Serializable
public data class PlatformLength(
    val platform: String? = null,
    val length: Int? = null,
    val limit: Int? = null,
    val unit: String? = null,
    val ok: Boolean? = null,
    val signals: List<ContentSignal> = emptyList(),
)

/** A file check. [mimeType] and [type] (`image`, `video`, `audio`, `document`) are present only when [ok]. */
@Serializable
public data class MediaValidation(
    val ok: Boolean? = null,
    val issues: List<String> = emptyList(),
    val name: String? = null,
    /** Bytes fetched. */
    val size: Long? = null,
    @SerialName("mime_type") @JsonNames("mimeType") val mimeType: String? = null,
    val type: String? = null,
)

/**
 * Whether a subreddit exists and takes a post from one account.
 *
 * [ok] is true when both hold. A private, banned or missing subreddit answers with [exists]
 * false rather than failing.
 */
@Serializable
public data class SubredditValidation(
    val subreddit: String? = null,
    val exists: Boolean = false,
    @SerialName("can_post") @JsonNames("canPost") val canPost: Boolean = false,
    @SerialName("over_18") @JsonNames("over18") val over18: Boolean = false,
    @SerialName("flair_enabled") @JsonNames("flairEnabled") val flairEnabled: Boolean = false,
    val ok: Boolean = false,
)
