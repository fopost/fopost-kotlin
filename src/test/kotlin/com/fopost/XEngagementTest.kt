package com.fopost

import com.fopost.param.XSearchParams
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class XEngagementTest {

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
    fun `follows and unfollows an X user with the numeric id in the body`() = runTest {
        server.enqueue(json(200, """{"data":{"account_id":"a1","user_id":"12345","following":true}}"""))
        server.enqueue(json(200, """{"data":{"account_id":"a1","user_id":"12345","following":false}}"""))

        server.client().use { client ->
            val followed = client.accounts.followXUser("a1", "12345")
            assertEquals("a1", followed.accountId)
            assertEquals("12345", followed.userId)
            assertEquals(true, followed.following)

            assertEquals(false, client.accounts.unfollowXUser("a1", "12345").following)
        }

        server.takeRequest().let {
            assertEquals("POST", it.method)
            assertEquals("/v1/accounts/a1/x/follow", it.path)
            assertEquals("""{"user_id":"12345"}""", it.body.readUtf8())
        }
        server.takeRequest().let {
            assertEquals("POST", it.method)
            assertEquals("/v1/accounts/a1/x/unfollow", it.path)
            assertEquals("""{"user_id":"12345"}""", it.body.readUtf8())
        }
    }

    @Test
    fun `reads one X post with the account in the query`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":{"id":"999","text":"Hello","permalink":"https://x.com/acme/status/999",
                  "author_name":"Acme","author_handle":"acme","author_avatar_url":null,"author_followers":42,
                  "conversation_id":"999","parent_external_id":null,"likes":3,"replies":1,"reposts":0,"quotes":null,
                  "platform_created_at":"2026-10-01T09:00:00Z"}}
                """.trimIndent(),
            ),
        )

        val post = server.client().use { it.accounts.getXPost("999", "a1") }

        server.takeRequest().let {
            assertEquals("GET", it.method)
            assertEquals("/v1/x/posts/999?account_id=a1", it.path)
        }
        assertEquals("999", post.id)
        assertEquals("acme", post.authorHandle)
        assertEquals(42, post.authorFollowers)
        assertEquals(3, post.likes)
        assertNull(post.quotes)
        assertEquals(Instant.parse("2026-10-01T09:00:00Z"), post.platformCreatedAt)
    }

    @Test
    fun `searches X posts and reads the next token`() = runTest {
        server.enqueue(json(200, """{"data":[{"id":"1","text":"a"},{"id":"2","text":"b"}],"meta":{"next_token":"tok2"}}"""))
        server.enqueue(json(200, """{"data":[],"meta":{"next_token":null}}"""))

        server.client().use { client ->
            val first = client.accounts.searchXPosts(XSearchParams(accountId = "a1", q = "fopost", maxResults = 20))
            assertEquals(listOf("1", "2"), first.map { it.id })
            assertEquals("tok2", first.nextToken)

            val last = client.accounts.searchXPosts(XSearchParams(accountId = "a1", q = "fopost", nextToken = first.nextToken))
            assertEquals(0, last.data.size)
            assertNull(last.nextToken)
        }

        server.takeRequest().let {
            assertEquals("GET", it.method)
            assertEquals("/v1/x/search?account_id=a1&q=fopost&max_results=20", it.path)
        }
        assertEquals("/v1/x/search?account_id=a1&q=fopost&next_token=tok2", server.takeRequest().path)
    }
}
