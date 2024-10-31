// src/main/kotlin/com/harbor/calendar/model/OperationResponse.kt
package io.harbor.calendly.repositories.models

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject
import io.vertx.core.json.JsonArray

@DataObject(generateConverter = true)
data class OperationResponse(
    val success: Boolean,
    val message: String? = null,
    val error: OperationError? = null,
    val data: JsonObject? = null,
    val metadata: ResponseMetadata = ResponseMetadata()
) {
    constructor(json: JsonObject) : this(
        success = json.getBoolean("success"),
        message = json.getString("message"),
        error = json.getJsonObject("error")?.let { OperationError(it) },
        data = json.getJsonObject("data"),
        metadata = json.getJsonObject("metadata")?.let { ResponseMetadata(it) }
            ?: ResponseMetadata()
    )

    fun toJson(): JsonObject = JsonObject()
        .put("success", success)
        .put("message", message)
        .put("error", error?.toJson())
        .put("data", data)
        .put("metadata", metadata.toJson())

    companion object {
        fun success(
            data: JsonObject? = null,
            message: String? = null,
            metadata: ResponseMetadata = ResponseMetadata()
        ) = OperationResponse(
            success = true,
            message = message,
            data = data,
            metadata = metadata
        )

        fun failure(
            error: OperationError,
            message: String? = null,
            metadata: ResponseMetadata = ResponseMetadata()
        ) = OperationResponse(
            success = false,
            message = message,
            error = error,
            metadata = metadata
        )

        fun <T> fromRepositoryResult(result: RepositoryResult<T>): OperationResponse {
            return when {
                result.success -> success(
                    data = when (result.data) {
                        is JsonObject -> result.data
                        is String -> JsonObject().put("id", result.data)
                        is Collection<*> -> JsonObject().put("items", JsonArray(result.data.toList()))
                        else -> result.data?.let { JsonObject.mapFrom(it) }
                    },
                    metadata = ResponseMetadata(
                        affectedRows = result.affectedRows,
                        operationType = result.operation
                    )
                )
                else -> failure(
                    error = result.error ?: OperationError(
                        code = ErrorCode.UNKNOWN_ERROR,
                        message = "Unknown error occurred"
                    ),
                    metadata = ResponseMetadata(
                        affectedRows = result.affectedRows,
                        operationType = result.operation
                    )
                )
            }
        }
    }
}

@DataObject(generateConverter = true)
data class ResponseMetadata(
    val timestamp: Long = System.currentTimeMillis(),
    val affectedRows: Int = 0,
    val operationType: OperationType? = null,
    val additionalInfo: JsonObject? = null
) {
    constructor(json: JsonObject) : this(
        timestamp = json.getLong("timestamp", System.currentTimeMillis()),
        affectedRows = json.getInteger("affectedRows", 0),
        operationType = json.getString("operationType")?.let { OperationType.valueOf(it) },
        additionalInfo = json.getJsonObject("additionalInfo")
    )

    fun toJson(): JsonObject = JsonObject()
        .put("timestamp", timestamp)
        .put("affectedRows", affectedRows)
        .put("operationType", operationType?.name)
        .put("additionalInfo", additionalInfo)
}

// Extension function to help convert repository results to operation responses
fun <T> RepositoryResult<T>.toOperationResponse(): OperationResponse =
    OperationResponse.fromRepositoryResult(this)
