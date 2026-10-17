package com.fopost

import com.fopost.model.MetaGreetingText
import com.fopost.model.MetaIceBreaker
import com.fopost.model.MetaMenuItem
import com.fopost.model.MetaPersistentMenuEntry
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MetaMessagingTest {

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
    fun `ice breakers round trip`() = runTest {
        val body = """{"data":{"ice_breakers":[{"question":"Hours?","payload":"HOURS"}]}}"""
        server.enqueue(json(200, body))
        server.enqueue(json(200, body))
        server.enqueue(json(200, """{"data":{"ice_breakers":[]}}"""))

        server.client().use { client ->
            val listed = client.accounts.getIceBreakers("a1")
            assertEquals("HOURS", listed.iceBreakers[0].payload)

            val set = client.accounts.setIceBreakers("a1", listOf(MetaIceBreaker("Hours?", "HOURS")))
            assertEquals("Hours?", set.iceBreakers[0].question)

            val cleared = client.accounts.deleteIceBreakers("a1")
            assertTrue(cleared.iceBreakers.isEmpty())
        }

        server.takeRequest().let {
            assertEquals("GET", it.method)
            assertEquals("/v1/accounts/a1/messaging/ice-breakers", it.path)
        }
        server.takeRequest().let {
            assertEquals("PUT", it.method)
            assertEquals("""{"ice_breakers":[{"question":"Hours?","payload":"HOURS"}]}""", it.body.readUtf8())
        }
        assertEquals("DELETE", server.takeRequest().method)
    }

    @Test
    fun `a link menu item omits the payload key`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":{"persistent_menu":[{"locale":"default","call_to_actions":[
                  {"type":"web_url","title":"Shop","url":"https://example.com/shop"}]}]}}
                """.trimIndent(),
            ),
        )

        val set = server.client().use {
            it.accounts.setPersistentMenu(
                "a1",
                listOf(MetaPersistentMenuEntry(callToActions = listOf(MetaMenuItem.link("Shop", "https://example.com/shop")))),
            )
        }

        server.takeRequest().let {
            assertEquals("PUT", it.method)
            assertEquals("/v1/accounts/a1/messaging/persistent-menu", it.path)
            assertEquals(
                """{"persistent_menu":[{"locale":"default","call_to_actions":""" +
                    """[{"type":"web_url","title":"Shop","url":"https://example.com/shop"}]}]}""",
                it.body.readUtf8(),
            )
        }
        assertEquals("https://example.com/shop", set.persistentMenu[0].callToActions[0].url)
    }

    @Test
    fun `the greeting defaults its locale`() = runTest {
        server.enqueue(json(200, """{"data":{"greeting":[{"locale":"default","text":"Hi!"}]}}"""))

        val saved = server.client().use { it.accounts.setGreeting("a1", listOf(MetaGreetingText(text = "Hi!"))) }

        assertEquals("""{"greeting":[{"locale":"default","text":"Hi!"}]}""", server.takeRequest().body.readUtf8())
        assertEquals("default", saved.greeting[0].locale)
    }

    @Test
    fun `a lapsed subscription is reported and resubscribed`() = runTest {
        server.enqueue(json(200, """{"data":{"subscribed":false,"fields":["feed"],"missing_fields":["messages"]}}"""))
        server.enqueue(json(200, """{"data":{"subscribed":true,"fields":["feed","messages"],"missing_fields":[]}}"""))

        server.client().use { client ->
            val lapsed = client.accounts.getWebhookSubscription("a1")
            assertFalse(lapsed.subscribed)
            assertEquals(listOf("messages"), lapsed.missingFields)

            assertTrue(client.accounts.resubscribeWebhook("a1").subscribed)
        }

        assertEquals("/v1/accounts/a1/webhook-subscription", server.takeRequest().path)
        assertEquals("POST", server.takeRequest().method)
    }

    @Test
    fun `handover passes and takes control`() = runTest {
        server.enqueue(json(200, """{"data":{"app_id":"263902037430900","control":"passed"}}"""))
        server.enqueue(json(200, """{"data":{"app_id":null,"control":"taken"}}"""))

        server.client().use { client ->
            val passed = client.inbox.handover("t_1", "a1", appId = "263902037430900")
            assertEquals("passed", passed.control)

            val taken = client.inbox.handover("t_1", "a1")
            assertNull(taken.appId)
            assertEquals("taken", taken.control)
        }

        server.takeRequest().let {
            assertEquals("/v1/inbox/conversations/t_1/handover", it.path)
            assertEquals("""{"account_id":"a1","app_id":"263902037430900"}""", it.body.readUtf8())
        }
        assertEquals("""{"account_id":"a1"}""", server.takeRequest().body.readUtf8())
    }
}
