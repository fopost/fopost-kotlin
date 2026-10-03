package com.fopost

import com.fopost.model.ContactChannel
import com.fopost.param.ContactListParams
import com.fopost.param.ConversationAnalyticsParams
import com.fopost.param.CreateContactFieldParams
import com.fopost.param.CreateContactParams
import com.fopost.param.UpdateContactParams
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

class ContactsTest {

    private lateinit var server: MockWebServer

    private val contact = """
        {"id":"con_1","display_name":"Ada Okafor",
         "channels":[{"platform":"instagram","handle":"adaokafor","externalId":"178414"},
                     {"platform":"x","handle":"ada_writes","externalId":null}],
         "source":"inbox","note":null,
         "first_seen_at":"2026-04-02T09:14:00Z","last_seen_at":"2026-09-18T14:30:00Z",
         "fields":{"plan_tier":"Pro"},
         "labels":[{"id":"lbl_1","name":"VIP","color":"#0070f3"}]}
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
    fun `list pages on pagination, not meta, and reads every channel`() = runTest {
        server.enqueue(
            json(200, """{"data":[$contact],"pagination":{"page":1,"per_page":25,"total":1}}"""),
        )

        val page = server.client().use {
            it.contacts.list(ContactListParams(workspaceId = "w1", search = "ada"))
        }

        val request = server.takeRequest()
        assertEquals("/v1/contacts?workspace_id=w1&search=ada", request.path)
        assertEquals(1, page.meta?.total)
        assertEquals(25, page.meta?.perPage)

        val found = page.data.single()
        assertEquals(listOf("adaokafor", "ada_writes"), found.channels.map { it.handle })
        assertEquals("178414", found.channels.first().externalId)
        assertNull(found.channels.last().externalId)
        assertEquals(mapOf("plan_tier" to "Pro"), found.fields)
        assertEquals("VIP", found.labels.single().name)
    }

    @Test
    fun `create sends the channels the API expects`() = runTest {
        server.enqueue(json(200, """{"data":$contact}"""))

        server.client().use {
            it.contacts.create(
                CreateContactParams(
                    workspaceId = "w1",
                    channels = listOf(ContactChannel(platform = "x", handle = "ada_writes")),
                    displayName = "Ada Okafor",
                ),
            )
        }

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        val body = Json.parseToJsonElement(request.body.readUtf8()).jsonObject
        assertEquals("w1", body["workspace_id"]?.jsonPrimitive?.content)
        assertEquals("Ada Okafor", body["display_name"]?.jsonPrimitive?.content)
    }

    @Test
    fun `update patches only what it was given`() = runTest {
        server.enqueue(json(200, """{"data":$contact}"""))

        server.client().use {
            it.contacts.update("con_1", UpdateContactParams(displayName = "Ada O."))
        }

        val request = server.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/v1/contacts/con_1", request.path)
        val body = Json.parseToJsonElement(request.body.readUtf8()).jsonObject
        assertEquals("Ada O.", body["display_name"]?.jsonPrimitive?.content)
        assertNull(body["channels"])
    }

    @Test
    fun `a field create carries the workspace in the query too`() = runTest {
        server.enqueue(
            json(200, """{"data":{"id":"f1","key":"plan_tier","name":"Plan Tier","type":"select","options":["Free","Pro"]}}"""),
        )

        val field = server.client().use {
            it.contacts.createField(
                CreateContactFieldParams(
                    workspaceId = "w1",
                    key = "plan_tier",
                    name = "Plan Tier",
                    type = "select",
                    options = listOf("Free", "Pro"),
                ),
            )
        }

        assertEquals("/v1/contacts/fields?workspace_id=w1", server.takeRequest().path)
        assertEquals(listOf("Free", "Pro"), field.options)
    }

    @Test
    fun `conversation analytics reads under analytics and keeps the key opaque`() = runTest {
        server.enqueue(
            json(
                200,
                """{"data":{"conversations":[{"key":"9f2c7a10b4e83d5612ff0a8c4d1e6b73",
                    "accountId":"a1","platform":"instagram","received":9,"sent":5,"answered":5,
                    "open":1,"medianResponseMinutes":47,
                    "firstMessageAt":"2026-09-01T08:02:00Z","lastMessageAt":"2026-09-18T14:30:00Z"}],
                    "total":128,"page":1,"perPage":25}}""".trimIndent(),
            ),
        )

        val analytics = server.client().use {
            it.contacts.conversationAnalytics(ConversationAnalyticsParams(days = 30, sort = "slowest"))
        }

        assertEquals("/v1/analytics/inbox/conversations?days=30&sort=slowest", server.takeRequest().path)
        assertEquals(128, analytics.total)
        val row = analytics.conversations.single()
        assertEquals(47, row.medianResponseMinutes)
        // The digest, not a handle: nothing about who wrote it.
        assertEquals("9f2c7a10b4e83d5612ff0a8c4d1e6b73", row.key)
    }
}
