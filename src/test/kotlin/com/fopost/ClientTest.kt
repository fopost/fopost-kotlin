package com.fopost

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ClientTest {

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

    @Test
    fun `authenticates with the api key header, not a bearer token`() = runTest {
        server.enqueue(json(200, """{"data":[]}"""))

        server.client().use { it.workspaces.list() }

        val request = server.takeRequest()
        assertEquals("fp_test", request.getHeader("X-API-Key"))
        assertEquals(null, request.getHeader("Authorization"))
        assertEquals("application/json", request.getHeader("Accept"))
        assertTrue(request.getHeader("User-Agent")!!.startsWith("fopost-kotlin/"))
        assertEquals("/v1/workspaces", request.path)
    }

    @Test
    fun `prefixes a caller-supplied user agent`() = runTest {
        server.enqueue(json(200, """{"data":[]}"""))

        FoPost(apiKey = "fp_test", baseUrl = server.url("/v1").toString(), userAgent = "acme/2.0")
            .use { it.workspaces.list() }

        assertEquals("acme/2.0 fopost-kotlin/${FoPost.VERSION}", server.takeRequest().getHeader("User-Agent"))
    }

    @Test
    fun `refuses to build without a key`() {
        assertThrows<IllegalArgumentException> { FoPost(apiKey = "   ", baseUrl = "https://example.test") }
    }

    @Test
    fun `unwraps the data envelope and passes a bare body through`() = runTest {
        server.enqueue(json(200, """{"data":{"id":"post_1","status":"draft"}}"""))
        server.enqueue(json(200, """{"updated":2,"action":"delete"}"""))

        server.client().use { client ->
            assertEquals("post_1", client.posts.get("post_1").id)
            assertEquals(2, client.posts.bulkDelete("ws_1", listOf("post_1", "post_2")).updated)
        }
    }

    @Test
    fun `the escape hatch sends an authenticated request and returns the raw body`() = runTest {
        server.enqueue(json(200, """{"data":{"ok":true}}"""))

        val body = server.client().use { client ->
            client.request("GET", "/platforms", query = mapOf("days" to 30, "ids" to listOf("a", "b")))
        }

        val request = server.takeRequest()
        assertEquals("fp_test", request.getHeader("X-API-Key"))
        assertEquals("/v1/platforms?days=30&ids=a&ids=b", request.path)
        assertEquals("true", body.jsonObjectData()["ok"]!!.jsonPrimitive.content)
    }

    private fun kotlinx.serialization.json.JsonElement.jsonObjectData() =
        (this as kotlinx.serialization.json.JsonObject)["data"] as kotlinx.serialization.json.JsonObject
}
