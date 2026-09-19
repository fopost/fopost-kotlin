package com.fopost

import com.fopost.param.ValidateLengthParams
import com.fopost.param.ValidateMediaInput
import com.fopost.param.ValidatePostParams
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

class ValidateTest {

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
    fun `validates a draft and parses the per-platform verdicts`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":{"ready":false,"platforms":[
                  {"platform":"twitter","ready":false,"issues":["over_length"],"score":42,
                   "signals":[{"level":"warn","code":"over_length","message":"Too long"}]},
                  {"platform":"bluesky","ready":true,"issues":[],"signals":[]}
                ]}}
                """.trimIndent(),
            ),
        )

        val result = server.client().use { client ->
            client.validate.post(
                ValidatePostParams(
                    platforms = listOf("twitter", "bluesky"),
                    content = "Hello",
                    media = listOf(
                        ValidateMediaInput(url = "https://example.com/a.png", mimeType = "image/png", size = 1234),
                    ),
                ),
            )
        }

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/v1/validate/post", request.path)
        val body = Json.parseToJsonElement(request.body.readUtf8()).jsonObject
        assertEquals("Hello", body["content"]!!.jsonPrimitive.content)
        assertEquals(listOf("twitter", "bluesky"), body["platforms"]!!.jsonArray.map { it.jsonPrimitive.content })
        val media = body["media"]!!.jsonArray.single().jsonObject
        assertEquals("https://example.com/a.png", media["url"]!!.jsonPrimitive.content)
        assertEquals("image/png", media["mime_type"]!!.jsonPrimitive.content)
        assertEquals(1234, media["size"]!!.jsonPrimitive.content.toInt())

        assertEquals(false, result.ready)
        assertEquals(2, result.platforms.size)
        assertEquals("twitter", result.platforms[0].platform)
        assertEquals(listOf("over_length"), result.platforms[0].issues)
        assertEquals(42.0, result.platforms[0].score)
        assertEquals("warn", result.platforms[0].signals.single().level)
        assertEquals(true, result.platforms[1].ready)
        assertNull(result.platforms[1].score)
    }

    @Test
    fun `measures text length per platform`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":{"ok":false,"platforms":[
                  {"platform":"twitter","length":300,"limit":280,"unit":"chars","ok":false,
                   "signals":[{"level":"warn","code":"over_length","message":"20 over"}]},
                  {"platform":"linkedin","length":300,"limit":null,"unit":"chars","ok":true,"signals":[]}
                ]}}
                """.trimIndent(),
            ),
        )

        val result = server.client().use { client ->
            client.validate.length(
                ValidateLengthParams(text = "x".repeat(300), platforms = listOf("twitter", "linkedin")),
            )
        }

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/v1/validate/length", request.path)
        val body = Json.parseToJsonElement(request.body.readUtf8()).jsonObject
        assertEquals(300, body["text"]!!.jsonPrimitive.content.length)
        assertEquals(listOf("twitter", "linkedin"), body["platforms"]!!.jsonArray.map { it.jsonPrimitive.content })

        assertFalse(result.ok!!)
        assertEquals(280, result.platforms[0].limit)
        assertEquals("chars", result.platforms[0].unit)
        assertEquals("over_length", result.platforms[0].signals.single().code)
        assertNull(result.platforms[1].limit)
        assertTrue(result.platforms[1].ok!!)
    }

    @Test
    fun `checks a file by url`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":{"ok":true,"issues":[],"name":"a.png","size":1234,"mime_type":"image/png","type":"image"}}
                """.trimIndent(),
            ),
        )

        val result = server.client().use { client -> client.validate.media("https://example.com/a.png") }

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/v1/validate/media", request.path)
        val body = Json.parseToJsonElement(request.body.readUtf8()).jsonObject
        assertEquals("https://example.com/a.png", body["url"]!!.jsonPrimitive.content)
        assertEquals(setOf("url"), body.keys)

        assertTrue(result.ok!!)
        assertEquals("image/png", result.mimeType)
        assertEquals("image", result.type)
        assertEquals(1234L, result.size)
        assertTrue(result.issues.isEmpty())
    }
}
