package com.fopost

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RedditTest {

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
    fun `lists subreddits with posting rights and the default marked`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":[{"name":"webdev","title":"Web Development","subscribers":2000000,
                          "over18":false,"canPost":true,"flairEnabled":true,"iconUrl":null,"isDefault":true},
                         {"name":"announcements","title":"Announcements","subscribers":1,
                          "over18":false,"canPost":false,"flairEnabled":false,"iconUrl":null,"isDefault":false}]}
                """.trimIndent(),
            ),
        )

        val subreddits = server.client().use { it.accounts.listRedditSubreddits("a1") }

        server.takeRequest().let {
            assertEquals("GET", it.method)
            assertEquals("/v1/accounts/a1/reddit/subreddits", it.path)
        }
        assertEquals("webdev", subreddits[0].name)
        assertTrue(subreddits[0].canPost)
        assertTrue(subreddits[0].isDefault)
        assertFalse(subreddits[1].canPost)
    }

    @Test
    fun `rules and flairs unwrap the subreddit envelope`() = runTest {
        server.enqueue(
            json(
                200,
                """{"data":{"subreddit":"webdev","rules":[{"name":"No self promotion",
                   "description":"Keep it useful","appliesTo":"link"}]}}""".trimIndent(),
            ),
        )
        server.enqueue(
            json(
                200,
                """{"data":{"subreddit":"webdev","flairs":[{"id":"flair_1","text":"Showoff Saturday","editable":false}]}}""",
            ),
        )

        val (rules, flairs) = server.client().use { client ->
            client.accounts.listRedditSubredditRules("a1", "webdev") to
                client.accounts.listRedditFlairs("a1", "webdev")
        }

        assertEquals("/v1/accounts/a1/reddit/subreddits/webdev/rules", server.takeRequest().path)
        assertEquals("/v1/accounts/a1/reddit/flairs?subreddit=webdev", server.takeRequest().path)
        assertEquals("link", rules.single().appliesTo)
        assertEquals("flair_1", flairs.single().id)
        assertFalse(flairs.single().editable)
    }

    @Test
    fun `a null default subreddit is sent explicitly`() = runTest {
        server.enqueue(json(200, """{"data":{"subreddit":"u_someone"}}"""))

        val now = server.client().use { it.accounts.setRedditDefaultSubreddit("a1", null) }

        server.takeRequest().let {
            assertEquals("PUT", it.method)
            assertEquals("/v1/accounts/a1/reddit/default-subreddit", it.path)
            assertEquals("""{"subreddit":null}""", it.body.readUtf8())
        }
        assertEquals("u_someone", now)
    }

    @Test
    fun `a vote sends its direction and reads the vote back`() = runTest {
        server.enqueue(json(200, """{"data":{"id":"i1","vote":"down","canVote":true,"liked":false}}"""))

        val item = server.client().use { it.inbox.vote("i1", "down") }

        server.takeRequest().let {
            assertEquals("POST", it.method)
            assertEquals("/v1/inbox/i1/vote", it.path)
            assertEquals("""{"direction":"down"}""", it.body.readUtf8())
        }
        assertEquals("down", item.vote)
        assertEquals(true, item.canVote)
        assertEquals(false, item.liked)
    }

    @Test
    fun `the subreddit check passes the account and name as query params`() = runTest {
        server.enqueue(
            json(
                200,
                """{"data":{"subreddit":"webdev","exists":true,"can_post":false,
                   "over_18":false,"flair_enabled":true,"ok":false}}""".trimIndent(),
            ),
        )

        val result = server.client().use { it.validate.subreddit("a1", "webdev") }

        server.takeRequest().let {
            assertEquals("GET", it.method)
            assertEquals("/v1/validate/subreddit?account_id=a1&name=webdev", it.path)
        }
        assertTrue(result.exists)
        assertFalse(result.canPost)
        assertFalse(result.ok)
    }
}
