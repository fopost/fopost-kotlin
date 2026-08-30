package com.fopost

import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RetryTest {

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
    fun `retries a 429 and waits for the interval the api asked for`() = runTest {
        server.enqueue(json(429, """{"error":"rate_limited"}""").setHeader("Retry-After", "2"))
        server.enqueue(json(200, """{"data":[]}"""))

        val client = server.client()
        val waits = client.recordWaits()
        client.use { it.workspaces.list() }

        assertEquals(2, server.requestCount)
        assertEquals(listOf(2.seconds), waits)
    }

    @Test
    fun `retries a 500 with exponential backoff`() = runTest {
        server.enqueue(json(500, """{"error":"server_error"}"""))
        server.enqueue(json(500, """{"error":"server_error"}"""))
        server.enqueue(json(200, """{"data":[]}"""))

        val client = server.client()
        val waits = client.recordWaits()
        client.use { it.workspaces.list() }

        assertEquals(3, server.requestCount)
        assertEquals(listOf(500.milliseconds, 1000.milliseconds), waits)
    }

    @Test
    fun `never retries a 400`() = runTest {
        server.enqueue(json(400, """{"error":"validation_failed","message":"accounts is required"}"""))

        val client = server.client()
        val waits = client.recordWaits()
        val failure = assertThrows<ValidationException> { client.use { it.workspaces.list() } }

        assertEquals(1, server.requestCount)
        assertEquals(emptyList(), waits)
        assertEquals("accounts is required", failure.message)
    }

    @Test
    fun `gives up once the attempts are spent`() = runTest {
        repeat(3) { server.enqueue(json(503, """{"error":"unavailable"}""")) }

        val client = server.client()
        client.recordWaits()
        val failure = assertThrows<ServerException> { client.use { it.workspaces.list() } }

        assertEquals(3, server.requestCount)
        assertEquals(503, failure.status)
    }
}
