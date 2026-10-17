package com.fopost.resource

import com.fopost.ServerException
import com.fopost.internal.ApiClient
import com.fopost.model.BlogArticle
import com.fopost.model.BlogArticleFull
import com.fopost.model.BlogKeyword
import com.fopost.model.BlogPlanQueued
import com.fopost.model.BlogSettings
import com.fopost.model.BlogSettingsUpdate
import com.fopost.model.BlogWebsiteSecret
import com.fopost.model.BlogWebsiteTest
import com.fopost.param.BlogArticleListParams
import com.fopost.param.BlogKeywordListParams
import com.fopost.param.CreateBlogArticleParams
import com.fopost.param.CreateBlogKeywordParams
import com.fopost.param.PlanBlogKeywordsParams
import com.fopost.param.UpdateBlogArticleParams
import com.fopost.param.UpdateBlogKeywordParams
import com.fopost.param.UpdateBlogSettingsParams
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import okhttp3.RequestBody

/**
 * The blog: a keyword plan, articles written from it, and where they are published.
 *
 * An article goes to a website, never to a social network. Reads, settings and keyword edits
 * need the `posts` scope; [planKeywords], [requestArticle] and [regenerateArticle] spend AI
 * credits and need `ai`; [publishArticle] needs `publish`.
 *
 * Every route is scoped by `workspaceId`, which travels as a query parameter or a body field as
 * the endpoint requires.
 */
public class BlogResource internal constructor(private val http: ApiClient) {

    // Settings

    public suspend fun getSettings(workspaceId: String): BlogSettings =
        http.call("GET", "/blog/settings", BlogSettings.serializer(), query = workspaceQuery(workspaceId))

    /**
     * Changes the settings. The result carries `websiteSecret` only when this call set
     * `websiteUrl` for the first time; it is never shown again.
     */
    public suspend fun updateSettings(workspaceId: String, params: UpdateBlogSettingsParams): BlogSettingsUpdate =
        http.call(
            "PATCH",
            "/blog/settings",
            BlogSettingsUpdate.serializer(),
            workspaceBody(workspaceId, params, UpdateBlogSettingsParams.serializer()),
            unwrap = false,
        )

    /** Issues a new website secret. The previous one stops verifying at once. */
    public suspend fun rotateWebsiteSecret(workspaceId: String): BlogWebsiteSecret =
        http.call(
            "POST",
            "/blog/settings/website-secret",
            BlogWebsiteSecret.serializer(),
            workspaceBody(workspaceId),
        )

    /** Sends a signed `article.test` delivery to the website target and reports what it answered. */
    public suspend fun testWebsite(workspaceId: String): BlogWebsiteTest =
        try {
            http.call(
                "POST",
                "/blog/settings/website-test",
                BlogWebsiteTest.serializer(),
                workspaceBody(workspaceId),
            )
        } catch (e: ServerException) {
            // A failed test answers 502 with the same body, so the verdict is still readable.
            val data = (e.body as? JsonObject)?.get("data") ?: throw e
            http.decodeElement(BlogWebsiteTest.serializer(), data)
        }

    // Keywords

    public suspend fun listKeywords(
        workspaceId: String,
        params: BlogKeywordListParams = BlogKeywordListParams(),
    ): List<BlogKeyword> =
        http.callList("GET", "/blog/keywords", BlogKeyword.serializer(), query = workspaceQuery(workspaceId) + params.toQuery())

    /**
     * Researches keywords in the background and adds the suggestions to the plan. Spends AI
     * credits; a `402` means the balance ran out.
     */
    public suspend fun planKeywords(
        workspaceId: String,
        params: PlanBlogKeywordsParams = PlanBlogKeywordsParams(),
    ): BlogPlanQueued =
        http.call(
            "POST",
            "/blog/keywords/plan",
            BlogPlanQueued.serializer(),
            workspaceBody(workspaceId, params, PlanBlogKeywordsParams.serializer()),
        )

