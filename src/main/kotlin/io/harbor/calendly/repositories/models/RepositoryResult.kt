// src/main/kotlin/com/harbor/calendar/model/RepositoryResult.kt
package io.harbor.calendly.repositories.models

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject

data class RepositoryResult<T>(
    val success: Boolean,
    val data: T? = null,
    val error: OperationError? = null,
    val affectedRows: Int = 0,
    val operation: OperationType
) {
    constructor(json: JsonObject) : this(
        success = json.getBoolean("success"),
        data = null, // Type T handling needs to be done in specific implementations
        error = json.getJsonObject("error")?.let { OperationError(it) },
        affectedRows = json.getInteger("affectedRows", 0),
        operation = OperationType.valueOf(json.getString("operation"))
    )

    fun toJson(): JsonObject = JsonObject()
        .put("success", success)
        .put("error", error?.toJson())
        .put("affectedRows", affectedRows)
        .put("operation", operation.name)

    companion object {
        fun <T> success(
            data: T? = null,
            affectedRows: Int = 0,
            operation: OperationType
        ) = RepositoryResult<T>(
            success = true,
            data = data,
            affectedRows = affectedRows,
            operation = operation
        )

        fun <T> failure(
            error: OperationError,
            operation: OperationType
        ) = RepositoryResult<T>(
            success = false,
            error = error,
            operation = operation
        )
    }
}

@DataObject(generateConverter = true)
data class OperationError(
    val code: ErrorCode,
    val message: String,
    val details: JsonObject? = null
) {
    constructor(json: JsonObject) : this(
        code = ErrorCode.valueOf(json.getString("code")),
        message = json.getString("message"),
        details = json.getJsonObject("details")
    )

    fun toJson(): JsonObject = JsonObject()
        .put("code", code.name)
        .put("message", message)
        .put("details", details)
}

enum class ErrorCode {
    DUPLICATE_ENTRY,
    NOT_FOUND,
    INVALID_INPUT,
    CONSTRAINT_VIOLATION,
    DATABASE_ERROR,
    UNKNOWN_ERROR
}

enum class OperationType {
    INSERT,
    UPDATE,
    DELETE,
    SELECT
}
