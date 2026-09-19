@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames

/** A file in the workspace media library. */
@Serializable
public data class MediaLibraryItem(
    val id: String? = null,
    @SerialName("user_id") @JsonNames("userId") val userId: String? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
    val name: String? = null,
    val url: String? = null,
    val type: String? = null,
    @SerialName("mime_type") @JsonNames("mimeType") val mimeType: String? = null,
    val size: Long? = null,
    @SerialName("alt_text") @JsonNames("altText") val altText: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
)

/** A file as returned by an upload, ready to attach to a content block. */
@Serializable
public data class UploadedMedia(
    val id: String? = null,
    val type: String? = null,
    val name: String? = null,
    val url: String? = null,
    val size: Long? = null,
) {
    /** The same file shaped as a content-block attachment. */
    public fun toMediaItem(): MediaItem = MediaItem(type = type, name = name, url = url, size = size)
}

/** A one-time upload slot: PUT the bytes to [uploadUrl] with [headers], then complete it. */
@Serializable
public data class PresignedUpload(
    @SerialName("upload_id") @JsonNames("uploadId") val uploadId: String? = null,
    @SerialName("upload_url") @JsonNames("uploadUrl") val uploadUrl: String? = null,
    val method: String? = null,
    val headers: Map<String, String>? = null,
    @SerialName("expires_at") @JsonNames("expiresAt") val expiresAt: Instant? = null,
)
