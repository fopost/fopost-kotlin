package com.fopost.param

import com.fopost.model.VideoEditOperation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The body of `POST /video-edits`: the operations to run, in order, on one library video.
 * An operation that uses AI needs the `ai` scope as well.
 */
@Serializable
public data class CreateVideoEditParams(
    @SerialName("media_id") val mediaId: String,
    val operations: List<VideoEditOperation>,
    @SerialName("workspace_id") val workspaceId: String? = null,
)
