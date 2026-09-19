package com.fopost

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
import org.junit.jupiter.api.assertThrows

class MediaTest {

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
    fun `uploadDirect presigns, puts the bytes, then completes`() = runTest {
        val uploadUrl = server.url("/bucket/up_1?sig=abc").toString()
        server.enqueue(
            json(
                201,
                """{"data":{"uploadId":"up_1","uploadUrl":"$uploadUrl","method":"PUT",
                    "headers":{"Content-Type":"image/png"},"expiresAt":"2026-09-19T10:00:00Z"}}""",
            ),
        )
        server.enqueue(json(200, ""))
        server.enqueue(
            json(
                201,
                """{"data":{"id":"m1","type":"image","name":"chart.png","url":"https://cdn.test/chart.png",
                    "previewUrl":"https://api.test/v1/media/m1/file","size":3}}""",
            ),
        )

        val item = server.client().use { client ->
            client.media.uploadDirect("ws_1", "chart.png", "image/png", byteArrayOf(1, 2, 3))
        }

        assertEquals("m1", item.id)
        assertEquals("https://cdn.test/chart.png", item.url)

        val presign = server.takeRequest()
        assertEquals("POST", presign.method)
        assertEquals("/v1/media/presign", presign.path)
        assertEquals("fp_test", presign.getHeader("X-API-Key"))
        val body = Json.parseToJsonElement(presign.body.readUtf8()).jsonObject
        assertEquals("ws_1", body["workspaceId"]!!.jsonPrimitive.content)
        assertEquals("chart.png", body["filename"]!!.jsonPrimitive.content)
        assertEquals("image/png", body["mimeType"]!!.jsonPrimitive.content)
        assertEquals("3", body["size"]!!.jsonPrimitive.content)

        val put = server.takeRequest()
        assertEquals("PUT", put.method)
        assertEquals("/bucket/up_1?sig=abc", put.path)
        assertEquals("image/png", put.getHeader("Content-Type"))
        assertEquals("3", put.getHeader("Content-Length"))
        assertNull(put.getHeader("X-API-Key"))
        assertEquals(listOf<Byte>(1, 2, 3), put.body.readByteArray().toList())

        val complete = server.takeRequest()
        assertEquals("POST", complete.method)
        assertEquals("/v1/media/presign/up_1/complete", complete.path)
        assertEquals("fp_test", complete.getHeader("X-API-Key"))
        assertEquals(0L, complete.bodySize)
    }

    @Test
    fun `a rejected PUT raises before complete is called`() = runTest {
        val uploadUrl = server.url("/bucket/up_1").toString()
        server.enqueue(json(201, """{"data":{"uploadId":"up_1","uploadUrl":"$uploadUrl","method":"PUT","headers":{"Content-Type":"image/png"}}}"""))
        server.enqueue(json(403, """{"error":"forbidden","message":"signature expired"}"""))

        assertThrows<PermissionDeniedException> {
            server.client().use { it.media.uploadDirect("ws_1", "chart.png", "image/png", byteArrayOf(1)) }
        }

        assertEquals(2, server.requestCount)
    }
}
