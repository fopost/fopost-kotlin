package com.fopost.model

/**
 * Every platform the API can publish to.
 *
 * Model fields stay plain [String], so a platform added server-side still parses on an older
 * SDK. These constants are a convenience for callers, never a validation gate.
 */
public object Platforms {
    public const val TWITTER: String = "twitter"
    public const val LINKEDIN: String = "linkedin"
    public const val FACEBOOK: String = "facebook"
    public const val INSTAGRAM: String = "instagram"
    public const val INSTAGRAM_BUSINESS: String = "instagram-business"
    public const val TELEGRAM: String = "telegram"
    public const val TWITCH: String = "twitch"
    public const val DISCORD: String = "discord"
    public const val SLACK: String = "slack"
    public const val REDDIT: String = "reddit"
    public const val PINTEREST: String = "pinterest"
    public const val SNAPCHAT: String = "snapchat"
    public const val TUMBLR: String = "tumblr"
    public const val DRIBBBLE: String = "dribbble"
    public const val MEWE: String = "mewe"
    public const val TIKTOK: String = "tiktok"
    public const val YOUTUBE: String = "youtube"
    public const val BLUESKY: String = "bluesky"
    public const val THREADS: String = "threads"
    public const val MASTODON: String = "mastodon"
    public const val LEMMY: String = "lemmy"
    public const val DEVTO: String = "devto"
    public const val HASHNODE: String = "hashnode"
    public const val MEDIUM: String = "medium"
    public const val SUBSTACK: String = "substack"
    public const val GOOGLE_BUSINESS: String = "google-business"
    public const val KICK: String = "kick"
    public const val LISTMONK: String = "listmonk"
    public const val WORDPRESS: String = "wordpress"
    public const val NOSTR: String = "nostr"
    public const val WHOP: String = "whop"
    public const val SKOOL: String = "skool"

    public val ALL: List<String> = listOf(
        TWITTER, LINKEDIN, FACEBOOK, INSTAGRAM, INSTAGRAM_BUSINESS, TELEGRAM, TWITCH, DISCORD,
        SLACK, REDDIT, PINTEREST, SNAPCHAT, TUMBLR, DRIBBBLE, MEWE, TIKTOK, YOUTUBE, BLUESKY, THREADS,
        MASTODON, LEMMY, DEVTO, HASHNODE, MEDIUM, SUBSTACK, GOOGLE_BUSINESS, KICK, LISTMONK,
        WORDPRESS, NOSTR, WHOP, SKOOL,
    )
}

/**
 * The statuses a post moves through.
 *
 * Only [DRAFT] and [SCHEDULED] may be set by a client; the rest are set by the API as delivery
 * progresses.
 */
public object PostStatus {
    public const val DRAFT: String = "draft"
    public const val SCHEDULED: String = "scheduled"
    public const val PUBLISHING: String = "publishing"
    public const val PUBLISHED: String = "published"
    public const val PARTIALLY_FAILED: String = "partially_failed"
    public const val FAILED: String = "failed"
    public const val CANCELLED: String = "cancelled"

    public val ALL: List<String> =
        listOf(DRAFT, SCHEDULED, PUBLISHING, PUBLISHED, PARTIALLY_FAILED, FAILED, CANCELLED)
}

/** The events a webhook can subscribe to. */
public object WebhookEvents {
    public const val POST_PUBLISHED: String = "post.published"
    public const val POST_FAILED: String = "post.failed"
    public const val POST_PARTIALLY_FAILED: String = "post.partially_failed"
    public const val DELIVERY_PUBLISHED: String = "delivery.published"
    public const val DELIVERY_FAILED: String = "delivery.failed"
    public const val DELIVERY_DELAYED: String = "delivery.delayed"
    public const val ACCOUNT_HEALTH_CHANGED: String = "account.health_changed"

    public val ALL: List<String> = listOf(
        POST_PUBLISHED, POST_FAILED, POST_PARTIALLY_FAILED, DELIVERY_PUBLISHED,
        DELIVERY_FAILED, DELIVERY_DELAYED, ACCOUNT_HEALTH_CHANGED,
    )
}
