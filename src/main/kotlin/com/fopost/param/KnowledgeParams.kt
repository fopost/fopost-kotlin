package com.fopost.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The body of `POST /knowledge/sources`.
 *
 * [kind] is `faq`, `text`, `url` or `file`. An `faq` or `text` source needs
 * [content], a `url` source needs [url], and a `file` source needs [mediaId]
 * pointing at a plain-text or CSV item in the same workspace. Whatever is left
 * null is not sent.
 */
@Serializable
public data class CreateKnowledgeSourceParams(
    val kind: String,
    val title: String,
    val content: String? = null,
    val url: String? = null,
    @SerialName("media_id") val mediaId: String? = null,
    @SerialName("brand_voice_id") val brandVoiceId: String? = null,
    @SerialName("workspace_id") val workspaceId: String? = null,
)

/**
 * The body of `PATCH /knowledge/sources/{id}`. Only the fields you set are
 * sent, so it stays a partial update. Changing the content or the URL returns
 * the source to `pending` and re-indexes it.
 */
@Serializable
public data class UpdateKnowledgeSourceParams(
    val title: String? = null,
    val content: String? = null,
    val url: String? = null,
    @SerialName("brand_voice_id") val brandVoiceId: String? = null,
)
