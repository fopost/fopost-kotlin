package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.Enrolled
import com.fopost.model.Enrollment
import com.fopost.model.Page
import com.fopost.model.PageMeta
import com.fopost.model.Sequence
import com.fopost.model.Unenrolled
import com.fopost.param.CreateSequenceParams
import com.fopost.param.EnrollParams
import com.fopost.param.EnrollmentListParams
import com.fopost.param.SequenceListParams
import com.fopost.param.UnenrollParams
import com.fopost.param.UpdateSequenceParams
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonObject

/**
 * Drip sequences: a series of messages, each a delay after the one before, walked per enrolled
 * contact.
 *
 * The messaging window applies to every step. A step that comes due outside it is skipped rather
 * than sent, and the enrollment carries on — so someone can complete a sequence having received
 * only some of its messages.
 *
 * Reading needs the `inbox` scope; [enroll] and [unenroll] also need `publish`.
 */
public class SequencesResource internal constructor(private val http: ApiClient) {

    /** One page of sequences. */
    public suspend fun list(params: SequenceListParams = SequenceListParams()): Page<Sequence> =
        paged("/sequences", params.toQuery(), Sequence.serializer())

    /** One sequence. */
    public suspend fun get(sequenceId: String): Sequence =
        http.call("GET", "/sequences/$sequenceId", Sequence.serializer())

    /** Writes a sequence. Creating one enrolls nobody. */
    public suspend fun create(params: CreateSequenceParams): Sequence =
        http.call(
            "POST",
            "/sequences",
            Sequence.serializer(),
            http.jsonBody(params, CreateSequenceParams.serializer()),
        )

    /**
     * Changes a sequence. Pausing stops every enrollment from firing without ending any of them;
     * resuming picks them up where they stood.
     */
    public suspend fun update(sequenceId: String, params: UpdateSequenceParams): Sequence =
        http.call(
            "PATCH",
            "/sequences/$sequenceId",
            Sequence.serializer(),
            http.jsonBody(params, UpdateSequenceParams.serializer()),
        )

    /**
     * Puts contacts on the sequence, by id or by audience.
     *
     * Re-enrolling someone restarts their walk from the first step rather than running two in
     * parallel. Needs `publish` as well as `inbox`.
     */
    public suspend fun enroll(sequenceId: String, params: EnrollParams): Enrolled =
        http.call(
            "POST",
            "/sequences/$sequenceId/enroll",
            Enrolled.serializer(),
            http.jsonBody(params, EnrollParams.serializer()),
        )

    /**
     * Takes contacts off the sequence. Nothing further fires for them. Needs the `publish` scope.
     */
    public suspend fun unenroll(sequenceId: String, contactIds: List<String>): Unenrolled =
        http.call(
            "POST",
            "/sequences/$sequenceId/unenroll",
            Unenrolled.serializer(),
            http.jsonBody(UnenrollParams(contactIds), UnenrollParams.serializer()),
        )

    /** Who is on the sequence, what step they are at, and when the next one is due. */
    public suspend fun enrollments(
        sequenceId: String,
        params: EnrollmentListParams = EnrollmentListParams(),
    ): Page<Enrollment> =
        paged("/sequences/$sequenceId/enrollments", params.toQuery(), Enrollment.serializer())

    /** Removes a sequence and every enrollment on it. */
    public suspend fun delete(sequenceId: String) {
        http.send("DELETE", "/sequences/$sequenceId")
    }

    private suspend fun <T> paged(
        path: String,
        query: Map<String, Any?>,
        serializer: KSerializer<T>,
    ): Page<T> {
        val element = http.send("GET", path, null, query) as? JsonObject
            ?: return Page(emptyList(), null)
        val items = element["data"]
            ?.let { http.decodeElement(ListSerializer(serializer), it) }
            ?: emptyList()
        val meta = (element["pagination"] as? JsonObject)
            ?.let { http.decodeElement(PageMeta.serializer(), it) }
        return Page(items, meta)
    }
}
