package com.fopost

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PlatformExtrasTest {

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
    fun `creating a pinterest board sends only what was given`() = runTest {
        server.enqueue(json(201, """{"data":{"id":"b1","name":"Recipes","privacy":"PUBLIC"}}"""))

        val board = server.client().use { it.accounts.createPinterestBoard("a1", name = "Recipes") }

        server.takeRequest().let {
            assertEquals("POST", it.method)
            assertEquals("/v1/accounts/a1/pinterest/boards", it.path)
            assertEquals("""{"name":"Recipes"}""", it.body.readUtf8())
        }
        assertEquals("b1", board.id)
    }

    @Test
    fun `setting the default youtube playlist sends null to clear it`() = runTest {
        server.enqueue(json(200, """{"data":{"playlist_id":null}}"""))

        val stored = server.client().use { it.accounts.setDefaultYouTubePlaylist("a1", null) }

        server.takeRequest().let {
            assertEquals("PUT", it.method)
            assertEquals("""{"playlist_id":null}""", it.body.readUtf8())
        }
        assertNull(stored)
    }

    @Test
    fun `playlists mark the stored default`() = runTest {
        server.enqueue(json(200, """{"data":[{"id":"PL1","title":"Tutorials","is_default":true}]}"""))

        val playlist = server.client().use { it.accounts.listYouTubePlaylists("a1") }.single()

        assertTrue(playlist.isDefault)
    }

    @Test
    fun `bluesky languages round trip`() = runTest {
        server.enqueue(json(200, """{"data":{"languages":["en","pt-BR"]}}"""))

        val result = server.client().use { it.accounts.setBlueskyLanguages("a1", listOf("en", "pt-BR")) }

        assertEquals("""{"languages":["en","pt-BR"]}""", server.takeRequest().body.readUtf8())
        assertEquals(listOf("en", "pt-BR"), result.languages)
    }

    @Test
    fun `tiktok creator info reports the account's own switches`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":{"privacy_level_options":["PUBLIC_TO_EVERYONE"],"comment_disabled":false,
                         "duet_disabled":true,"stitch_disabled":false,
                         "max_video_post_duration_sec":600}}
                """.trimIndent(),
            ),
        )

        val info = server.client().use { it.accounts.getTikTokCreatorInfo("a1") }

        assertTrue(info.duetDisabled)
        assertFalse(info.stitchDisabled)
        assertEquals(600, info.maxVideoPostDurationSec)
    }

    @Test
    fun `tiktok music search passes the query through`() = runTest {
        server.enqueue(json(200, """{"data":[{"id":"m1","title":"Sunrise","author":"Kite"}]}"""))

        val tracks = server.client().use { it.accounts.searchTikTokMusic("a1", "sunrise", limit = 5) }

        assertEquals("m1", tracks.single().id)
        val path = server.takeRequest().path
        assertTrue(path!!.contains("q=sunrise"))
        assertTrue(path.contains("limit=5"))
    }

    @Test
    fun `tiktok video lookup returns the address a repurpose run reads`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":{"video_id":"7300000000000000000",
                         "download_url":"https://www.tiktok.com/@a/video/7300000000000000000"}}
                """.trimIndent(),
            ),
        )

        val video =
            server.client().use {
                it.accounts.lookupTikTokVideo("a1", "https://www.tiktok.com/@a/video/7300000000000000000")
            }

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("7300000000000000000", video.videoId)
        assertNotNull(video.downloadUrl)
    }

    @Test
    fun `instagram stories ask for insights only when requested`() = runTest {
        server.enqueue(json(200, """{"data":[{"id":"s1","media_type":"IMAGE"}]}"""))
        server.enqueue(json(200, """{"data":[{"id":"s1","media_type":"IMAGE","insights":{"views":40}}]}"""))

        val stories =
            server.client().use {
                it.accounts.listInstagramStories("a1")
                it.accounts.listInstagramStories("a1", insights = true)
            }

        assertEquals("/v1/accounts/a1/instagram/stories", server.takeRequest().path)
        assertEquals("/v1/accounts/a1/instagram/stories?insights=true", server.takeRequest().path)
        assertEquals(40, stories.single().insights?.get("views"))
    }

    @Test
    fun `linkedin mentions carry the annotation to paste`() = runTest {
        server.enqueue(
            json(
                200,
                """
                {"data":[{"urn":"urn:li:organization:2414183","name":"Devtestco",
                          "annotation":"@[Devtestco](urn:li:organization:2414183)"}]}
                """.trimIndent(),
            ),
        )

        val mention = server.client().use { it.accounts.searchLinkedInMentions("a1", "devtestco") }.single()

        assertEquals("/v1/accounts/a1/linkedin/mentions?q=devtestco", server.takeRequest().path)
        assertEquals("@[Devtestco](urn:li:organization:2414183)", mention.annotation)
    }
}
