@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonObject

/** The statuses an article moves through. Only the API sets them. */
public object BlogArticleStatus {
    public const val QUEUED: String = "queued"
    public const val WRITING: String = "writing"
    public const val DRAFT: String = "draft"
    public const val SCHEDULED: String = "scheduled"
    public const val PUBLISHING: String = "publishing"
    public const val PUBLISHED: String = "published"
    public const val FAILED: String = "failed"
    public const val REJECTED: String = "rejected"

    public val ALL: List<String> =
        listOf(QUEUED, WRITING, DRAFT, SCHEDULED, PUBLISHING, PUBLISHED, FAILED, REJECTED)
}

/** The statuses a keyword moves through. [PLANNED] and [DISMISSED] may be set by a client. */
public object BlogKeywordStatus {
    public const val SUGGESTED: String = "suggested"
    public const val PLANNED: String = "planned"
    public const val WRITTEN: String = "written"
    public const val DISMISSED: String = "dismissed"

    public val ALL: List<String> = listOf(SUGGESTED, PLANNED, WRITTEN, DISMISSED)
}

/** Where published articles go. */
public object BlogTargetKind {
    public const val PRODUCT_BLOG: String = "product_blog"
    public const val ACCOUNT: String = "account"
    public const val WEBSITE: String = "website"

    public val ALL: List<String> = listOf(PRODUCT_BLOG, ACCOUNT, WEBSITE)
}

/**
 * A workspace's blog settings.
 *
 * [hasWebsiteSecret] says whether a website secret exists; the secret itself is only ever
 * returned by `blog.updateSettings` on the first `websiteUrl` set and by `blog.rotateWebsiteSecret`.
 */
@Serializable
public data class BlogSettings(
    @JsonNames("site_url") val siteUrl: String? = null,
    val topic: String? = null,
    val audience: String? = null,
    val language: String? = null,
    @JsonNames("location_code") val locationCode: Int? = null,
    /** Articles planned per week; 0 turns planning off. */
    @JsonNames("cadence_per_week") val cadencePerWeek: Int? = null,
    /** Days a draft waits for review before it may auto-publish. */
    @JsonNames("review_hold_days") val reviewHoldDays: Int? = null,
    @JsonNames("auto_publish") val autoPublish: Boolean? = null,
    /** `draft`, `standard` or `best`. */
    @JsonNames("image_tier") val imageTier: String? = null,
    @JsonNames("in_body_images") val inBodyImages: Int? = null,
    @JsonNames("min_words") val minWords: Int? = null,
    @JsonNames("max_words") val maxWords: Int? = null,
    @JsonNames("brand_voice_id") val brandVoiceId: String? = null,
    /** One of [BlogTargetKind]. */
    @JsonNames("target_kind") val targetKind: String? = null,
    @JsonNames("target_account_id") val targetAccountId: String? = null,
    @JsonNames("target_platform") val targetPlatform: String? = null,
    @JsonNames("website_url") val websiteUrl: String? = null,
    @JsonNames("has_website_secret") val hasWebsiteSecret: Boolean? = null,
    /** Whether search volume and difficulty can be fetched on this deployment. */
    @JsonNames("keyword_data_available") val keywordDataAvailable: Boolean? = null,
    @JsonNames("updated_at") val updatedAt: Instant? = null,
)

/**
 * What `blog.updateSettings` answers.
 *
 * [websiteSecret] is set only when the call set `websiteUrl` for the first time, and is never
 * shown again — store it then.
 */
@Serializable
public data class BlogSettingsUpdate(
    @SerialName("data") val settings: BlogSettings,
    @JsonNames("website_secret") val websiteSecret: String? = null,
)

/** A freshly issued website secret. The previous one stops verifying at once. */
@Serializable
public data class BlogWebsiteSecret(
    @JsonNames("website_secret") val websiteSecret: String? = null,
)

/** The outcome of a test delivery to the website target. */
@Serializable
public data class BlogWebsiteTest(
    val ok: Boolean = false,
    /** The HTTP status the website answered, when it answered. */
    val status: Int? = null,
    val error: String? = null,
)

/** What a keyword plan request answers: the plan runs in the background. */
@Serializable
public data class BlogPlanQueued(
    val status: String? = null,
)

/**
 * A keyword in the plan.
 *
 * [status] is one of [BlogKeywordStatus]; [source] is `provider`, `user` or `agent`.
 */
