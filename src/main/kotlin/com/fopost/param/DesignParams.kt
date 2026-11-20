package com.fopost.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** The body of `POST /designs`. Without a [document] the API starts a blank one for [kind]. */
@Serializable
public data class CreateDesignParams(
    @SerialName("workspace_id") val workspaceId: String,
    /** `story`, `post`, `carousel`, `thumbnail`, `card`, or `collage`. */
    val kind: String,
    val name: String? = null,
    val document: JsonObject? = null,
    @SerialName("brand_voice_id") val brandVoiceId: String? = null,
)

/** The body of `PATCH /designs/{id}`. Only the fields you set are sent. */
@Serializable
public data class UpdateDesignParams(
    val name: String? = null,
    val kind: String? = null,
    val document: JsonObject? = null,
    /** Also save the new document as a version. */
    @SerialName("create_version") val createVersion: Boolean? = null,
)

/** The body of `POST /designs/{id}/export`. */
@Serializable
public data class ExportDesignParams(
    /** `png`, `jpg`, `pdf`, `mp4`, or `gif`. */
    val format: String,
    /** Zero-based pages to export; every page when null. */
    @SerialName("page_indexes") val pageIndexes: List<Int>? = null,
    /** 5, 10 or 15 seconds, for `mp4` and `gif`. */
    @SerialName("duration_sec") val durationSec: Int? = null,
)

/** The body of `POST /designs/{id}/bulk`: one new design per row, layer names as the keys. */
@Serializable
public data class BulkDesignParams(
    val rows: List<Map<String, String>>,
    /** The row key whose value names each new design. */
    @SerialName("name_column") val nameColumn: String? = null,
)

/** The body of `POST /designs/text-to-slides`. Needs [text] or [postId]. */
@Serializable
public data class TextToSlidesParams(
    @SerialName("workspace_id") val workspaceId: String,
    val text: String? = null,
    @SerialName("post_id") val postId: String? = null,
    @SerialName("max_slides") val maxSlides: Int? = null,
    @SerialName("brand_voice_id") val brandVoiceId: String? = null,
)

/** The body of `POST /designs/from-post`. Needs [text] or [postId]. */
@Serializable
public data class DesignFromPostParams(
    @SerialName("workspace_id") val workspaceId: String,
    val text: String? = null,
    @SerialName("post_id") val postId: String? = null,
    /** `quote`, `post`, or `plain`. */
    val style: String? = null,
    val preset: String? = null,
    @SerialName("account_id") val accountId: String? = null,
    val carousel: Boolean? = null,
    val name: String? = null,
    @SerialName("brand_voice_id") val brandVoiceId: String? = null,
)
