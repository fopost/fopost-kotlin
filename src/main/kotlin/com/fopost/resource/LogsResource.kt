package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.RequestLogPage
import com.fopost.param.LogListParams

/** The request log: every call made with your API keys, with its status and timing. */
public class LogsResource internal constructor(private val http: ApiClient) {

    /** One page, newest first, plus the period's totals in [RequestLogPage.summary]. */
    public suspend fun list(params: LogListParams = LogListParams()): RequestLogPage =
        http.call("GET", "/logs", RequestLogPage.serializer(), query = params.toQuery(), unwrap = false)
}
