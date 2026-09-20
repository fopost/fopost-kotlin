package com.fopost.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The body of a create or an update on an article.
 *
 * Null fields are left out of the request, so an update sends only what the
 * caller named and every other field keeps whatever the site already had. A
 * create needs at least [title] and [body]; an update needs at least one field.
 */
@Serializable
public data class ArticleParams(
    val title: String? = null,
    /** FoPost body markup; the site's own format is rendered from it. */
    val body: String? = null,
    val excerpt: String? = null,
    /** `published`, `draft`, `pending` or `scheduled`. */
    val status: String? = null,
    val tags: List<String>? = null,
    @SerialName("author_name") val authorName: String? = null,
    /** Public http(s) URL of the featured image. */
    @SerialName("image_url") val imageUrl: String? = null,
)

/**
 * The body of an update on a store product. Null fields are left out, so only
 * what the caller named changes. Set at least one.
 */
@Serializable
public data class ProductParams(
    val title: String? = null,
    /** Body markup, rendered to HTML on the store. */
    val description: String? = null,
    /** `active`, `draft` or `archived`. */
    val status: String? = null,
    val tags: List<String>? = null,
    @SerialName("product_type") val productType: String? = null,
    val vendor: String? = null,
)
