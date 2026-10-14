package com.fopost

import com.fopost.model.BlogArticleStatus
import com.fopost.model.BlogKeywordStatus
import com.fopost.model.BlogTargetKind
import com.fopost.param.BlogArticleListParams
import com.fopost.param.BlogKeywordListParams
import com.fopost.param.CreateBlogArticleParams
import com.fopost.param.CreateBlogKeywordParams
import com.fopost.param.PlanBlogKeywordsParams
import com.fopost.param.UpdateBlogArticleParams
import com.fopost.param.UpdateBlogKeywordParams
import com.fopost.param.UpdateBlogSettingsParams
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BlogTest {

    private lateinit var server: MockWebServer

    private val settings = """
        {"siteUrl":"https://yourbrand.com","topic":"Social scheduling","audience":"Marketers",
         "language":"en","locationCode":2840,"cadencePerWeek":2,"reviewHoldDays":3,
         "autoPublish":false,"imageTier":"standard","inBodyImages":1,"minWords":900,"maxWords":1500,
         "brandVoiceId":null,"targetKind":"website","targetAccountId":null,"targetPlatform":null,
         "websiteUrl":"https://yourbrand.com/hooks/fopost","hasWebsiteSecret":true,
         "keywordDataAvailable":true,"updatedAt":"2026-10-11T09:00:00Z"}
    """.trimIndent()

    private val keyword = """
        {"id":"kw_1","keyword":"social media calendar","searchVolume":5400,"difficulty":42,
         "cpc":1.25,"intent":"informational","source":"provider","status":"planned",
         "plannedFor":"2026-10-20","createdAt":"2026-10-11T09:00:00Z"}
    """.trimIndent()

    private val article = """
        {"id":"ar_1","title":"How to build a social media calendar","slug":"social-media-calendar",
         "keyword":"social media calendar","excerpt":"A calendar that holds.","metaTitle":null,
         "metaDescription":null,"featuredImage":{"mediaId":"md_1","url":"https://cdn.example/f.jpg","alt":"A calendar"},
         "tags":["planning"],"wordCount":1200,"status":"draft","origin":"plan","reviewUntil":"2026-10-14T09:00:00Z",
         "publishAt":null,"publishedAt":null,"publishedUrl":null,"error":null,
         "createdAt":"2026-10-11T09:00:00Z","updatedAt":"2026-10-11T10:00:00Z"}
    """.trimIndent()

    @BeforeEach
    fun start() {
        server = MockWebServer()
        server.start()
    }

    @AfterEach
    fun stop() {
        server.shutdown()
    }

    @Test
    fun `settings travel by query on GET and in the body on PATCH`() = runTest {
        server.enqueue(json(200, """{"data":$settings}"""))
        server.enqueue(json(200, """{"data":$settings,"websiteSecret":"whsec_first"}"""))

        server.client().use {
            val current = it.blog.getSettings("w1")
            assertEquals(BlogTargetKind.WEBSITE, current.targetKind)
            assertEquals(true, current.hasWebsiteSecret)

            val updated = it.blog.updateSettings(
                "w1",
                UpdateBlogSettingsParams(websiteUrl = "https://yourbrand.com/hooks/fopost", cadencePerWeek = 2),
            )
            assertEquals("whsec_first", updated.websiteSecret)
            assertEquals(2, updated.settings.cadencePerWeek)
        }

        val get = server.takeRequest()
        assertEquals("GET", get.method)
        assertEquals("/v1/blog/settings?workspaceId=w1", get.path)

        val patch = server.takeRequest()
        assertEquals("PATCH", patch.method)
        assertEquals("/v1/blog/settings", patch.path)
        val body = Json.parseToJsonElement(patch.body.readUtf8()).jsonObject
        assertEquals("w1", body["workspaceId"]?.jsonPrimitive?.content)
        assertEquals("https://yourbrand.com/hooks/fopost", body["websiteUrl"]?.jsonPrimitive?.content)
        assertEquals(2, body["cadencePerWeek"]?.jsonPrimitive?.content?.toInt())
        // An unset field stays out of the patch, so the server keeps its own value.
        assertFalse(body.containsKey("topic"))
    }

    @Test
    fun `the website secret rotates and the website test reads a 502 verdict`() = runTest {
        server.enqueue(json(200, """{"data":{"websiteSecret":"whsec_next"}}"""))
        server.enqueue(json(502, """{"data":{"ok":false,"status":500,"error":"Website answered 500"}}"""))

        server.client(maxRetries = 1).use {
            assertEquals("whsec_next", it.blog.rotateWebsiteSecret("w1").websiteSecret)
            val test = it.blog.testWebsite("w1")
            assertFalse(test.ok)
            assertEquals(500, test.status)
            assertEquals("Website answered 500", test.error)
        }

        val rotate = server.takeRequest()
        assertEquals("/v1/blog/settings/website-secret", rotate.path)
        assertEquals("w1", Json.parseToJsonElement(rotate.body.readUtf8()).jsonObject["workspaceId"]?.jsonPrimitive?.content)
        assertEquals("/v1/blog/settings/website-test", server.takeRequest().path)
    }

    @Test
    fun `keywords list, plan, add, update and delete`() = runTest {
        server.enqueue(json(200, """{"data":[$keyword]}"""))
        server.enqueue(json(202, """{"data":{"status":"queued"}}"""))
        server.enqueue(json(201, """{"data":$keyword}"""))
        server.enqueue(json(200, """{"data":$keyword}"""))
        server.enqueue(json(204, ""))

        server.client().use {
            val listed = it.blog.listKeywords("w1", BlogKeywordListParams(status = "planned,suggested", limit = 10))
            assertEquals(5400, listed.single().searchVolume)
            assertEquals(BlogKeywordStatus.PLANNED, listed.single().status)
            assertEquals("2026-10-20", listed.single().plannedFor)

            assertEquals("queued", it.blog.planKeywords("w1", PlanBlogKeywordsParams(seeds = listOf("scheduling"))).status)
            assertEquals("kw_1", it.blog.addKeyword("w1", CreateBlogKeywordParams("social media calendar", "2026-10-20")).id)
            assertEquals("kw_1", it.blog.updateKeyword("w1", "kw_1", UpdateBlogKeywordParams(status = "dismissed")).id)
            it.blog.deleteKeyword("w1", "kw_1")
        }

        val list = server.takeRequest()
        assertEquals("/v1/blog/keywords?workspaceId=w1&status=planned%2Csuggested&limit=10", list.path)

        val plan = server.takeRequest()
        assertEquals("POST", plan.method)
        assertEquals("/v1/blog/keywords/plan", plan.path)
        val planBody = Json.parseToJsonElement(plan.body.readUtf8()).jsonObject
        assertEquals("w1", planBody["workspaceId"]?.jsonPrimitive?.content)
        assertEquals("scheduling", planBody["seeds"]?.jsonArray?.single()?.jsonPrimitive?.content)

        val add = server.takeRequest()
        assertEquals("/v1/blog/keywords", add.path)
        val addBody = Json.parseToJsonElement(add.body.readUtf8()).jsonObject
        assertEquals("w1", addBody["workspaceId"]?.jsonPrimitive?.content)
        assertEquals("social media calendar", addBody["keyword"]?.jsonPrimitive?.content)
        assertEquals("2026-10-20", addBody["plannedFor"]?.jsonPrimitive?.content)

        val update = server.takeRequest()
        assertEquals("PATCH", update.method)
        assertEquals("/v1/blog/keywords/kw_1?workspaceId=w1", update.path)
        val updateBody = Json.parseToJsonElement(update.body.readUtf8()).jsonObject
        assertEquals("dismissed", updateBody["status"]?.jsonPrimitive?.content)
        assertFalse(updateBody.containsKey("workspaceId"))

        val delete = server.takeRequest()
        assertEquals("DELETE", delete.method)
        assertEquals("/v1/blog/keywords/kw_1?workspaceId=w1", delete.path)
    }

    @Test
    fun `articles list, request, get, update and act`() = runTest {
        val full = article.trimEnd('}') + ""","instructions":null,"outline":["Why","How"],
            "brief":{"sources":2},"bodyMarkdown":"# Hello","bodyHtml":"<h1>Hello</h1>",
            "images":[{"mediaId":"md_2","url":"https://cdn.example/b.jpg","alt":"Board","position":1}]}"""
        server.enqueue(json(200, """{"data":[$article]}"""))
        server.enqueue(json(202, """{"data":$article}"""))
        server.enqueue(json(200, """{"data":$full}"""))
        server.enqueue(json(200, """{"data":$full}"""))
        server.enqueue(json(202, """{"data":$article}"""))
        server.enqueue(json(202, """{"data":$article}"""))
        server.enqueue(json(200, """{"data":$article}"""))
        server.enqueue(json(204, ""))

        server.client().use {
            val listed = it.blog.listArticles("w1", BlogArticleListParams(status = "draft", limit = 5))
            assertEquals(BlogArticleStatus.DRAFT, listed.single().status)
            assertEquals("A calendar", listed.single().featuredImage?.alt)

            val requested = it.blog.requestArticle("w1", CreateBlogArticleParams(keywordId = "kw_1", instructions = "Short."))
            assertEquals("ar_1", requested.id)

            val got = it.blog.getArticle("w1", "ar_1")
            assertEquals("# Hello", got.bodyMarkdown)
            assertEquals(listOf("Why", "How"), got.outline)
            assertEquals(1, got.images.single().position)
            assertEquals("2", got.brief?.get("sources")?.jsonPrimitive?.content)

            it.blog.updateArticle("w1", "ar_1", UpdateBlogArticleParams(title = "New title", tags = listOf("planning")))
            it.blog.regenerateArticle("w1", "ar_1")
            it.blog.publishArticle("w1", "ar_1")
            it.blog.rejectArticle("w1", "ar_1")
            it.blog.deleteArticle("w1", "ar_1")
        }

        assertEquals("/v1/blog/articles?workspaceId=w1&status=draft&limit=5", server.takeRequest().path)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/v1/blog/articles", request.path)
        val requestBody = Json.parseToJsonElement(request.body.readUtf8()).jsonObject
        assertEquals("w1", requestBody["workspaceId"]?.jsonPrimitive?.content)
        assertEquals("kw_1", requestBody["keywordId"]?.jsonPrimitive?.content)
        assertEquals("Short.", requestBody["instructions"]?.jsonPrimitive?.content)
        assertFalse(requestBody.containsKey("title"))

        assertEquals("/v1/blog/articles/ar_1?workspaceId=w1", server.takeRequest().path)

        val update = server.takeRequest()
        assertEquals("PATCH", update.method)
        assertEquals("/v1/blog/articles/ar_1?workspaceId=w1", update.path)
        val updateBody = Json.parseToJsonElement(update.body.readUtf8()).jsonObject
        assertEquals("New title", updateBody["title"]?.jsonPrimitive?.content)
        assertEquals("planning", updateBody["tags"]?.jsonArray?.single()?.jsonPrimitive?.content)

        for (action in listOf("regenerate", "publish", "reject")) {
            val sent = server.takeRequest()
            assertEquals("POST", sent.method)
            assertEquals("/v1/blog/articles/ar_1/$action?workspaceId=w1", sent.path)
        }
        val delete = server.takeRequest()
        assertEquals("DELETE", delete.method)
        assertEquals("/v1/blog/articles/ar_1?workspaceId=w1", delete.path)
    }

    @Test
    fun `an empty balance is a 402 with the upgrade url and a missing target a 409`() = runTest {
        server.enqueue(
            json(402, """{"error":"insufficient_credits","message":"balance ran out","upgrade_url":"https://fopost.com/billing"}"""),
        )
        server.enqueue(json(409, """{"error":"no_target","message":"no target set"}"""))

        server.client(maxRetries = 1).use { client ->
            val payment = assertThrows<PaymentRequiredException> {
                client.blog.requestArticle("w1", CreateBlogArticleParams(title = "Hello"))
            }
            assertEquals("insufficient_credits", payment.code)
            assertEquals("https://fopost.com/billing", payment.upgradeUrl)

            val conflict = assertThrows<ApiException> { client.blog.publishArticle("w1", "ar_1") }
            assertEquals(409, conflict.status)
            assertEquals("no_target", conflict.code)
            assertNull(conflict.rateLimit)
            assertTrue(conflict.toString().contains("no_target"))
        }
    }
}
