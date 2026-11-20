package com.fopost

import com.fopost.model.VideoEditOperation
import com.fopost.param.CreateVideoEditParams
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VideoEditsTest {

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

    private val job = """{"id":"j1","status":"succeeded","progress":1,"source_media_id":"m1",
        "operations":[{"op":"trim","params":{"start":1.5,"end":9}}],"result_media_id":"m2",
        "result_media":{"id":"m2","preview_url":"https://api.test/v1/media/m2/file","mime_type":"video/mp4"},
        "extra_outputs":[{"media_id":"m3","label":"Subtitles","mime":"text/vtt","preview_url":null}],
        "result":null,"error":null,"created_at":"2026-11-01T10:00:00Z","updated_at":"2026-11-01T10:01:00Z",
        "completed_at":"2026-11-01T10:01:00Z"}"""

    @Test
    fun `ops lists the operations`() = runTest {
        server.enqueue(json(200, """{"data":[{"name":"trim","available":true,"uses_ai":false},
            {"name":"subtitles","available":false,"uses_ai":true}]}"""))

        val ops = server.client().use { it.videoEdits.ops() }

        assertEquals(listOf("trim", "subtitles"), ops.map { it.name })
        assertEquals(true, ops[1].usesAi)
        assertEquals(false, ops[1].available)
        val req = server.takeRequest()
        assertEquals("GET", req.method)
        assertEquals("/v1/video-edits/ops", req.path)
    }

    @Test
    fun `list sends the filters and decodes jobs`() = runTest {
        server.enqueue(json(200, """{"data":[$job]}"""))

        val jobs = server.client().use { it.videoEdits.list(workspaceId = "ws_1", mediaId = "m1", limit = 5) }

        val j = jobs.single()
        assertEquals("j1", j.id)
        assertEquals("succeeded", j.status)
        assertEquals(1.0, j.progress)
        assertEquals("m2", j.resultMedia!!.id)
        assertEquals("video/mp4", j.resultMedia!!.mimeType)
        assertEquals("Subtitles", j.extraOutputs!!.single().label)
        assertNull(j.extraOutputs!!.single().previewUrl)
        assertEquals(Instant.parse("2026-11-01T10:01:00Z"), j.completedAt)
        assertEquals(1.5, j.operations!!.single().params!!["start"]!!.jsonPrimitive.double)
        assertEquals("/v1/video-edits?workspace_id=ws_1&media_id=m1&limit=5", server.takeRequest().path)
    }

    @Test
    fun `create posts the operations and decodes the bare job`() = runTest {
        server.enqueue(json(201, job))

        val created = server.client().use {
            it.videoEdits.create(
                CreateVideoEditParams(
                    mediaId = "m1",
                    operations = listOf(
                        VideoEditOperation("trim", buildJsonObject { put("start", 1.5); put("end", 9) }),
                        VideoEditOperation("denoise"),
                    ),
                ),
            )
        }

        assertEquals("j1", created.id)
        assertEquals("m1", created.sourceMediaId)
        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/v1/video-edits", req.path)
        val body = Json.parseToJsonElement(req.body.readUtf8()).jsonObject
        assertEquals("m1", body["media_id"]!!.jsonPrimitive.content)
        assertFalse(body.containsKey("workspace_id"))
        val ops = body["operations"]!!.jsonArray
        assertEquals("trim", ops[0].jsonObject["op"]!!.jsonPrimitive.content)
        assertEquals(9.0, ops[0].jsonObject["params"]!!.jsonObject["end"]!!.jsonPrimitive.double)
        assertEquals("denoise", ops[1].jsonObject["op"]!!.jsonPrimitive.content)
        assertFalse(ops[1].jsonObject.containsKey("params"))
    }

    @Test
    fun `get reads one job`() = runTest {
        server.enqueue(json(200, job))

        val j = server.client().use { it.videoEdits.get("j1", workspaceId = "ws_1") }

        assertEquals("m2", j.resultMediaId)
        val req = server.takeRequest()
        assertEquals("GET", req.method)
        assertEquals("/v1/video-edits/j1?workspace_id=ws_1", req.path)
    }
}
