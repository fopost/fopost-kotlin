package com.fopost

import com.fopost.model.ActivityKinds
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ActivityTest {

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
    fun `reads the audit log and keeps the cursor`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":[{"id":"evt_1","workspace_id":"ws_1","kind":"security",
                  "ref_type":"member_removed","ref_id":"usr_2","summary":"Removed sam@example.com",
                  "actor":{"type":"user","name":"Ada"},"time":"2026-09-20T10:00:00Z"}],
                 "meta":{"next_cursor":"42"}}
                """.trimIndent(),
            ),
        )

        val page = server.client().use { client ->
            client.activity.list(workspaceId = "ws_1", kind = ActivityKinds.SECURITY, limit = 1)
        }

        assertEquals("/v1/activity?workspace_id=ws_1&kind=security&limit=1", server.takeRequest().path)
        val event = page.single()
        assertEquals("member_removed", event.refType)
        assertEquals("Ada", event.actor?.name)
        assertEquals("42", page.meta.nextCursor)
    }

    @Test
    fun `the end of the list is a null cursor`() = runTest {
        server.enqueue(json(200, """{"data":[],"meta":{"next_cursor":null}}"""))

        val page = server.client().use { client -> client.activity.list() }

        assertTrue(page.isEmpty())
        assertNull(page.meta.nextCursor)
    }
}
