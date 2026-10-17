package com.fopost.param

import kotlinx.serialization.Serializable

/**
 * The body of `blog.updateSettings`. Only what is set is sent.
 *
 * Setting [websiteUrl] for the first time issues a website secret, returned once on the result.
 */
@Serializable
public data class UpdateBlogSettingsParams(
    val siteUrl: String? = null,
    val topic: String? = null,
    val audience: String? = null,
    val language: String? = null,
    val locationCode: Int? = null,
    /** 0 to 7; 0 turns planning off. */
    val cadencePerWeek: Int? = null,
    /** 0 to 14. */
    val reviewHoldDays: Int? = null,
    val autoPublish: Boolean? = null,
    /** `draft`, `standard` or `best`. */
    val imageTier: String? = null,
    /** 0 to 3. */
    val inBodyImages: Int? = null,
    val minWords: Int? = null,
    val maxWords: Int? = null,
    val brandVoiceId: String? = null,
    /** One of [com.fopost.model.BlogTargetKind]. */
    val targetKind: String? = null,
    val targetAccountId: String? = null,
    /** Where published articles are POSTed as a signed webhook. */
    val websiteUrl: String? = null,
)

/** Filters for listing keywords. */
public data class BlogKeywordListParams(
    /** One or more of [com.fopost.model.BlogKeywordStatus], comma-separated. */
    val status: String? = null,
    val limit: Int? = null,
) {
    public fun toQuery(): Map<String, Any?> = mapOf(
        "status" to status,
        "limit" to limit,
    )
}

/** The body of `blog.planKeywords`. Unset seeds let the plan start from the settings' topic. */
@Serializable
public data class PlanBlogKeywordsParams(
    val seeds: List<String>? = null,
)

/** The body of `blog.addKeyword`. [plannedFor] is a calendar date, `YYYY-MM-DD`. */
@Serializable
public data class CreateBlogKeywordParams(
    val keyword: String,
    val plannedFor: String? = null,
)

/** The body of `blog.updateKeyword`. Only what is set is sent. */
@Serializable
public data class UpdateBlogKeywordParams(
    /** One of [com.fopost.model.BlogKeywordStatus]. */
    val status: String? = null,
    /** A calendar date, `YYYY-MM-DD`. */
    val plannedFor: String? = null,
)

/** Filters for listing articles. */
public data class BlogArticleListParams(
    /** One or more of [com.fopost.model.BlogArticleStatus], comma-separated. */
    val status: String? = null,
    val limit: Int? = null,
) {
    public fun toQuery(): Map<String, Any?> = mapOf(
        "status" to status,
        "limit" to limit,
    )
}

/** The body of `blog.requestArticle`. One of [title], [keyword] or [keywordId] is required. */
@Serializable
public data class CreateBlogArticleParams(
    val title: String? = null,
    val keyword: String? = null,
    /** A keyword from the plan; its term becomes the target. */
    val keywordId: String? = null,
    /** Up to 4000 characters of direction for the writer. */
    val instructions: String? = null,
)

/** The body of `blog.updateArticle`. Only what is set is sent, and only a draft can be edited. */
@Serializable
public data class UpdateBlogArticleParams(
    val title: String? = null,
    /** Lowercase letters, digits and hyphens. */
    val slug: String? = null,
    val excerpt: String? = null,
    val metaTitle: String? = null,
    val metaDescription: String? = null,
    val bodyMarkdown: String? = null,
    val tags: List<String>? = null,
    /** ISO 8601. Puts the draft on the calendar. */
    val publishAt: String? = null,
)
