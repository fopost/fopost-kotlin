package com.fopost.param

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** A partial profile update: null fields are dropped, so they keep their value. */
@Serializable
public data class UpdateWhatsappProfileParams(
    val about: String? = null,
    val address: String? = null,
    val description: String? = null,
    val vertical: String? = null,
    val websites: List<String>? = null,
    /** A library media id, uploaded first. */
    @SerialName("profile_picture_media_id") val profilePictureMediaId: String? = null,
)

/** Files a template for review. */
@Serializable
public data class CreateWhatsappTemplateParams(
    /** Lowercase letters, digits and underscores. */
    val name: String,
    val language: String,
    /** `MARKETING`, `UTILITY` or `AUTHENTICATION`. */
    val category: String,
    val components: List<JsonElement>,
    /** Lets the platform re-file a template it judges to be another category. */
    @SerialName("allow_category_change") val allowCategoryChange: Boolean? = null,
)

/** Creates a template from one of the platform's library entries. */
@Serializable
public data class ImportWhatsappTemplateParams(
    @SerialName("library_template_name") val libraryTemplateName: String,
    val name: String,
    val language: String,
    val category: String,
    @SerialName("library_template_button_inputs")
    val libraryTemplateButtonInputs: List<JsonElement>? = null,
)

/** Edits a template. The name cannot change; create a new one instead. */
@Serializable
public data class UpdateWhatsappTemplateParams(
    val category: String? = null,
    val components: List<JsonElement>? = null,
)

/** Creates or updates a group. */
@Serializable
public data class WhatsappGroupParams(
    val subject: String? = null,
    val description: String? = null,
)

/** Turns the cart or the catalog on or off. */
@Serializable
public data class UpdateWhatsappCommerceParams(
    @SerialName("is_cart_enabled") val cartEnabled: Boolean? = null,
    @SerialName("is_catalog_visible") val catalogVisible: Boolean? = null,
)

/** Creates a draft flow; its screens are uploaded separately. */
@Serializable
public data class CreateWhatsappFlowParams(
    val name: String,
    val categories: List<String>,
    /** Where the platform calls back for a flow that reads live data. */
    @SerialName("endpoint_uri") val endpointUri: String? = null,
    @SerialName("clone_flow_id") val cloneFlowId: String? = null,
)

/** Changes a flow's metadata, not its screens. */
@Serializable
public data class UpdateWhatsappFlowParams(
    val name: String? = null,
    val categories: List<String>? = null,
    @SerialName("endpoint_uri") val endpointUri: String? = null,
)
