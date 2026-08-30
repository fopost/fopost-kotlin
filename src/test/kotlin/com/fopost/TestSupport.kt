package com.fopost

import kotlin.time.Duration
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

internal fun MockWebServer.client(maxRetries: Int = FoPost.DEFAULT_MAX_RETRIES): FoPost =
    FoPost(apiKey = "fp_test", baseUrl = url("/v1").toString(), maxRetries = maxRetries)

/** Replaces the retry wait with a recorder, so a test asserts the delay instead of serving it. */
internal fun FoPost.recordWaits(): MutableList<Duration> {
    val waits = mutableListOf<Duration>()
    api.sleeper = { waits += it }
    return waits
}

internal fun json(status: Int, body: String): MockResponse =
    MockResponse()
        .setResponseCode(status)
        .setHeader("Content-Type", "application/json")
        .setBody(body)
