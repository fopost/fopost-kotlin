package com.fopost.internal

import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** Reads and writes an ISO 8601 timestamp as a [java.time.Instant]. */
public object InstantSerializer : KSerializer<Instant> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.fopost.Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        val raw = decoder.decodeString()
        return try {
            Instant.parse(raw)
        } catch (e: DateTimeParseException) {
            // Some fields carry a zone offset rather than a Z suffix.
            OffsetDateTime.parse(raw).toInstant()
        }
    }
}
