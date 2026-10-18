package com.fopost

import com.fopost.param.LogListParams
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LogsTest {

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
    fun `reads a page of the request log with its summary`() = runTest {
        server.enqueue(
            json(
                200,
                """{"data":[{"id":41,"workspace_id":"ws_1","time":"2026-10-18T12:00:00Z","auth_method":"api_key",
                 "method":"POST","route":"/v1/posts/{id}/publish","path":"/v1/posts/post_1/publish","status_code":402,
                 "duration_ms":35,"platform":null,"message":"Balance ran out"}],
                 "meta":{"next_cursor":null,"has_more":false},
                 "summary":{"since":"2026-10-11T12:00:00Z","total":120,"errors":3,
                 "buckets":[{"at":"2026-10-18T00:00:00Z","total":10,"errors":1}]}}""",
            ),
        )

        server.client().use { client ->
            val page = client.logs.list(LogListParams(workspaceId = "ws_1", days = 7, status = "failed", limit = 50))
            val request = server.takeRequest()
            assertEquals("GET", request.method)
            assertEquals("/v1/logs?workspace_id=ws_1&days=7&status=failed&limit=50", request.path)
            val entry = page.single()
            assertEquals(41L, entry.id)
            assertEquals("/v1/posts/{id}/publish", entry.route)
            assertEquals(402, entry.statusCode)
            assertEquals("Balance ran out", entry.message)
            assertEquals(Instant.parse("2026-10-18T12:00:00Z"), entry.time)
            assertNull(page.meta?.nextCursor)
            assertEquals(false, page.meta?.hasMore)
            assertEquals(3, page.summary?.errors)
            assertEquals(10, page.summary?.buckets?.single()?.total)
        }
    }
}
