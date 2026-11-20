package com.fopost

import java.io.File
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class FontsTest {

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

    private val uploaded = """{"id":"f1","family":"Brand Sans","style":"italic","weight":700,"format":"woff2",
        "file_url":"https://api.test/v1/fonts/f1/file","created_at":"2026-11-01T10:00:00Z"}"""

    @Test
    fun `list decodes the library and the uploads`() = runTest {
        server.enqueue(
            json(
                200,
                """{"library":[{"slug":"geist","family":"Geist","category":"sans-serif","scripts":["latin"],
                    "weights":[400,700],"files":[{"path":"geist/latin.woff2","subset":"latin","weight":400,
                    "style":"normal","unicode_range":"U+0000-00FF"}]}],"uploaded":[$uploaded]}""",
            ),
        )

        val fonts = server.client().use { it.fonts.list("ws_1") }

        val lib = fonts.library!!.single()
        assertEquals("geist", lib.slug)
        assertEquals(listOf(400, 700), lib.weights)
        assertEquals("U+0000-00FF", lib.files!!.single().unicodeRange)
        val own = fonts.uploaded!!.single()
        assertEquals("f1", own.id)
        assertEquals("https://api.test/v1/fonts/f1/file", own.fileUrl)
        assertEquals(Instant.parse("2026-11-01T10:00:00Z"), own.createdAt)

        val req = server.takeRequest()
        assertEquals("GET", req.method)
        assertEquals("/v1/fonts?workspace_id=ws_1", req.path)
    }

    @Test
    fun `upload sends the file and fields as multipart`() = runTest {
        server.enqueue(json(201, uploaded))

        val font = server.client().use {
            it.fonts.upload("ws_1", "brand.woff2", byteArrayOf(1, 2, 3), family = "Brand Sans", weight = 700, style = "italic")
        }

        assertEquals("f1", font.id)
        assertEquals(700, font.weight)
        assertEquals("italic", font.style)
        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/v1/fonts", req.path)
        assertTrue(req.getHeader("Content-Type")!!.startsWith("multipart/form-data"))
        val body = req.body.readUtf8()
        assertTrue(body.contains("name=\"workspace_id\"\r\nContent-Length: 4\r\n\r\nws_1"))
        assertTrue(body.contains("name=\"file\"; filename=\"brand.woff2\""))
        assertTrue(body.contains("name=\"family\""))
        assertTrue(body.contains("Brand Sans"))
        assertTrue(body.contains("name=\"weight\""))
        assertTrue(body.contains("name=\"style\""))
    }

    @Test
    fun `upload from a file leaves unset fields out`(@TempDir dir: File) = runTest {
        server.enqueue(json(201, uploaded))
        val file = File(dir, "brand.ttf").apply { writeBytes(byteArrayOf(9, 9)) }

        server.client().use { it.fonts.upload("ws_1", file) }

        val body = server.takeRequest().body.readUtf8()
        assertTrue(body.contains("filename=\"brand.ttf\""))
        assertFalse(body.contains("name=\"family\""))
        assertFalse(body.contains("name=\"weight\""))
        assertFalse(body.contains("name=\"style\""))
    }

    @Test
    fun `delete sends a DELETE and accepts an empty 204`() = runTest {
        server.enqueue(MockResponse().setResponseCode(204))

        server.client().use { it.fonts.delete("f1") }

        val req = server.takeRequest()
        assertEquals("DELETE", req.method)
        assertEquals("/v1/fonts/f1", req.path)
    }
}
