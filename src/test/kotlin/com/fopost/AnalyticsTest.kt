package com.fopost

import com.fopost.param.AnalyticsParams
import com.fopost.param.MetricChangesParams
import com.fopost.param.NativePostsParams
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/** Decay, cadence, per-post timelines, the changes cursor and posts made outside FoPost. */
class AnalyticsTest {

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
    fun `reads the decay bands and the half life`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":{"days":30,"postsMeasured":2,"halfLifeBucket":"1h_3h","bands":[
                {"bucket":"under_1h","label":"First hour","posts":2,"avgEngagements":25,
                 "avgImpressions":300,"shareOfFinal":0.3},
                {"bucket":"6h_12h","label":"6-12 hours","posts":0,"avgEngagements":0,
                 "avgImpressions":0,"shareOfFinal":null}]}}
                """,
            ),
        )

        val decay = server.client().analytics.decay(AnalyticsParams(days = 30, accountId = "acc_1"))
        val request = server.takeRequest()

        assertEquals("/v1/analytics/decay?accountId=acc_1&days=30", request.path)
        assertEquals("1h_3h", decay.halfLifeBucket)
        assertEquals(2, decay.postsMeasured)
        assertEquals(0.3, decay.bands[0].shareOfFinal)
        // A band nothing was measured in reports no share rather than zero
        assertNull(decay.bands[1].shareOfFinal)
    }

    @Test
    fun `reads the cadence weeks and the best band`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":{"days":90,
                "weeks":[{"weekStart":"2026-03-02","posts":2,"engagements":240,
                          "avgEngagementsPerPost":120}],
                "bands":[{"band":"under_3","label":"1-2 a week","weeks":1,"posts":2,
                          "avgPostsPerWeek":2,"avgEngagementsPerPost":120,"engagementRate":0.12}],
                "best":{"band":"under_3","label":"1-2 a week","avgEngagementsPerPost":120}}}
                """,
            ),
        )

        val cadence = server.client().analytics.frequency(AnalyticsParams(days = 90))
        server.takeRequest()

        assertEquals("2026-03-02", cadence.weeks[0].weekStart)
        assertEquals(0.12, cadence.bands[0].engagementRate)
        assertEquals("1-2 a week", cadence.best?.label)
    }

    @Test
    fun `addresses a timeline by permalink`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":{"postId":null,"deliveries":[{"accountId":"acc_1","platform":"twitter",
                "username":"acme","externalPostId":"1","postedAt":"2026-03-02T00:00:00.000Z",
                "points":[{"at":"2026-03-02T00:30:00.000Z","ageMinutes":30,"engagements":40,
                "impressions":400,"reach":null,"likes":30,"comments":null,"shares":null,
                "videoViews":null,"delta":{"impressions":400,"reach":0,"engagements":40,
                "likes":30,"comments":0,"shares":0}}]}]}}
                """,
            ),
        )

        val timeline = server.client().analytics.timeline("https://x.com/acme/status/1")
        val request = server.takeRequest()

        assertEquals(
            "/v1/analytics/posts/https%3A%2F%2Fx.com%2Facme%2Fstatus%2F1/timeline",
            request.path,
        )
        // A post made on the network has no FoPost id
        assertNull(timeline.postId)
        assertEquals(30L, timeline.deliveries[0].points[0].ageMinutes)
        assertEquals(40L, timeline.deliveries[0].points[0].delta.engagements)
    }

    @Test
    fun `carries the changes cursor`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":{"since":"2026-03-02T00:00:00.000Z","cursor":"2026-03-02T06:00:00.000Z",
                "hasMore":true,"changes":[{"accountId":"acc_1","platform":"twitter",
                "externalPostId":"1","postId":"post_1","postedAt":"2026-03-02T00:00:00.000Z",
                "fetchedAt":"2026-03-02T06:00:00.000Z","impressions":900,"reach":null,
                "engagements":90,"likes":70,"comments":10,"shares":10}]}}
                """,
            ),
        )

        val page = server.client().analytics.changes(
            MetricChangesParams(since = "2026-03-02T00:00:00Z", limit = 100),
        )
        val request = server.takeRequest()

        assertTrue(request.path!!.contains("limit=100"))
        assertTrue(page.hasMore)
        assertEquals("post_1", page.changes[0].postId)
    }

    @Test
    fun `reports each delivery of an on-demand refresh`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":{"collected":1,"deliveries":[{"accountId":"acc_1","platform":"twitter",
                "externalPostId":"1","collected":true,"fetchedAt":"2026-03-02T00:30:00.000Z",
                "message":null}]}}
                """,
            ),
        )

        val result = server.client().analytics.collectPost("post_1")
        val request = server.takeRequest()

        assertEquals("POST", request.method)
        assertEquals("/v1/posts/post_1/analytics/collect", request.path)
        assertEquals(1, result.collected)
        assertTrue(result.deliveries[0].collected)
    }

    @Test
    fun `keeps the meta envelope on posts made outside FoPost`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":[{"externalPostId":"1","text":"Posted by hand",
                "permalink":"https://x.com/acme/status/1","thumbnailUrl":null,"mediaType":null,
                "postedAt":"2026-03-02T00:00:00.000Z","fetchedAt":"2026-03-02T06:00:00.000Z",
                "metrics":{"impressions":900,"reach":null,"engagements":90,"likes":70,
                "comments":10,"shares":10,"videoViews":null}}],
                "meta":{"page":1,"perPage":20,"total":1}}
                """,
            ),
        )

        val page = server.client().analytics.nativePosts(
            "acc_1",
            NativePostsParams(page = 1, perPage = 20),
        )
        val request = server.takeRequest()

        assertEquals("/v1/accounts/acc_1/native-posts?page=1&per_page=20", request.path)
        assertEquals(1, page.data.size)
        assertEquals("https://x.com/acme/status/1", page.data[0].permalink)
        assertEquals(90L, page.data[0].metrics.engagements)
        assertEquals(1, page.meta?.total)
    }
}
