package com.fopost.internal

import kotlinx.serialization.json.Json

/**
 * The single [Json] the SDK decodes and encodes with.
 *
 * A field the server adds must never break an older client, and a parameter the caller never
 * set must never be sent, so unknown keys are ignored and nulls are dropped on the way out.
 */
internal fun defaultJson(): Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
    encodeDefaults = true
    coerceInputValues = true
}
