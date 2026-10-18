package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.ConnectSession
import com.fopost.param.CreateConnectSessionParams

/** Hosted connect links, so your users connect their own accounts without seeing the dashboard. */
public class ConnectSessionsResource internal constructor(private val http: ApiClient) {

    /** The returned [ConnectSession.url] is shown once; hand it to the user now. */
    public suspend fun create(params: CreateConnectSessionParams): ConnectSession =
        http.call(
            "POST",
            "/connect-sessions",
            ConnectSession.serializer(),
            http.jsonBody(params, CreateConnectSessionParams.serializer()),
        )

    /** Poll this to learn when the session was used and which account it produced. */
    public suspend fun get(sessionId: String): ConnectSession =
        http.call("GET", "/connect-sessions/$sessionId", ConnectSession.serializer())
}
