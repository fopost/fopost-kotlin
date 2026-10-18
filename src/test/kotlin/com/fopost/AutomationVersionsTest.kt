package com.fopost

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AutomationVersionsTest {

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
    fun `lists, reads and restores a version`() = runTest {
        server.enqueue(
            json(
                200,
                """{"data":[{"version":3,"note":"Added a delay","created_by":{"name":"Sam Rivera"},
                 "created_at":"2026-10-18T12:00:00Z","node_count":4,
                 "nodes_added":[{"id":"n4","kind":"delay","label":null}],"nodes_removed":[]}],
                 "meta":{"page":1,"per_page":20,"total":3,"total_pages":1}}""",
            ),
        )
        server.enqueue(
            json(
                200,
                """{"data":{"version":2,"note":null,"created_by":null,"created_at":"2026-10-17T12:00:00Z",
                 "graph":{"nodes":[{"id":"n1","kind":"trigger","label":"New post","config":{"event":"post.published"},
                 "position":0,"layout":{"x":10.5,"y":20}}],"edges":[{"id":"e1","source":"n1","source_handle":null,"target":"n2"}]}}}""",
            ),
        )
        server.enqueue(
            json(
                200,
                """{"data":{"version":4,"graph":{"nodes":[],"edges":[]},
                 "steps":[{"position":0,"node_id":"n2","action_type":"publish","action_config":{"accounts":["acc_1"]}}]}}""",
            ),
        )

        server.client().use { client ->
            val versions = client.automations.versions("au_1", page = 1, perPage = 20)
            val list = server.takeRequest()
            assertEquals("GET", list.method)
            assertEquals("/v1/automations/au_1/versions?page=1&per_page=20", list.path)
            assertEquals(3, versions.single().version)
            assertEquals("Sam Rivera", versions.single().createdBy?.name)
            assertEquals("delay", versions.single().nodesAdded.single().kind)
            assertEquals(1, versions.meta?.lastPage)
            assertEquals(3, versions.meta?.total)

            val version = client.automations.version("au_1", 2)
            val get = server.takeRequest()
            assertEquals("GET", get.method)
            assertEquals("/v1/automations/au_1/versions/2", get.path)
            assertNull(version.createdBy)
            val node = version.graph!!.nodes.single()
            assertEquals("trigger", node.kind)
            assertEquals("post.published", node.config!!["event"]!!.jsonPrimitive.content)
            assertEquals(10.5, node.layout?.x)
            assertEquals("n2", version.graph!!.edges.single().target)

            val restored = client.automations.restoreVersion("au_1", 2)
            val restore = server.takeRequest()
            assertEquals("POST", restore.method)
            assertEquals("/v1/automations/au_1/versions/2/restore", restore.path)
            assertEquals(4, restored.version)
            assertEquals("n2", restored.steps.single().nodeId)
            assertEquals("publish", restored.steps.single().actionType)
        }
    }
}
