package com.fopost

import com.fopost.param.AdCompany
import com.fopost.param.ConversionEvent
import com.fopost.param.MetaAuthorizeParams
import kotlin.test.assertEquals
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

/** A second ad network behind the same endpoints. */
class AdsNetworksTest {

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
    fun `authorize reaches whichever network the registry named`() = runTest {
        server.enqueue(json(200, """{"data":{"url":"https://www.linkedin.com/oauth"}}"""))

        val url = server.client().use { client ->
            client.ads.authorize("linkedin", MetaAuthorizeParams(workspaceId = "ws_1", returnTo = "/ads"))
        }

        assertEquals("https://www.linkedin.com/oauth", url)
        val request = server.takeRequest()
        assertEquals("/v1/ads/connections/linkedin/authorize", request.requestUrl?.encodedPath)
    }

    @Test
    fun `providers carry what each network supports`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":[{"id":"linkedin","name":"LinkedIn Ads","configured":false,"connectMethods":[],
                  "capabilities":{"conversions":true},"targetingFacets":["country","job_title"],
                  "trackingMacros":[{"token":"{{LINKEDIN_CAMPAIGN_ID}}","description":"Campaign"}]}]}
                """.trimIndent(),
            ),
        )

        val providers = server.client().use { client -> client.ads.providers() }

        assertEquals(1, providers.size)
        assertEquals(false, providers[0].configured)
        assertEquals(true, providers[0].capabilities["conversions"])
        assertEquals(listOf("country", "job_title"), providers[0].targetingFacets)
        assertEquals("{{LINKEDIN_CAMPAIGN_ID}}", providers[0].trackingMacros[0].token)
    }

    @Test
    fun `company rows travel with the request`() = runTest {
        server.enqueue(json(200, """{"data":{"added":2}}"""))

        val added = server.client().use { client ->
            client.ads.addAudienceCompanies(
                audienceId = "urn:li:adSegment:44",
                workspaceId = "ws_1",
                connectionId = "conn_1",
                companies = listOf(AdCompany(domain = "northwind.example"), AdCompany(name = "Contoso")),
            )
        }

        assertEquals(2, added)
        val request = server.takeRequest()
        val body = Json.parseToJsonElement(request.body.readUtf8()).jsonObject
        assertEquals(
            "northwind.example",
            body["companies"]?.jsonArray?.get(0)?.jsonObject?.get("domain")?.jsonPrimitive?.content,
        )
    }

    @Test
    fun `conversion events send the identity the API hashes`() = runTest {
        server.enqueue(json(200, """{"data":{"accepted":1}}"""))

        val accepted = server.client().use { client ->
            client.ads.sendConversionEvents(
                ruleId = "urn:li:conversion:9",
                workspaceId = "ws_1",
                connectionId = "conn_1",
                events = listOf(ConversionEvent(happenedAt = 1758326400000, email = "buyer@example.test")),
            )
        }

        assertEquals(1, accepted)
        val request = server.takeRequest()
        assertTrue(
            request.requestUrl.toString().contains("/ads/linkedin/conversion-rules/urn:li:conversion:9/events"),
        )
        assertEquals("conn_1", request.requestUrl?.queryParameter("connection_id"))
    }
}
