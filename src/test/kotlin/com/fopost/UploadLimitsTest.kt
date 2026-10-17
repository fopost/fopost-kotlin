package com.fopost

import com.fopost.param.ContentBlockInput
import com.fopost.param.CreatePostParams
import com.fopost.param.PersonConsent
import com.fopost.param.PresignUploadParams
import com.fopost.param.UpdatePostParams
import com.fopost.param.UploadFile
import java.io.File
import kotlin.test.assertEquals
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
import org.junit.jupiter.api.io.TempDir

class UploadLimitsTest {

    private lateinit var server: MockWebServer

    @TempDir
    lateinit var dir: File

    @BeforeEach
    fun start() {
        server = MockWebServer()
        server.start()
    }

    @AfterEach
    fun stop() {
        server.shutdown()
    }

    private fun limits(multipartMax: Long = 104857600) = json(
        200,
        """{"data":{"maxFileBytes":5368709120,"source":"add_on","connectedAccounts":3,"largeUploadsEnabled":true,
            "nextTier":null,"multipartMaxFileBytes":$multipartMax,"maxFilesPerUpload":5,"directUploadAvailable":true,
            "allowedTypes":["image/png","video/mp4"]}}""",
    )

    private val post = json(201, """{"data":{"id":"post_1","workspace_id":"ws_1","status":"draft","content":[]}}""")

    @Test
    fun `limits reads the account's ceilings`() = runTest {
        server.enqueue(
            json(
                200,
                """{"data":{"maxFileBytes":419430400,"source":"accounts","connectedAccounts":5,"largeUploadsEnabled":false,
                    "nextTier":{"upToAccounts":15,"maxFileBytes":2147483648},"multipartMaxFileBytes":104857600,
                    "maxFilesPerUpload":5,"directUploadAvailable":true,"allowedTypes":["image/png"]}}""",
            ),
        )

        val limits = server.client().use { it.media.limits("ws_1") }

        assertEquals(419430400L, limits.maxFileBytes)
        assertEquals("accounts", limits.source)
        assertEquals(5, limits.connectedAccounts)
        assertEquals(false, limits.largeUploadsEnabled)
        assertEquals(15, limits.nextTier?.upToAccounts)
        assertEquals(2147483648L, limits.nextTier?.maxFileBytes)
        assertEquals(104857600L, limits.multipartMaxFileBytes)
        assertEquals(5, limits.maxFilesPerUpload)
        assertEquals(listOf("image/png"), limits.allowedTypes)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/v1/media/limits?workspaceId=ws_1", request.path)
    }

    @Test
    fun `presign sends the optional fields and reads maxFileBytes`() = runTest {
        server.enqueue(
            json(201, """{"data":{"uploadId":"up_1","uploadUrl":"https://bucket.test/up_1","maxFileBytes":5368709120}}"""),
        )

        val slot = server.client().use {
            it.media.presign(
                PresignUploadParams(
                    "ws_1",
                    "team.jpg",
                    "image/jpeg",
                    3221225472L,
                    folderId = "fld_1",
                    brandRole = "person",
                    brandVoiceId = "bv_1",
                    personConsent = PersonConsent("Jane", "agreed on 2026-10-01"),
                ),
            )
        }

        assertEquals(5368709120L, slot.maxFileBytes)
        val body = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject
        assertEquals("3221225472", body["size"]!!.jsonPrimitive.content)
        assertEquals("fld_1", body["folderId"]!!.jsonPrimitive.content)
        assertEquals("person", body["brandRole"]!!.jsonPrimitive.content)
        assertEquals("bv_1", body["brandVoiceId"]!!.jsonPrimitive.content)
        assertEquals("Jane", body["personConsent"]!!.jsonObject["subject"]!!.jsonPrimitive.content)
        assertNull(body["origin"])
    }

    @Test
    fun `a 413 surfaces as FileTooLargeException with the ceiling`() = runTest {
        server.enqueue(
            json(
                413,
                """{"error":"file_too_large","message":"too big","max_file_bytes":104857600,"file_bytes":209715200,
                    "upgrade_url":"https://fopost.com/pricing"}""",
            ),
        )

        val failure = assertThrows<FileTooLargeException> {
            server.client().use { it.media.presign("ws_1", "big.mp4", "video/mp4", 209715200L) }
        }

        assertEquals(413, failure.status)
        assertEquals("file_too_large", failure.code)
        assertEquals(104857600L, failure.maxFileBytes)
        assertEquals(209715200L, failure.fileBytes)
        assertEquals("https://fopost.com/pricing", failure.upgradeUrl)
        assertEquals("104857600", failure.field("max_file_bytes")!!.jsonPrimitive.content)
    }

