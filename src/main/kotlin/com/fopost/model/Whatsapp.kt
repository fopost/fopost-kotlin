@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNames

/**
 * The business profile on a WhatsApp number, plus how the platform rates it.
 * [displayNameStatus] is the platform's review state for the display name.
 */
@Serializable
public data class WhatsappProfile(
    val about: String? = null,
    val address: String? = null,
    val description: String? = null,
    val email: String? = null,
    val vertical: String? = null,
    val websites: List<String> = emptyList(),
    @SerialName("profilePictureUrl")
    @JsonNames("profile_picture_url")
    val profilePictureUrl: String? = null,
    @SerialName("displayName") @JsonNames("display_name") val displayName: String? = null,
    @SerialName("displayNameStatus")
    @JsonNames("display_name_status")
    val displayNameStatus: String? = null,
    val username: String? = null,
    @SerialName("qualityRating") @JsonNames("quality_rating") val qualityRating: String? = null,
    @SerialName("messagingLimitTier")
    @JsonNames("messaging_limit_tier")
    val messagingLimitTier: String? = null,
)

/**
 * A message template. [status] is the review outcome the platform assigned;
 * nothing marks a template approved but the platform.
 */
@Serializable
public data class WhatsappTemplate(
    val id: String? = null,
    val name: String? = null,
    val language: String? = null,
    val category: String? = null,
    val status: String? = null,
    @SerialName("rejectedReason") @JsonNames("rejected_reason") val rejectedReason: String? = null,
    val components: List<JsonElement> = emptyList(),
    @SerialName("qualityScore") @JsonNames("quality_score") val qualityScore: String? = null,
)

/**
 * A group on the business number. Participation is invite-only: no endpoint adds
 * anyone, so [inviteLink] is how they join.
 */
@Serializable
public data class WhatsappGroup(
    val id: String? = null,
    val subject: String? = null,
    val description: String? = null,
    @SerialName("participantCount")
    @JsonNames("participant_count")
    val participantCount: Int? = null,
    @SerialName("inviteLink") @JsonNames("invite_link") val inviteLink: String? = null,
    @SerialName("createdAt") @JsonNames("created_at") val createdAt: Instant? = null,
)

/** What the platform took and what it refused. */
@Serializable
public data class WhatsappBlockResult(
    val blocked: List<String> = emptyList(),
    val unblocked: List<String> = emptyList(),
    val failed: List<String> = emptyList(),
)

/** Whether the cart and catalog show on the number, and which catalog is linked. */
@Serializable
public data class WhatsappCommerceSettings(
    @SerialName("cartEnabled") @JsonNames("cart_enabled") val cartEnabled: Boolean? = null,
    @SerialName("catalogVisible") @JsonNames("catalog_visible") val catalogVisible: Boolean? = null,
    @SerialName("catalogId") @JsonNames("catalog_id") val catalogId: String? = null,
)

/** One problem the platform found in a flow definition. */
@Serializable
public data class WhatsappFlowValidationError(
    val error: String? = null,
    val message: String? = null,
)

/** An in-chat form. The platform validates it and owns its status. */
@Serializable
public data class WhatsappFlow(
    val id: String? = null,
    val name: String? = null,
    /** `DRAFT`, `PUBLISHED`, `DEPRECATED` or `BLOCKED`. */
    val status: String? = null,
    val categories: List<String> = emptyList(),
    @SerialName("validationErrors")
    @JsonNames("validation_errors")
    val validationErrors: List<WhatsappFlowValidationError> = emptyList(),
    @SerialName("endpointUri") @JsonNames("endpoint_uri") val endpointUri: String? = null,
    @SerialName("jsonVersion") @JsonNames("json_version") val jsonVersion: String? = null,
    @SerialName("previewUrl") @JsonNames("preview_url") val previewUrl: String? = null,
    @SerialName("previewExpiresAt")
    @JsonNames("preview_expires_at")
    val previewExpiresAt: Instant? = null,
)

/**
 * The platform's verdict on an uploaded definition. It answers with the errors
 * rather than refusing the upload, so they arrive as data.
 */
@Serializable
public data class WhatsappFlowJsonResult(
    val success: Boolean = false,
    @SerialName("validationErrors")
    @JsonNames("validation_errors")
    val validationErrors: List<WhatsappFlowValidationError> = emptyList(),
)

/** What one person submitted through a flow. */
@Serializable
public data class WhatsappFlowResponse(
    @SerialName("messageId") @JsonNames("message_id") val messageId: String? = null,
    @SerialName("waId") @JsonNames("wa_id") val waId: String? = null,
    @SerialName("flowToken") @JsonNames("flow_token") val flowToken: String? = null,
    val answers: JsonElement? = null,
    @SerialName("respondedAt") @JsonNames("responded_at") val respondedAt: Instant? = null,
)

/** Whether a business public key is registered. The key itself never comes back. */
@Serializable
public data class WhatsappEncryptionKeyStatus(
    @SerialName("hasKey") @JsonNames("has_key") val hasKey: Boolean = false,
    @SerialName("signatureStatus")
    @JsonNames("signature_status")
    val signatureStatus: String? = null,
)

/**
 * A sandbox invitation. Only the last four digits of the tester's number travel;
 * the number itself is never stored.
 */
@Serializable
public data class WhatsappSandboxSession(
    val id: String? = null,
    /** `invited`, `active` or `expired`. */
    val status: String? = null,
    @SerialName("phoneNumberLast4")
    @JsonNames("phone_number_last4")
    val phoneNumberLast4: String? = null,
    @SerialName("invitedAt") @JsonNames("invited_at") val invitedAt: Instant? = null,
    @SerialName("activatedAt") @JsonNames("activated_at") val activatedAt: Instant? = null,
    @SerialName("expiresAt") @JsonNames("expires_at") val expiresAt: Instant? = null,
)
