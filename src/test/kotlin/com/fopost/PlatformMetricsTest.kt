package com.fopost

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PlatformMetricsTest {

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
    fun `asks for raw and decodes the set`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":{"platform":"facebook",
                  "account":{"fetched_at":"2026-09-20T02:00:00.000Z","metrics":[
                    {"key":"page_daily_video_ad_break_earnings","label":"Ad Break Earnings",
                     "kind":"currency_usd","value":42.15},
                    {"key":"page_impressions_paid","label":"Paid Impressions","kind":"count","value":1500}]},
                  "post":{"external_post_id":"123_456","fetched_at":"2026-09-20T02:00:00.000Z","metrics":[]}}}
                """.trimIndent(),
            ),
        )

        val metrics = server.client().use { it.accounts.platformMetrics("a1") }

        server.takeRequest().let {
            assertEquals("GET", it.method)
            assertEquals("/v1/accounts/a1/insights?raw=true", it.path)
        }
        assertEquals("facebook", metrics.platform)
        assertEquals("2026-09-20T02:00:00.000Z", metrics.account.fetchedAt)
        assertEquals(
            listOf("page_daily_video_ad_break_earnings", "page_impressions_paid"),
            metrics.account.metrics.map { it.key },
        )
        assertEquals(42.15, metrics.account.metrics[0].number())
        assertEquals("123_456", metrics.post.externalPostId)
        assertTrue(metrics.post.metrics.isEmpty())
    }

    @Test
    fun `a series value survives as json`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":{"platform":"youtube",
                  "account":{"fetched_at":null,"metrics":[
                    {"key":"daily_views","label":"Views by Day","kind":"series",
                     "value":[{"day":"2026-09-19","views":600}]}]},
                  "post":{"external_post_id":null,"fetched_at":null,"metrics":[]}}}
                """.trimIndent(),
            ),
        )

        val row = server.client().use { it.accounts.platformMetrics("a1") }.account.metrics[0]

        assertNull(row.number())
        assertEquals(
            "600",
            row.value!!.jsonArray[0].jsonObject["views"]!!.jsonPrimitive.content,
        )
    }

    @Test
    fun `a pending metric grant throws`() = runTest {
        server.enqueue(
            json(
                503,
                """
                {"error":"platform_metrics_unavailable",
                 "message":"google-business metrics are not available on this deployment yet."}
                """.trimIndent(),
            ),
        )

        val failure = assertThrows<FoPostException> {
            server.client(maxRetries = 1).use { it.accounts.platformMetrics("a1") }
        }

        assertEquals(503, failure.status)
        assertEquals("platform_metrics_unavailable", failure.code)
    }
}
