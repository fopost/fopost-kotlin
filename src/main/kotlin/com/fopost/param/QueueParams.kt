package com.fopost.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A queue slot to create. [weekday] runs 0 (Sunday) to 6 (Saturday), [time] is `HH:MM`
 * 24-hour in [timezone] (an IANA zone name). Leave [accountId] unset for every account.
 */
@Serializable
public data class CreateQueueSlotParams(
    @SerialName("workspace_id") val workspaceId: String,
    val weekday: Int,
    val time: String,
    val timezone: String,
    @SerialName("account_id") val accountId: String? = null,
)

/** A partial update to a queue slot. Only the fields you set are sent. */
@Serializable
public data class UpdateQueueSlotParams(
    @SerialName("account_id") val accountId: String? = null,
    val weekday: Int? = null,
    val time: String? = null,
    val timezone: String? = null,
)
