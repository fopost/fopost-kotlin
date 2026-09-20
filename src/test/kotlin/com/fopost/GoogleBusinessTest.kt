package com.fopost

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Business Profile management: one call per route, pinning the method, the path
 * and the body each endpoint actually receives.
 */
class GoogleBusinessTest {

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
    fun `every method maps onto its route`() = runTest {
        val calls: List<Triple<String, String, suspend (FoPost) -> Unit>> = listOf(
            Triple("GET", "/v1/accounts/a1/gbp/location") { it.googleBusiness.getLocation("a1") },
            Triple("PATCH", "/v1/accounts/a1/gbp/location") {
                it.googleBusiness.updateLocation("a1", JsonObject(emptyMap()))
            },
            Triple("GET", "/v1/accounts/a1/gbp/attributes") { it.googleBusiness.getAttributes("a1") },
            Triple("PATCH", "/v1/accounts/a1/gbp/attributes") {
                it.googleBusiness.updateAttributes("a1", emptyList())
            },
            Triple("GET", "/v1/accounts/a1/gbp/menus") { it.googleBusiness.getMenus("a1") },
            Triple("PUT", "/v1/accounts/a1/gbp/menus") { it.googleBusiness.replaceMenus("a1", emptyList()) },
            Triple("GET", "/v1/accounts/a1/gbp/services") { it.googleBusiness.getServices("a1") },
            Triple("PUT", "/v1/accounts/a1/gbp/services") {
                it.googleBusiness.replaceServices("a1", emptyList())
            },
            Triple("GET", "/v1/accounts/a1/gbp/media") { it.googleBusiness.listMedia("a1") },
            Triple("POST", "/v1/accounts/a1/gbp/media") { it.googleBusiness.addMedia("a1", "m1") },
            Triple("DELETE", "/v1/accounts/a1/gbp/media/CAoSL") {
                it.googleBusiness.deleteMedia("a1", "CAoSL")
            },
            Triple("GET", "/v1/accounts/a1/gbp/place-actions") { it.googleBusiness.listPlaceActions("a1") },
            Triple("POST", "/v1/accounts/a1/gbp/place-actions") {
                it.googleBusiness.createPlaceAction("a1", "https://example.test/book", "APPOINTMENT")
            },
            Triple("PATCH", "/v1/accounts/a1/gbp/place-actions/links-1") {
                it.googleBusiness.updatePlaceAction("a1", "links-1", isPreferred = true)
            },
            Triple("DELETE", "/v1/accounts/a1/gbp/place-actions/links-1") {
                it.googleBusiness.deletePlaceAction("a1", "links-1")
            },
            Triple("GET", "/v1/accounts/a1/gbp/verification") {
                it.googleBusiness.getVerificationOptions("a1")
            },
            Triple("POST", "/v1/accounts/a1/gbp/verification/start") {
                it.googleBusiness.startVerification("a1", "SMS")
            },
            Triple("POST", "/v1/accounts/a1/gbp/verification/complete") {
                it.googleBusiness.completeVerification("a1", "v1", "123456")
            },
            Triple("GET", "/v1/accounts/a1/gbp/performance") {
                it.googleBusiness.getPerformance("a1", "2026-09-01", "2026-09-07")
            },
        )

        server.client().use { client ->
            for ((method, path, call) in calls) {
                server.enqueue(json(200, """{"data":{"ok":true}}"""))
                call(client)
                val request = server.takeRequest()
                assertEquals(method, request.method, path)
                assertEquals(path, request.path?.substringBefore('?'))
            }
        }
    }

    @Test
    fun `a patch carries only the fields the caller set`() = runTest {
        server.enqueue(json(200, """{"data":{}}"""))

        server.client().use {
            it.googleBusiness.updateLocation("a1", buildJsonObject { put("store_code", "S-12") })
        }

        assertEquals("""{"store_code":"S-12"}""", server.takeRequest().body.readUtf8())
    }

    @Test
    fun `a photo is named by its library id`() = runTest {
        server.enqueue(json(200, """{"data":{}}"""))

        server.client().use { it.googleBusiness.addMedia("a1", "m1", category = "INTERIOR") }

        assertEquals("""{"media_id":"m1","category":"INTERIOR"}""", server.takeRequest().body.readUtf8())
    }

    @Test
    fun `performance repeats the metric parameter`() = runTest {
        server.enqueue(json(200, """{"data":{}}"""))

        server.client().use {
            it.googleBusiness.getPerformance(
                "a1",
                "2026-09-01",
                "2026-09-07",
                listOf("CALL_CLICKS", "WEBSITE_CLICKS"),
            )
        }

        val url = server.takeRequest().requestUrl!!
        assertEquals(listOf("CALL_CLICKS", "WEBSITE_CLICKS"), url.queryParameterValues("daily_metrics"))
        assertEquals("2026-09-01", url.queryParameter("start_date"))
    }

    @Test
    fun `search keywords asks the same route for the monthly terms`() = runTest {
        server.enqueue(json(200, """{"data":{}}"""))

        server.client().use {
            it.googleBusiness.getSearchKeywords("a1", "2026-08-01", "2026-09-01")
        }

        val url = server.takeRequest().requestUrl!!
        assertEquals("/v1/accounts/a1/gbp/performance", url.encodedPath)
        assertEquals("true", url.queryParameter("keywords"))
    }

    @Test
    fun `assign hands the location to another workspace`() = runTest {
        server.enqueue(json(200, """{"data":{"id":"a1","platform":"google-business"}}"""))

        val moved = server.client().use { it.googleBusiness.assign("a1", "w2") }

        assertEquals("a1", moved.id)
        assertEquals("""{"workspace_id":"w2"}""", server.takeRequest().body.readUtf8())
    }

    @Test
    fun `a pending api grant surfaces as an error`() = runTest {
        server.enqueue(json(503, """{"error":"configuration_error","message":"Not available yet"}"""))

        // maxRetries = 1: a 503 is retryable, and one canned response is all there is.
        val error = assertThrows<FoPostException> {
            server.client(maxRetries = 1).use { it.googleBusiness.getLocation("a1") }
        }

        assertEquals(503, error.status)
        assertEquals("configuration_error", error.code)
        assertTrue(error.message!!.isNotBlank())
    }
}
