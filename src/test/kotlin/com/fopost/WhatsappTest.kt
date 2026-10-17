package com.fopost

import com.fopost.model.Platforms
import com.fopost.param.CreateWhatsappTemplateParams
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WhatsappTest {

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
    fun `whatsapp is on the platform list`() {
        assertEquals("whatsapp", Platforms.WHATSAPP)
        assertTrue(Platforms.ALL.contains(Platforms.WHATSAPP))
    }

    @Test
    fun `a template create returns the review status the platform gave it`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":{"id":"tpl-1","name":"order_shipped","language":"en_US","category":"UTILITY",
                         "status":"PENDING","rejectedReason":null,"components":[],"qualityScore":null}}
                """.trimIndent(),
            ),
        )

        val template =
            server.client().use {
                it.whatsapp.createTemplate(
                    "a1",
                    CreateWhatsappTemplateParams(
                        name = "order_shipped",
                        language = "en_US",
                        category = "UTILITY",
                        components =
                            listOf(
                                buildJsonObject {
                                    put("type", "BODY")
                                    put("text", "On its way.")
                                }
                            ),
                    ),
                )
            }

        server.takeRequest().let {
            assertEquals("POST", it.method)
            assertEquals("/v1/accounts/a1/whatsapp/templates", it.path)
        }
        // Nothing marks a template approved but the platform.
        assertEquals("PENDING", template.status)
        assertEquals("order_shipped", template.name)
    }

    @Test
    fun `deleting a template names it in the query`() = runTest {
        server.enqueue(json(200, """{"data":{"deleted":true}}"""))

        server.client().use { it.whatsapp.deleteTemplate("a1", "tpl-1", "order_shipped") }

        server.takeRequest().let {
            assertEquals("DELETE", it.method)
            assertEquals(
                "/v1/accounts/a1/whatsapp/templates/tpl-1?name=order_shipped",
                it.path,
            )
        }
    }

    @Test
    fun `a sandbox session carries only the last four digits`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":{"id":"ses-1","status":"invited","phoneNumberLast4":"4567",
                         "invitedAt":"2026-09-20T10:00:00Z","activatedAt":null,
                         "expiresAt":"2026-09-21T10:00:00Z"}}
                """.trimIndent(),
            ),
        )

        val session =
            server.client().use { it.whatsapp.createSandboxSession("ws", "+15551234567") }

        assertEquals("/v1/whatsapp/sandbox/sessions", server.takeRequest().path)
        assertEquals("4567", session.phoneNumberLast4)
        assertEquals("invited", session.status)
    }
}
