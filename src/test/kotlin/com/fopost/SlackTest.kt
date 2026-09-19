package com.fopost

import com.fopost.model.SlackChannel
import com.fopost.param.UpdateSlackIdentityParams
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SlackTest {

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
    fun `lists channels with their flags`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":[{"id":"C1","name":"general","is_private":false,"is_member":true,"is_current":true},
                         {"id":"C2","name":"ops","is_private":true,"is_member":true,"is_current":false}]}
                """.trimIndent(),
            ),
        )

        val channels = server.client().use { it.accounts.listSlackChannels("a1") }

        server.takeRequest().let {
            assertEquals("GET", it.method)
            assertEquals("/v1/accounts/a1/slack/channels", it.path)
        }
        assertEquals(SlackChannel("C1", "general", isPrivate = false, isMember = true, isCurrent = true), channels[0])
        assertTrue(channels[1].isPrivate)
    }

    @Test
    fun `lists members and keeps null names`() = runTest {
        server.enqueue(
            json(
                200,
                """{"data":[{"id":"U1","name":"sam","real_name":"Sam Rivers","display_name":null,"avatar":null,"is_bot":false}]}""",
            ),
        )

        val member = server.client().use { it.accounts.listSlackMembers("a1") }.single()

        assertEquals("/v1/accounts/a1/slack/members", server.takeRequest().path)
        assertEquals("Sam Rivers", member.realName)
        assertNull(member.displayName)
        assertFalse(member.isBot)
    }

    @Test
    fun `reads the identity and patches only the fields set`() = runTest {
        server.enqueue(json(200, """{"data":{"username":null,"icon_url":null,"icon_emoji":":rocket:"}}"""))
        server.enqueue(json(200, """{"data":{"username":"Release Bot","icon_url":null,"icon_emoji":null}}"""))

        val (got, updated) = server.client().use { client ->
            client.accounts.getSlackIdentity("a1") to client.accounts.updateSlackIdentity(
                "a1",
                UpdateSlackIdentityParams().username("Release Bot").iconEmoji(null),
            )
        }

        server.takeRequest().let {
            assertEquals("GET", it.method)
            assertEquals("/v1/accounts/a1/slack/identity", it.path)
        }
        server.takeRequest().let {
            assertEquals("PATCH", it.method)
            assertEquals("/v1/accounts/a1/slack/identity", it.path)
            assertEquals("""{"username":"Release Bot","icon_emoji":null}""", it.body.readUtf8())
        }
        assertEquals(":rocket:", got.iconEmoji)
        assertEquals("Release Bot", updated.username)
    }

    @Test
    fun `a webhook connection is a conflict`() = runTest {
        server.enqueue(json(409, """{"error":"webhook_connection","message":"Reconnect with the Slack app"}"""))

        val failure = assertThrows<FoPostException> { server.client().use { it.accounts.listSlackChannels("a1") } }

        assertEquals(409, failure.status)
        assertEquals("webhook_connection", failure.code)
    }
}
