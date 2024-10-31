package io.harbor.calendly.util

import io.harbor.calendly.repositories.models.ApiResponse
import io.harbor.calendly.repositories.models.ErrorCode
import io.harbor.calendly.repositories.models.OperationError
import io.harbor.calendly.repositories.models.OperationType
import io.harbor.calendly.repositories.models.RepositoryResult
import io.harbor.calendly.repositories.scheduler.Event
import io.harbor.calendly.repositories.scheduler.EventMetadata
import io.harbor.calendly.repositories.scheduler.EventStatus
import io.harbor.calendly.repositories.scheduler.NotificationPreference
import io.harbor.calendly.repositories.scheduler.Participant
import io.harbor.calendly.repositories.scheduler.ParticipantResponse
import io.harbor.calendly.utils.TimeUtils
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.reactivex.core.http.HttpHeaders
import io.vertx.reactivex.core.http.HttpServerResponse
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun RowSet<Row>.toJson(): List<JsonObject> = asSequence().map { row ->
//  val json = JsonObject()
//  columnsNames().asSequence().forEach {
//    json.put(it, toPgType(row.getValue(it)))
//  }
  row.toJson()
}.toList()

private fun toPgType(value: Any?): Any? = when (value) {
  is String -> value
  is LocalDate -> value.format(DateTimeFormatter.ISO_DATE)
  is LocalDateTime -> value.format(DateTimeFormatter.ISO_DATE_TIME)
  is UUID -> value.toString()
  else -> value
}

fun HttpServerResponse.toApiResponse(
  data: JsonArray = JsonArray(),
  statusCode: Int = 200,
  message: String = "Success",
  apiStatus: Int = 200
) {
  val body = JsonObject()
  body.put("data", data).put("statusCode", statusCode).put("message", message)
  setStatusCode(apiStatus)
  putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
  end(body.encode())
}

fun HttpServerResponse.toApiResponse(ob: ApiResponse) {
  statusCode = ob.statusCode
  putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
  end(ob.toJson().encode())
}

fun JsonObject?.isNullOrEmpty(): Boolean {
  return this == null || this.isEmpty
}

fun JsonObject?.isNotNullOrEmpty(): Boolean {
  return this != null && !this.isEmpty
}

fun JsonArray?.isNullOrEmpty(): Boolean {
  return this == null || this.isEmpty
}

fun JsonArray?.isNotNullOrEmpty(): Boolean {
  return this != null && !this.isEmpty
}

fun Long.toOffsetDateTime(): OffsetDateTime {
  return TimeUtils.toOffsetDateTime(this)
}

fun <T> handleDatabaseException(
  e: Exception,
  operation: OperationType
): RepositoryResult<T> {
  val error = when {
    e.message?.contains("duplicate key") == true -> OperationError(
      code = ErrorCode.DUPLICATE_ENTRY,
      message = "Duplicate entry found",
      details = JsonObject().put("originalError", e.message)
    )

    e.message?.contains("foreign key") == true -> OperationError(
      code = ErrorCode.CONSTRAINT_VIOLATION,
      message = "Foreign key constraint violation",
      details = JsonObject().put("originalError", e.message)
    )

    else -> OperationError(
      code = ErrorCode.DATABASE_ERROR,
      message = e.message ?: "Unknown database error",
      details = JsonObject().put("originalError", e.message)
    )
  }
  return RepositoryResult.failure(error, operation)
}


fun Row.toEvent(): Event {
  return Event(
    id = getString("id"),
    title = getString("title"),
    description = getString("description"),
    startTime = getLocalDateTime("start_time")
      .atZone(ZoneId.systemDefault())
      .toString(),
    endTime = getLocalDateTime("end_time")
      .atZone(ZoneId.systemDefault())
      .toString(),
    organizerId = getString("organizer_id"),
    status = EventStatus.valueOf(getString("status")),
    metadata = getJsonObject("metadata")?.let { EventMetadata(it) },
    participants = getJsonArray("participants")
      ?.map { participant ->
        val json = participant as JsonObject
        Participant(
          userId = json.getString("userId"),
          response = ParticipantResponse.valueOf(
            json.getString("response")
          ),
          notificationPreference = NotificationPreference.valueOf(
            json.getString("notificationPreference")
          )
        )
      } ?: emptyList()
  )
}

// Convenience extension for nullable rows
fun Row?.toEventOrNull(): Event? = this?.toEvent()

// Extension for handling collections
fun RowSet<Row>.toEvents(): List<Event> = map { it.toEvent() }

// Additional useful extensions
fun Row.getZonedDateTime(columnName: String): ZonedDateTime =
  getLocalDateTime(columnName).atZone(ZoneId.systemDefault())

fun Row.getZonedDateTimeOrNull(columnName: String): ZonedDateTime? =
  getLocalDateTime(columnName)?.atZone(ZoneId.systemDefault())

fun Row.toEventMetadata(): EventMetadata? =
  getJsonObject("metadata")?.let { EventMetadata(it) }

fun Row.toParticipants(): List<Participant> =
  getJsonArray("participants")
    ?.map { Participant(it as JsonObject) }
    ?: emptyList()

fun String.toUUID(): UUID = UUID.fromString(this)
