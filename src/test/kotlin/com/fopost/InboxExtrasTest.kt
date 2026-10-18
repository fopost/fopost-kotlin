package com.fopost

import com.fopost.param.InboxAnalyticsParams
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InboxExtrasTest {

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
    fun `unpublishes a post`() = runTest {
        server.enqueue(
            json(
                200,
                """{"data":{"post_status":"unpublished","deliveries":[{"account_id":"acc_1","platform":"twitter","removed":true},
                 {"account_id":"acc_2","platform":"linkedin","removed":false}]}}""",
            ),
        )

        server.client().use { client ->
            val result = client.posts.unpublish("post_1")
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/v1/posts/post_1/unpublish", request.path)
            assertEquals("unpublished", result.postStatus)
            assertEquals(listOf(true, false), result.deliveries.map { it.removed })
            assertEquals("linkedin", result.deliveries[1].platform)
        }
    }

    @Test
    fun `reads inbox analytics`() = runTest {
        server.enqueue(
            json(
                200,
                """{"data":{"totals":{"received":40,"sent":25,"answered":20,"open":5,"conversations":12,"median_response_minutes":42.5},
                 "deltas":{"received":10,"sent":null,"answered":-5},
                 "series":[{"date":"2026-10-18","received":4,"sent":2,"answered":2}],
                 "by_platform":[{"platform":"twitter","received":30,"sent":20}],
                 "by_type":[{"type":"dm","count":12}],
                 "by_account":[{"account_id":"acc_1","platform":"twitter","username":"yourbrand","name":"Your Brand","avatar":null,"received":30,"sent":20}],
                 "response_time":[{"bucket":"under_1h","count":15}],
                 "heatmap":[{"weekday":1,"hour":9,"count":6}],"time_zone":"Europe/Zagreb"}}""",
            ),
        )

        server.client().use { client ->
            val analytics = client.analytics.inbox(InboxAnalyticsParams(workspaceId = "ws_1", days = 30, tz = "Europe/Zagreb"))
            assertEquals("/v1/analytics/inbox?workspace_id=ws_1&days=30&tz=Europe%2FZagreb", server.takeRequest().path)
            assertEquals(42.5, analytics.totals?.medianResponseMinutes)
            assertNull(analytics.deltas?.sent)
            assertEquals(-5.0, analytics.deltas?.answered)
            assertEquals("dm", analytics.byType.single().type)
            assertEquals("yourbrand", analytics.byAccount.single().username)
            assertEquals("under_1h", analytics.responseTime.single().bucket)
            assertEquals(6, analytics.heatmap.single().count)
            assertEquals("Europe/Zagreb", analytics.timeZone)
        }
    }

    @Test
    fun `deletes a conversation and downloads an attachment`() = runTest {
        server.enqueue(MockResponse().setResponseCode(204))
        val bytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "image/png")
                .setBody(Buffer().write(bytes)),
        )

        server.client().use { client ->
            client.inbox.deleteConversation("conv_1", accountId = "acc_1")
            val delete = server.takeRequest()
            assertEquals("DELETE", delete.method)
            assertEquals("/v1/inbox/conversations/conv_1?account_id=acc_1", delete.path)

            val attachment = client.inbox.attachment("ib_1", 0, variant = "preview")
            val get = server.takeRequest()
            assertEquals("GET", get.method)
            assertEquals("/v1/inbox/ib_1/attachments/0?variant=preview", get.path)
            assertEquals("fp_test", get.getHeader("X-API-Key"))
            assertContentEquals(bytes, attachment)
        }
    }
}
