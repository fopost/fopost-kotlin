package com.fopost

import com.fopost.param.CreateGoogleKeywordParams
import com.fopost.param.GoogleAdScheduleInput
import com.fopost.param.GoogleAdsScope
import com.fopost.param.GoogleAuthorizeParams
import com.fopost.param.GoogleQueryParams
import com.fopost.param.SetGoogleAdScheduleParams
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GoogleAdsTest {

    private lateinit var server: MockWebServer

    private val scope = GoogleAdsScope(
        workspaceId = "ws_1",
        connectionId = "conn_1",
        customerId = "1234567890",
    )

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
    fun `keywords name the connection and the customer`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":[{"id":"1234567890~keyword~77~99","adGroupId":"1234567890~adGroup~77",
                  "text":"running shoes","matchType":"EXACT","status":"ENABLED","cpcBidMinor":180,
                  "negative":false}]}
                """.trimIndent(),
            ),
        )

        val keywords = server.client().use { client ->
            client.googleAds.keywords(scope, adGroupId = "1234567890~adGroup~77")
        }

        assertEquals("running shoes", keywords[0].text)
        assertEquals(180L, keywords[0].cpcBidMinor)

        val request = server.takeRequest()
        assertEquals("conn_1", request.requestUrl?.queryParameter("connection_id"))
        assertEquals("1234567890", request.requestUrl?.queryParameter("customer_id"))
        assertEquals("1234567890~adGroup~77", request.requestUrl?.queryParameter("ad_group_id"))
    }

    @Test
    fun `create keyword sends the scope in the body`() = runTest {
        server.enqueue(json(201, """{"data":{"id":"1234567890~keyword~77~99"}}"""))

        val id = server.client().use { client ->
            client.googleAds.createKeyword(
                CreateGoogleKeywordParams(
                    workspaceId = "ws_1",
                    connectionId = "conn_1",
                    customerId = "1234567890",
                    adGroupId = "1234567890~adGroup~77",
                    text = "running shoes",
                    matchType = "EXACT",
                ),
            )
        }

        assertEquals("1234567890~keyword~77~99", id)
        val body = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject
        assertEquals("1234567890", body["customerId"]?.jsonPrimitive?.content)
        assertEquals("EXACT", body["matchType"]?.jsonPrimitive?.content)
    }

    @Test
    fun `delete carries the scope in the body`() = runTest {
        server.enqueue(json(204, ""))

        server.client().use { client ->
            client.googleAds.deleteAsset("1234567890~asset~4321", scope)
        }

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        val body = Json.parseToJsonElement(request.body.readUtf8()).jsonObject
        assertEquals("conn_1", body["connectionId"]?.jsonPrimitive?.content)
    }

    @Test
    fun `ad schedule is replaced with put`() = runTest {
        server.enqueue(json(200, """{"data":{"slots":2}}"""))

        val slots = server.client().use { client ->
            client.googleAds.setAdSchedule(
                SetGoogleAdScheduleParams(
                    workspaceId = "ws_1",
                    connectionId = "conn_1",
                    customerId = "1234567890",
                    campaignId = "1234567890~campaign~55",
                    slots = listOf(GoogleAdScheduleInput("MONDAY", 9, 18)),
                ),
            )
        }

        assertEquals(2, slots)
        assertEquals("PUT", server.takeRequest().method)
    }

    @Test
    fun `query returns rows as google sends them`() = runTest {
        server.enqueue(json(200, """{"data":{"rows":[{"campaign":{"id":"55"}}]}}"""))

        val result = server.client().use { client ->
            client.googleAds.query(
                GoogleQueryParams(
                    connectionId = "conn_1",
                    customerId = "1234567890",
                    query = "SELECT campaign.id FROM campaign",
                ),
            )
        }

        assertEquals(1, result.rows.size)
        assertEquals("/v1/ads/insights/query", server.takeRequest().requestUrl?.encodedPath)
    }

    @Test
    fun `authorize google has its own route`() = runTest {
        server.enqueue(json(200, """{"data":{"url":"https://accounts.google.com/o/x"}}"""))

        val url = server.client().use { client ->
            client.ads.authorizeGoogle(GoogleAuthorizeParams(workspaceId = "ws_1"))
        }

        assertEquals("https://accounts.google.com/o/x", url)
        assertEquals(
            "/v1/ads/connections/google/authorize",
            server.takeRequest().requestUrl?.encodedPath,
        )
    }
}