@Serializable
public data class BlogKeyword(
    val id: String? = null,
    val keyword: String? = null,
    @JsonNames("search_volume") val searchVolume: Int? = null,
    /** 0 to 100. */
    val difficulty: Int? = null,
    val cpc: Double? = null,
    val intent: String? = null,
    val source: String? = null,
    val status: String? = null,
    /** A calendar date, `YYYY-MM-DD`. */
    @JsonNames("planned_for") val plannedFor: String? = null,
    @JsonNames("created_at") val createdAt: Instant? = null,
)

/** An image placed in an article. [position] is absent on the featured image. */
@Serializable
public data class BlogArticleImage(
    @JsonNames("media_id") val mediaId: String? = null,
    val url: String? = null,
    val alt: String? = null,
    /** Index of the outline heading the image sits under. */
    val position: Int? = null,
)

/**
 * An article as it appears in a list.
 *
 * [status] is one of [BlogArticleStatus]; [origin] is `plan`, `user` or `agent`.
 */
@Serializable
public data class BlogArticle(
    val id: String? = null,
    val title: String? = null,
    val slug: String? = null,
    val keyword: String? = null,
    val excerpt: String? = null,
    @JsonNames("meta_title") val metaTitle: String? = null,
    @JsonNames("meta_description") val metaDescription: String? = null,
    @JsonNames("featured_image") val featuredImage: BlogArticleImage? = null,
    val tags: List<String> = emptyList(),
    @JsonNames("word_count") val wordCount: Int? = null,
    val status: String? = null,
    val origin: String? = null,
    @JsonNames("review_until") val reviewUntil: Instant? = null,
    @JsonNames("publish_at") val publishAt: Instant? = null,
    @JsonNames("published_at") val publishedAt: Instant? = null,
    @JsonNames("published_url") val publishedUrl: String? = null,
    val error: String? = null,
    @JsonNames("created_at") val createdAt: Instant? = null,
    @JsonNames("updated_at") val updatedAt: Instant? = null,
)

/** An article with its body, outline, brief and images: what `blog.getArticle` answers. */
@Serializable
public data class BlogArticleFull(
    val id: String? = null,
    val title: String? = null,
    val slug: String? = null,
    val keyword: String? = null,
    val excerpt: String? = null,
    @JsonNames("meta_title") val metaTitle: String? = null,
    @JsonNames("meta_description") val metaDescription: String? = null,
    @JsonNames("featured_image") val featuredImage: BlogArticleImage? = null,
    val tags: List<String> = emptyList(),
    @JsonNames("word_count") val wordCount: Int? = null,
    val status: String? = null,
    val origin: String? = null,
    @JsonNames("review_until") val reviewUntil: Instant? = null,
    @JsonNames("publish_at") val publishAt: Instant? = null,
    @JsonNames("published_at") val publishedAt: Instant? = null,
    @JsonNames("published_url") val publishedUrl: String? = null,
    val error: String? = null,
    @JsonNames("created_at") val createdAt: Instant? = null,
    @JsonNames("updated_at") val updatedAt: Instant? = null,
    val instructions: String? = null,
    val outline: List<String> = emptyList(),
    /** The research the writer worked from. */
    val brief: JsonObject? = null,
    @JsonNames("body_markdown") val bodyMarkdown: String? = null,
    @JsonNames("body_html") val bodyHtml: String? = null,
    val images: List<BlogArticleImage> = emptyList(),
)

/**
 * The `data` of an `article.published` webhook, also what the website target receives.
 *
 * Image URLs stay fetchable for six hours; download and store your own copy.
 */
@Serializable
public data class BlogArticlePublished(
    val id: String? = null,
    val title: String? = null,
    val slug: String? = null,
    val excerpt: String? = null,
    @JsonNames("meta_title") val metaTitle: String? = null,
    @JsonNames("meta_description") val metaDescription: String? = null,
    @JsonNames("body_markdown") val bodyMarkdown: String? = null,
    @JsonNames("body_html") val bodyHtml: String? = null,
    @JsonNames("featured_image") val featuredImage: BlogArticleImage? = null,
    val images: List<BlogArticleImage> = emptyList(),
    val tags: List<String> = emptyList(),
    val keyword: String? = null,
    @JsonNames("word_count") val wordCount: Int? = null,
    @JsonNames("published_at") val publishedAt: Instant? = null,
    @JsonNames("published_url") val publishedUrl: String? = null,
)
