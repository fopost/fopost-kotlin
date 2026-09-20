package com.fopost.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

// Per-network extras under /accounts/{id}/<platform>/…, all on the accounts scope.

/** A Pinterest board a Pin can land on; pass [id] as the `board_id` platform setting. */
@Serializable
public data class PinterestBoard(
    val id: String? = null,
    val name: String? = null,
    val privacy: String? = null,
    val description: String? = null,
    /** The board cover image. */
    val image: String? = null,
)

/** A playlist on the channel; [isDefault] marks the one a new video joins when none is picked. */
@Serializable
public data class YouTubePlaylist(
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val privacy: String? = null,
    @SerialName("item_count") @JsonNames("itemCount") val itemCount: Int? = null,
    @SerialName("thumbnail_url") @JsonNames("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerialName("is_default") @JsonNames("isDefault") val isDefault: Boolean = false,
)

/** A caption track on one of the channel's videos; [language] is a BCP-47 tag. */
@Serializable
public data class YouTubeCaptionTrack(
    val id: String? = null,
    val language: String? = null,
    val name: String? = null,
    @SerialName("track_kind") @JsonNames("trackKind") val trackKind: String? = null,
    @SerialName("is_draft") @JsonNames("isDraft") val isDraft: Boolean = false,
    @SerialName("is_auto_synced") @JsonNames("isAutoSynced") val isAutoSynced: Boolean = false,
    @SerialName("last_updated") @JsonNames("lastUpdated") val lastUpdated: String? = null,
)

/** One caption track read back as text, in SRT. */
@Serializable
public data class YouTubeTranscript(
    @SerialName("caption_id") @JsonNames("captionId") val captionId: String? = null,
    val transcript: String = "",
)

/** The default post languages for a Bluesky connection: up to three BCP-47 tags. */
@Serializable
public data class BlueskyLanguages(
    val languages: List<String> = emptyList(),
)

/**
 * The switches TikTok enforces at publish time. They are set on the TikTok account itself, not in
 * FoPost, so a disabled one cannot be turned back on here.
 */
@Serializable
public data class TikTokCreatorInfo(
    val username: String? = null,
    val nickname: String? = null,
    @SerialName("avatar_url") @JsonNames("avatarUrl") val avatarUrl: String? = null,
    /** The levels this creator may publish at right now. */
    @SerialName("privacy_level_options")
    @JsonNames("privacyLevelOptions")
    val privacyLevelOptions: List<String> = emptyList(),
    @SerialName("comment_disabled") @JsonNames("commentDisabled") val commentDisabled: Boolean = false,
    @SerialName("duet_disabled") @JsonNames("duetDisabled") val duetDisabled: Boolean = false,
    @SerialName("stitch_disabled") @JsonNames("stitchDisabled") val stitchDisabled: Boolean = false,
    @SerialName("max_video_post_duration_sec")
    @JsonNames("maxVideoPostDurationSec")
    val maxVideoPostDurationSec: Int? = null,
)

/**
 * A track from TikTok's Commercial Music Library; pass [id] as the `music_id` platform setting.
 */
@Serializable
public data class TikTokMusic(
    val id: String,
    val title: String = "",
    val author: String? = null,
    @SerialName("duration_sec") @JsonNames("durationSec") val durationSec: Int? = null,
    @SerialName("cover_url") @JsonNames("coverUrl") val coverUrl: String? = null,
    @SerialName("preview_url") @JsonNames("previewUrl") val previewUrl: String? = null,
)

/** A place a post can be tagged with; pass [id] as the `location_id` platform setting. */
@Serializable
public data class TikTokPlace(
    val id: String,
    val name: String = "",
    val address: String? = null,
    val city: String? = null,
    val country: String? = null,
)

/**
 * One of the account's own videos, resolved from a share link. TikTok serves no raw media file,
 * so [downloadUrl] is the share address, which is what a repurpose run reads.
 */
@Serializable
public data class TikTokVideoSource(
    @SerialName("video_id") @JsonNames("videoId") val videoId: String,
    val title: String? = null,
    val description: String? = null,
    @SerialName("duration_sec") @JsonNames("durationSec") val durationSec: Int? = null,
    @SerialName("cover_image_url") @JsonNames("coverImageUrl") val coverImageUrl: String? = null,
    @SerialName("share_url") @JsonNames("shareUrl") val shareUrl: String? = null,
    @SerialName("embed_link") @JsonNames("embedLink") val embedLink: String? = null,
    @SerialName("download_url") @JsonNames("downloadUrl") val downloadUrl: String? = null,
)

/** A track a Reel can carry; pass [id] as the `audio_id` platform setting. */
@Serializable
public data class InstagramAudio(
    val id: String? = null,
    val title: String? = null,
    val artist: String? = null,
    @SerialName("duration_ms") @JsonNames("durationMs") val durationMs: Int? = null,
    @SerialName("audio_type") @JsonNames("audioType") val audioType: String? = null,
    @SerialName("cover_artwork_url") @JsonNames("coverArtworkUrl") val coverArtworkUrl: String? = null,
    @SerialName("preview_url") @JsonNames("previewUrl") val previewUrl: String? = null,
    val username: String? = null,
    @SerialName("is_ads_eligible") @JsonNames("isAdsEligible") val isAdsEligible: Boolean? = null,
)

/** What this account has published in the rolling window, and what is left. */
@Serializable
public data class InstagramPublishingLimit(
    @SerialName("quota_usage") @JsonNames("quotaUsage") val quotaUsage: Int = 0,
    @SerialName("quota_total") @JsonNames("quotaTotal") val quotaTotal: Int? = null,
    @SerialName("quota_duration_sec") @JsonNames("quotaDurationSec") val quotaDurationSec: Int? = null,
    val remaining: Int? = null,
)

/** A story still inside its 24 hours; [insights] is present only when asked for. */
@Serializable
public data class InstagramStory(
    val id: String? = null,
    @SerialName("media_type") @JsonNames("mediaType") val mediaType: String? = null,
    @SerialName("media_product_type") @JsonNames("mediaProductType") val mediaProductType: String? = null,
    val permalink: String? = null,
    @SerialName("media_url") @JsonNames("mediaUrl") val mediaUrl: String? = null,
    @SerialName("thumbnail_url") @JsonNames("thumbnailUrl") val thumbnailUrl: String? = null,
    val caption: String? = null,
    val timestamp: String? = null,
    val insights: Map<String, Int>? = null,
)

/** The insight set for one story. */
@Serializable
public data class InstagramStoryInsights(
    @SerialName("story_id") @JsonNames("storyId") val storyId: String? = null,
    val insights: Map<String, Int> = emptyMap(),
)

/** An entity a post can mention; [annotation] is what the post text carries. */
@Serializable
public data class LinkedInMention(
    val urn: String = "",
    val name: String = "",
    @SerialName("vanity_name") @JsonNames("vanityName") val vanityName: String? = null,
    @SerialName("logo_url") @JsonNames("logoUrl") val logoUrl: String? = null,
    val type: String? = null,
    val annotation: String = "",
)
