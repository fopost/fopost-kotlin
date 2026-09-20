@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames

/**
 * A blog on a connected site. [id] is the platform's own id, never a FoPost id.
 *
 * A Shopify store reports every blog it has; WordPress has one implicit blog
 * and reports it under the id `default`, so both answer the same shape.
 */
@Serializable
public data class RemoteBlog(
    val id: String? = null,
    val title: String? = null,
    val handle: String? = null,
    val url: String? = null,
)

/** An article that already lives on a connected site. */
@Serializable
public data class RemoteArticle(
    val id: String? = null,
    @SerialName("blog_id") @JsonNames("blogId") val blogId: String? = null,
    val title: String? = null,
    @SerialName("body_html") @JsonNames("bodyHtml") val bodyHtml: String? = null,
    val excerpt: String? = null,
    /** `published`, `draft`, `pending` or `scheduled`. */
    val status: String? = null,
    @SerialName("author_name") @JsonNames("authorName") val authorName: String? = null,
    val tags: List<String> = emptyList(),
    @SerialName("image_url") @JsonNames("imageUrl") val imageUrl: String? = null,
    val url: String? = null,
    @SerialName("published_at") @JsonNames("publishedAt") val publishedAt: Instant? = null,
    @SerialName("updated_at") @JsonNames("updatedAt") val updatedAt: Instant? = null,
)

/** A product on a connected store. [price] is the lowest variant price. */
@Serializable
public data class RemoteProduct(
    val id: String? = null,
    val title: String? = null,
    val handle: String? = null,
    /** `active`, `draft` or `archived`. */
    val status: String? = null,
    val description: String? = null,
    val vendor: String? = null,
    @SerialName("product_type") @JsonNames("productType") val productType: String? = null,
    val tags: List<String> = emptyList(),
    @SerialName("image_url") @JsonNames("imageUrl") val imageUrl: String? = null,
    val url: String? = null,
    val price: String? = null,
    val currency: String? = null,
    @SerialName("updated_at") @JsonNames("updatedAt") val updatedAt: Instant? = null,
)
