package com.fopost

import com.fopost.model.WebhookEvents
import java.time.Instant
import kotlin.test.assertEquals
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class WebhooksVerifyTest {

    private val secret = "whsec_test"
    private val now = Instant.parse("2026-10-11T12:00:00Z")

    private val body = """
        {"event":"post.published","timestamp":"2026-10-11T12:00:00Z",
         "data":{"id":"post_1","status":"published"}}
    """.trimIndent().toByteArray()

    private fun timestamped(bytes: ByteArray, at: Long = now.epochSecond): Map<String, String> = mapOf(
        "x-fopost-timestamp" to at.toString(),
        "x-fopost-signature-256" to Webhooks.sign(bytes, secret, at),
        "X-FoPost-Signature" to Webhooks.sign(bytes, secret),
        "X-FoPost-Event" to "post.published",
    )

    @Test
    fun `a valid timestamped signature passes and the payload decodes`() {
        val envelope = Webhooks.verify(body, timestamped(body), secret, now = now)

        assertEquals(WebhookEvents.POST_PUBLISHED, envelope.event)
        assertEquals("2026-10-11T12:00:00Z", envelope.timestamp)
        val post = envelope.dataAs<JsonObject>()
        assertEquals("post_1", post["id"]?.jsonPrimitive?.content)
        assertEquals("published", post["status"]?.jsonPrimitive?.content)
    }

    @Test
    fun `a tampered body fails`() {
        val headers = timestamped(body)
        val tampered = body.toString(Charsets.UTF_8).replace("post_1", "post_2").toByteArray()

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
        assertEquals(WebhookEvents.POST_PUBLISHED, Webhooks.verify(body, legacy, secret, now = now).event)

        // Once a timestamp travels, the plain signature alone no longer vouches for the body.
        val withTimestamp = legacy + ("X-FoPost-Timestamp" to now.epochSecond.toString())
        val failure = assertThrows<WebhookVerificationException> { Webhooks.verify(body, withTimestamp, secret, now = now) }
        assertEquals("missing_signature", failure.code)

        val wrong = mapOf("X-FoPost-Signature" to "sha256=00")
        assertEquals("invalid_signature", assertThrows<WebhookVerificationException> { Webhooks.verify(body, wrong, secret) }.code)
    }
}
