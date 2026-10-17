package com.fopost

import com.fopost.model.BlogArticlePublished
import com.fopost.model.WebhookEvents
import java.time.Instant
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class WebhooksVerifyTest {

    private val secret = "whsec_test"
    private val now = Instant.parse("2026-10-11T12:00:00Z")

    private val body = """
        {"event":"article.published","timestamp":"2026-10-11T12:00:00Z",
         "data":{"id":"ar_1","title":"How to build a social media calendar","slug":"social-media-calendar",
                 "excerpt":null,"metaTitle":null,"metaDescription":null,
                 "bodyMarkdown":"# Hello","bodyHtml":"<h1>Hello</h1>",
                 "featuredImage":{"mediaId":"md_1","url":"https://cdn.example/f.jpg","alt":"A calendar"},
                 "images":[],"tags":["planning"],"keyword":"social media calendar","wordCount":1200,
                 "publishedAt":"2026-10-11T12:00:00Z","publishedUrl":"https://yourbrand.com/blog/social-media-calendar"}}
    """.trimIndent().toByteArray()

    private fun timestamped(bytes: ByteArray, at: Long = now.epochSecond): Map<String, String> = mapOf(
        "x-fopost-timestamp" to at.toString(),
        "x-fopost-signature-256" to Webhooks.sign(bytes, secret, at),
        "X-FoPost-Signature" to Webhooks.sign(bytes, secret),
        "X-FoPost-Event" to "article.published",
    )

    @Test
    fun `a valid timestamped signature passes and the payload decodes`() {
        val envelope = Webhooks.verify(body, timestamped(body), secret, now = now)

        assertEquals(WebhookEvents.ARTICLE_PUBLISHED, envelope.event)
        assertEquals("2026-10-11T12:00:00Z", envelope.timestamp)
        val article = envelope.dataAs<BlogArticlePublished>()
        assertEquals("social-media-calendar", article.slug)
        assertEquals("https://yourbrand.com/blog/social-media-calendar", article.publishedUrl)
        assertEquals("A calendar", article.featuredImage?.alt)
        assertEquals(1200, article.wordCount)
    }

    @Test
    fun `a tampered body fails`() {
        val headers = timestamped(body)
        val tampered = body.toString(Charsets.UTF_8).replace("ar_1", "ar_2").toByteArray()

        val failure = assertThrows<WebhookVerificationException> { Webhooks.verify(tampered, headers, secret, now = now) }
        assertEquals("invalid_signature", failure.code)
    }

    @Test
    fun `a stale timestamp fails even with a matching signature`() {
        val stale = now.epochSecond - 301
        val headers = timestamped(body, at = stale)

        val failure = assertThrows<WebhookVerificationException> { Webhooks.verify(body, headers, secret, now = now) }
        assertEquals("stale_timestamp", failure.code)
    }

    @Test
    fun `the plain body signature passes only when no timestamp header is present`() {
        val legacy = mapOf("X-FoPost-Signature" to Webhooks.sign(body, secret))
        assertEquals(WebhookEvents.ARTICLE_PUBLISHED, Webhooks.verify(body, legacy, secret, now = now).event)

        // Once a timestamp travels, the plain signature alone no longer vouches for the body.
        val withTimestamp = legacy + ("X-FoPost-Timestamp" to now.epochSecond.toString())
        val failure = assertThrows<WebhookVerificationException> { Webhooks.verify(body, withTimestamp, secret, now = now) }
        assertEquals("missing_signature", failure.code)

        val wrong = mapOf("X-FoPost-Signature" to "sha256=00")
        assertEquals("invalid_signature", assertThrows<WebhookVerificationException> { Webhooks.verify(body, wrong, secret) }.code)
    }
}
