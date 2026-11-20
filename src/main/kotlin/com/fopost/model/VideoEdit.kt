@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonObject

/** One video edit operation and whether this deployment can run it. */
@Serializable
public data class VideoEditOp(
    val name: String? = null,
    val available: Boolean? = null,
    @SerialName("uses_ai") @JsonNames("usesAi") val usesAi: Boolean? = null,
)

/**
 * One step of a video edit: an [op] such as `trim`, `subtitles` or `reframe`, and its [params]
 * passed through as JSON.
 */
@Serializable
public data class VideoEditOperation(
    val op: String,
    val params: JsonObject? = null,
)

/** A video edit job. Poll `videoEdits.get` until [status] is `succeeded` or `failed`. */
@Serializable
public data class VideoEdit(
    val id: String? = null,
    /** `queued`, `running`, `succeeded`, or `failed`. */
    val status: String? = null,
    val progress: Double? = null,
    @SerialName("source_media_id") @JsonNames("sourceMediaId") val sourceMediaId: String? = null,
    val operations: List<VideoEditOperation>? = null,
    @SerialName("result_media_id") @JsonNames("resultMediaId") val resultMediaId: String? = null,
    @SerialName("result_media") @JsonNames("resultMedia") val resultMedia: VideoEditResultMedia? = null,
    @SerialName("extra_outputs") @JsonNames("extraOutputs") val extraOutputs: List<VideoEditOutput>? = null,
    val result: JsonObject? = null,
    val error: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    @SerialName("updated_at") @JsonNames("updatedAt") val updatedAt: Instant? = null,
    @SerialName("completed_at") @JsonNames("completedAt") val completedAt: Instant? = null,
)

@Serializable
public data class VideoEditResultMedia(
    val id: String? = null,
    @SerialName("preview_url") @JsonNames("previewUrl") val previewUrl: String? = null,
    @SerialName("mime_type") @JsonNames("mimeType") val mimeType: String? = null,
)

/** A file an edit wrote besides the main result, such as a subtitle file or a clip. */
@Serializable
public data class VideoEditOutput(
    @SerialName("media_id") @JsonNames("mediaId") val mediaId: String? = null,
    val label: String? = null,
    val mime: String? = null,
    @SerialName("preview_url") @JsonNames("previewUrl") val previewUrl: String? = null,
)
