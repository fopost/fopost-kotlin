@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames

/** The fonts a design may use: the shipped library and the workspace's own uploads. */
@Serializable
public data class FontList(
    val library: List<LibraryFont>? = null,
    val uploaded: List<UploadedFont>? = null,
)

/** A font family shipped with FoPost. */
@Serializable
public data class LibraryFont(
    val slug: String? = null,
    val family: String? = null,
    val category: String? = null,
    val scripts: List<String>? = null,
    val weights: List<Int>? = null,
    val files: List<LibraryFontFile>? = null,
)

@Serializable
public data class LibraryFontFile(
    val path: String? = null,
    val subset: String? = null,
    val weight: Int? = null,
    val style: String? = null,
    @SerialName("unicode_range") @JsonNames("unicodeRange") val unicodeRange: String? = null,
)

/** A font file uploaded to a workspace. */
@Serializable
public data class UploadedFont(
    val id: String? = null,
    val family: String? = null,
    /** `normal` or `italic`. */
    val style: String? = null,
    val weight: Int? = null,
    val format: String? = null,
    @SerialName("file_url") @JsonNames("fileUrl") val fileUrl: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
)
