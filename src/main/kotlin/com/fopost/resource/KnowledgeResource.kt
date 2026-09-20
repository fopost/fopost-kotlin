package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.KnowledgeMatch
import com.fopost.model.KnowledgeSource
import com.fopost.model.KnowledgeSyncResult
import com.fopost.param.CreateKnowledgeSourceParams
import com.fopost.param.UpdateKnowledgeSourceParams

/**
 * The workspace knowledge base: what the workspace has told FoPost about itself.
 *
 * A source is an FAQ, a note, a page on your own site, or a plain-text/CSV item
 * from the media library. Retrieval over these is what grounds a drafted inbox
 * reply in your own answers instead of an invented one. Needs the `inbox` scope.
 */
public class KnowledgeResource internal constructor(private val http: ApiClient) {

    /** Every source in the workspace. Only a `ready` one is searched. */
    public suspend fun list(workspaceId: String? = null): List<KnowledgeSource> =
        http.callList(
            "GET",
            "/knowledge/sources",
            KnowledgeSource.serializer(),
            query = mapOf("workspace_id" to workspaceId),
        )

    /** Adds a source and queues it for indexing, so it comes back `pending`. */
    public suspend fun create(params: CreateKnowledgeSourceParams): KnowledgeSource =
        http.call(
            "POST",
            "/knowledge/sources",
            KnowledgeSource.serializer(),
            http.jsonBody(params, CreateKnowledgeSourceParams.serializer()),
        )

    /** Edits a source. Changing the content or the URL re-indexes it. */
    public suspend fun update(
        sourceId: String,
        params: UpdateKnowledgeSourceParams,
    ): KnowledgeSource =
        http.call(
            "PATCH",
            "/knowledge/sources/$sourceId",
            KnowledgeSource.serializer(),
            http.jsonBody(params, UpdateKnowledgeSourceParams.serializer()),
        )

    /** Removes the source and every passage indexed from it. */
    public suspend fun delete(sourceId: String) {
        http.send("DELETE", "/knowledge/sources/$sourceId")
    }

    /**
     * Reads the source again — a `url` source is re-fetched. Returns once the
     * re-index is queued, not once it has finished.
     */
    public suspend fun sync(sourceId: String): KnowledgeSyncResult =
        http.call("POST", "/knowledge/sources/$sourceId/sync", KnowledgeSyncResult.serializer())

    /**
     * The passages closest to a question, best first. An empty list is the
     * honest answer when nothing stored answers it. [topK] defaults to 5 and
     * caps at 20.
     */
    public suspend fun search(
        q: String,
        topK: Int? = null,
        brandVoiceId: String? = null,
        workspaceId: String? = null,
    ): List<KnowledgeMatch> =
        http.callList(
            "GET",
            "/knowledge/search",
            KnowledgeMatch.serializer(),
            query = mapOf(
                "q" to q,
                "top_k" to topK,
                "brand_voice_id" to brandVoiceId,
                "workspace_id" to workspaceId,
            ),
        )
}
