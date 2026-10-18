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

    /** Submitted for approval; publishing and scheduling are open from here. */
    public const val IN_REVIEW: String = "in_review"
    public const val SCHEDULED: String = "scheduled"
    public const val PUBLISHING: String = "publishing"
    public const val PUBLISHED: String = "published"
    public const val FAILED: String = "failed"
    public const val REJECTED: String = "rejected"

    public val ALL: List<String> =
        listOf(QUEUED, WRITING, DRAFT, IN_REVIEW, SCHEDULED, PUBLISHING, PUBLISHED, FAILED, REJECTED)
}

/** The statuses a keyword moves through. [PLANNED] and [DISMISSED] may be set by a client. */
public object BlogKeywordStatus {
    public const val SUGGESTED: String = "suggested"
    public const val PLANNED: String = "planned"
    public const val WRITTEN: String = "written"
    public const val DISMISSED: String = "dismissed"

    public val ALL: List<String> = listOf(SUGGESTED, PLANNED, WRITTEN, DISMISSED)
}

/** What the blog is for; steers suggested terms and keyword ranking. */
public object BlogGoal {
    public const val TRAFFIC: String = "traffic"
    public const val LEADS: String = "leads"
    public const val SALES: String = "sales"
    public const val SIGNUPS: String = "signups"
    public const val AUTHORITY: String = "authority"

    public val ALL: List<String> = listOf(TRAFFIC, LEADS, SALES, SIGNUPS, AUTHORITY)
}

/** The statuses a keyword research run moves through. */
public object BlogPlanRunStatus {
    public const val QUEUED: String = "queued"
    public const val RUNNING: String = "running"
    public const val DONE: String = "done"
    public const val FAILED: String = "failed"

    public val ALL: List<String> = listOf(QUEUED, RUNNING, DONE, FAILED)
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
    @SerialName("site_url") @JsonNames("siteUrl") val siteUrl: String? = null,
    val topic: String? = null,
    val audience: String? = null,
    /** One of [BlogGoal]. */
    val goal: String? = null,
    /** What the blog wants to write about. */
    @SerialName("focus_topics") @JsonNames("focusTopics") val focusTopics: String? = null,
    /** What the blog never writes about. */
    @SerialName("avoid_topics") @JsonNames("avoidTopics") val avoidTopics: String? = null,
    val language: String? = null,
    @SerialName("location_code") @JsonNames("locationCode") val locationCode: Int? = null,
    /** Articles planned per week; 0 turns planning off. */
    @SerialName("cadence_per_week") @JsonNames("cadencePerWeek") val cadencePerWeek: Int? = null,
    /** Days before its planned date that a picked keyword starts being written. */
    @SerialName("review_hold_days") @JsonNames("reviewHoldDays") val reviewHoldDays: Int? = null,
    /** `draft`, `standard` or `best`. */
    @SerialName("image_tier") @JsonNames("imageTier") val imageTier: String? = null,
    @SerialName("in_body_images") @JsonNames("inBodyImages") val inBodyImages: Int? = null,
    @SerialName("min_words") @JsonNames("minWords") val minWords: Int? = null,
    @SerialName("max_words") @JsonNames("maxWords") val maxWords: Int? = null,
    @SerialName("brand_voice_id") @JsonNames("brandVoiceId") val brandVoiceId: String? = null,
    /** One of [BlogTargetKind]. */
    @SerialName("target_kind") @JsonNames("targetKind") val targetKind: String? = null,
    @SerialName("target_account_id") @JsonNames("targetAccountId") val targetAccountId: String? = null,
    @SerialName("target_platform") @JsonNames("targetPlatform") val targetPlatform: String? = null,
    @SerialName("website_url") @JsonNames("websiteUrl") val websiteUrl: String? = null,
    @SerialName("has_website_secret") @JsonNames("hasWebsiteSecret") val hasWebsiteSecret: Boolean? = null,
    /** The last keyword research run; null before the first. */
    @SerialName("plan_run") @JsonNames("planRun") val planRun: BlogPlanRun? = null,
    /** Whether search volume and difficulty can be fetched on this deployment. */
    @SerialName("keyword_data_available") @JsonNames("keywordDataAvailable") val keywordDataAvailable: Boolean? = null,
    @SerialName("updated_at") @JsonNames("updatedAt") val updatedAt: Instant? = null,
)

/** The last keyword research run. [status] is one of [BlogPlanRunStatus]. */
@Serializable
public data class BlogPlanRun(
    val status: String? = null,
    val error: String? = null,
    /** New keywords the run found. */
    val added: Int? = null,
    @SerialName("finished_at") @JsonNames("finishedAt") val finishedAt: Instant? = null,
)

