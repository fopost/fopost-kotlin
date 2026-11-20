package com.fopost

import com.fopost.param.BulkDesignParams
import com.fopost.param.CreateDesignParams
import com.fopost.param.DesignFromPostParams
import com.fopost.param.ExportDesignParams
import com.fopost.param.TextToSlidesParams
import com.fopost.param.UpdateDesignParams
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DesignsTest {

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

    private fun RecordedRequest.json(): JsonObject = Json.parseToJsonElement(body.readUtf8()).jsonObject

    private val design = """{"id":"d1","name":"Launch","kind":"story","width":1080,"height":1920,
        "document":{"pages":[{"layers":[]}]},"thumbnail_media_id":"m9","thumbnail_url":"https://t.test/d1.png",
        "created_at":"2026-11-01T10:00:00Z","updated_at":"2026-11-02T10:00:00Z"}"""

    @Test
    fun `list sends the filters and decodes designs`() = runTest {
        server.enqueue(json(200, """{"data":[$design]}"""))

        val designs = server.client().use { it.designs.list("ws_1", kind = "story", limit = 10, offset = 20) }

        assertEquals(1, designs.size)
        val d = designs.first()
        assertEquals("d1", d.id)
        assertEquals("story", d.kind)
        assertEquals(1080, d.width)
        assertEquals("m9", d.thumbnailMediaId)
        assertEquals("https://t.test/d1.png", d.thumbnailUrl)
        assertEquals(Instant.parse("2026-11-02T10:00:00Z"), d.updatedAt)
        assertTrue(d.document!!.containsKey("pages"))

        val req = server.takeRequest()
        assertEquals("GET", req.method)
        assertEquals("/v1/designs?workspace_id=ws_1&kind=story&limit=10&offset=20", req.path)
    }

    @Test
    fun `create posts the document through untouched`() = runTest {
        server.enqueue(json(201, """{"data":$design}"""))
        val document = buildJsonObject { put("version", 1) }

        val created = server.client().use {
            it.designs.create(CreateDesignParams("ws_1", "story", name = "Launch", document = document))
        }

        assertEquals("d1", created.id)
        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/v1/designs", req.path)
        val body = req.json()
        assertEquals("ws_1", body["workspace_id"]!!.jsonPrimitive.content)
        assertEquals("story", body["kind"]!!.jsonPrimitive.content)
        assertEquals("Launch", body["name"]!!.jsonPrimitive.content)
        assertEquals(1, body["document"]!!.jsonObject["version"]!!.jsonPrimitive.int)
        assertFalse(body.containsKey("brand_voice_id"))
    }

    @Test
    fun `get reads one design`() = runTest {
        server.enqueue(json(200, """{"data":$design}"""))

        val d = server.client().use { it.designs.get("d1") }

        assertEquals("Launch", d.name)
        val req = server.takeRequest()
        assertEquals("GET", req.method)
        assertEquals("/v1/designs/d1", req.path)
    }

    @Test
    fun `update answers the design and the version beside it`() = runTest {
        server.enqueue(
            json(
                200,
                """{"data":$design,"version":{"id":"v2","version":2,"created_at":"2026-11-02T10:00:00Z"}}""",
            ),
        )

        val saved = server.client().use {
            it.designs.update("d1", UpdateDesignParams(name = "Renamed", createVersion = true))
        }

        assertEquals("d1", saved.design!!.id)
        assertEquals("v2", saved.version!!.id)
        assertEquals(2, saved.version!!.version)
        val req = server.takeRequest()
        assertEquals("PATCH", req.method)
        assertEquals("/v1/designs/d1", req.path)
        val body = req.json()
        assertEquals("Renamed", body["name"]!!.jsonPrimitive.content)
        assertEquals("true", body["create_version"]!!.jsonPrimitive.content)
        assertFalse(body.containsKey("document"))
    }

    @Test
    fun `update without a version decodes a null version`() = runTest {
        server.enqueue(json(200, """{"data":$design,"version":null}"""))

        val saved = server.client().use { it.designs.update("d1", UpdateDesignParams(name = "X")) }

        assertEquals("d1", saved.design!!.id)
        assertNull(saved.version)
    }

    @Test
    fun `delete answers the deleted id`() = runTest {
        server.enqueue(json(200, """{"data":{"id":"d1","deleted":true}}"""))

        val result = server.client().use { it.designs.delete("d1") }

        assertEquals("d1", result.id)
        assertEquals(true, result.deleted)
        val req = server.takeRequest()
        assertEquals("DELETE", req.method)
        assertEquals("/v1/designs/d1", req.path)
    }

    @Test
    fun `versions asks for documents only when told to`() = runTest {
        server.enqueue(json(200, """{"data":[{"id":"v1","version":1,"document":{"pages":[]},"created_at":"2026-11-01T10:00:00Z"}]}"""))
        server.enqueue(json(200, """{"data":[]}"""))

        server.client().use { client ->
            val versions = client.designs.versions("d1", includeDocument = true)
            assertEquals(1, versions.first().version)
            assertTrue(versions.first().document!!.containsKey("pages"))
            client.designs.versions("d1")
        }

        assertEquals("/v1/designs/d1/versions?include=document", server.takeRequest().path)
        assertEquals("/v1/designs/d1/versions", server.takeRequest().path)
    }

    @Test
    fun `restoreVersion posts to the version and answers the saved design`() = runTest {
        server.enqueue(json(200, """{"data":$design,"version":{"id":"v3","version":3}}"""))

        val saved = server.client().use { it.designs.restoreVersion("d1", "v1") }

        assertEquals("v3", saved.version!!.id)
        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/v1/designs/d1/versions/v1/restore", req.path)
    }

    @Test
    fun `duplicate sends the name when given`() = runTest {
        server.enqueue(json(201, """{"data":$design}"""))
        server.enqueue(json(201, """{"data":$design}"""))

        server.client().use { client ->
            client.designs.duplicate("d1", name = "Copy")
            client.designs.duplicate("d1")
        }

        val named = server.takeRequest()
        assertEquals("POST", named.method)
        assertEquals("/v1/designs/d1/duplicate", named.path)
        assertEquals("Copy", named.json()["name"]!!.jsonPrimitive.content)
        assertEquals("{}", server.takeRequest().body.readUtf8())
    }

    @Test
    fun `export sends the format and decodes the files`() = runTest {
        server.enqueue(
            json(
                200,
                """{"data":{"format":"png","files":[{"media_id":"m1","page_index":0,"mime_type":"image/png",
                    "preview_url":"https://api.test/v1/media/m1/file"}]}}""",
            ),
        )

        val result = server.client().use {
            it.designs.export("d1", ExportDesignParams("png", pageIndexes = listOf(0, 2)))
        }

        assertEquals("png", result.format)
        val file = result.files!!.single()
        assertEquals("m1", file.mediaId)
        assertEquals(0, file.pageIndex)
        assertEquals("image/png", file.mimeType)
        assertEquals("https://api.test/v1/media/m1/file", file.previewUrl)
        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/v1/designs/d1/export", req.path)
        val body = req.json()
        assertEquals("png", body["format"]!!.jsonPrimitive.content)
        assertEquals(listOf(0, 2), body["page_indexes"]!!.jsonArray.map { it.jsonPrimitive.int })
        assertFalse(body.containsKey("duration_sec"))
    }

    @Test
    fun `bulk sends the rows and answers the new designs`() = runTest {
        server.enqueue(json(201, """{"data":[$design,$design]}"""))

        val created = server.client().use {
            it.designs.bulk(
                "d1",
                BulkDesignParams(listOf(mapOf("title" to "A"), mapOf("title" to "B")), nameColumn = "title"),
            )
        }

        assertEquals(2, created.size)
        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/v1/designs/d1/bulk", req.path)
        val body = req.json()
        assertEquals("B", body["rows"]!!.jsonArray[1].jsonObject["title"]!!.jsonPrimitive.content)
        assertEquals("title", body["name_column"]!!.jsonPrimitive.content)
    }

    @Test
    fun `posts lists the posts using a design`() = runTest {
        server.enqueue(
            json(
                200,
                """{"data":[{"id":"p1","status":"published","title":null,"text":"Hi","schedule_at":null,
                    "published_at":"2026-11-03T09:00:00Z","platforms":["x"],"engagement":12,"views":340}]}""",
            ),
        )

        val posts = server.client().use { it.designs.posts("d1") }

        val p = posts.single()
        assertEquals("p1", p.id)
        assertEquals("published", p.status)
        assertEquals(Instant.parse("2026-11-03T09:00:00Z"), p.publishedAt)
        assertEquals(listOf("x"), p.platforms)
        assertEquals(340L, p.views)
        val req = server.takeRequest()
        assertEquals("GET", req.method)
        assertEquals("/v1/designs/d1/posts", req.path)
    }

    @Test
    fun `textToSlides sends the text and decodes the slides`() = runTest {
        server.enqueue(
            json(
                200,
                """{"data":{"cover":{"title":"T","subtitle":"S"},"slides":[{"heading":"H","body":"B"}],
                    "cta":{"heading":"Follow","body":"More"},"brand":{"colors":["#000"],"fonts":{"heading":"Geist"},
                    "logo_media_id":null}}}""",
            ),
        )

        val slides = server.client().use {
            it.designs.textToSlides(TextToSlidesParams("ws_1", text = "Long text", maxSlides = 5))
        }

        assertEquals("T", slides.cover!!.title)
        assertEquals("H", slides.slides!!.single().heading)
        assertEquals("Follow", slides.cta!!.heading)
        assertEquals("Geist", slides.brand!!.fonts!!.heading)
        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/v1/designs/text-to-slides", req.path)
        val body = req.json()
        assertEquals("ws_1", body["workspace_id"]!!.jsonPrimitive.content)
        assertEquals("Long text", body["text"]!!.jsonPrimitive.content)
        assertEquals(5, body["max_slides"]!!.jsonPrimitive.int)
        assertFalse(body.containsKey("post_id"))
    }

    @Test
    fun `fromPost sends the post and answers a design`() = runTest {
        server.enqueue(json(201, """{"data":$design}"""))

        val d = server.client().use {
            it.designs.fromPost(DesignFromPostParams("ws_1", postId = "p1", style = "quote", carousel = true))
        }

        assertEquals("d1", d.id)
        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/v1/designs/from-post", req.path)
        val body = req.json()
        assertEquals("p1", body["post_id"]!!.jsonPrimitive.content)
        assertEquals("quote", body["style"]!!.jsonPrimitive.content)
        assertEquals("true", body["carousel"]!!.jsonPrimitive.content)
    }
}
