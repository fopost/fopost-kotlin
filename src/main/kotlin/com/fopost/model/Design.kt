@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonObject

/**
 * A design: pages of layers that export to images, a PDF, or a video.
 *
 * [document] is the design document as the API stores it, passed through untouched. A list omits
 * it; `get` returns it.
 */
@Serializable
public data class Design(
    val id: String? = null,
    val name: String? = null,
    /** `story`, `post`, `carousel`, `thumbnail`, `card`, or `collage`. */
    val kind: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val document: JsonObject? = null,
    @SerialName("thumbnail_media_id") @JsonNames("thumbnailMediaId") val thumbnailMediaId: String? = null,
    @SerialName("thumbnail_url") @JsonNames("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    @SerialName("updated_at") @JsonNames("updatedAt") val updatedAt: Instant? = null,
)

/** One saved version of a design. [document] is present only when asked for. */
@Serializable
public data class DesignVersion(
    val id: String? = null,
    val version: Int? = null,
    val document: JsonObject? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
)

/** A design after an update or a restore, with the version it wrote, if any. */
@Serializable
public data class DesignSaved(
    @SerialName("data") val design: Design? = null,
    val version: DesignVersion? = null,
)

/** The answer to deleting a design. */
@Serializable
public data class DesignDeleted(
    val id: String? = null,
    val deleted: Boolean? = null,
)

/** The files an export wrote to the media library. */
@Serializable
public data class DesignExport(
    val format: String? = null,
    val files: List<DesignExportFile>? = null,
)

@Serializable
public data class DesignExportFile(
    @SerialName("media_id") @JsonNames("mediaId") val mediaId: String? = null,
    @SerialName("page_index") @JsonNames("pageIndex") val pageIndex: Int? = null,
    @SerialName("mime_type") @JsonNames("mimeType") val mimeType: String? = null,
    @SerialName("preview_url") @JsonNames("previewUrl") val previewUrl: String? = null,
)

/** A post that uses a design, with its performance when measured. */
@Serializable
public data class DesignPost(
    val id: String? = null,
    val status: String? = null,
    val title: String? = null,
    val text: String? = null,
    @SerialName("schedule_at") @JsonNames("scheduleAt") val scheduleAt: Instant? = null,
    @SerialName("published_at") @JsonNames("publishedAt") val publishedAt: Instant? = null,
    val platforms: List<String>? = null,
    val engagement: Long? = null,
    val views: Long? = null,
)

/** Text split into carousel slides: a cover, the slides, a closing call to action, and the brand. */
@Serializable
public data class TextSlides(
    val cover: SlideCover? = null,
    val slides: List<SlidePoint>? = null,
    val cta: SlidePoint? = null,
    val brand: SlidesBrand? = null,
)

@Serializable
public data class SlideCover(
    val title: String? = null,
    val subtitle: String? = null,
)

@Serializable
public data class SlidePoint(
    val heading: String? = null,
    val body: String? = null,
)

@Serializable
public data class SlidesBrand(
    val colors: List<String>? = null,
    val fonts: SlidesFonts? = null,
    @SerialName("logo_media_id") @JsonNames("logoMediaId") val logoMediaId: String? = null,
)

@Serializable
public data class SlidesFonts(
    val heading: String? = null,
    val body: String? = null,
)
