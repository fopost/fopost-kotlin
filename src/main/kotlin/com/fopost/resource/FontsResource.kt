package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.FontList
import com.fopost.model.UploadedFont
import java.io.File
import java.net.URLConnection
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** Fonts for designs: the shipped library plus the workspace's uploads. Needs `posts`. */
public class FontsResource internal constructor(private val http: ApiClient) {

    public suspend fun list(workspaceId: String? = null): FontList =
        http.call("GET", "/fonts", FontList.serializer(), query = mapOf("workspace_id" to workspaceId))

    /**
     * Uploads one font file. [family] defaults to the file name, [weight] to 400 and [style]
     * (`normal` or `italic`) to `normal`.
     */
    public suspend fun upload(
        workspaceId: String,
        file: File,
        family: String? = null,
        weight: Int? = null,
        style: String? = null,
    ): UploadedFont = upload(workspaceId, file.name, file.readBytes(), family, weight, style)

    public suspend fun upload(
        workspaceId: String,
        filename: String,
        content: ByteArray,
        family: String? = null,
        weight: Int? = null,
        style: String? = null,
        contentType: String? = null,
    ): UploadedFont {
        val type = (contentType ?: URLConnection.guessContentTypeFromName(filename) ?: "application/octet-stream")
            .toMediaTypeOrNull()
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("workspace_id", workspaceId)
            .addFormDataPart("file", filename, content.toRequestBody(type))
            .apply {
                family?.let { addFormDataPart("family", it) }
                weight?.let { addFormDataPart("weight", it.toString()) }
                style?.let { addFormDataPart("style", it) }
            }
            .build()
        return http.call("POST", "/fonts", UploadedFont.serializer(), body)
    }

    public suspend fun delete(fontId: String) {
        http.send("DELETE", "/fonts/$fontId")
    }
}
