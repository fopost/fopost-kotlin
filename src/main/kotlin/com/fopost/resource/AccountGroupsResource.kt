package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.AccountGroup
import com.fopost.param.CreateAccountGroupParams
import com.fopost.param.SetAccountGroupMembersParams
import com.fopost.param.UpdateAccountGroupParams

/**
 * Account groups: named sets of accounts a post can target with `accountGroupId`.
 *
 * ```
 * val group = client.accountGroups.create(workspaceId, "Brand A", listOf(accountId))
 * client.posts.create(CreatePostParams(workspaceId, content = blocks, accountGroupId = group.id))
 * ```
 */
public class AccountGroupsResource internal constructor(private val http: ApiClient) {

    public suspend fun list(workspaceId: String? = null): List<AccountGroup> =
        http.callList(
            "GET",
            "/account-groups",
            AccountGroup.serializer(),
            query = mapOf("workspace_id" to workspaceId),
        )

    public suspend fun get(groupId: String): AccountGroup =
        http.call("GET", "/account-groups/$groupId", AccountGroup.serializer())

    public suspend fun create(workspaceId: String, name: String, accountIds: List<String>? = null): AccountGroup =
        http.call(
            "POST",
            "/account-groups",
            AccountGroup.serializer(),
            http.jsonBody(
                CreateAccountGroupParams(workspaceId, name, accountIds),
                CreateAccountGroupParams.serializer(),
            ),
        )

    /** Rename the group. */
    public suspend fun update(groupId: String, name: String): AccountGroup =
        http.call(
            "PATCH",
            "/account-groups/$groupId",
            AccountGroup.serializer(),
            http.jsonBody(UpdateAccountGroupParams(name), UpdateAccountGroupParams.serializer()),
        )

    /** Deletes the group only; its accounts stay connected. */
    public suspend fun delete(groupId: String) {
        http.send("DELETE", "/account-groups/$groupId")
    }

    /** Replace the group's members with exactly [accountIds]. */
    public suspend fun setMembers(groupId: String, accountIds: List<String>): AccountGroup =
        http.call(
            "PUT",
            "/account-groups/$groupId/members",
            AccountGroup.serializer(),
            http.jsonBody(SetAccountGroupMembersParams(accountIds), SetAccountGroupMembersParams.serializer()),
        )
}
