package com.fopost.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/** A tappable prompt Messenger or Instagram shows before the first message. */
@Serializable
public data class MetaIceBreaker(
    /** Up to 80 characters. */
    val question: String,
    /** What your webhook receives when the prompt is tapped. */
    val payload: String,
)

/** The ice breakers set on one account. */
@Serializable
public data class MetaIceBreakers(
    @SerialName("ice_breakers") @JsonNames("iceBreakers") val iceBreakers: List<MetaIceBreaker> = emptyList(),
)

/**
 * A persistent-menu item: a `postback` carrying [payload], or a `web_url` carrying an http(s)
 * [url]. The unused one stays null and is not sent.
 */
@Serializable
public data class MetaMenuItem(
    val type: String,
    /** Up to 30 characters. */
    val title: String,
    val payload: String? = null,
    val url: String? = null,
) {
    public companion object {
        /** An item that sends [payload] to your webhook when tapped. */
        public fun postback(title: String, payload: String): MetaMenuItem =
            MetaMenuItem(type = "postback", title = title, payload = payload)

        /** An item that opens [url]. */
        public fun link(title: String, url: String): MetaMenuItem =
            MetaMenuItem(type = "web_url", title = title, url = url)
    }
}

/** One locale's menu; `default` is the fallback every language uses. */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
public data class MetaPersistentMenuEntry(
    @EncodeDefault val locale: String = "default",
    @SerialName("call_to_actions")
    @JsonNames("callToActions")
    @EncodeDefault
    val callToActions: List<MetaMenuItem> = emptyList(),
    @SerialName("composer_input_disabled")
    @JsonNames("composerInputDisabled")
    val composerInputDisabled: Boolean? = null,
)

/** The persistent menu set on one account, one entry per locale. */
@Serializable
public data class MetaPersistentMenu(
    @SerialName("persistent_menu")
    @JsonNames("persistentMenu")
    val persistentMenu: List<MetaPersistentMenuEntry> = emptyList(),
)

/** One locale's greeting, up to 160 characters. */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
public data class MetaGreetingText(
    @EncodeDefault val locale: String = "default",
    val text: String,
)

/** The greeting set on one account, one entry per locale. */
@Serializable
public data class MetaGreeting(
    val greeting: List<MetaGreetingText> = emptyList(),
)

/**
 * What the network delivers to the FoPost webhook for one account. [subscribed] is false when the
 * subscription lapsed or a required field is missing.
 */
@Serializable
public data class WebhookSubscription(
    val subscribed: Boolean = false,
    val fields: List<String> = emptyList(),
    @SerialName("missing_fields") @JsonNames("missingFields") val missingFields: List<String> = emptyList(),
)

/** The outcome of a Messenger hand-over; [appId] is null when control was taken back. */
@Serializable
public data class InboxHandover(
    @SerialName("app_id") @JsonNames("appId") val appId: String? = null,
    /** `passed` or `taken`. */
    val control: String,
)
