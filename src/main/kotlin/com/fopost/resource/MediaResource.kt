package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.MediaLibraryItem
import com.fopost.model.UploadedMedia
import java.io.File
import java.net.URLConnection
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** The workspace media library. Upload once, then attach the returned url to a content block. */
public class MediaResource internal constructor(private val http: ApiClient) {

    public suspend fun list(workspaceId: String? = null): List<MediaLibraryItem> =
        http.callList("GET", "/media", MediaLibraryItem.serializer(), query = mapOf("workspaceId" to workspaceId))

    /** Upload one or more files. Each is checked against the plan's storage allowance. */
    public suspend fun upload(workspaceId: String?, vararg files: File): List<UploadedMedia> {
        require(files.isNotEmpty()) { "fopost: upload needs at least one file" }
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        files.forEach { file ->
            builder.addFormDataPart("files", file.name, file.readBytes().toRequestBody(contentTypeOf(file.name)))
        }
        workspaceId?.let { builder.addFormDataPart("workspaceId", it) }
        return http.callList("POST", "/media/upload", UploadedMedia.serializer(), builder.build())
    }

    public suspend fun upload(
        workspaceId: String?,
        filename: String,
        content: ByteArray,
        contentType: String? = null,
    ): UploadedMedia? {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "files",
                filename,
                content.toRequestBody(contentType?.toMediaTypeOrNull() ?: contentTypeOf(filename)),
            )
            .apply { workspaceId?.let { addFormDataPart("workspaceId", it) } }
            .build()
        return http.callList("POST", "/media/upload", UploadedMedia.serializer(), body).firstOrNull()
    }

    /** Removes the file from the library. Posts already published keep the copy on the platform. */
    public suspend fun delete(mediaId: String) {
        http.send("DELETE", "/media/$mediaId")
    }

    private fun contentTypeOf(filename: String) =
        (URLConnection.guessContentTypeFromName(filename) ?: "application/octet-stream").toMediaTypeOrNull()
}
