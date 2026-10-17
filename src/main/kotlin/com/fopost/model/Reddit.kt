package com.fopost.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/**
 * A subreddit a Reddit account can post to.
 *
 * [name] carries no `r/` prefix. [canPost] is false where the account may read but not submit,
 * and [isDefault] marks the subreddit posts go to when a post names none.
 */
@Serializable
public data class RedditSubreddit(
    val name: String? = null,
    val title: String? = null,
    val subscribers: Long? = null,
    val over18: Boolean = false,
    @SerialName("can_post") @JsonNames("canPost") val canPost: Boolean = false,
    @SerialName("flair_enabled") @JsonNames("flairEnabled") val flairEnabled: Boolean = false,
    @SerialName("icon_url") @JsonNames("iconUrl") val iconUrl: String? = null,
    @SerialName("is_default") @JsonNames("isDefault") val isDefault: Boolean = false,
)

/** One rule a subreddit publishes. [appliesTo] is `link`, `comment` or `all`. */
@Serializable
public data class RedditSubredditRule(
    val name: String? = null,
    val description: String? = null,
    @SerialName("applies_to") @JsonNames("appliesTo") val appliesTo: String? = null,
)

/** The rules of one subreddit. */
@Serializable
public data class RedditSubredditRules(
    val subreddit: String? = null,
    val rules: List<RedditSubredditRule> = emptyList(),
)

/** A post flair a subreddit offers. The id is valid only in the subreddit it came from. */
@Serializable
public data class RedditFlair(
    val id: String? = null,
    val text: String? = null,
    val editable: Boolean = false,
)

/** The post flairs of one subreddit. */
@Serializable
public data class RedditFlairs(
    val subreddit: String? = null,
    val flairs: List<RedditFlair> = emptyList(),
)

/** The subreddit a Reddit account posts to when a post names none. */
@Serializable
public data class RedditDefaultSubreddit(val subreddit: String? = null)
