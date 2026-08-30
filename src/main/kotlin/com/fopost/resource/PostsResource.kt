package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.BulkActionResult
import com.fopost.model.BulkImportResult
import com.fopost.model.BulkImportValidation
import com.fopost.model.CancelResult
import com.fopost.model.Delivery
import com.fopost.model.DuplicatedPost
import com.fopost.model.Page
import com.fopost.model.Post
import com.fopost.model.PostAnalytics
import com.fopost.model.PreflightResult
import com.fopost.model.PublishResult
import com.fopost.model.PublishRun
import com.fopost.model.RetryResult
import com.fopost.param.CancelParams
import com.fopost.param.CreatePostParams
import com.fopost.param.PostListParams
import com.fopost.param.PublishParams
import com.fopost.param.RetryParams
import com.fopost.param.UpdatePostParams
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** Create, schedule, publish, and inspect posts. */
public class PostsResource internal constructor(private val http: ApiClient) {

    /** One page of posts. The page iterates over its items directly. */
    public suspend fun list(params: PostListParams = PostListParams()): Page<Post> =
        http.page("/posts", Post.serializer(), params.toQuery())

    /**
     * Every matching post, fetching one page at a time as the flow is collected.
     *
     * ```
     * client.posts.listAll(PostListParams(workspaceId = id)).collect { println(it.id) }
     * ```
     */
    public fun listAll(params: PostListParams = PostListParams()): Flow<Post> = flow {
        val perPage = params.perPage ?: DEFAULT_PER_PAGE
        var page = params.page ?: 1
        while (true) {
            val fetched = list(params.copy(page = page, perPage = perPage))
            fetched.data.forEach { emit(it) }
            val lastPage = fetched.meta?.lastPage
            val done = fetched.data.isEmpty() ||
                (lastPage != null && page >= lastPage) ||
                (lastPage == null && fetched.data.size < perPage)
            if (done) return@flow
            page++
        }
    }

    public suspend fun get(postId: String): Post =
        http.call("GET", "/posts/$postId", Post.serializer())

    public suspend fun create(params: CreatePostParams): Post =
        http.call("POST", "/posts", Post.serializer(), http.jsonBody(params, CreatePostParams.serializer()))

    public suspend fun update(postId: String, params: UpdatePostParams): Post =
        http.call("PUT", "/posts/$postId", Post.serializer(), http.jsonBody(params, UpdatePostParams.serializer()))

    public suspend fun delete(postId: String) {
        http.send("DELETE", "/posts/$postId")
    }

    /** Copy a post as a fresh draft, keeping its content, accounts, and labels. */
    public suspend fun duplicate(postId: String): DuplicatedPost =
        http.call("POST", "/posts/$postId/duplicate", DuplicatedPost.serializer())

    /**
     * Queue the post for delivery to every account on it.
     *
     * It returns once delivery is queued, not once the post is live — watch the deliveries or a
     * webhook for that.
     */
    public suspend fun publish(postId: String, params: PublishParams = PublishParams()): PublishResult =
        http.call(
            "POST",
            "/posts/$postId/publish",
            PublishResult.serializer(),
            http.jsonBody(params, PublishParams.serializer()),
        )

    /** Retry the deliveries that failed, leaving the ones that succeeded alone. */
    public suspend fun retry(postId: String, params: RetryParams = RetryParams()): RetryResult =
        http.call(
            "POST",
            "/posts/$postId/retry",
            RetryResult.serializer(),
            http.jsonBody(params, RetryParams.serializer()),
        )

    /** Cancel the deliveries that have not gone out yet. */
    public suspend fun cancel(postId: String, accountIds: List<String>? = null): CancelResult =
        http.call(
            "POST",
            "/posts/$postId/cancel",
            CancelResult.serializer(),
            http.jsonBody(CancelParams(accountIds), CancelParams.serializer()),
        )

