package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.Design
import com.fopost.model.DesignDeleted
import com.fopost.model.DesignExport
import com.fopost.model.DesignPost
import com.fopost.model.DesignSaved
import com.fopost.model.DesignVersion
import com.fopost.model.TextSlides
import com.fopost.param.BulkDesignParams
import com.fopost.param.CreateDesignParams
import com.fopost.param.DesignFromPostParams
import com.fopost.param.ExportDesignParams
import com.fopost.param.TextToSlidesParams
import com.fopost.param.UpdateDesignParams
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Designs: stories, posts, carousels and more, exported to the media library. Needs `posts`. */
public class DesignsResource internal constructor(private val http: ApiClient) {

    /** The workspace's designs, without their documents. */
    public suspend fun list(
        workspaceId: String,
        kind: String? = null,
        limit: Int? = null,
        offset: Int? = null,
    ): List<Design> =
        http.callList(
            "GET",
            "/designs",
            Design.serializer(),
            query = mapOf("workspace_id" to workspaceId, "kind" to kind, "limit" to limit, "offset" to offset),
        )

    public suspend fun create(params: CreateDesignParams): Design =
        http.call("POST", "/designs", Design.serializer(), http.jsonBody(params, CreateDesignParams.serializer()))

    public suspend fun get(designId: String): Design =
        http.call("GET", "/designs/$designId", Design.serializer())

    /** Saves changes. With `createVersion` the answer carries the version it wrote. */
    public suspend fun update(designId: String, params: UpdateDesignParams): DesignSaved =
        http.call(
            "PATCH",
            "/designs/$designId",
            DesignSaved.serializer(),
            http.jsonBody(params, UpdateDesignParams.serializer()),
            unwrap = false,
        )

    public suspend fun delete(designId: String): DesignDeleted =
        http.call("DELETE", "/designs/$designId", DesignDeleted.serializer())

    /** Saved versions. [includeDocument] adds each version's document. */
    public suspend fun versions(designId: String, includeDocument: Boolean = false): List<DesignVersion> =
        http.callList(
            "GET",
            "/designs/$designId/versions",
            DesignVersion.serializer(),
            query = mapOf("include" to if (includeDocument) "document" else null),
        )

    /** Puts a saved version back as the current document. */
    public suspend fun restoreVersion(designId: String, versionId: String): DesignSaved =
        http.call(
            "POST",
            "/designs/$designId/versions/$versionId/restore",
            DesignSaved.serializer(),
            unwrap = false,
        )

    public suspend fun duplicate(designId: String, name: String? = null): Design =
        http.call(
            "POST",
            "/designs/$designId/duplicate",
            Design.serializer(),
            http.jsonBody(buildJsonObject { name?.let { put("name", it) } }),
        )

    /** Renders the design and stores each file in the media library. */
    public suspend fun export(designId: String, params: ExportDesignParams): DesignExport =
        http.call(
            "POST",
            "/designs/$designId/export",
            DesignExport.serializer(),
            http.jsonBody(params, ExportDesignParams.serializer()),
        )

    /** One new design per row, filling the template's named text layers. */
    public suspend fun bulk(designId: String, params: BulkDesignParams): List<Design> =
        http.callList(
            "POST",
            "/designs/$designId/bulk",
            Design.serializer(),
            http.jsonBody(params, BulkDesignParams.serializer()),
        )

    /** The posts that use this design. */
    public suspend fun posts(designId: String): List<DesignPost> =
        http.callList("GET", "/designs/$designId/posts", DesignPost.serializer())

    /** Splits text into carousel slides. Spends AI, so a key also needs the `ai` scope. */
    public suspend fun textToSlides(params: TextToSlidesParams): TextSlides =
        http.call(
            "POST",
            "/designs/text-to-slides",
            TextSlides.serializer(),
            http.jsonBody(params, TextToSlidesParams.serializer()),
        )

    /** Turns a post's text into a design. */
    public suspend fun fromPost(params: DesignFromPostParams): Design =
        http.call(
            "POST",
            "/designs/from-post",
            Design.serializer(),
            http.jsonBody(params, DesignFromPostParams.serializer()),
        )
}
