package com.fopost

import com.fopost.param.ConnectSessionBrandingParams
import com.fopost.param.CreateConnectSessionParams
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConnectSessionsTest {

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
    fun `creates a session and reads it back once used`() = runTest {
        server.enqueue(
            json(
                201,
                """{"data":{"id":"cs_1","status":"pending","external_user_id":"user-42","platforms":["twitter","linkedin"],
                 "redirect_url":"https://yourbrand.com/connected","branding":{"name":"Your Brand","logo_url":null},
                 "account_id":null,"url":"https://app.fopost.com/connect/cs_1","expires_at":"2026-10-19T12:00:00Z",
                 "used_at":null,"created_at":"2026-10-18T12:00:00Z"}}""",
            ),
        )
        server.enqueue(
            json(
                200,
                """{"data":{"id":"cs_1","status":"used","external_user_id":"user-42","platforms":["twitter","linkedin"],
                 "account_id":"acc_9","expires_at":"2026-10-19T12:00:00Z","used_at":"2026-10-18T12:30:00Z",
                 "created_at":"2026-10-18T12:00:00Z"}}""",
            ),
        )

        server.client().use { client ->
            val session = client.connectSessions.create(
                CreateConnectSessionParams(
                    externalUserId = "user-42",
                    platforms = listOf("twitter", "linkedin"),
                    workspaceId = "ws_1",
                    redirectUrl = "https://yourbrand.com/connected",
                    branding = ConnectSessionBrandingParams(name = "Your Brand"),
                ),
            )
            val create = server.takeRequest()
            assertEquals("POST", create.method)
            assertEquals("/v1/connect-sessions", create.path)
            val body = Json.parseToJsonElement(create.body.readUtf8()).jsonObject
            assertEquals("user-42", body["externalUserId"]!!.jsonPrimitive.content)
            assertEquals("ws_1", body["workspaceId"]!!.jsonPrimitive.content)
            assertEquals(2, body["platforms"]!!.jsonArray.size)
            assertEquals("Your Brand", body["branding"]!!.jsonObject["name"]!!.jsonPrimitive.content)
            assertNull(body["branding"]!!.jsonObject["logoUrl"])
            assertEquals("https://app.fopost.com/connect/cs_1", session.url)
            assertEquals("pending", session.status)
            assertEquals("Your Brand", session.branding?.name)
            assertEquals(Instant.parse("2026-10-19T12:00:00Z"), session.expiresAt)

            val used = client.connectSessions.get("cs_1")
            val get = server.takeRequest()
            assertEquals("GET", get.method)
            assertEquals("/v1/connect-sessions/cs_1", get.path)
            assertEquals("used", used.status)
            assertEquals("acc_9", used.accountId)
            assertNull(used.url)
            assertEquals(Instant.parse("2026-10-18T12:30:00Z"), used.usedAt)
        }
    }
}
