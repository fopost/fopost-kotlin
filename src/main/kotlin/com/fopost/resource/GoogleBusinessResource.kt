package com.fopost.resource

import com.fopost.internal.ApiClient
import com.fopost.model.Account
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * Manage a connected Google Business Profile location: the profile itself,
 * attributes, food menus, services, photos, place action links, verification
 * and performance.
 *
 * Google grants Business Profile API access per project. Until that grant lands
 * on a deployment every call here throws a 503 `configuration_error`.
 *
 * Responses relay Google's own shape, field for field, so they come back as
 * [JsonElement] rather than models we would have to keep chasing.
 */
public class GoogleBusinessResource internal constructor(private val http: ApiClient) {

    public companion object {
        /** The daily metrics fetched when a caller names none. */
        public val DEFAULT_DAILY_METRICS: List<String> = listOf(
            "BUSINESS_IMPRESSIONS_DESKTOP_MAPS",
            "BUSINESS_IMPRESSIONS_DESKTOP_SEARCH",
            "BUSINESS_IMPRESSIONS_MOBILE_MAPS",
            "BUSINESS_IMPRESSIONS_MOBILE_SEARCH",
            "CALL_CLICKS",
            "WEBSITE_CLICKS",
            "BUSINESS_DIRECTION_REQUESTS",
        )
    }

    /** The connected location, in the Business Information shape. */
    public suspend fun getLocation(accountId: String): JsonElement =
        read("GET", path(accountId, "/location"))

    /**
     * Patch the profile. Only the keys [fields] carries change, and a JSON null
     * clears that field. Keys are the API's own snake_case names: `title`,
     * `description`, `website_uri`, `primary_phone`, `additional_phones`,
     * `store_code` and `regular_hours`.
     */
    public suspend fun updateLocation(accountId: String, fields: JsonObject): JsonElement =
        read("PATCH", path(accountId, "/location"), body = fields)

    /**
     * The attribute values set on the location, or, with [available], the
     * attributes Google offers for its category and region.
     */
    public suspend fun getAttributes(
        accountId: String,
        available: Boolean = false,
        categoryName: String? = null,
        regionCode: String? = null,
        languageCode: String? = null,
    ): JsonElement = read(
        "GET",
        path(accountId, "/attributes"),
        query = mapOf(
            "available" to if (available) "true" else null,
            "category_name" to categoryName,
            "region_code" to regionCode,
            "language_code" to languageCode,
        ),
    )

    /** Only the named attributes change; every other one is left alone. */
    public suspend fun updateAttributes(accountId: String, attributes: List<JsonElement>): JsonElement =
        read(
            "PATCH",
            path(accountId, "/attributes"),
            body = buildJsonObject { putJsonArray("attributes") { attributes.forEach { add(it) } } },
        )

    /** The location's food menus. */
    public suspend fun getMenus(accountId: String): JsonElement = read("GET", path(accountId, "/menus"))

    /** Google has no per-section patch, so the whole menu set is replaced. */
    public suspend fun replaceMenus(accountId: String, menus: List<JsonElement>): JsonElement =
        read(
            "PUT",
            path(accountId, "/menus"),
            body = buildJsonObject { putJsonArray("menus") { menus.forEach { add(it) } } },
        )

    /** The location's service list. */
    public suspend fun getServices(accountId: String): JsonElement = read("GET", path(accountId, "/services"))

    /** Replace the whole service list. */
    public suspend fun replaceServices(accountId: String, serviceItems: List<JsonElement>): JsonElement =
        read(
            "PUT",
            path(accountId, "/services"),
            body = buildJsonObject { putJsonArray("service_items") { serviceItems.forEach { add(it) } } },
        )

    /** The photos on the location. */
    public suspend fun listMedia(
        accountId: String,
        pageSize: Int? = null,
        pageToken: String? = null,
    ): JsonElement = read(
        "GET",
        path(accountId, "/media"),
        query = mapOf("page_size" to pageSize, "page_token" to pageToken),
    )

    /**
     * Add a photo from the media library. The asset has to be in a workspace the
     * caller can reach, and JPEG or PNG.
     */
    public suspend fun addMedia(
        accountId: String,
        mediaId: String,
        category: String = "ADDITIONAL",
        description: String? = null,
    ): JsonElement = read(
        "POST",
        path(accountId, "/media"),
        body = buildJsonObject {
            put("media_id", mediaId)
            put("category", category)
            if (description != null) put("description", description)
        },
    )

