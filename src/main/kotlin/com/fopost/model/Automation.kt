@file:UseSerializers(InstantSerializer::class)

package com.fopost.model

import com.fopost.internal.InstantSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonObject

/**
 * An automation: one trigger and the ordered steps it runs.
 *
 * [steps] and [secret] arrive from the detail and create calls; the list gives the summary
 * fields only.
 */
@Serializable
public data class Automation(
    val id: String? = null,
    @SerialName("workspace_id") @JsonNames("workspaceId") val workspaceId: String? = null,
    val name: String? = null,
    @SerialName("trigger_type") @JsonNames("triggerType") val triggerType: String? = null,
    @SerialName("trigger_config") @JsonNames("triggerConfig") val triggerConfig: JsonObject? = null,
    val active: Boolean? = null,
    val secret: String? = null,
    val steps: List<AutomationStep> = emptyList(),
    @SerialName("last_triggered_at") @JsonNames("lastTriggeredAt") val lastTriggeredAt: Instant? = null,
    @SerialName("run_count") @JsonNames("runCount") val runCount: Int? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    @SerialName("updated_at") @JsonNames("updatedAt") val updatedAt: Instant? = null,
)

/** One step of an automation. `actionType` is publish, delay or transform. */
@Serializable
public data class AutomationStep(
    val id: Int? = null,
    val position: Int? = null,
    @SerialName("node_id") @JsonNames("nodeId") val nodeId: String? = null,
    @SerialName("action_type") @JsonNames("actionType") val actionType: String? = null,
    @SerialName("action_config") @JsonNames("actionConfig") val actionConfig: JsonObject? = null,
)

/** One execution of an automation. [logs] arrive from the single-run endpoint. */
@Serializable
public data class AutomationRun(
    val id: Long? = null,
    @SerialName("automation_id") @JsonNames("automationId") val automationId: String? = null,
    val status: String? = null,
    @SerialName("current_step") @JsonNames("currentStep") val currentStep: Int? = null,
    @SerialName("trigger_event") @JsonNames("triggerEvent") val triggerEvent: JsonObject? = null,
    val context: JsonObject? = null,
    @SerialName("started_at") @JsonNames("startedAt") val startedAt: Instant? = null,
    @SerialName("completed_at") @JsonNames("completedAt") val completedAt: Instant? = null,
    @SerialName("error_message") @JsonNames("errorMessage") val errorMessage: String? = null,
    val logs: List<AutomationRunLog> = emptyList(),
)

/** What one step of a run did. */
@Serializable
public data class AutomationRunLog(
    val id: Long? = null,
    @SerialName("step_position") @JsonNames("stepPosition") val stepPosition: Int? = null,
    val status: String? = null,
    @SerialName("input_snapshot") @JsonNames("inputSnapshot") val inputSnapshot: JsonObject? = null,
    @SerialName("output_snapshot") @JsonNames("outputSnapshot") val outputSnapshot: JsonObject? = null,
    @SerialName("started_at") @JsonNames("startedAt") val startedAt: Instant? = null,
    @SerialName("completed_at") @JsonNames("completedAt") val completedAt: Instant? = null,
    @SerialName("duration_ms") @JsonNames("durationMs") val durationMs: Long? = null,
    @SerialName("error_message") @JsonNames("errorMessage") val errorMessage: String? = null,
)

/** Counts across every automation the key can reach. */
@Serializable
public data class AutomationStats(
    @SerialName("total_automations") @JsonNames("totalAutomations") val totalAutomations: Int? = null,
    @SerialName("active_automations") @JsonNames("activeAutomations") val activeAutomations: Int? = null,
    @SerialName("total_runs_24h") @JsonNames("totalRuns24h") val totalRuns24h: Int? = null,
    @SerialName("runs_by_status") @JsonNames("runsByStatus") val runsByStatus: Map<String, Int> = emptyMap(),
    @SerialName("recent_runs") @JsonNames("recentRuns") val recentRuns: List<AutomationRecentRun> = emptyList(),
)

/** A recent run, as the stats endpoint summarises it. */
@Serializable
public data class AutomationRecentRun(
    val id: Long? = null,
    @SerialName("automation_id") @JsonNames("automationId") val automationId: String? = null,
    val status: String? = null,
    @SerialName("started_at") @JsonNames("startedAt") val startedAt: Instant? = null,
    @SerialName("completed_at") @JsonNames("completedAt") val completedAt: Instant? = null,
)

/** The run a manual automation trigger started. */
@Serializable
public data class AutomationTrigger(
    @SerialName("run_id") @JsonNames("runId") val runId: Long? = null,
    val triggered: Boolean? = null,
)

/**
 * One saved version of an automation's graph.
 *
 * The list gives [nodeCount] and the nodes that changed against the previous version; the
 * single-version call fills [graph] instead.
 */
@Serializable
public data class AutomationVersion(
    val version: Int? = null,
    val note: String? = null,
    @SerialName("created_by") @JsonNames("createdBy") val createdBy: AutomationVersionAuthor? = null,
    @SerialName("created_at") @JsonNames("createdAt") val createdAt: Instant? = null,
    @SerialName("node_count") @JsonNames("nodeCount") val nodeCount: Int? = null,
    @SerialName("nodes_added") @JsonNames("nodesAdded") val nodesAdded: List<AutomationVersionNodeRef> = emptyList(),
    @SerialName("nodes_removed") @JsonNames("nodesRemoved") val nodesRemoved: List<AutomationVersionNodeRef> = emptyList(),
    val graph: WorkflowGraph? = null,
)

/** Who saved a version. */
@Serializable
public data class AutomationVersionAuthor(
    val name: String? = null,
)

/** A node named in a version's diff. */
@Serializable
public data class AutomationVersionNodeRef(
    val id: String? = null,
    val kind: String? = null,
    val label: String? = null,
)

/** The answer to a restore: the new version number, its graph, and the steps compiled from it. */
@Serializable
public data class AutomationVersionRestore(
    val version: Int? = null,
    val graph: WorkflowGraph? = null,
    val steps: List<AutomationStep> = emptyList(),
)

/** An automation's canvas: its nodes and the edges between them. */
@Serializable
public data class WorkflowGraph(
    val nodes: List<WorkflowNode> = emptyList(),
    val edges: List<WorkflowEdge> = emptyList(),
)

/** One node on the canvas. [config] is echoed back exactly as authored. */
@Serializable
public data class WorkflowNode(
    val id: String? = null,
    val kind: String? = null,
    val label: String? = null,
    val config: JsonObject? = null,
    val position: Int? = null,
    val layout: WorkflowLayout? = null,
)

/** Where a node sits on the canvas. */
@Serializable
public data class WorkflowLayout(
    val x: Double? = null,
    val y: Double? = null,
)

/** A connection from one node's handle to another node. */
@Serializable
public data class WorkflowEdge(
    val id: String? = null,
    val source: String? = null,
    @SerialName("source_handle") @JsonNames("sourceHandle") val sourceHandle: String? = null,
    val target: String? = null,
)
