package com.fopost.param

import java.io.File
import java.io.InputStream
import java.net.URLConnection
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import okio.source

/**
 * A file to upload, streamed from a [File] or an [InputStream] of known [size].
 *
 * A [File] is re-read on a retry; an [InputStream] is read once, so a request that fails after
 * the body started streaming is not retried.
 */
public class UploadFile private constructor(
    public val filename: String,
    public val mimeType: String,
    public val size: Long,
    private val body: () -> RequestBody,
) {

    public constructor(file: File, mimeType: String? = null) : this(
        file.name,
        mimeType ?: guessMimeType(file.name),
        file.length(),
        { file.asRequestBody(mimeType.orGuess(file.name)) },
    )

    public constructor(filename: String, mimeType: String, stream: InputStream, size: Long) : this(
        filename,
        mimeType,
        size,
        { StreamBody(stream, size, mimeType) },
    )

    public constructor(filename: String, mimeType: String, content: ByteArray) : this(
        filename,
        mimeType,
        content.size.toLong(),
        { content.toRequestBody(mimeType.toMediaTypeOrNull()) },
    )

    /** Whether a `multipart/form-data` request may carry this file. */
    public fun fitsMultipart(ceiling: Long): Boolean = size <= ceiling

    /** `image`, `video`, or `gif`, as a content block names it. */
    public val mediaType: String
        get() = when {
            mimeType.equals("image/gif", ignoreCase = true) -> "gif"
            mimeType.startsWith("video/", ignoreCase = true) -> "video"
            else -> "image"
        }

    internal fun requestBody(): RequestBody = body()

    private class StreamBody(
        private val stream: InputStream,
        private val size: Long,
        private val mimeType: String,
    ) : RequestBody() {
        override fun contentType() = mimeType.toMediaTypeOrNull()
        override fun contentLength() = size
        override fun isOneShot() = true
        override fun writeTo(sink: BufferedSink) {
            stream.source().use { sink.writeAll(it) }
        }
    }

    private companion object {
        fun guessMimeType(filename: String): String =
            URLConnection.guessContentTypeFromName(filename) ?: "application/octet-stream"

        fun String?.orGuess(filename: String) = (this ?: guessMimeType(filename)).toMediaTypeOrNull()
    }
}
