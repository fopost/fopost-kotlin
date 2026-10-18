package com.fopost

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WebhookDeliveriesTest {

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
    fun `lists, reads and redelivers an attempt`() = runTest {
        server.enqueue(
            json(
                200,
                """{"data":[{"id":"wd_1","webhook_id":"wh_1","event":"post.published","delivery_key":"dk_1","attempt":1,
                 "status":"failed","response_status":500,"response_snippet":"boom","duration_ms":120,
                 "payload_hash":"abc","next_retry_at":"2026-10-18T12:05:00Z","created_at":"2026-10-18T12:00:00Z"}]}""",
            ),
        )
        server.enqueue(
            json(
                200,
                """{"data":{"id":"wd_1","webhook_id":"wh_1","event":"post.published","delivery_key":"dk_1","attempt":1,
                 "status":"failed","response_status":null,"duration_ms":120,"payload_hash":"abc",
                 "created_at":"2026-10-18T12:00:00Z","payload":{"event":"post.published","data":{"id":"post_1"}}}}""",
            ),
        )
        server.enqueue(MockResponse().setResponseCode(202))

        server.client().use { client ->
            val failed = client.webhooks.deliveries("wh_1", status = "failed", limit = 10)
            val list = server.takeRequest()
            assertEquals("GET", list.method)
            assertEquals("/v1/webhooks/wh_1/deliveries?status=failed&limit=10", list.path)
            assertEquals("dk_1", failed.single().deliveryKey)
            assertEquals(500, failed.single().responseStatus)
            assertEquals("boom", failed.single().responseSnippet)
            assertNull(failed.single().payload)

            val delivery = client.webhooks.delivery("wh_1", "wd_1")
            val get = server.takeRequest()
            assertEquals("GET", get.method)
            assertEquals("/v1/webhooks/wh_1/deliveries/wd_1", get.path)
            assertNull(delivery.responseStatus)
            assertEquals("post.published", delivery.payload!!["event"]!!.jsonPrimitive.content)

            client.webhooks.redeliver("wh_1", "wd_1")
            val redeliver = server.takeRequest()
            assertEquals("POST", redeliver.method)
            assertEquals("/v1/webhooks/wh_1/deliveries/wd_1/redeliver", redeliver.path)
        }
    }
}
