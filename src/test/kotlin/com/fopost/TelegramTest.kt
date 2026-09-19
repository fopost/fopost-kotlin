package com.fopost

import com.fopost.model.TelegramBotCommand
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TelegramTest {

    private lateinit var server: MockWebServer

    private val commands = """{"data":{"commands":[{"command":"start","description":"Start the bot"}]}}"""

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
    fun `creates a connect code for a workspace and reads the links`() = runTest {
        server.enqueue(
            json(
                201,
                """
                {"data":{"code":"ABC123","command":"/connect ABC123","bot_username":"fopost_bot",
                         "deep_link":"https://t.me/fopost_bot?start=ABC123","group_link":null,
                         "expires_at":"2026-09-19T12:15:00Z"}}
                """.trimIndent(),
            ),
        )
        server.enqueue(json(201, """{"data":{"code":"X","command":"/connect X","expires_at":"2026-09-19T12:15:00Z"}}"""))

        val code = server.client().use { client ->
            client.accounts.createTelegramConnectCode("w1").also { client.accounts.createTelegramConnectCode() }
        }

        server.takeRequest().let {
            assertEquals("POST", it.method)
            assertEquals("/v1/accounts/telegram/connect-code", it.path)
            assertEquals("""{"workspaceId":"w1"}""", it.body.readUtf8())
        }
        assertEquals("{}", server.takeRequest().body.readUtf8())
        assertEquals("fopost_bot", code.botUsername)
        assertEquals("https://t.me/fopost_bot?start=ABC123", code.deepLink)
        assertNull(code.groupLink)
        assertEquals("2026-09-19T12:15:00Z", code.expiresAt.toString())
    }

    @Test
    fun `reads a connect code status`() = runTest {
        server.enqueue(json(200, """{"data":{"status":"failed","account_id":null,"reason":"card_required"}}"""))

        val status = server.client().use { it.accounts.getTelegramConnectStatus("ABC123") }

        assertEquals("/v1/accounts/telegram/connect-code/status?code=ABC123", server.takeRequest().path)
        assertEquals("failed", status.status)
        assertNull(status.accountId)
        assertEquals("card_required", status.reason)
    }

    @Test
    fun `gets, sets and clears bot commands`() = runTest {
        server.enqueue(json(200, commands))
        server.enqueue(json(200, commands))
        server.enqueue(json(200, """{"data":{"commands":[]}}"""))

        val (got, cleared) = server.client().use { client ->
            val got = client.accounts.getTelegramBotCommands("a1")
            client.accounts.setTelegramBotCommands("a1", listOf(TelegramBotCommand("start", "Start the bot")))
            got to client.accounts.deleteTelegramBotCommands("a1")
        }

        server.takeRequest().let {
            assertEquals("GET", it.method)
            assertEquals("/v1/accounts/a1/telegram/commands", it.path)
        }
        server.takeRequest().let {
            assertEquals("PUT", it.method)
            assertEquals("/v1/accounts/a1/telegram/commands", it.path)
            assertEquals("""{"commands":[{"command":"start","description":"Start the bot"}]}""", it.body.readUtf8())
        }
        server.takeRequest().let {
            assertEquals("DELETE", it.method)
            assertEquals("/v1/accounts/a1/telegram/commands", it.path)
        }
        assertEquals(TelegramBotCommand("start", "Start the bot"), got.commands.single())
        assertTrue(cleared.commands.isEmpty())
    }
}
