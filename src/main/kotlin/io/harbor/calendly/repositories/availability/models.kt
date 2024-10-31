package io.harbor.calendly.repositories.availability
// src/main/kotlin/com/harbor/calendar/model/TimeSlot.kt

import io.harbor.calendly.utils.TimeUtils
import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject
import java.time.LocalTime
import java.time.ZonedDateTime

@DataObject(generateConverter = true)
data class TimeSlot(
  val start: String, // ISO-8601 string format for ZonedDateTime
  val end: String,   // ISO-8601 string format for ZonedDateTime
) {
  constructor(json: JsonObject) : this(
    start = json.getString("start"),
    end = json.getString("end")
  )

  fun toJson(): JsonObject = JsonObject()
    .put("start", start)
    .put("end", end)

  // Convenience methods for ZonedDateTime conversion
  fun getStartTime(): ZonedDateTime = ZonedDateTime.parse(start, TimeUtils.DEFAULT_FORMATTER_UNI)
  fun getEndTime(): ZonedDateTime = ZonedDateTime.parse(end, TimeUtils.DEFAULT_FORMATTER_UNI)
}


@DataObject(generateConverter = true)
data class AvailabilityWindow(
  val dayOfWeek: Int,    // 1-7 for Monday-Sunday
  val startTime: String, // LocalTime in HH:mm:ss format
  val endTime: String    // LocalTime in HH:mm:ss format
) {
  constructor(json: JsonObject) : this(
    dayOfWeek = json.getInteger("dayOfWeek"),
    startTime = json.getString("startTime"),
    endTime = json.getString("endTime")
  )

  fun toJson(): JsonObject = JsonObject()
    .put("dayOfWeek", dayOfWeek)
    .put("startTime", startTime)
    .put("endTime", endTime)

  fun isValid(): Boolean {
    return try {
      val start = LocalTime.parse(startTime)
      val end = LocalTime.parse(endTime)
      dayOfWeek in 1..7 && start.isBefore(end)
    } catch (e: Exception) {
      false
    }
  }

  /**
   * Checks if this availability window overlaps with another window.
   * Windows overlap if they are on the same day and their time ranges intersect.
   */
  fun overlaps(other: AvailabilityWindow): Boolean {
    // Different days don't overlap
    if (this.dayOfWeek != other.dayOfWeek) {
      return false
    }

    val thisStart = LocalTime.parse(this.startTime)
    val thisEnd = LocalTime.parse(this.endTime)
    val otherStart = LocalTime.parse(other.startTime)
    val otherEnd = LocalTime.parse(other.endTime)

    // Check if one window starts during the other window
    // Window 1:   |-------|
    // Window 2:      |-------|
    // or
    // Window 1:      |-------|
    // Window 2:   |-------|
    return !thisStart.isAfter(otherEnd) && !thisEnd.isBefore(otherStart)
  }

  /**
   * Returns the intersection of two availability windows if they overlap.
   * Returns null if they don't overlap.
   */
  fun intersect(other: AvailabilityWindow): AvailabilityWindow? {
    if (!overlaps(other)) {
      return null
    }

    val thisStart = LocalTime.parse(this.startTime)
    val thisEnd = LocalTime.parse(this.endTime)
    val otherStart = LocalTime.parse(other.startTime)
    val otherEnd = LocalTime.parse(other.endTime)

    val intersectionStart = if (thisStart.isAfter(otherStart)) thisStart else otherStart
    val intersectionEnd = if (thisEnd.isBefore(otherEnd)) thisEnd else otherEnd

    return AvailabilityWindow(
      dayOfWeek = this.dayOfWeek,
      startTime = intersectionStart.toString(),
      endTime = intersectionEnd.toString()
    )
  }

  /**
   * Checks if this window can be merged with another window
   * (i.e., they are adjacent or overlapping)
   */
  fun canMergeWith(other: AvailabilityWindow): Boolean {
    if (this.dayOfWeek != other.dayOfWeek) {
      return false
    }

    val thisStart = LocalTime.parse(this.startTime)
    val thisEnd = LocalTime.parse(this.endTime)
    val otherStart = LocalTime.parse(other.startTime)
    val otherEnd = LocalTime.parse(other.endTime)

    // Check if windows are adjacent or overlapping
    return !thisStart.isAfter(otherEnd) && !thisEnd.isBefore(otherStart)
  }

  /**
   * Merges this window with another window if they can be merged.
   * Returns null if they cannot be merged.
   */
  fun mergeWith(other: AvailabilityWindow): AvailabilityWindow? {
    if (!canMergeWith(other)) {
      return null
    }

    val thisStart = LocalTime.parse(this.startTime)
    val thisEnd = LocalTime.parse(this.endTime)
    val otherStart = LocalTime.parse(other.startTime)
    val otherEnd = LocalTime.parse(other.endTime)

    return AvailabilityWindow(
      dayOfWeek = this.dayOfWeek,
      startTime = (if (thisStart.isBefore(otherStart)) thisStart else otherStart).toString(),
      endTime = (if (thisEnd.isAfter(otherEnd)) thisEnd else otherEnd).toString()
    )
  }
}

@DataObject(generateConverter = true)
data class AvailabilityWindowWithId(
  val id: String,
  val window: AvailabilityWindow
) {
  constructor(json: JsonObject) : this(
    id = json.getString("id"),
    window = AvailabilityWindow(json.getJsonObject("window"))
  )

  fun toJson(): JsonObject = JsonObject()
    .put("id", id)
    .put("window", window.toJson())
}

@DataObject(generateConverter = true)
data class AvailabilityOperation(
  val operation: Operation,
  val windowId: String? = null,
  val window: AvailabilityWindow? = null
) {
  enum class Operation {
    ADD,
    REMOVE,
    UPDATE
  }

  constructor(json: JsonObject) : this(
    operation = Operation.valueOf(json.getString("operation")),
    windowId = json.getString("windowId"),
    window = json.getJsonObject("window")?.let { AvailabilityWindow(it) }
  )

  fun toJson(): JsonObject = JsonObject()
    .put("operation", operation.name)
    .put("windowId", windowId)
    .put("window", window?.toJson())
}
