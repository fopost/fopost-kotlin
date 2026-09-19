package com.fopost

import com.fopost.param.InboxListParams
import com.fopost.param.MarkInboxReadParams
import com.fopost.param.StartInboxConversationParams
import com.fopost.param.UpdateInboxItemParams
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InboxTest {

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
    fun `lists items with filters and reads the inbox meta`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":[{"id":"ib_1","workspaceId":"ws_1","platform":"instagram","type":"comment",
                  "state":"unread","direction":"inbound","authorName":"Sam Rivera","authorHandle":"samrivera",
                  "text":"Love this","attachments":[{"kind":"image","url":"https://api.test/v1/inbox/ib_1/attachments/0"}],
                  "postExternalId":"17900000000","platformCreatedAt":"2026-09-01T10:00:00Z",
                  "canReply":true,"hidden":false,"canHide":true,"canDelete":false,
                  "postContext":{"externalId":"17900000000","isOwn":true,"text":"New drop at yourbrand.com"},
                  "account":{"id":"acc_1","platform":"instagram","username":"yourbrand"}}],
                 "meta":{"page":2,"perPage":25,"total":51}}
                """.trimIndent(),
            ),
        )

        val page = server.client().use { client ->
            client.inbox.list(InboxListParams(workspaceId = "ws_1", type = "comment", state = "unread", page = 2))
        }

        assertEquals("/v1/inbox?workspace_id=ws_1&type=comment&state=unread&page=2", server.takeRequest().path)
        assertEquals(2, page.meta?.currentPage)
        assertEquals(25, page.meta?.perPage)
        assertEquals(51, page.meta?.total)
        val item = page.single()
        assertEquals("ib_1", item.id)
        assertEquals("comment", item.type)
        assertEquals("samrivera", item.authorHandle)
        assertEquals(Instant.parse("2026-09-01T10:00:00Z"), item.platformCreatedAt)
        assertEquals("image", item.attachments.single().kind)
        assertEquals(true, item.postContext?.isOwn)
        assertEquals("yourbrand", item.account?.username)
    }

    @Test
    fun `marks a thread read and snoozes an item`() = runTest {
        server.enqueue(json(200, """{"data":{"updated":3}}"""))
        server.enqueue(json(200, """{"data":{"id":"ib_1","state":"snoozed","snoozedUntil":"2026-09-02T09:00:00Z"}}"""))

        server.client().use { client ->
            val updated = client.inbox.markThreadRead(
                MarkInboxReadParams(workspaceId = "ws_1", accountId = "acc_1", postExternalId = "17900000000"),
            )
            assertEquals(3, updated)

            val item = client.inbox.update(
                "ib_1",
                UpdateInboxItemParams(state = "snoozed", snoozedUntil = Instant.parse("2026-09-02T09:00:00Z")),
            )
            assertEquals("snoozed", item.state)
            assertEquals(Instant.parse("2026-09-02T09:00:00Z"), item.snoozedUntil)
        }

        val read = server.takeRequest()
        assertEquals("POST", read.method)
        assertEquals("/v1/inbox/read", read.path)
        val readBody = Json.parseToJsonElement(read.body.readUtf8()).jsonObject
        assertEquals("ws_1", readBody["workspace_id"]!!.jsonPrimitive.content)
        assertEquals("acc_1", readBody["account_id"]!!.jsonPrimitive.content)
        assertEquals("17900000000", readBody["post_external_id"]!!.jsonPrimitive.content)
        assertNull(readBody["conversation_id"])

        val update = server.takeRequest()
        assertEquals("PATCH", update.method)
        assertEquals("/v1/inbox/ib_1", update.path)
        val updateBody = Json.parseToJsonElement(update.body.readUtf8()).jsonObject
        assertEquals("snoozed", updateBody["state"]!!.jsonPrimitive.content)
        assertEquals("2026-09-02T09:00:00Z", updateBody["snoozedUntil"]!!.jsonPrimitive.content)
    }

    @Test
    fun `replies, reads the unread count, and decides an approval`() = runTest {
        server.enqueue(json(200, """{"data":{"item":{"id":"ib_1","state":"read","repliedAt":"2026-09-01T11:00:00Z"},"reply":{"externalId":"18000000000","externalUrl":"https://www.instagram.com/p/abc/"}}}"""))
        server.enqueue(json(200, """{"count":7}"""))
        server.enqueue(json(200, """{"data":{"id":42,"outcome":"approved"}}"""))

        server.client().use { client ->
            val result = client.inbox.reply("ib_1", "Thanks!")
            assertEquals("read", result.item?.state)
            assertEquals("18000000000", result.reply?.externalId)

            assertEquals(7, client.inbox.unreadCount("ws_1"))

            val decision = client.inbox.approveReply(42, text = "Thanks, Sam!")
            assertEquals(42, decision.id)
            assertEquals("approved", decision.outcome)
        }

        val reply = server.takeRequest()
        assertEquals("/v1/inbox/ib_1/reply", reply.path)
        assertEquals("Thanks!", Json.parseToJsonElement(reply.body.readUtf8()).jsonObject["text"]!!.jsonPrimitive.content)

        assertEquals("/v1/inbox/unread-count?workspace_id=ws_1", server.takeRequest().path)

        val approve = server.takeRequest()
        assertEquals("/v1/inbox/approvals/42/approve", approve.path)
        assertEquals("Thanks, Sam!", Json.parseToJsonElement(approve.body.readUtf8()).jsonObject["text"]!!.jsonPrimitive.content)
    }

    @Test
    fun `likes, pins, reacts and edits a comment`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":{"id":"ib_1","liked":true,"pinned":false,"reaction":null,"editedAt":null,
                  "canLike":true,"canPin":false,"canEdit":false,"canReact":false,"canSendMedia":false,
                  "canQuickReply":false,"canPrivateReply":true,"canDelete":true}}
                """.trimIndent(),
            ),
        )
        server.enqueue(json(200, """{"data":{"id":"ib_2","pinned":true}}"""))
        server.enqueue(json(200, """{"data":{"id":"ib_3","reaction":null}}"""))
        server.enqueue(json(200, """{"data":{"id":"ib_2","text":"Fixed typo","editedAt":"2026-09-01T12:00:00Z"}}"""))

        server.client().use { client ->
            val liked = client.inbox.like("ib_1")
            assertEquals(true, liked.liked)
            assertEquals(true, liked.canLike)
            assertEquals(true, liked.canPrivateReply)
            assertEquals(false, liked.canSendMedia)

            assertEquals(true, client.inbox.pin("ib_2").pinned)
            assertNull(client.inbox.react("ib_3", null).reaction)

            val edited = client.inbox.editComment("ib_2", "Fixed typo")
            assertEquals(Instant.parse("2026-09-01T12:00:00Z"), edited.editedAt)
        }

        val like = server.takeRequest()
        assertEquals("POST", like.method)
        assertEquals("/v1/inbox/ib_1/like", like.path)

        assertEquals("/v1/inbox/ib_2/pin", server.takeRequest().path)

        val react = server.takeRequest()
        assertEquals("/v1/inbox/ib_3/react", react.path)
        assertEquals(JsonNull, Json.parseToJsonElement(react.body.readUtf8()).jsonObject["reaction"])

        val edit = server.takeRequest()
        assertEquals("PATCH", edit.method)
        assertEquals("/v1/inbox/ib_2", edit.path)
        assertEquals("""{"text":"Fixed typo"}""", edit.body.readUtf8())
    }

    @Test
    fun `replies with media, starts a conversation and sets typing`() = runTest {
        server.enqueue(json(200, """{"data":{"item":{"id":"ib_1"},"reply":{"externalId":"m_1"}}}"""))
        server.enqueue(json(201, """{"data":{"conversationId":"t_9","item":{"id":"ib_9","type":"dm","direction":"outbound"}}}"""))
        server.enqueue(json(200, """{"data":{"typing":true}}"""))

        server.client().use { client ->
            client.inbox.reply("ib_1", mediaIds = listOf("med_1"), quickReplies = listOf("Yes", "No"))

            val started = client.inbox.startConversation(
                StartInboxConversationParams(text = "Thanks for the comment!", commentId = "ib_1"),
            )
            assertEquals("t_9", started.conversationId)
            assertEquals("outbound", started.item?.direction)

            assertEquals(true, client.inbox.setTyping("t_9", accountId = "acc_1"))
        }

        val reply = server.takeRequest().body.readUtf8().let { Json.parseToJsonElement(it).jsonObject }
        assertNull(reply["text"])
        assertEquals("med_1", reply["media_ids"]!!.jsonArray.single().jsonPrimitive.content)
        assertEquals(2, reply["quick_replies"]!!.jsonArray.size)

        val start = server.takeRequest()
        assertEquals("POST", start.method)
        assertEquals("/v1/inbox/conversations", start.path)
        assertEquals("""{"text":"Thanks for the comment!","comment_id":"ib_1"}""", start.body.readUtf8())

        val typing = server.takeRequest()
        assertEquals("/v1/inbox/conversations/t_9/typing", typing.path)
        assertEquals("""{"account_id":"acc_1"}""", typing.body.readUtf8())
    }

    @Test
    fun `reads canStartConversation on inbox accounts`() = runTest {
        server.enqueue(json(200, """{"data":[{"id":"acc_1","platform":"x","canStartConversation":true}]}"""))

        val accounts = server.client().use { client -> client.inbox.accounts("ws_1") }

        assertEquals(true, accounts.single().canStartConversation)
    }
}
