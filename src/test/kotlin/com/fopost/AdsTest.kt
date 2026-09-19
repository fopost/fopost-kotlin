package com.fopost

import com.fopost.model.AdBudget
import com.fopost.model.AdTargeting
import com.fopost.model.AdTargetingLocation
import com.fopost.param.AdCreativeCard
import com.fopost.param.AdObjectRef
import com.fopost.param.AudienceSpec
import com.fopost.param.BoostPostParams
import com.fopost.param.BulkAdStatusParams
import com.fopost.param.CreateAdCreativeParams
import com.fopost.param.CreateAudienceParams
import com.fopost.param.MetaAuthorizeParams
import com.fopost.param.UpdateAdCampaignParams
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

    @Test
    fun `reads the account tree with the connection in the query`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":{"adAccountId":"act_123","currency":"USD","workspaceId":"ws_1",
                  "campaigns":[{"id":"cmp_1","name":"Launch","status":"PAUSED","budgetMinor":null,
                    "adSets":[{"id":"set_1","campaignId":"cmp_1","status":"PAUSED","budgetMinor":2000,"budgetType":"daily",
                      "ads":[{"id":"ad_9","adSetId":"set_1","creativeId":"cr_1","status":"PAUSED"}]}]}]}}
                """.trimIndent(),
            ),
        )

        val tree = server.client().use { it.ads.accountTree("act_123", "conn_1", workspaceId = "ws_1") }

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/v1/ads/accounts/act_123/tree?workspace_id=ws_1&connection_id=conn_1", request.path)
        assertNull(tree.campaigns[0].budgetMinor)
        assertEquals(2000L, tree.campaigns[0].adSets[0].budgetMinor)
        assertEquals("cr_1", tree.campaigns[0].adSets[0].ads[0].creativeId)
    }

    @Test
    fun `updates, duplicates and bulk-pauses campaign objects`() = runTest {
        server.enqueue(json(200, """{"data":{"id":"cmp_1","status":"ACTIVE"}}"""))
        server.enqueue(json(201, """{"data":{"id":"cmp_2"}}"""))
        server.enqueue(
            json(
                200,
                """{"data":[{"id":"cmp_1","level":"campaign","ok":true,"error":null},
                   {"id":"ad_9","level":"ad","ok":false,"error":"Not found"}]}""",
            ),
        )

        val (copyId, results) = server.client().use { client ->
            client.ads.updateCampaign("cmp_1", "ws_1", "conn_1", UpdateAdCampaignParams(status = "active"))
            val copy = client.ads.duplicateCampaign("cmp_1", "ws_1", "conn_1", paused = false)
            copy to client.ads.bulkSetStatus(
                BulkAdStatusParams(
                    workspaceId = "ws_1",
                    connectionId = "conn_1",
                    status = "paused",
                    objects = listOf(AdObjectRef("cmp_1", "campaign"), AdObjectRef("ad_9", "ad")),
                ),
            )
        }

        val update = server.takeRequest()
        assertEquals("PATCH", update.method)
        assertEquals("/v1/ads/campaigns/cmp_1?workspace_id=ws_1&connection_id=conn_1", update.path)
        assertEquals("""{"status":"active"}""", update.body.readUtf8())

        val duplicate = server.takeRequest()
        assertEquals("/v1/ads/campaigns/cmp_1/duplicate?workspace_id=ws_1&connection_id=conn_1", duplicate.path)
        assertEquals("""{"paused":false}""", duplicate.body.readUtf8())
        assertEquals("cmp_2", copyId)

        val bulk = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject
        assertEquals("campaign", bulk["objects"]!!.jsonArray[0].jsonObject["level"]!!.jsonPrimitive.content)
        assertEquals("Not found", results[1].error)
    }

    @Test
    fun `sends the insights range, breakdown and daily flag`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":{"objectId":"cmp_1","currency":"USD","since":"2026-09-01","until":"2026-09-07","breakdownBy":"age",
                  "totals":{"impressions":1000,"reach":800,"clicks":40,"spendMinor":1500,"ctr":4.0,"leads":3},
                  "breakdown":[{"key":"25-34","metrics":{"impressions":600,"spendMinor":900,"ctr":5.0}}],
                  "timeline":[{"date":"2026-09-01","metrics":{"impressions":100,"spendMinor":150}}]}}
                """.trimIndent(),
            ),
        )
        server.enqueue(json(200, """{"data":{"objectId":"x","totals":null,"breakdown":[],"timeline":[]}}"""))

        val (report, empty) = server.client().use { client ->
            client.ads.insights(
                "conn_1",
                "cmp_1",
                since = "2026-09-01",
                until = "2026-09-07",
                breakdown = "age",
                daily = true,
                workspaceId = "ws_1",
            ) to client.ads.adInsights("ad_1", "ws_1", since = "2026-09-01", until = "2026-09-07")
        }

        assertEquals(
            "/v1/ads/insights?workspace_id=ws_1&connection_id=conn_1&object_id=cmp_1" +
                "&since=2026-09-01&until=2026-09-07&breakdown=age&daily=true",
            server.takeRequest().path,
        )
        assertEquals("/v1/ads/ad_1/insights?workspace_id=ws_1&since=2026-09-01&until=2026-09-07", server.takeRequest().path)
        assertEquals(4.0, report.totals?.ctr)
        assertEquals("25-34", report.breakdown[0].key)
        assertEquals(150L, report.timeline[0].metrics?.spendMinor)
        assertNull(empty.totals)
    }

    @Test
    fun `pages the leads feed with the cursor`() = runTest {
        server.enqueue(
            json(
                200,
                """{"data":{"leads":[{"id":"f_1","leadId":"m_1","pageId":"page_1","isOrganic":true,
                   "fields":[{"name":"email","values":["jordan@yourbrand.com"]}],
                   "submittedAt":"2026-09-02T08:00:00Z"}],"nextCursor":"cur_2"}}""",
            ),
        )
        server.enqueue(json(200, """{"data":{"leads":[],"nextCursor":null}}"""))

        val (first, next) = server.client().use { client ->
            val first = client.ads.leadsFeed(workspaceId = "ws_1", formId = "form_1", limit = 50)
            first to client.ads.leadsFeed(workspaceId = "ws_1", cursor = first.nextCursor)
        }

        assertEquals("/v1/ads/leads?workspace_id=ws_1&form_id=form_1&limit=50", server.takeRequest().path)
        assertEquals("/v1/ads/leads?workspace_id=ws_1&cursor=cur_2", server.takeRequest().path)
        assertEquals("m_1", first.leads[0].leadId)
        assertEquals(listOf("jordan@yourbrand.com"), first.leads[0].fields[0].values)
        assertEquals(Instant.parse("2026-09-02T08:00:00Z"), first.leads[0].submittedAt)
        assertNull(next.nextCursor)
    }

    @Test
    fun `creates a carousel creative with url tags and lists creatives`() = runTest {
        server.enqueue(json(201, """{"data":{"id":"cr_1","format":"carousel","urlTags":"utm_source=meta"}}"""))
        server.enqueue(json(200, """{"data":{"creatives":[{"id":"cr_1","name":"Spring"}],"workspaceId":"ws_1"}}"""))

        val (creative, list) = server.client().use { client ->
            client.ads.createCreative(
                CreateAdCreativeParams(
                    workspaceId = "ws_1",
                    connectionId = "conn_1",
                    adAccountId = "act_123",
                    pageId = "page_1",
                    name = "Spring",
                    format = "carousel",
                    text = "New season",
                    urlTags = "utm_source=meta",
                    cards = listOf(
                        AdCreativeCard("https://cdn.yourbrand.com/1.jpg"),
                        AdCreativeCard("https://cdn.yourbrand.com/2.jpg"),
                    ),
                ),
            ) to client.ads.creatives("conn_1", "act_123")
        }

        val body = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject
        assertEquals("utm_source=meta", body["urlTags"]!!.jsonPrimitive.content)
        assertEquals(2, body["cards"]!!.jsonArray.size)
        assertEquals("utm_source=meta", creative.urlTags)
        assertEquals("/v1/ads/creatives?connection_id=conn_1&ad_account_id=act_123", server.takeRequest().path)
        assertEquals("Spring", list[0].name)
    }
}