/**
 * What `blog.updateSettings` answers.
 *
 * [websiteSecret] is set only when the call set `websiteUrl` for the first time, and is never
 * shown again — store it then.
 */
public data class BlogSettingsUpdate(
    val settings: BlogSettings,
    val websiteSecret: String? = null,
)

/** A freshly issued website secret. The previous one stops verifying at once. */
@Serializable
public data class BlogWebsiteSecret(
    @SerialName("website_secret") @JsonNames("websiteSecret") val websiteSecret: String? = null,
)

/** The outcome of a test delivery to the website target. [error] explains a failed test. */
@Serializable
public data class BlogWebsiteTest(
    val ok: Boolean = false,
    /** The HTTP status the website answered, when it answered. */
    @SerialName("http_status") @JsonNames("httpStatus") val httpStatus: Int? = null,
    val error: String? = null,
)

/** Search terms suggested from the blog goal and topics, for editing before research. */
@Serializable
public data class BlogSeedSuggestions(
    val seeds: List<String> = emptyList(),
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
    @SerialName("search_volume") @JsonNames("searchVolume") val searchVolume: Int? = null,
    /** 0 to 100. */
    val difficulty: Int? = null,
    val cpc: Double? = null,
    val intent: String? = null,
    val source: String? = null,
    val status: String? = null,
    /** A calendar date, `YYYY-MM-DD`. */
    @SerialName("planned_for") @JsonNames("plannedFor") val plannedFor: String? = null,
    /** Why the keyword serves the blog goal. */
    @SerialName("fit_reason") @JsonNames("fitReason") val fitReason: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
)

/** An image placed in an article. [position] is absent on the featured image. */
@Serializable
public data class BlogArticleImage(
    @SerialName("media_id") @JsonNames("mediaId") val mediaId: String? = null,
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
    @SerialName("meta_title") @JsonNames("metaTitle") val metaTitle: String? = null,
    @SerialName("meta_description") @JsonNames("metaDescription") val metaDescription: String? = null,
    @SerialName("featured_image") @JsonNames("featuredImage") val featuredImage: BlogArticleImage? = null,
    val tags: List<String> = emptyList(),
    @SerialName("word_count") @JsonNames("wordCount") val wordCount: Int? = null,
    val status: String? = null,
    val origin: String? = null,
    @SerialName("review_until") @JsonNames("reviewUntil") val reviewUntil: Instant? = null,
    @SerialName("publish_at") @JsonNames("publishAt") val publishAt: Instant? = null,
    @SerialName("published_at") @JsonNames("publishedAt") val publishedAt: Instant? = null,
    @SerialName("published_url") @JsonNames("publishedUrl") val publishedUrl: String? = null,
    val error: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    @SerialName("updated_at") @JsonNames("updatedAt") val updatedAt: Instant? = null,
)

/** An article with its body, outline, brief and images: what `blog.getArticle` answers. */
@Serializable
public data class BlogArticleFull(
    val id: String? = null,
    val title: String? = null,
    val slug: String? = null,
    val keyword: String? = null,
    val excerpt: String? = null,
    @SerialName("meta_title") @JsonNames("metaTitle") val metaTitle: String? = null,
    @SerialName("meta_description") @JsonNames("metaDescription") val metaDescription: String? = null,
    @SerialName("featured_image") @JsonNames("featuredImage") val featuredImage: BlogArticleImage? = null,
    val tags: List<String> = emptyList(),
    @SerialName("word_count") @JsonNames("wordCount") val wordCount: Int? = null,
    val status: String? = null,
    val origin: String? = null,
    @SerialName("review_until") @JsonNames("reviewUntil") val reviewUntil: Instant? = null,
    @SerialName("publish_at") @JsonNames("publishAt") val publishAt: Instant? = null,
    @SerialName("published_at") @JsonNames("publishedAt") val publishedAt: Instant? = null,
    @SerialName("published_url") @JsonNames("publishedUrl") val publishedUrl: String? = null,
    val error: String? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    @SerialName("updated_at") @JsonNames("updatedAt") val updatedAt: Instant? = null,
    val instructions: String? = null,
    val outline: List<String> = emptyList(),
    /** The research the writer worked from. */
    val brief: JsonObject? = null,
    @SerialName("body_markdown") @JsonNames("bodyMarkdown") val bodyMarkdown: String? = null,
    @SerialName("body_html") @JsonNames("bodyHtml") val bodyHtml: String? = null,
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