    /** Per-account blockers and advisory content signals, without publishing anything. */
    public suspend fun preflight(postId: String): PreflightResult =
        http.call("POST", "/posts/$postId/preflight", PreflightResult.serializer())

    public suspend fun deliveries(postId: String): List<Delivery> =
        http.callList("GET", "/posts/$postId/deliveries", Delivery.serializer())

    /** Every publish attempt made for this post, newest first. */
    public suspend fun publishRuns(postId: String): List<PublishRun> =
        http.callList("GET", "/posts/$postId/publish-runs", PublishRun.serializer())

    public suspend fun analytics(postId: String): PostAnalytics =
        http.call("GET", "/posts/$postId/analytics", PostAnalytics.serializer())

    /**
     * Move a selection's schedule by [offsetMinutes], forwards or back.
     *
     * One transaction: a selection containing a post the key cannot reach changes nothing.
     */
    public suspend fun bulkShift(workspaceId: String, postIds: List<String>, offsetMinutes: Int): BulkActionResult =
        bulk(BulkActionRequest("shift", workspaceId, postIds, offsetMinutes = offsetMinutes))

    /** [mode] is `replace` (the default), `add`, or `remove`. */
    public suspend fun bulkLabel(
        workspaceId: String,
        postIds: List<String>,
        labelIds: List<String>,
        mode: String? = null,
    ): BulkActionResult = bulk(BulkActionRequest("label", workspaceId, postIds, labelIds = labelIds, mode = mode))

    public suspend fun bulkDelete(workspaceId: String, postIds: List<String>): BulkActionResult =
        bulk(BulkActionRequest("delete", workspaceId, postIds))

    /** Check a CSV without creating anything. Every row comes back with its errors, if any. */
    public suspend fun validateImport(csv: File, workspaceId: String): BulkImportValidation =
        validateImport(csv.name, csv.readBytes(), workspaceId)

    public suspend fun validateImport(filename: String, csv: ByteArray, workspaceId: String): BulkImportValidation =
        http.call(
            "POST",
            "/posts/bulk-import/validate",
            BulkImportValidation.serializer(),
            csvUpload(filename, csv, workspaceId),
            unwrap = false,
        )

    /** Create every valid row. Keep the returned batch id to roll the import back. */
    public suspend fun commitImport(csv: File, workspaceId: String): BulkImportResult =
        commitImport(csv.name, csv.readBytes(), workspaceId)

    public suspend fun commitImport(filename: String, csv: ByteArray, workspaceId: String): BulkImportResult =
        http.call(
            "POST",
            "/posts/bulk-import/commit",
            BulkImportResult.serializer(),
            csvUpload(filename, csv, workspaceId),
            unwrap = false,
        )

    /** Delete every post a committed batch created. */
    public suspend fun rollbackImport(batchId: String) {
        http.send("DELETE", "/posts/bulk-import/$batchId")
    }

    private suspend fun bulk(request: BulkActionRequest): BulkActionResult =
        http.call(
            "POST",
            "/posts/bulk",
            BulkActionResult.serializer(),
            http.jsonBody(request, BulkActionRequest.serializer()),
            unwrap = false,
        )

    private fun csvUpload(filename: String, csv: ByteArray, workspaceId: String) =
        MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", filename, csv.toRequestBody(CSV_MEDIA_TYPE))
            .addFormDataPart("workspace_id", workspaceId)
            .build()

    @Serializable
    private data class BulkActionRequest(
        val action: String,
        @SerialName("workspace_id") val workspaceId: String,
        @SerialName("post_ids") val postIds: List<String>,
        @SerialName("offset_minutes") val offsetMinutes: Int? = null,
        @SerialName("label_ids") val labelIds: List<String>? = null,
        val mode: String? = null,
    )

    private companion object {
        const val DEFAULT_PER_PAGE = 30
        val CSV_MEDIA_TYPE = "text/csv".toMediaType()
    }
}
