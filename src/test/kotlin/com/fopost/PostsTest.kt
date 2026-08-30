package com.fopost

import com.fopost.model.MediaItem
import com.fopost.param.ContentBlockInput
import com.fopost.param.CreatePostParams
import com.fopost.param.PostListParams
import com.fopost.param.PublishParams
import com.fopost.param.contentOf
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PostsTest {

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
    fun `creates a scheduled post and publishes it`() = runTest {
        server.enqueue(
            json(
                201,
                """
                {"data":{"id":"post_1","workspace_id":"ws_1","status":"scheduled",
                 "schedule_at":"2026-09-01T10:00:00Z",
                 "content":[{"id":"b1","text":"Hello from Kotlin","position":0}],
                 "accounts":[{"id":"acc_1","platform":"twitter","publish_status":"pending"}]}}
                """.trimIndent(),
            ),
        )
        server.enqueue(
            json(
                200,
                """{"data":{"post_status":"publishing","deliveries":[{"id":"d1","accountId":"acc_1","status":"queued"}]}}""",
            ),
        )

        server.client().use { client ->
            val post = client.posts.create(
                CreatePostParams(
                    workspaceId = "ws_1",
                    accounts = listOf("acc_1"),
                    content = contentOf("Hello from Kotlin"),
                ).scheduledAt(Instant.parse("2026-09-01T10:00:00Z")),
            )

            assertEquals("post_1", post.id)
            assertEquals("scheduled", post.status)
            assertEquals(Instant.parse("2026-09-01T10:00:00Z"), post.scheduleAt)
            assertEquals("Hello from Kotlin", post.content.single().text)
            assertEquals("twitter", post.accounts.single().platform)

            val result = client.posts.publish(post.id!!, PublishParams(accountIds = listOf("acc_1")))
            assertEquals("publishing", result.postStatus)
            assertEquals("queued", result.deliveries.single().status)
            assertEquals("acc_1", result.deliveries.single().accountId)
        }

        val create = server.takeRequest()
        assertEquals("POST", create.method)
        assertEquals("/v1/posts", create.path)
        val body = Json.parseToJsonElement(create.body.readUtf8()).jsonObject
        assertEquals("ws_1", body["workspace_id"]!!.jsonPrimitive.content)
        assertEquals("scheduled", body["status"]!!.jsonPrimitive.content)
        assertEquals("2026-09-01T10:00:00Z", body["schedule_at"]!!.jsonPrimitive.content)
        assertEquals(listOf("acc_1"), body["accounts"]!!.jsonArray.map { it.jsonPrimitive.content })
        assertEquals("Hello from Kotlin", body["content"]!!.jsonArray.single().jsonObject["text"]!!.jsonPrimitive.content)
        // An unset parameter is never sent, so the server keeps its own default.
        assertNull(body["title"])

        val publish = server.takeRequest()
        assertEquals("/v1/posts/post_1/publish", publish.path)
        assertEquals(
            listOf("acc_1"),
            Json.parseToJsonElement(publish.body.readUtf8()).jsonObject["accountIds"]!!
                .jsonArray.map { it.jsonPrimitive.content },
        )
    }

    @Test
    fun `reads the pagination meta and walks every page`() = runTest {
        val firstPage = """
            {"data":[{"id":"post_1"},{"id":"post_2"}],
             "meta":{"current_page":1,"per_page":2,"total":3,"last_page":2,"from":1,"to":2}}
        """.trimIndent()
        val secondPage =
            """{"data":[{"id":"post_3"}],"meta":{"current_page":2,"per_page":2,"total":3,"last_page":2}}"""

        // One for list, then one per page the flow walks.
        server.enqueue(json(200, firstPage))
        server.enqueue(json(200, firstPage))
        server.enqueue(json(200, secondPage))

        server.client().use { client ->
            val first = client.posts.list(PostListParams(workspaceId = "ws_1", perPage = 2))
            assertEquals(3, first.meta?.total)
            assertEquals(2, first.meta?.lastPage)
            assertEquals(listOf("post_1", "post_2"), first.map { it.id })

            val all = client.posts.listAll(PostListParams(workspaceId = "ws_1", perPage = 2)).toList()
            assertEquals(listOf("post_1", "post_2", "post_3"), all.map { it.id })
        }

        assertEquals("/v1/posts?workspace_id=ws_1&per_page=2", server.takeRequest().path)
        assertEquals("/v1/posts?workspace_id=ws_1&page=1&per_page=2", server.takeRequest().path)
        assertEquals("/v1/posts?workspace_id=ws_1&page=2&per_page=2", server.takeRequest().path)
    }

    @Test
    fun `attaches an uploaded file to a content block`() = runTest {
        server.enqueue(json(200, """{"data":[{"id":"m1","type":"image","name":"chart.png","url":"https://cdn.test/chart.png"}]}"""))

        val uploaded = server.client().use { client ->
            client.media.upload("ws_1", "chart.png", byteArrayOf(1, 2, 3), "image/png")
        }

        assertEquals("https://cdn.test/chart.png", uploaded?.url)
        assertEquals(
            ContentBlockInput("Numbers are in", listOf(MediaItem("image", "chart.png", "https://cdn.test/chart.png"))),
            ContentBlockInput("Numbers are in", listOf(uploaded!!.toMediaItem())),
        )

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/v1/media/upload", request.path)
        assertTrue(request.getHeader("Content-Type")!!.startsWith("multipart/form-data; boundary="))
        val body = request.body.readUtf8()
        assertTrue(body.contains("""name="files"; filename="chart.png""""), body)
        assertTrue(body.contains("Content-Type: image/png"), body)
        assertTrue(body.contains("""name="workspaceId""""), body)
    }

    @Test
    fun `keeps free-form settings as json`() = runTest {
        server.enqueue(json(200, """{"data":{"id":"post_1","settings":{"twitter":{"thread":true}}}}"""))

        val post = server.client().use { it.posts.get("post_1") }

        assertEquals(
            true,
            (post.settings as JsonObject)["twitter"]!!.jsonObject["thread"]!!.jsonPrimitive.content.toBoolean(),
        )
    }
}