    /** Remove a photo by the media key Google returned. */
    public suspend fun deleteMedia(accountId: String, mediaKey: String): JsonElement =
        read("DELETE", path(accountId, "/media/$mediaKey"))

    /** The Book, Order and Reserve links on the listing. */
    public suspend fun listPlaceActions(accountId: String): JsonElement =
        read("GET", path(accountId, "/place-actions"))

    /** Add an action link to the listing. */
    public suspend fun createPlaceAction(
        accountId: String,
        uri: String,
        placeActionType: String,
        isPreferred: Boolean? = null,
    ): JsonElement = read(
        "POST",
        path(accountId, "/place-actions"),
        body = buildJsonObject {
            put("uri", uri)
            put("place_action_type", placeActionType)
            if (isPreferred != null) put("is_preferred", isPreferred)
        },
    )

    /** Patch one action link; a null argument is left alone. */
    public suspend fun updatePlaceAction(
        accountId: String,
        linkId: String,
        uri: String? = null,
        isPreferred: Boolean? = null,
    ): JsonElement = read(
        "PATCH",
        path(accountId, "/place-actions/$linkId"),
        body = buildJsonObject {
            if (uri != null) put("uri", uri)
            if (isPreferred != null) put("is_preferred", isPreferred)
        },
    )

    /** Remove one action link. */
    public suspend fun deletePlaceAction(accountId: String, linkId: String): JsonElement =
        read("DELETE", path(accountId, "/place-actions/$linkId"))

    /** The ways Google will let this location be verified. */
    public suspend fun getVerificationOptions(accountId: String, languageCode: String? = null): JsonElement =
        read("GET", path(accountId, "/verification"), query = mapOf("language_code" to languageCode))

    /**
     * Start a verification. [method] is `ADDRESS`, `EMAIL`, `PHONE_CALL`, `SMS`,
     * `AUTO` or `VETTED_PARTNER`; the response names the pending verification to
     * complete with the PIN.
     */
    public suspend fun startVerification(
        accountId: String,
        method: String,
        languageCode: String? = null,
        phoneNumber: String? = null,
        emailAddress: String? = null,
        mailerContactName: String? = null,
    ): JsonElement = read(
        "POST",
        path(accountId, "/verification/start"),
        body = buildJsonObject {
            put("method", method)
            if (languageCode != null) put("language_code", languageCode)
            if (phoneNumber != null) put("phone_number", phoneNumber)
            if (emailAddress != null) put("email_address", emailAddress)
            if (mailerContactName != null) put("mailer_contact_name", mailerContactName)
        },
    )

    /** Complete a pending verification with the PIN Google sent. */
    public suspend fun completeVerification(
        accountId: String,
        verificationName: String,
        pin: String,
    ): JsonElement = read(
        "POST",
        path(accountId, "/verification/complete"),
        body = buildJsonObject {
            put("verification_name", verificationName)
            put("pin", pin)
        },
    )

    /**
     * Daily impressions, calls, direction requests and clicks for the range. An
     * empty [dailyMetrics] leaves the API's own default set.
     */
    public suspend fun getPerformance(
        accountId: String,
        startDate: String,
        endDate: String,
        dailyMetrics: List<String> = emptyList(),
    ): JsonElement = read(
        "GET",
        path(accountId, "/performance"),
        query = mapOf(
            "start_date" to startDate,
            "end_date" to endDate,
            "daily_metrics" to dailyMetrics.ifEmpty { null },
        ),
    )

    /** The search terms people used to find the listing, by month. */
    public suspend fun getSearchKeywords(
        accountId: String,
        startDate: String,
        endDate: String,
        pageToken: String? = null,
    ): JsonElement = read(
        "GET",
        path(accountId, "/performance"),
        query = mapOf(
            "keywords" to "true",
            "start_date" to startDate,
            "end_date" to endDate,
            "page_token" to pageToken,
        ),
    )

    /**
     * Hand the location to another workspace the caller owns. The connection and
     * every row keyed to it move in one transaction.
     */
    public suspend fun assign(accountId: String, workspaceId: String): Account = http.call(
        "POST",
        path(accountId, "/assign"),
        Account.serializer(),
        http.jsonBody(buildJsonObject { put("workspace_id", workspaceId) }),
    )

    private suspend fun read(
        method: String,
        path: String,
        body: JsonObject? = null,
        query: Map<String, Any?>? = null,
    ): JsonElement = http.unwrap(http.send(method, path, body?.let { http.jsonBody(it) }, query))

    private fun path(accountId: String, suffix: String): String = "/accounts/$accountId/gbp$suffix"
}
