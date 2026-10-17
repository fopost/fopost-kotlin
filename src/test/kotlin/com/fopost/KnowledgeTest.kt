package com.fopost

import com.fopost.param.CreateKnowledgeSourceParams
import com.fopost.param.UpdateKnowledgeSourceParams
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * The knowledge base: the path, the query casing, the snake_case request body,
 * and that a camelCase response decodes into the model.
 */
class KnowledgeTest {

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

    private val source = """
        {"id":"know_1","kind":"url","title":"Refund policy","status":"ready","statusMessage":null,
         "url":"https://yourbrand.com/help/refunds","mediaId":null,"brandVoiceId":null,
         "chunkCount":3,"content":null,"lastSyncedAt":"2026-09-20T00:00:00Z",
         "createdAt":"2026-09-19T00:00:00Z","updatedAt":"2026-09-20T00:00:00Z"}
    """.trimIndent()

    @Test
    fun `lists sources for one workspace and reads the camelCase fields`() = runTest {
        server.enqueue(json(200, """{"data":[$source]}"""))

        val sources = server.client().use { it.knowledge.list(workspaceId = "ws_1") }

        assertEquals("/v1/knowledge/sources?workspace_id=ws_1", server.takeRequest().path)
        val only = sources.single()
        assertEquals("know_1", only.id)
        assertEquals("ready", only.status)
        assertEquals(3, only.chunkCount)
        assertNull(only.statusMessage)
        assertEquals(Instant.parse("2026-09-20T00:00:00Z"), only.lastSyncedAt)
    }

    @Test
    fun `creates a source with a snake_case body, omitting what the kind does not use`() = runTest {
        server.enqueue(json(200, """{"data":$source}"""))

        server.client().use {
            it.knowledge.create(
                CreateKnowledgeSourceParams(
                    kind = "file",
                    title = "Price list",
                    mediaId = "media_1",
                    workspaceId = "ws_1",
                ),
            )
        }

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/v1/knowledge/sources", request.path)

        val body = Json.parseToJsonElement(request.body.readUtf8()).jsonObject
        assertEquals("file", body["kind"]?.jsonPrimitive?.content)
        assertEquals("media_1", body["media_id"]?.jsonPrimitive?.content)
        assertEquals("ws_1", body["workspace_id"]?.jsonPrimitive?.content)
        // Nothing the kind does not use reaches the wire.
        assertNull(body["url"])
        assertNull(body["content"])
    }

    @Test
    fun `patches only the fields that were set`() = runTest {
        server.enqueue(json(200, """{"data":$source}"""))

        server.client().use {
            it.knowledge.update("know_1", UpdateKnowledgeSourceParams(title = "Refunds"))
        }

        val request = server.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/v1/knowledge/sources/know_1", request.path)
        assertEquals("""{"title":"Refunds"}""", request.body.readUtf8())
    }

    @Test
    fun `searches with top_k and reads the matches`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":[{"sourceId":"know_1","sourceTitle":"Refund policy","sourceKind":"url",
                  "sourceUrl":"https://yourbrand.com/help/refunds",
                  "text":"We refund within 30 days.","score":0.82}]}
                """.trimIndent(),
            ),
        )

        val matches = server.client().use { it.knowledge.search("refunds", topK = 3) }

        assertEquals("/v1/knowledge/search?q=refunds&top_k=3", server.takeRequest().path)
        val match = matches.single()
        assertEquals("Refund policy", match.sourceTitle)
        assertEquals(0.82, match.score)
    }

    @Test
    fun `syncs a source through its own path`() = runTest {
        server.enqueue(json(200, """{"data":{"id":"know_1","status":"pending"}}"""))

        val queued = server.client().use { it.knowledge.sync("know_1") }

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/v1/knowledge/sources/know_1/sync", request.path)
        assertEquals("pending", queued.status)
    }
}
