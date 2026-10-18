package com.fopost

import com.fopost.model.ConversionActionSources
import com.fopost.model.CustomConversionEventTypes
import com.fopost.model.CustomConversionRule
import com.fopost.model.PixelDataUseSettings
import com.fopost.param.ConversionEvent
import com.fopost.param.CreateCustomConversionParams
import com.fopost.param.DeleteCustomConversionParams
import com.fopost.param.UploadConversionsParams
import kotlin.test.assertEquals
import kotlin.test.assertNull
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

class AdsTrackingTest {

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
    fun `sends pixel conversions under an idempotency key and reads a replay`() = runTest {
        server.enqueue(json(202, """{"data":{"id":"cb_1","accepted":1,"eventCount":1,"replayed":true}}"""))

        val result = server.client().use { client ->
            client.ads.sendPixelConversions(
                UploadConversionsParams(
                    workspaceId = "ws_1",
                    connectionId = "conn_1",
                    adAccountId = "act_1",
                    pixelId = "px_1",
                    events = listOf(
                        ConversionEvent(
                            eventName = "Purchase",
                            occurredAt = "2026-10-18T10:04:00Z",
                            eventId = "order-77",
                            actionSource = ConversionActionSources.WEBSITE,
                            email = "buyer@yourbrand.com",
                            valueMinor = 4999,
                            currency = "USD",
                            contentIds = listOf("sku_1"),
                        ),
                    ),
                ),
                idempotencyKey = "key-77",
            )
        }

        assertEquals("cb_1", result.id)
        assertEquals(1, result.accepted)
        assertTrue(result.replayed)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/v1/ads/conversions/events", request.path)
        assertEquals("key-77", request.getHeader("Idempotency-Key"))
        val event = Json.parseToJsonElement(request.body.readUtf8()).jsonObject["events"]!!.jsonArray[0].jsonObject
        assertEquals("order-77", event["eventId"]!!.jsonPrimitive.content)
        assertEquals("website", event["actionSource"]!!.jsonPrimitive.content)
        assertEquals("sku_1", event["contentIds"]!!.jsonArray[0].jsonPrimitive.content)
    }

    @Test
    fun `lists batches with hashed identifiers`() = runTest {
        server.enqueue(
            json(
                200,
                """{"data":[{"id":"cb_1","workspaceId":"ws_1","connectionId":"conn_1","adAccountId":"act_1",
                  "pixelId":"px_1","idempotencyKey":"key-77","eventCount":1,"accepted":1,
                  "events":[{"eventName":"Purchase","occurredAt":"2026-10-18T10:04:00Z","eventId":null,
                  "actionSource":"website","emailHash":"ab12","phoneHash":null,"externalIdHash":null,
                  "valueMinor":4999,"currency":"USD","orderId":null}],"createdAt":"2026-10-18T10:05:00Z"}]}""",
            ),
        )

        val batches = server.client().use { client ->
            client.ads.conversionBatches(workspaceId = "ws_1", connectionId = "conn_1", limit = 10)
        }

        assertEquals(1, batches.size)
        assertEquals("key-77", batches[0].idempotencyKey)
        assertEquals("ab12", batches[0].events[0].emailHash)
        assertNull(batches[0].events[0].eventId)
        assertEquals(
            "/v1/ads/conversions/events?workspace_id=ws_1&connection_id=conn_1&limit=10",
            server.takeRequest().path,
        )
    }

    @Test
    fun `reads a pixel with its install script`() = runTest {
        server.enqueue(
            json(
                200,
                """{"data":{"id":"px_1","name":"Site","code":"<script>fbq()</script>","createdAt":"2026-01-01T00:00:00Z",
                  "lastFiredAt":null,"isUnavailable":false,"dataUseSetting":"advertising_and_analytics"}}""",
            ),
        )

        val pixel = server.client().use { client ->
            client.ads.pixel("px_1", connectionId = "conn_1", adAccountId = "act_1", workspaceId = "ws_1")
        }

        assertEquals("<script>fbq()</script>", pixel.code)
        assertEquals(PixelDataUseSettings.ADVERTISING_AND_ANALYTICS, pixel.dataUseSetting)
        assertNull(pixel.lastFiredAt)
        assertEquals(
            "/v1/ads/pixels/px_1?workspace_id=ws_1&connection_id=conn_1&ad_account_id=act_1",
            server.takeRequest().path,
        )
    }

    @Test
    fun `creates and deletes a custom conversion`() = runTest {
        server.enqueue(
            json(
                201,
                """{"data":{"id":"cc_1","name":"Checkout","pixelId":"px_1","eventType":"PURCHASE",
                  "rule":{"eventName":"PageView","urlContains":"/thank-you"},"description":null,
                  "defaultValueMinor":4999,"isArchived":false,"isUnavailable":false,"createdAt":null,"lastFiredAt":null}}""",
            ),
        )
        server.enqueue(json(204, ""))

        server.client().use { client ->
            val created = client.ads.createCustomConversion(
                CreateCustomConversionParams(
                    workspaceId = "ws_1",
                    connectionId = "conn_1",
                    adAccountId = "act_1",
                    pixelId = "px_1",
                    name = "Checkout",
                    eventType = CustomConversionEventTypes.PURCHASE,
                    rule = CustomConversionRule(urlContains = "/thank-you"),
                    defaultValueMinor = 4999,
                ),
            )
            assertEquals("cc_1", created.id)
            assertEquals("/thank-you", created.rule?.urlContains)
            assertEquals(4999L, created.defaultValueMinor)

            client.ads.deleteCustomConversion(
                "cc_1",
                DeleteCustomConversionParams(workspaceId = "ws_1", connectionId = "conn_1", adAccountId = "act_1"),
            )
        }

        val create = server.takeRequest()
        assertEquals("/v1/ads/custom-conversions", create.path)
        val body = Json.parseToJsonElement(create.body.readUtf8()).jsonObject
        assertEquals("PURCHASE", body["eventType"]!!.jsonPrimitive.content)
        assertEquals("/thank-you", body["rule"]!!.jsonObject["urlContains"]!!.jsonPrimitive.content)
        assertNull(body["rule"]!!.jsonObject["urlEquals"])

        val delete = server.takeRequest()
        assertEquals("DELETE", delete.method)
        assertEquals("/v1/ads/custom-conversions/cc_1", delete.path)
        assertEquals("act_1", Json.parseToJsonElement(delete.body.readUtf8()).jsonObject["adAccountId"]!!.jsonPrimitive.content)
    }
}
