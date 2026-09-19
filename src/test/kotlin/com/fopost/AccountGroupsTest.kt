package com.fopost

import com.fopost.param.CreatePostParams
import com.fopost.param.contentOf
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AccountGroupsTest {

    private lateinit var server: MockWebServer

    private val group = """
        {"data":{"id":"g1","name":"Brand A","account_ids":["a1","a2"],
                 "created_at":"2026-09-01T00:00:00Z","updated_at":"2026-09-02T00:00:00Z"}}
    """.trimIndent()

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
    fun `lists groups for a workspace`() = runTest {
        server.enqueue(json(200, """{"data":[{"id":"g1","name":"Brand A","account_ids":["a1","a2"]}]}"""))

        val groups = server.client().use { it.accountGroups.list("w1") }

        assertEquals("/v1/account-groups?workspace_id=w1", server.takeRequest().path)
        assertEquals(listOf("a1", "a2"), groups.single().accountIds)
    }

    @Test
    fun `creates a group with its members`() = runTest {
        server.enqueue(json(201, group))

        val created = server.client().use { it.accountGroups.create("w1", "Brand A", listOf("a1", "a2")) }

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals(
            """{"workspace_id":"w1","name":"Brand A","account_ids":["a1","a2"]}""",
            request.body.readUtf8(),
        )
        assertEquals("g1", created.id)
        assertEquals("2026-09-02T00:00:00Z", created.updatedAt.toString())
    }

    @Test
    fun `gets, renames, deletes and replaces members`() = runTest {
        server.enqueue(json(200, group))
        server.enqueue(json(200, group))
        server.enqueue(json(200, """{"message":"Account group deleted"}"""))
        server.enqueue(json(200, group))

        server.client().use { client ->
            client.accountGroups.get("g1")
            client.accountGroups.update("g1", "Brand B")
            client.accountGroups.delete("g1")
            client.accountGroups.setMembers("g1", listOf("a3"))
        }

        assertEquals("/v1/account-groups/g1", server.takeRequest().path)
        server.takeRequest().let {
            assertEquals("PATCH", it.method)
            assertEquals("""{"name":"Brand B"}""", it.body.readUtf8())
        }
        assertEquals("DELETE", server.takeRequest().method)
        server.takeRequest().let {
            assertEquals("PUT", it.method)
            assertEquals("/v1/account-groups/g1/members", it.path)
            assertEquals("""{"account_ids":["a3"]}""", it.body.readUtf8())
        }
    }

    @Test
    fun `filters accounts by group and reads the platform name`() = runTest {
        server.enqueue(json(200, """{"data":[{"id":"a1","name":"Shop","platformName":"Acme"}]}"""))

        val accounts = server.client().use { it.accounts.list(workspaceId = "w1", groupId = "g1") }

        assertEquals("/v1/accounts?workspaceId=w1&group_id=g1", server.takeRequest().path)
        assertEquals("Acme", accounts.single().platformName)
    }

    @Test
    fun `rename sends an explicit null to restore the platform name`() = runTest {
        server.enqueue(json(200, """{"data":{"id":"a1","name":"Acme","platform_name":"Acme"}}"""))

        val account = server.client().use { it.accounts.rename("a1", null) }

        val request = server.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/v1/accounts/a1", request.path)
        assertEquals(JsonNull, Json.parseToJsonElement(request.body.readUtf8()).jsonObject["display_name"])
        assertEquals("Acme", account.platformName)
    }

    @Test
    fun `move returns the new workspace and surfaces blocking tables`() = runTest {
        server.enqueue(json(200, """{"data":{"id":"a1","workspace_id":"w2"}}"""))
        server.enqueue(
            json(409, """{"error":"move_blocked","message":"Blocked","blocking_tables":["posts"]}"""),
        )

        server.client().use { client ->
            assertEquals("w2", client.accounts.move("a1", "w2").workspaceId)
            val error = assertFailsWith<FoPostException> { client.accounts.move("a1", "w2") }
            assertEquals(409, error.status)
            assertEquals("move_blocked", error.code)
            assertEquals(
                "posts",
                error.body!!.jsonObject["blocking_tables"]!!.jsonArray.single().jsonPrimitive.content,
            )
        }

        val request = server.takeRequest()
        assertEquals("/v1/accounts/a1/move", request.path)
        assertEquals("""{"workspace_id":"w2"}""", request.body.readUtf8())
    }

    @Test
    fun `a post can target a group without naming accounts`() = runTest {
        server.enqueue(json(201, """{"id":"p1"}"""))

        server.client().use {
            it.posts.create(CreatePostParams(workspaceId = "w1", content = contentOf("Hi"), accountGroupId = "g1"))
        }

        val body = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject
        assertEquals("g1", body["account_group_id"]!!.jsonPrimitive.content)
        assertFalse(body["accounts"]!!.jsonArray.isNotEmpty())
    }
}
