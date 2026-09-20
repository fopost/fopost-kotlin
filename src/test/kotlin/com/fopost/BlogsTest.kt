package com.fopost

import com.fopost.param.ArticleParams
import com.fopost.param.ProductParams
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BlogsTest {

    private lateinit var server: MockWebServer

    @BeforeEach
    fun start() {
        server = MockWebServer()
        server.start()
    }

    @AfterEach
    fun stop() {
        server.shutdown()
    }

    private val article = """
        {"id":"99","blog_id":"11","title":"Spring drop","body_html":"<p>Hello</p>",
         "excerpt":"A short summary","status":"published","author_name":"Store Owner",
         "tags":["news"],"image_url":null,
         "url":"https://demo.myshopify.com/blogs/article/spring-drop",
         "published_at":"2026-09-01T10:00:00.000Z","updated_at":null}
    """.trimIndent()

    @Test
    fun `lists every blog on the site`() = runTest {
        server.enqueue(json(200, """{"data":[{"id":"11","title":"News","handle":"news","url":null}]}"""))

        val blogs = server.client().use { it.blogs.listBlogs("a1") }

        assertEquals("/v1/accounts/a1/blogs", server.takeRequest().path)
        assertEquals("11", blogs.single().id)
        assertEquals("News", blogs.single().title)
    }

    @Test
    fun `lists articles with the filters`() = runTest {
        server.enqueue(json(200, """{"data":[$article]}"""))

        val articles = server.client().use {
            it.blogs.listArticles("a1", "11", limit = 5, status = "draft", q = "spring")
        }

        assertEquals(
            "/v1/accounts/a1/blogs/11/articles?limit=5&status=draft&q=spring",
            server.takeRequest().path,
        )
        assertEquals("99", articles.single().id)
        assertEquals(listOf("news"), articles.single().tags)
        assertEquals("Store Owner", articles.single().authorName)
    }

    /**
     * The article id is in the path and only the fields set on the params are
     * sent, which is what stops an edit from creating a second post on the site.
     */
    @Test
    fun `updates the live article in place`() = runTest {
        server.enqueue(json(200, """{"data":$article}"""))

        server.client().use {
            it.blogs.updateArticle("a1", "11", "99", ArticleParams(title = "Spring drop, restocked"))
        }

        server.takeRequest().let {
            assertEquals("PATCH", it.method)
            assertEquals("/v1/accounts/a1/blogs/11/articles/99", it.path)
            assertEquals("""{"title":"Spring drop, restocked"}""", it.body.readUtf8())
        }
    }

    @Test
    fun `creates an article with only what it was given`() = runTest {
        server.enqueue(json(200, """{"data":$article}"""))

        server.client().use {
            it.blogs.createArticle("a1", "11", ArticleParams(title = "Spring drop", body = "Hello", status = "draft"))
        }

        server.takeRequest().let {
            assertEquals("POST", it.method)
            assertEquals("""{"title":"Spring drop","body":"Hello","status":"draft"}""", it.body.readUtf8())
        }
    }

    @Test
    fun `deletes an article by its own id`() = runTest {
        server.enqueue(json(204, ""))

        server.client().use { it.blogs.deleteArticle("a1", "11", "99") }

        server.takeRequest().let {
            assertEquals("DELETE", it.method)
            assertEquals("/v1/accounts/a1/blogs/11/articles/99", it.path)
        }
    }

    @Test
    fun `updates a product with only what changed`() = runTest {
        server.enqueue(
            json(
                200,
                """{"data":{"id":"7","title":"Mug XL","handle":"mug","status":"draft","description":null,
                   "vendor":null,"product_type":"Drinkware","tags":[],"image_url":null,"url":null,
                   "price":"12.00","currency":"USD","updated_at":null}}""".trimIndent(),
            ),
        )

        val product = server.client().use {
            it.blogs.updateProduct("a1", "7", ProductParams(title = "Mug XL", productType = "Drinkware"))
        }

        server.takeRequest().let {
            assertEquals("PATCH", it.method)
            assertEquals("""{"title":"Mug XL","product_type":"Drinkware"}""", it.body.readUtf8())
        }
        assertEquals("12.00", product.price)
    }
}
