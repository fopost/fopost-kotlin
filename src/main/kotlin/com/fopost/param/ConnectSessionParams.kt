package com.fopost.param

import kotlinx.serialization.Serializable

/**
 * A connect session to create. [externalUserId] is your own id for the user; [platforms] lists
 * the networks they may connect. The user lands on [redirectUrl] once done.
 */
@Serializable
public data class CreateConnectSessionParams(
    val externalUserId: String,
    val platforms: List<String>,
    val workspaceId: String? = null,
    val redirectUrl: String? = null,
    val branding: ConnectSessionBrandingParams? = null,
)

/** The name and logo the hosted page shows in place of the default. */
@Serializable
public data class ConnectSessionBrandingParams(
    val name: String? = null,
    val logoUrl: String? = null,
)
