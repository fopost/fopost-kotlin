package com.fopost

import com.fopost.model.AdBudget
import com.fopost.model.AdTargeting
import com.fopost.model.AdTargetingLocation
import com.fopost.param.AudienceSpec
import com.fopost.param.BoostPostParams
import com.fopost.param.CreateAudienceParams
import com.fopost.param.MetaAuthorizeParams
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

class AdsTest {

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
    fun `boosts a post with a camelCase body and parses the ad`() = runTest {
        server.enqueue(
            json(
                201,
                """
                {"data":{"id":"ad_1","workspaceId":"ws_1","kind":"boost","name":"Launch week","goal":"engagement",
                  "status":"paused","effectiveStatus":"PAUSED","connectionId":"conn_1","adAccountId":"act_123",
                  "sourcePostId":"post_1","budgetMinor":5000,"budgetType":"daily","currency":"USD",
                  "targeting":{"countries":["US"],"ageMin":21,"locations":[{"key":"2418779","name":"Austin","type":"city"}]},
                  "insights":{"impressions":0,"reach":0,"clicks":0,"spendMinor":0},
                  "createdAt":"2026-09-01T10:00:00Z"}}
                """.trimIndent(),
            ),
        )

        val ad = server.client().use { client ->
            client.ads.boost(
                BoostPostParams(
                    workspaceId = "ws_1",
                    connectionId = "conn_1",
                    adAccountId = "act_123",
                    postId = "post_1",
                    accountId = "acc_1",
                    name = "Launch week",
                    goal = "engagement",
                    budget = AdBudget(minor = 5000, type = "daily"),
                    targeting = AdTargeting(
                        countries = listOf("US"),
                        ageMin = 21,
                        locations = listOf(AdTargetingLocation("2418779", "Austin", "city")),
                    ),
                ),
            )
        }

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/v1/ads/boost", request.path)
        val body = Json.parseToJsonElement(request.body.readUtf8()).jsonObject
        assertEquals("ws_1", body["workspaceId"]!!.jsonPrimitive.content)
        assertEquals("act_123", body["adAccountId"]!!.jsonPrimitive.content)
        assertEquals("post_1", body["postId"]!!.jsonPrimitive.content)
        assertEquals("5000", body["budget"]!!.jsonObject["minor"]!!.jsonPrimitive.content)
        assertEquals("21", body["targeting"]!!.jsonObject["ageMin"]!!.jsonPrimitive.content)
        assertEquals("city", body["targeting"]!!.jsonObject["locations"]!!.jsonArray.single().jsonObject["type"]!!.jsonPrimitive.content)
        assertNull(body["paused"])
        assertNull(body["targeting"]!!.jsonObject["gender"])

        assertEquals("ad_1", ad.id)
        assertEquals("boost", ad.kind)
        assertEquals("paused", ad.status)
        assertEquals(5000, ad.budgetMinor)
        assertEquals(listOf("US"), ad.targeting?.countries)
        assertEquals(21, ad.targeting?.ageMin)
        assertEquals("Austin", ad.targeting?.locations?.single()?.name)
        assertEquals(0, ad.insights?.spendMinor)
        assertEquals(Instant.parse("2026-09-01T10:00:00Z"), ad.createdAt)
    }

    @Test
    fun `sets a status with the workspace in the query`() = runTest {
        server.enqueue(json(200, """{"data":{"id":"ad_1","status":"active"}}"""))
        server.enqueue(json(200, """{"data":{"deleted":true}}"""))

        server.client().use { client ->
            assertEquals("active", client.ads.setStatus("ad_1", "ws_1", "active").status)
            client.ads.delete("ad_1", "ws_1")
        }

        val patch = server.takeRequest()
        assertEquals("PATCH", patch.method)
        assertEquals("/v1/ads/ad_1?workspace_id=ws_1", patch.path)
        assertEquals("active", Json.parseToJsonElement(patch.body.readUtf8()).jsonObject["status"]!!.jsonPrimitive.content)

        val delete = server.takeRequest()
        assertEquals("DELETE", delete.method)
        assertEquals("/v1/ads/ad_1?workspace_id=ws_1", delete.path)
    }

    @Test
    fun `authorizes Meta, creates a lookalike audience, and pages leads`() = runTest {
        server.enqueue(json(200, """{"data":{"url":"https://www.facebook.com/dialog/oauth?state=abc"}}"""))
        server.enqueue(json(201, """{"data":{"id":"aud_1","added":0}}"""))
        server.enqueue(
            json(
                200,
                """{"data":{"leads":[{"id":"lead_1","createdAt":"2026-09-01T10:00:00+0000","fields":[{"name":"email","values":["sam@yourbrand.com"]}],"isOrganic":false}],"nextCursor":"c2"}}""",
            ),
        )

        server.client().use { client ->
            assertEquals(
                "https://www.facebook.com/dialog/oauth?state=abc",
                client.ads.authorizeMeta(MetaAuthorizeParams(workspaceId = "ws_1", method = "business")),
            )

            val audience = client.ads.createAudience(
                CreateAudienceParams(
                    workspaceId = "ws_1",
                    connectionId = "conn_1",
                    adAccountId = "act_123",
                    name = "Lookalike US",
                    spec = AudienceSpec.lookalike(originAudienceId = "aud_0", country = "US", ratio = 0.05),
                ),
            )
            assertEquals("aud_1", audience.id)

            val leads = client.ads.leads("form_1", connectionId = "conn_1", pageId = "1234", after = "c1")
            assertEquals("lead_1", leads.leads.single().id)
            assertEquals("c2", leads.nextCursor)
        }

        val authorize = server.takeRequest()
        assertEquals("/v1/ads/connections/meta/authorize", authorize.path)
        val authorizeBody = Json.parseToJsonElement(authorize.body.readUtf8()).jsonObject
        assertEquals("ws_1", authorizeBody["workspaceId"]!!.jsonPrimitive.content)
        assertEquals("business", authorizeBody["method"]!!.jsonPrimitive.content)
        assertNull(authorizeBody["returnTo"])

        val create = server.takeRequest()
        assertEquals("/v1/ads/audiences", create.path)
        val spec = Json.parseToJsonElement(create.body.readUtf8()).jsonObject["spec"]!!.jsonObject
        assertEquals("LOOKALIKE", spec["subtype"]!!.jsonPrimitive.content)
        assertEquals("aud_0", spec["originAudienceId"]!!.jsonPrimitive.content)
        assertEquals("0.05", spec["ratio"]!!.jsonPrimitive.content)
        assertNull(spec["emails"])
        assertNull(spec["pixelId"])

        assertEquals(
            "/v1/ads/lead-forms/form_1/leads?connection_id=conn_1&page_id=1234&after=c1",
            server.takeRequest().path,
        )
    }
}
