package com.fopost

import com.fopost.model.AudienceFilter
import com.fopost.model.SequenceStep
import com.fopost.param.BroadcastListParams
import com.fopost.param.CreateBroadcastParams
import com.fopost.param.CreateSequenceParams
import com.fopost.param.EnrollParams
import com.fopost.param.RecipientListParams
import kotlin.test.assertEquals
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

class BroadcastsTest {

    private lateinit var server: MockWebServer

    private val broadcast = """
        {"id":"bc_1","name":"September check-in","text":"New colours just landed.",
         "account_id":"acc_1","audience":{"platforms":["instagram"]},
         "status":"sent","scheduled_at":null,
         "sent_at":"2026-09-19T10:04:00Z","created_at":"2026-09-19T09:58:00Z",
         "counts":{"total":3,"sent":2,"skipped":1,"failed":0,"pending":0}}
    """.trimIndent()

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
    fun `list pages on pagination, not meta, and keeps the counts`() = runTest {
        server.enqueue(
            json(200, """{"data":[$broadcast],"pagination":{"page":1,"per_page":25,"total":1}}"""),
        )

        val page = server.client().use {
            it.broadcasts.list(BroadcastListParams(workspaceId = "w1", status = "sent"))
        }

        val request = server.takeRequest()
        assertEquals("/v1/broadcasts?workspace_id=w1&status=sent", request.path)
        assertEquals(1, page.meta?.total)

        val found = page.data.single()
        assertEquals("September check-in", found.name)
        assertEquals(2, found.counts?.sent)
        assertEquals(1, found.counts?.skipped)
    }

    @Test
    fun `create sends the snake_case body`() = runTest {
        server.enqueue(json(200, """{"data":$broadcast}"""))

        server.client().use {
            it.broadcasts.create(
                CreateBroadcastParams(
                    workspaceId = "w1",
                    accountId = "acc_1",
                    name = "September check-in",
                    text = "New colours just landed.",
                    audience = AudienceFilter(platforms = listOf("instagram")),
                ),
            )
        }

        val body = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject
        assertEquals("w1", body["workspace_id"]?.jsonPrimitive?.content)
        assertEquals("acc_1", body["account_id"]?.jsonPrimitive?.content)
        assertEquals(
            "instagram",
            body["audience"]?.jsonObject?.get("platforms")?.jsonArray?.single()?.jsonPrimitive?.content,
        )
        // An unset clause must not travel as null: that would claim we mean it.
        assertTrue(body["audience"]?.jsonObject?.containsKey("label_ids") == false)
    }

    /** A closed messaging window has to be readable, or a non-send is a mystery. */
    @Test
    fun `a skipped recipient keeps its reason`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":[{"contact_id":"con_1","display_name":"Sam Rivera",
                          "status":"skipped","skip_reason":"window_closed",
                          "sent_at":null,"error":null}],
                 "pagination":{"page":1,"per_page":50,"total":1}}
                """.trimIndent(),
            ),
        )

        val page = server.client().use {
            it.broadcasts.recipients("bc_1", RecipientListParams(status = "skipped"))
        }

        assertEquals("/v1/broadcasts/bc_1/recipients?status=skipped", server.takeRequest().path)
        val recipient = page.data.single()
        assertEquals("skipped", recipient.status)
        assertEquals("window_closed", recipient.skipReason)
    }

    @Test
    fun `send reports how many matched`() = runTest {
        server.enqueue(json(200, """{"data":{"id":"bc_1","status":"sending","recipients":3}}"""))

        val sent = server.client().use { it.broadcasts.send("bc_1") }

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/v1/broadcasts/bc_1/send", request.path)
        assertEquals(3, sent.recipients)
        assertEquals("sending", sent.status)
    }

    @Test
    fun `sequence steps travel as given`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":{"id":"seq_1","name":"Welcome","account_id":"acc_1",
                 "steps":[{"delay_hours":0,"text":"Hi"},{"delay_hours":48,"text":"Still here?"}],
                 "status":"active","created_at":"2026-09-12T08:00:00Z"}}
                """.trimIndent(),
            ),
        )

        val sequence = server.client().use {
            it.sequences.create(
                CreateSequenceParams(
                    workspaceId = "w1",
                    accountId = "acc_1",
                    name = "Welcome",
                    steps = listOf(SequenceStep(delayHours = 0.0, text = "Hi")),
                ),
            )
        }

        assertEquals(48.0, sequence.steps[1].delayHours)
        val body = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject
        val step = body["steps"]?.jsonArray?.single()?.jsonObject
        assertEquals("Hi", step?.get("text")?.jsonPrimitive?.content)
        assertEquals("0.0", step?.get("delay_hours")?.jsonPrimitive?.content)
    }

    @Test
    fun `enroll takes ids or an audience`() = runTest {
        server.enqueue(json(200, """{"data":{"id":"seq_1","enrolled":2}}"""))
        server.enqueue(json(200, """{"data":{"id":"seq_1","enrolled":5}}"""))

        server.client().use {
            it.sequences.enroll("seq_1", EnrollParams(contactIds = listOf("con_1", "con_2")))
            it.sequences.enroll(
                "seq_1",
                EnrollParams(audience = AudienceFilter(platforms = listOf("telegram"))),
            )
        }

        val byId = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject
        assertEquals(2, byId["contact_ids"]?.jsonArray?.size)

        val byAudience = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject
        assertTrue(byAudience.containsKey("audience"))
        assertTrue(!byAudience.containsKey("contact_ids"))
    }

    @Test
    fun `unenroll names the contacts it stops`() = runTest {
        server.enqueue(json(200, """{"data":{"id":"seq_1","stopped":1}}"""))

        val stopped = server.client().use { it.sequences.unenroll("seq_1", listOf("con_1")) }

        val request = server.takeRequest()
        assertEquals("/v1/sequences/seq_1/unenroll", request.path)
        val body = Json.parseToJsonElement(request.body.readUtf8()).jsonObject
        assertEquals("con_1", body["contact_ids"]?.jsonArray?.single()?.jsonPrimitive?.content)
        assertEquals(1, stopped.stopped)
    }
}