    public suspend fun addKeyword(workspaceId: String, params: CreateBlogKeywordParams): BlogKeyword =
        http.call(
            "POST",
            "/blog/keywords",
            BlogKeyword.serializer(),
            workspaceBody(workspaceId, params, CreateBlogKeywordParams.serializer()),
        )

    public suspend fun updateKeyword(
        workspaceId: String,
        keywordId: String,
        params: UpdateBlogKeywordParams,
    ): BlogKeyword =
        http.call(
            "PATCH",
            "/blog/keywords/$keywordId",
            BlogKeyword.serializer(),
            http.jsonBody(params, UpdateBlogKeywordParams.serializer()),
            workspaceQuery(workspaceId),
        )

    public suspend fun deleteKeyword(workspaceId: String, keywordId: String) {
        http.send("DELETE", "/blog/keywords/$keywordId", null, workspaceQuery(workspaceId))
    }

    // Articles

    public suspend fun listArticles(
        workspaceId: String,
        params: BlogArticleListParams = BlogArticleListParams(),
    ): List<BlogArticle> =
        http.callList("GET", "/blog/articles", BlogArticle.serializer(), query = workspaceQuery(workspaceId) + params.toQuery())

    /**
     * Queues an article. It comes back `queued` and moves to `draft` once written. Spends AI
     * credits; a `402` means the balance ran out.
     */
    public suspend fun requestArticle(workspaceId: String, params: CreateBlogArticleParams): BlogArticle =
        http.call(
            "POST",
            "/blog/articles",
            BlogArticle.serializer(),
            workspaceBody(workspaceId, params, CreateBlogArticleParams.serializer()),
        )

    public suspend fun getArticle(workspaceId: String, articleId: String): BlogArticleFull =
        http.call("GET", "/blog/articles/$articleId", BlogArticleFull.serializer(), query = workspaceQuery(workspaceId))

    /** Edits a draft. An article that is not editable answers `409`. */
    public suspend fun updateArticle(
        workspaceId: String,
        articleId: String,
        params: UpdateBlogArticleParams,
    ): BlogArticleFull =
        http.call(
            "PATCH",
            "/blog/articles/$articleId",
            BlogArticleFull.serializer(),
            http.jsonBody(params, UpdateBlogArticleParams.serializer()),
            workspaceQuery(workspaceId),
        )

    /** Writes the article again from its brief. Spends AI credits. */
    public suspend fun regenerateArticle(workspaceId: String, articleId: String): BlogArticle =
        http.call("POST", "/blog/articles/$articleId/regenerate", BlogArticle.serializer(), query = workspaceQuery(workspaceId))

    /**
     * Publishes a draft to the configured target. Answers `409` with `no_target` when the
     * settings name no target. Needs the `publish` scope.
     */
    public suspend fun publishArticle(workspaceId: String, articleId: String): BlogArticle =
        http.call("POST", "/blog/articles/$articleId/publish", BlogArticle.serializer(), query = workspaceQuery(workspaceId))

    /** Takes a draft out of review without publishing it. */
    public suspend fun rejectArticle(workspaceId: String, articleId: String): BlogArticle =
        http.call("POST", "/blog/articles/$articleId/reject", BlogArticle.serializer(), query = workspaceQuery(workspaceId))

    public suspend fun deleteArticle(workspaceId: String, articleId: String) {
        http.send("DELETE", "/blog/articles/$articleId", null, workspaceQuery(workspaceId))
    }

    private fun workspaceQuery(workspaceId: String): Map<String, Any?> = mapOf("workspaceId" to workspaceId)

    private fun workspaceBody(workspaceId: String): RequestBody =
        http.jsonBody(JsonObject(mapOf("workspaceId" to JsonPrimitive(workspaceId))))

    private fun <T> workspaceBody(workspaceId: String, params: T, serializer: SerializationStrategy<T>): RequestBody {
        val fields = http.json.encodeToJsonElement(serializer, params).jsonObject
        return http.jsonBody(JsonObject(fields + ("workspaceId" to JsonPrimitive(workspaceId))))
    }
}
