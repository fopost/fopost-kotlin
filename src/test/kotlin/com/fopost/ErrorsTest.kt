package com.fopost

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ErrorsTest {

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
    fun `maps every documented status onto its own type`() = runTest {
        val cases = listOf(
            400 to ValidationException::class,
            422 to ValidationException::class,
            401 to AuthenticationException::class,
            402 to PaymentRequiredException::class,
            403 to PermissionDeniedException::class,
            404 to NotFoundException::class,
            409 to ApiException::class,
            500 to ServerException::class,
        )

        for ((status, type) in cases) {
            server.enqueue(json(status, """{"error":"boom","message":"it broke"}"""))
            val client = server.client(maxRetries = 1)
            val failure = assertThrows<FoPostException> { client.use { it.workspaces.list() } }
            assertEquals(type, failure::class, "status $status")
            assertEquals(status, failure.status)
            assertEquals("boom", failure.code)
            assertEquals("it broke", failure.message)
        }
    }

    @Test
    fun `a 402 carries the upgrade url and a 429 carries the wait`() = runTest {
        server.enqueue(
            json(402, """{"error":"subscription_required","message":"no plan","upgrade_url":"https://fopost.com/pricing"}"""),
        )
        server.enqueue(
            json(429, """{"error":"rate_limited","message":"slow down"}""")
                .setHeader("Retry-After", "7")
                .setHeader("X-RateLimit-Limit", "100")
                .setHeader("X-RateLimit-Remaining", "0"),
        )

        server.client(maxRetries = 1).use { client ->
            val payment = assertThrows<PaymentRequiredException> { client.workspaces.list() }
            assertEquals("https://fopost.com/pricing", payment.upgradeUrl)

            val limited = assertThrows<RateLimitException> { client.workspaces.list() }
            assertEquals(7.seconds, limited.retryAfter)
            assertEquals(100, limited.rateLimit?.limit)
            assertEquals(0, limited.rateLimit?.remaining)
        }
    }

    @Test
    fun `falls back to the status when the body is not the error envelope`() = runTest {
        server.enqueue(json(418, "not json at all"))

        val failure = assertThrows<ApiException> { server.client(maxRetries = 1).use { it.workspaces.list() } }

        assertEquals(418, failure.status)
        assertEquals("HTTP 418", failure.message)
        assertNull(failure.code)
    }
}
