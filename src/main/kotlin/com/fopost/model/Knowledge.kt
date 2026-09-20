@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames

/**
 * One thing the workspace has told FoPost about itself: an FAQ, a note, a page
 * on its own site, or a plain-text/CSV file from the media library.
 *
 * [kind] is `faq`, `text`, `url` or `file`. [status] is `pending`, `syncing`,
 * `ready` or `failed`; only a `ready` source is searched.
 */
@Serializable
public data class KnowledgeSource(
    val id: String? = null,
    val kind: String? = null,
    val title: String? = null,
    val status: String? = null,
    /** Why the last sync failed, in plain words. */
    @SerialName("statusMessage") @JsonNames("status_message") val statusMessage: String? = null,
    /** Set for `url` sources. */
    val url: String? = null,
    /** Set for `file` sources: the media library item read. */
    @SerialName("mediaId") @JsonNames("media_id") val mediaId: String? = null,
    /** Null means the source serves the whole workspace. */
    @SerialName("brandVoiceId") @JsonNames("brand_voice_id") val brandVoiceId: String? = null,
    /** Searchable passages the last sync produced. */
    @SerialName("chunkCount") @JsonNames("chunk_count") val chunkCount: Int? = null,
    /** The typed text, for `faq` and `text` sources only. */
    val content: String? = null,
    @SerialName("lastSyncedAt") @JsonNames("last_synced_at") val lastSyncedAt: Instant? = null,
    @SerialName("createdAt") @JsonNames("created_at") val createdAt: Instant? = null,
    @SerialName("updatedAt") @JsonNames("updated_at") val updatedAt: Instant? = null,
)

/** One retrieved passage, with the source it came from so a reply can cite it. */
@Serializable
public data class KnowledgeMatch(
    @SerialName("sourceId") @JsonNames("source_id") val sourceId: String? = null,
    @SerialName("sourceTitle") @JsonNames("source_title") val sourceTitle: String? = null,
    @SerialName("sourceKind") @JsonNames("source_kind") val sourceKind: String? = null,
    @SerialName("sourceUrl") @JsonNames("source_url") val sourceUrl: String? = null,
    val text: String? = null,
    /** Similarity to the question, 0-1. */
    val score: Double? = null,
)

/** What a sync answers: the source, and that it is queued. */
@Serializable
public data class KnowledgeSyncResult(
    val id: String? = null,
    val status: String? = null,
)