    @Test
    fun `create sends one multipart request when every file fits`() = runTest {
        server.enqueue(limits())
        server.enqueue(post)
        val file = File(dir, "chart.png").apply { writeBytes(byteArrayOf(1, 2, 3)) }

        server.client().use {
            it.posts.create(
                CreatePostParams(workspaceId = "ws_1", accounts = listOf("acc_1"), content = listOf(ContentBlockInput("Hi"))),
                files = listOf(UploadFile(file), UploadFile("clip.mp4", "video/mp4", byteArrayOf(9))),
            )
        }

        assertEquals("/v1/media/limits", server.takeRequest().path)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/v1/posts", request.path)
        assertTrue(request.getHeader("Content-Type")!!.startsWith("multipart/form-data"))
        val body = request.body.readUtf8()
        val postJson = Json.parseToJsonElement(partValue(body, "post")).jsonObject
        assertEquals("ws_1", postJson["workspace_id"]!!.jsonPrimitive.content)
        assertEquals("Hi", postJson["content"]!!.jsonArray[0].jsonObject["text"]!!.jsonPrimitive.content)
        assertTrue(body.contains("""name="files"; filename="chart.png""""))
        assertTrue(body.contains("Content-Type: image/png"))
        assertTrue(body.contains("""name="files"; filename="clip.mp4""""))
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `create uploads directly and attaches the media when a file is over the ceiling`() = runTest {
        val uploadUrl = server.url("/bucket/up_1").toString()
        server.enqueue(limits(multipartMax = 2))
        server.enqueue(json(201, """{"data":{"uploadId":"up_1","uploadUrl":"$uploadUrl","headers":{"Content-Type":"video/mp4"}}}"""))
        server.enqueue(json(200, ""))
        server.enqueue(json(201, """{"data":{"id":"m1","type":"video","name":"clip.mp4","url":"https://cdn.test/clip.mp4","size":3}}"""))
        server.enqueue(post)
        val file = File(dir, "clip.mp4").apply { writeBytes(byteArrayOf(1, 2, 3)) }

        server.client().use {
            it.posts.create(
                CreatePostParams(workspaceId = "ws_1", accounts = listOf("acc_1"), content = listOf(ContentBlockInput("Hi"))),
                files = listOf(UploadFile(file)),
            )
        }

        assertEquals("/v1/media/limits", server.takeRequest().path)
        assertEquals("/v1/media/presign", server.takeRequest().path)
        val put = server.takeRequest()
        assertEquals("PUT", put.method)
        assertEquals("3", put.getHeader("Content-Length"))
        assertEquals(listOf<Byte>(1, 2, 3), put.body.readByteArray().toList())
        assertEquals("/v1/media/presign/up_1/complete", server.takeRequest().path)

        val create = server.takeRequest()
        assertEquals("/v1/posts", create.path)
        assertEquals("application/json; charset=utf-8", create.getHeader("Content-Type"))
        val media = Json.parseToJsonElement(create.body.readUtf8()).jsonObject["content"]!!
            .jsonArray[0].jsonObject["media"]!!.jsonArray.single().jsonObject
        assertEquals("video", media["type"]!!.jsonPrimitive.content)
        assertEquals("clip.mp4", media["name"]!!.jsonPrimitive.content)
        assertEquals("https://cdn.test/clip.mp4", media["url"]!!.jsonPrimitive.content)
        assertEquals("3", media["size"]!!.jsonPrimitive.content)
    }

    @Test
    fun `update without content reads the post and appends to its first block`() = runTest {
        val uploadUrl = server.url("/bucket/up_1").toString()
        server.enqueue(limits(multipartMax = 1))
        server.enqueue(
            json(
                200,
                """{"data":{"id":"post_1","workspace_id":"ws_1","content":[
                    {"id":"b1","text":"First","media":[{"type":"image","url":"https://cdn.test/old.png"}]},
                    {"id":"b2","text":"Second"}]}}""",
            ),
        )
        server.enqueue(json(201, """{"data":{"uploadId":"up_1","uploadUrl":"$uploadUrl"}}"""))
        server.enqueue(json(200, ""))
        server.enqueue(json(201, """{"data":{"id":"m2","type":"image","name":"new.png","url":"https://cdn.test/new.png","size":2}}"""))
        server.enqueue(post)

        server.client().use {
            it.posts.update("post_1", UpdatePostParams(), files = listOf(UploadFile("new.png", "image/png", byteArrayOf(1, 2))))
        }

        assertEquals("/v1/media/limits", server.takeRequest().path)
        assertEquals("/v1/posts/post_1", server.takeRequest().path)
        server.takeRequest()
        server.takeRequest()
        server.takeRequest()
        val update = server.takeRequest()
        assertEquals("PUT", update.method)
        assertEquals("/v1/posts/post_1", update.path)
        val content = Json.parseToJsonElement(update.body.readUtf8()).jsonObject["content"]!!.jsonArray
        assertEquals(2, content.size)
        val urls = content[0].jsonObject["media"]!!.jsonArray.map { it.jsonObject["url"]!!.jsonPrimitive.content }
        assertEquals(listOf("https://cdn.test/old.png", "https://cdn.test/new.png"), urls)
        assertEquals("Second", content[1].jsonObject["text"]!!.jsonPrimitive.content)
    }

    private fun partValue(multipart: String, name: String): String {
        val start = multipart.indexOf("name=\"$name\"")
        val bodyStart = multipart.indexOf("\r\n\r\n", start) + 4
        val bodyEnd = multipart.indexOf("\r\n--", bodyStart)
        return multipart.substring(bodyStart, bodyEnd)
    }
}
