package com.fopost

import com.fopost.model.AdBudget
import com.fopost.model.AdTargeting
import com.fopost.param.AdCommentParams
import com.fopost.param.ConversionEvent
import com.fopost.param.CreateAdCampaignParams
import com.fopost.param.CreateAdParams
import com.fopost.param.UploadConversionsParams
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AdsTikTokTest {

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
    fun `reads business centers, identities and spark posts on the right paths`() = runTest {
        server.enqueue(json(200, """{"data":[{"id":"bc1","name":"Brand HQ","role":"ADMIN"}]}"""))
        server.enqueue(json(200, """{"data":[{"id":"idt_1","type":"CUSTOMIZED_USER","name":"Your Brand"}]}"""))
        server.enqueue(json(200, """{"data":[{"id":"item_99","identityId":"idt_1","views":48213}]}"""))

        server.client().use { client ->
            assertEquals("Brand HQ", client.ads.tiktokBusinessCenters("conn_1", "ws_1").single().name)
            assertEquals(
                "CUSTOMIZED_USER",
                client.ads.tiktokIdentities("conn_1", "7011", "ws_1").single().type,
            )
            assertEquals(
                48213L,
                client.ads.sparkPosts("conn_1", "7011", "idt_1", "ws_1").single().views,
            )
        }

        assertTrue(server.takeRequest().path!!.startsWith("/v1/ads/tiktok/business-centers"))
        assertTrue(server.takeRequest().path!!.contains("ad_account_id=7011"))
        assertTrue(server.takeRequest().path!!.contains("identity_id=idt_1"))
    }

    @Test
    fun `sends sparkPostId on an ad and smartPlus on a campaign`() = runTest {
        server.enqueue(json(201, """{"data":{"id":"ad_1","workspaceId":"ws_1","kind":"ad","name":"Spark","goal":"traffic","status":"paused"}}"""))
        server.enqueue(json(201, """{"data":{"id":"c1","name":"Smart","status":"PAUSED"}}"""))

        server.client().use { client ->
            client.ads.create(
                CreateAdParams(
                    workspaceId = "ws_1",
                    connectionId = "conn_1",
                    adAccountId = "7011",
                    pageId = "idt_1",
                    name = "Spark",
                    goal = "traffic",
                    budget = AdBudget(minor = 2000, type = "daily"),
                    targeting = AdTargeting(countries = listOf("US"), ageMin = 18, ageMax = 44),
                    text = "",
                    sparkPostId = "item_99",
                ),
            )
            client.ads.createCampaign(
                CreateAdCampaignParams(
                    workspaceId = "ws_1",
                    connectionId = "conn_1",
                    adAccountId = "7011",
                    name = "Smart",
                    goal = "traffic",
                    smartPlus = true,
                ),
            )
        }

        val adBody = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject
        assertEquals("item_99", adBody["sparkPostId"]!!.jsonPrimitive.content)
        val campaignBody = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject
        assertEquals("true", campaignBody["smartPlus"]!!.jsonPrimitive.content)
    }

    @Test
    fun `uploads conversions and reports what the network accepted`() = runTest {
        server.enqueue(json(202, """{"data":{"accepted":2}}"""))

        val accepted = server.client().use { client ->
            client.ads.uploadConversions(
                UploadConversionsParams(
                    workspaceId = "ws_1",
                    connectionId = "conn_1",
                    adAccountId = "7011",
                    pixelId = "px_1",
                    events = listOf(
                        ConversionEvent(
                            eventName = "CompletePayment",
                            occurredAt = "2026-09-18T10:04:00Z",
                            valueMinor = 4999,
                        ),
                    ),
                ),
            )
        }

        assertEquals(2L, accepted)
        val request = server.takeRequest()
        assertEquals("/v1/ads/conversions", request.path)
        val body = Json.parseToJsonElement(request.body.readUtf8()).jsonObject
        assertEquals("px_1", body["pixelId"]!!.jsonPrimitive.content)
    }

    @Test
    fun `reads a page of comments and answers, hides and deletes one`() = runTest {
        server.enqueue(
            json(
                200,
                """{"data":{"comments":[{"id":"cm_1","text":"nice","likes":3,"hidden":true}],"nextCursor":"2"}}""",
            ),
        )
        server.enqueue(json(201, """{"data":{"replyId":"cm_2"}}"""))
        server.enqueue(json(200, """{"message":"Comment hidden"}"""))
        server.enqueue(json(200, """{"message":"Comment deleted"}"""))

        val scope = AdCommentParams(workspaceId = "ws_1", connectionId = "conn_1", adId = "ad_1")

        server.client().use { client ->
            val page = client.ads.comments("conn_1", "ad_1", workspaceId = "ws_1")
            assertEquals("2", page.nextCursor)
            assertTrue(page.comments.single().hidden)
            assertEquals(3L, page.comments.single().likes)

            assertEquals("cm_2", client.ads.replyToComment("cm_1", scope.copy(text = "Friday!")))
            client.ads.setCommentHidden("cm_1", scope.copy(hidden = true))
            client.ads.deleteComment("cm_1", scope)
        }

        assertTrue(server.takeRequest().path!!.contains("ad_id=ad_1"))
        assertEquals("/v1/ads/comments/cm_1/reply", server.takeRequest().path)
        val hide = server.takeRequest()
        assertEquals("/v1/ads/comments/cm_1/hide", hide.path)
        assertEquals("true", Json.parseToJsonElement(hide.body.readUtf8()).jsonObject["hidden"]!!.jsonPrimitive.content)
        val delete = server.takeRequest()
        // The ad travels in the body, because the path already carries the comment.
        assertEquals("DELETE", delete.method)
        assertEquals(
            "ad_1",
            Json.parseToJsonElement(delete.body.readUtf8()).jsonObject["adId"]!!.jsonPrimitive.content,
        )
    }
}
