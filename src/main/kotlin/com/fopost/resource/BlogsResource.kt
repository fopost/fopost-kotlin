package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.RemoteArticle
import com.fopost.model.RemoteBlog
import com.fopost.model.RemoteProduct
import com.fopost.param.ArticleParams
import com.fopost.param.ProductParams

/**
 * Content a connected site already owns: the articles on a WordPress site or a
 * Shopify store's blog, and a Shopify store's products.
 *
 * Every id here is the platform's own, never a FoPost id. Reads need the
 * `posts` scope; anything that changes the site needs `posts` and `publish`,
 * because a change here is visible to the site's own readers.
 */
public class BlogsResource internal constructor(private val http: ApiClient) {

    /**
     * Blogs the account can write to. A Shopify store reports every blog it
     * has; WordPress reports its one implicit blog, under the id `default`.
     */
    public suspend fun listBlogs(accountId: String): List<RemoteBlog> =
        http.callList("GET", "/accounts/$accountId/blogs", RemoteBlog.serializer())

    /** Articles on the blog, newest first, drafts included. */
    public suspend fun listArticles(
        accountId: String,
        blogId: String,
        limit: Int? = null,
        status: String? = null,
        q: String? = null,
    ): List<RemoteArticle> =
        http.callList(
            "GET",
            articles(accountId, blogId),
            RemoteArticle.serializer(),
            query = mapOf("limit" to limit, "status" to status, "q" to q),
        )

    public suspend fun getArticle(accountId: String, blogId: String, articleId: String): RemoteArticle =
        http.call("GET", "${articles(accountId, blogId)}/$articleId", RemoteArticle.serializer())

    /** Writes a new article to the blog. Needs the `publish` scope. */
    public suspend fun createArticle(
        accountId: String,
        blogId: String,
        params: ArticleParams,
    ): RemoteArticle =
        http.call(
            "POST",
            articles(accountId, blogId),
            RemoteArticle.serializer(),
            http.jsonBody(params, ArticleParams.serializer()),
        )

    /**
     * Changes the live article in place. Needs the `publish` scope.
     *
     * The article is addressed by its own id and only the fields set on
     * [params] are sent, so an edit never creates a second post on the site.
     */
    public suspend fun updateArticle(
        accountId: String,
        blogId: String,
        articleId: String,
        params: ArticleParams,
    ): RemoteArticle =
        http.call(
            "PATCH",
            "${articles(accountId, blogId)}/$articleId",
            RemoteArticle.serializer(),
            http.jsonBody(params, ArticleParams.serializer()),
        )

    /** Removes the article from the site. Needs `publish`; cannot be undone. */
    public suspend fun deleteArticle(accountId: String, blogId: String, articleId: String) {
        http.send("DELETE", "${articles(accountId, blogId)}/$articleId")
    }

    /** The store's products. */
    public suspend fun listProducts(
        accountId: String,
        limit: Int? = null,
        status: String? = null,
        q: String? = null,
    ): List<RemoteProduct> =
        http.callList(
            "GET",
            "/accounts/$accountId/products",
            RemoteProduct.serializer(),
            query = mapOf("limit" to limit, "status" to status, "q" to q),
        )

    /** Changes a product on the store. Needs `publish`; only what is set changes. */
    public suspend fun updateProduct(
        accountId: String,
        productId: String,
        params: ProductParams,
    ): RemoteProduct =
        http.call(
            "PATCH",
            "/accounts/$accountId/products/$productId",
            RemoteProduct.serializer(),
            http.jsonBody(params, ProductParams.serializer()),
        )

    private fun articles(accountId: String, blogId: String): String =
        "/accounts/$accountId/blogs/$blogId/articles"
}
