package com.fopost.examples

import com.fopost.FoPost
import com.fopost.FoPostException
import com.fopost.PaymentRequiredException
import com.fopost.RateLimitException
import com.fopost.param.CreatePostParams
import com.fopost.param.contentOf
import kotlinx.coroutines.runBlocking

/**
 * Creates a post against a running API, and optionally publishes it.
 *
 * ```
 * export FOPOST_API_KEY=fp_...
 * export FOPOST_BASE_URL=http://localhost:8080/v1     # optional
 *
 * ./gradlew :examples:run --args="Hello from Kotlin --publish"
 * ```
 */
fun main(args: Array<String>): Unit = runBlocking {
    val text = args.firstOrNull { !it.startsWith("--") } ?: "Hello from the Kotlin SDK"
    val publish = args.contains("--publish")

    FoPost().use { client ->
        try {
            val workspace = client.workspaces.list().firstOrNull()
            if (workspace?.id == null) {
                println("No workspaces on this key.")
                return@use
            }

            val accounts = client.accounts.list(workspace.id)
            if (accounts.isEmpty()) {
                println("No connected accounts in ${workspace.name}.")
                return@use
            }

            val post = client.posts.create(
                CreatePostParams(
                    workspaceId = workspace.id!!,
                    accounts = accounts.mapNotNull { it.id },
                    content = contentOf(text),
                ),
            )
            println("Created ${post.id} (${post.status})")

            if (publish) {
                val result = client.posts.publish(post.id!!)
                println("Publishing: ${result.postStatus}")
                result.deliveries.forEach { println("  ${it.platform} -> ${it.status}") }
            }
        } catch (e: PaymentRequiredException) {
            println("Out of credits — upgrade at ${e.upgradeUrl}")
        } catch (e: RateLimitException) {
            println("Rate limited, retry in ${e.retryAfter}")
        } catch (e: FoPostException) {
            println("API ${e.status} (${e.code}): ${e.message}")
        }
    }
}
