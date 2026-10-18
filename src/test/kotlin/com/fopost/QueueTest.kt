package com.fopost

import com.fopost.model.PostSchedule
import com.fopost.param.CreatePostParams
import com.fopost.param.CreateQueueSlotParams
import com.fopost.param.UpdateQueueSlotParams
import com.fopost.param.contentOf
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class QueueTest {

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

    private val slot = """{"id":"qs_1","workspace_id":"ws_1","account_id":null,"weekday":1,"time":"09:30",
        "timezone":"Europe/Zagreb","created_at":"2026-10-01T08:00:00Z"}"""

    @Test
    fun `lists, creates, updates and deletes slots`() = runTest {
        server.enqueue(json(200, """{"data":[$slot]}"""))
        server.enqueue(json(201, """{"data":$slot}"""))
        server.enqueue(
            json(
                200,
                """{"data":{"id":"qs_1","workspace_id":"ws_1","account_id":"acc_1","weekday":2,"time":"09:30",
                 "timezone":"Europe/Zagreb","moved":[{"id":"post_1","from":"2026-10-05T07:30:00Z","to":"2026-10-06T07:30:00Z"}]}}""",
            ),
        )
        server.enqueue(json(200, """{"data":{"deleted":true,"moved":[]}}"""))

        server.client().use { client ->
            val slots = client.queue.slots("ws_1")
            assertEquals("/v1/queue/slots?workspace_id=ws_1", server.takeRequest().path)
            assertEquals(1, slots.single().weekday)
            assertEquals("09:30", slots.single().time)
            assertNull(slots.single().accountId)

            val created = client.queue.createSlot(
                CreateQueueSlotParams(workspaceId = "ws_1", weekday = 1, time = "09:30", timezone = "Europe/Zagreb"),
            )
            val create = server.takeRequest()
            assertEquals("POST", create.method)
            assertEquals("/v1/queue/slots", create.path)
            val body = Json.parseToJsonElement(create.body.readUtf8()).jsonObject
            assertEquals("ws_1", body["workspace_id"]!!.jsonPrimitive.content)
            assertEquals("09:30", body["time"]!!.jsonPrimitive.content)
            assertNull(body["account_id"])
            assertEquals("qs_1", created.id)

            val updated = client.queue.updateSlot("qs_1", UpdateQueueSlotParams(weekday = 2, accountId = "acc_1"))
            val update = server.takeRequest()
            assertEquals("PUT", update.method)
            assertEquals("/v1/queue/slots/qs_1", update.path)
            assertEquals("""{"account_id":"acc_1","weekday":2}""", update.body.readUtf8())
            assertEquals("post_1", updated.moved.single().id)
            assertEquals(Instant.parse("2026-10-06T07:30:00Z"), updated.moved.single().to)

            val deleted = client.queue.deleteSlot("qs_1")
            val delete = server.takeRequest()
            assertEquals("DELETE", delete.method)
            assertEquals("/v1/queue/slots/qs_1", delete.path)
            assertEquals(true, deleted.deleted)
            assertTrue(deleted.moved.isEmpty())
        }
    }

    @Test
    fun `reads the next slot and a preview, and queues a post`() = runTest {
        server.enqueue(json(200, """{"data":{"next":"2026-10-06T07:30:00Z"}}"""))
        server.enqueue(json(200, """{"data":["2026-10-06T07:30:00Z","2026-10-07T07:30:00Z"]}"""))
        server.enqueue(json(201, """{"data":{"id":"post_1","status":"scheduled","schedule_at":"2026-10-06T07:30:00Z"}}"""))

        server.client().use { client ->
            val next = client.queue.next("ws_1", accountId = "acc_1")
            assertEquals("/v1/queue/next?workspace_id=ws_1&account_id=acc_1", server.takeRequest().path)
            assertEquals(Instant.parse("2026-10-06T07:30:00Z"), next.next)

            val preview = client.queue.preview("ws_1", count = 2)
            assertEquals("/v1/queue/preview?workspace_id=ws_1&count=2", server.takeRequest().path)
            assertEquals(listOf(Instant.parse("2026-10-06T07:30:00Z"), Instant.parse("2026-10-07T07:30:00Z")), preview)

            val post = client.posts.create(
                CreatePostParams(
                    workspaceId = "ws_1",
                    accounts = listOf("acc_1"),
                    content = contentOf("Queued from Kotlin"),
                    schedule = PostSchedule.QUEUE,
                ),
            )
            val body = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject
            assertEquals("queue", body["schedule"]!!.jsonPrimitive.content)
            assertEquals(Instant.parse("2026-10-06T07:30:00Z"), post.scheduleAt)
        }
    }
}
