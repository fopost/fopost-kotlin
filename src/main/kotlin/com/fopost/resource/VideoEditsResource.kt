package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.VideoEdit
import com.fopost.model.VideoEditOp
import com.fopost.param.CreateVideoEditParams

/** Video edits: trim, subtitle, reframe and more, run as a job on a library video. Needs `posts`. */
public class VideoEditsResource internal constructor(private val http: ApiClient) {

    /** Every operation, and whether it can run here. */
    public suspend fun ops(): List<VideoEditOp> =
        http.callList("GET", "/video-edits/ops", VideoEditOp.serializer())

    public suspend fun list(
        workspaceId: String? = null,
        mediaId: String? = null,
        limit: Int? = null,
    ): List<VideoEdit> =
        http.callList(
            "GET",
            "/video-edits",
            VideoEdit.serializer(),
            query = mapOf("workspace_id" to workspaceId, "media_id" to mediaId, "limit" to limit),
        )

    /** Queues the job. It comes back `queued`; poll [get] for the result. */
    public suspend fun create(params: CreateVideoEditParams): VideoEdit =
        http.call(
            "POST",
            "/video-edits",
            VideoEdit.serializer(),
            http.jsonBody(params, CreateVideoEditParams.serializer()),
        )

    public suspend fun get(editId: String, workspaceId: String? = null): VideoEdit =
        http.call(
            "GET",
            "/video-edits/$editId",
            VideoEdit.serializer(),
            query = mapOf("workspace_id" to workspaceId),
        )
}
