package com.fopost

import com.fopost.param.DiscordEventParams
import com.fopost.param.DiscordRoleParams
import com.fopost.param.UpdateDiscordIdentityParams
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DiscordTest {

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
    fun `lists channels and switches the current one`() = runTest {
        server.enqueue(
            json(
                200,
                """{"data":[{"id":"c2","name":"launches","type":0,"parent_id":null,"nsfw":false,"is_current":true}]}""",
            ),
        )
        server.enqueue(json(200, """{"data":{"id":"c2","name":"launches","is_current":true}}"""))

        val channels = server.client().use { client ->
            val listed = client.accounts.listDiscordChannels("a1")
            client.accounts.switchDiscordChannel("a1", "c2")
            listed
        }

        assertTrue(channels[0].isCurrent)
        server.takeRequest().let {
            assertEquals("GET", it.method)
            assertEquals("/v1/accounts/a1/discord/channels", it.path)
        }
        server.takeRequest().let {
            assertEquals("PATCH", it.method)
            assertEquals("/v1/accounts/a1/discord/channels/current", it.path)
            assertEquals("""{"channel_id":"c2"}""", it.body.readUtf8())
        }
    }

    @Test
    fun `patches only the identity fields set`() = runTest {
        server.enqueue(json(200, """{"data":{"username":"Release Bot","avatar_url":null}}"""))

        val updated = server.client().use {
            it.accounts.updateDiscordIdentity("a1", UpdateDiscordIdentityParams().username("Release Bot"))
        }

        server.takeRequest().let {
            assertEquals("PATCH", it.method)
            assertEquals("/v1/accounts/a1/discord/identity", it.path)
            // An unset field never reaches the wire, so Discord keeps it.
            assertEquals("""{"username":"Release Bot"}""", it.body.readUtf8())
        }
        assertEquals("Release Bot", updated.username)
    }

    @Test
    fun `round-trips a scheduled event`() = runTest {
        val event = """
            {"id":"e1","name":"Launch stream","description":null,"channel_id":null,
             "location":"https://example.com/live","start_time":"2026-10-01T18:00:00.000Z",
             "end_time":"2026-10-01T19:00:00.000Z","status":"scheduled","user_count":0}
        """.trimIndent()
        server.enqueue(json(201, """{"data":$event}"""))
        server.enqueue(json(200, """{"data":[$event]}"""))
        server.enqueue(json(200, """{"data":{"id":"e1","name":"Launch stream","status":"canceled"}}"""))
        server.enqueue(json(200, """{"data":{"deleted":true}}"""))

        server.client().use { client ->
            val created = client.accounts.createDiscordEvent(
                "a1",
                DiscordEventParams(
                    name = "Launch stream",
                    startTime = "2026-10-01T18:00:00.000Z",
                    endTime = "2026-10-01T19:00:00.000Z",
                    location = "https://example.com/live",
                ),
            )
            assertEquals("e1", created.id)
            assertEquals(1, client.accounts.listDiscordEvents("a1").size)
            assertEquals(
                "canceled",
                client.accounts.updateDiscordEvent("a1", "e1", DiscordEventParams(status = "canceled")).status,
            )
            assertEquals(true, client.accounts.deleteDiscordEvent("a1", "e1").deleted)
        }

        server.takeRequest().let {
            assertEquals("POST", it.method)
            assertEquals("/v1/accounts/a1/discord/events", it.path)
            assertEquals(
                """{"name":"Launch stream","start_time":"2026-10-01T18:00:00.000Z",""" +
                    """"end_time":"2026-10-01T19:00:00.000Z","location":"https://example.com/live"}""",
                it.body.readUtf8(),
            )
        }
        server.takeRequest()
        server.takeRequest().let {
            assertEquals("PATCH", it.method)
            assertEquals("""{"status":"canceled"}""", it.body.readUtf8())
        }
        assertEquals("/v1/accounts/a1/discord/events/e1", server.takeRequest().path)
    }

    @Test
    fun `searches members, creates a role and sends a DM`() = runTest {
        server.enqueue(
            json(200, """{"data":[{"id":"u7","username":"ada","is_bot":false,"roles":["r1"]}]}"""),
        )
        server.enqueue(json(201, """{"data":{"id":"r2","name":"Beta"}}"""))
        server.enqueue(json(200, """{"data":{"assigned":true}}"""))
        server.enqueue(json(201, """{"data":{"id":"m1","channel_id":"dm1"}}"""))

        server.client().use { client ->
            val members = client.accounts.listDiscordMembers("a1", query = "ada")
            assertEquals(listOf("r1"), members[0].roles)

            val role = client.accounts.createDiscordRole("a1", DiscordRoleParams(name = "Beta"))
            assertEquals(true, client.accounts.addDiscordMemberRole("a1", role.id!!, "u7").assigned)
            assertEquals("dm1", client.accounts.sendDiscordDirectMessage("a1", "u7", "hi").channelId)
        }

        assertEquals("/v1/accounts/a1/discord/members?q=ada", server.takeRequest().path)
        assertEquals("/v1/accounts/a1/discord/roles", server.takeRequest().path)
        server.takeRequest().let {
            assertEquals("PUT", it.method)
            assertEquals("/v1/accounts/a1/discord/roles/r2/members/u7", it.path)
        }
        assertEquals("""{"member_id":"u7","content":"hi"}""", server.takeRequest().body.readUtf8())
    }

    @Test
    fun `a webhook connection is a conflict`() = runTest {
        server.enqueue(json(409, """{"error":"webhook_connection","message":"Upgrade it to the bot first"}"""))

        val failure = assertThrows<FoPostException> { server.client().use { it.accounts.listDiscordChannels("a1") } }

        assertEquals(409, failure.status)
        assertEquals("webhook_connection", failure.code)
    }
}
