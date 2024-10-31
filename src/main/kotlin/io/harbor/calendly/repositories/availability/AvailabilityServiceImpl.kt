package io.harbor.calendly.repositories.availability

import AvailabilityRepository
import io.harbor.calendly.repositories.BaseService
import io.harbor.calendly.repositories.models.*
import io.harbor.calendly.utils.TimeUtils
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class AvailabilityServiceImpl(
  private val vertx: Vertx

) : BaseService(vertx, AvailabilityServiceFactory.SERVICE_NAME), AvailabilityService {
  private val repository = AvailabilityRepository()

  override fun addAvailabilityWindow(
    userId: String,
    window: AvailabilityWindow
  ): Future<OperationResponse> {
    val promise = Promise.promise<OperationResponse>()

    launch {
      try {
        // Validate window
        if (!window.isValid()) {
          promise.complete(
            OperationResponse.failure(
              error = OperationError(
                code = ErrorCode.INVALID_INPUT,
                message = "Invalid availability window"
              ),
              message = "Validation failed: Time slots must be valid and end time must be after start time"
            )
          )
          return@launch
        }

        // Check for overlaps with existing windows
        val existingWindows = repository.getAvailabilityWindows(userId)
        if (existingWindows.success && existingWindows.data?.any {
            it.window.overlaps(window)
          } == true) {
          promise.complete(
            OperationResponse.failure(
              error = OperationError(
                code = ErrorCode.CONSTRAINT_VIOLATION,
                message = "Time slot overlaps with existing window"
              )
            )
          )
          return@launch
        }

        // Add window
        val result = repository.addAvailabilityWindow(userId, window)
        promise.complete(result.toOperationResponse())
      } catch (e: Exception) {
        promise.complete(
          OperationResponse.failure(
            error = OperationError(
              code = ErrorCode.UNKNOWN_ERROR,
              message = e.message ?: "Unknown error occurred"
            )
          )
        )
      }
    }

    return promise.future()
  }

  override fun updateAvailabilityWindow(
    userId: String,
    windowId: String,
    window: AvailabilityWindow
  ): Future<OperationResponse> {
    val promise = Promise.promise<OperationResponse>()

    launch {
      try {
        // Validate window
        if (!window.isValid()) {
          promise.complete(
            OperationResponse.failure(
              error = OperationError(
                code = ErrorCode.INVALID_INPUT,
                message = "Invalid availability window"
              )
            )
          )
          return@launch
        }

        // Check if window exists
        val existingResult = repository.getAvailabilityWindows(userId)
        val existingWindow = existingResult.data?.find { it.id == windowId }
        if (existingWindow == null) {
          promise.complete(
            OperationResponse.failure(
              error = OperationError(
                code = ErrorCode.NOT_FOUND,
                message = "Availability window not found"
              )
            )
          )
          return@launch
        }

        // Check for overlaps with other windows
        if (existingResult.data?.any {
            it.id != windowId && it.window.overlaps(window)
          } == true) {
          promise.complete(
            OperationResponse.failure(
              error = OperationError(
                code = ErrorCode.CONSTRAINT_VIOLATION,
                message = "Updated time slot overlaps with existing window"
              )
            )
          )
          return@launch
        }

        // Update window
        val result = repository.updateAvailabilityWindow(userId, windowId, window)
        promise.complete(result.toOperationResponse())
      } catch (e: Exception) {
        promise.complete(
          OperationResponse.failure(
            error = OperationError(
              code = ErrorCode.UNKNOWN_ERROR,
              message = e.message ?: "Unknown error occurred"
            )
          )
        )
      }
    }

    return promise.future()
  }

  override fun removeAvailabilityWindow(
    userId: String,
    windowId: String
  ): Future<OperationResponse> {
    val promise = Promise.promise<OperationResponse>()

    launch {
      try {
        val result = repository.removeAvailabilityWindow(userId, windowId)
        promise.complete(result.toOperationResponse())
      } catch (e: Exception) {
        promise.complete(
          OperationResponse.failure(
            error = OperationError(
              code = ErrorCode.UNKNOWN_ERROR,
              message = e.message ?: "Unknown error occurred"
            )
          )
        )
      }
    }

    return promise.future()
  }

  override fun getAvailabilityWindows(userId: String): Future<OperationResponse> {
    val promise = Promise.promise<OperationResponse>()

    launch {
      try {
        val result = repository.getAvailabilityWindows(userId)
        val response = when {
          result.success && !result.data.isNullOrEmpty() -> OperationResponse.success(
            data = JsonObject()
              .put("windows", JsonArray(result.data.map { it.toJson() })),
            message = "Retrieved ${result.data.size} availability windows",
            metadata = ResponseMetadata(
              affectedRows = result.data.size,
              operationType = OperationType.SELECT
            )
          )

          result.success -> OperationResponse.success(
            data = JsonObject().put("windows", JsonArray()),
            message = "No availability windows found"
          )

          else -> result.toOperationResponse()
        }
        promise.complete(response)
      } catch (e: Exception) {
        promise.complete(
          OperationResponse.failure(
            error = OperationError(
              code = ErrorCode.UNKNOWN_ERROR,
              message = e.message ?: "Unknown error occurred"
            )
          )
        )
      }
    }

    return promise.future()
  }

  override fun findOverlappingSlots(request: JsonObject): Future<OperationResponse> {
    val promise = Promise.promise<OperationResponse>()

    launch {
      try {
        val userIds = request.getJsonArray("userIds").map { it as String }
        val startDate = LocalDate.parse(request.getString("startDate"))
        val endDate = LocalDate.parse(request.getString("endDate"))
        val minDuration = Duration.ofMinutes(request.getLong("minDuration", 30L))

        if (userIds.size < 2) {
          promise.complete(
            OperationResponse.failure(
              error = OperationError(
                code = ErrorCode.INVALID_INPUT,
                message = "At least two users are required"
              )
            )
          )
          return@launch
        }

        // Get all users' availability
        val availabilities = userIds.map { userId ->
          repository.getAvailabilityWindows(userId)
        }

        // Check if any user has no availability
        val userWithNoAvailability = availabilities.find {
          !it.success || it.data.isNullOrEmpty()
        }
        if (userWithNoAvailability != null) {
          promise.complete(
            OperationResponse.success(
              data = JsonObject().put("slots", JsonArray()),
              message = "No overlapping slots found - some users have no availability"
            )
          )
          return@launch
        }

        // Find overlapping slots
        val overlappingSlots = findOverlappingTimeSlots(
          availabilities.map { it.data!! },
          startDate,
          endDate,
          minDuration
        )

        promise.complete(
          OperationResponse.success(
            data = JsonObject().put("slots", JsonArray(overlappingSlots.map { it.toJson() })),
            message = "Found ${overlappingSlots.size} overlapping slots",
            metadata = ResponseMetadata(
              affectedRows = overlappingSlots.size,
              operationType = OperationType.SELECT,
              additionalInfo = JsonObject()
                .put("userCount", userIds.size)
                .put("dateRange", "${startDate} to ${endDate}")
            )
          )
        )
      } catch (e: Exception) {
        promise.complete(
          OperationResponse.failure(
            error = OperationError(
              code = ErrorCode.UNKNOWN_ERROR,
              message = e.message ?: "Unknown error occurred"
            )
          )
        )
      }
    }

    return promise.future()
  }

  override fun getUserAvailabilityForDate(
    userId: String,
    date: String
  ): Future<OperationResponse> {
    val promise = Promise.promise<OperationResponse>()

    launch {
      try {
        val targetDate = LocalDate.parse(date)
        val result = repository.getAvailabilityWindows(userId)

        if (!result.success) {
          promise.complete(result.toOperationResponse())
          return@launch
        }

        // Filter windows for the specific day
        val dayOfWeek = targetDate.dayOfWeek.value
        val availableWindows = result.data
          ?.filter { it.window.dayOfWeek == dayOfWeek }
          ?.map { window ->
            TimeSlot(
              start = targetDate.atTime(LocalTime.parse(window.window.startTime))
                .atZone(ZoneId.systemDefault()).format(TimeUtils.HUMAN_READABLE_FORMATTER),
              end = targetDate.atTime(LocalTime.parse(window.window.endTime))
                .atZone(ZoneId.systemDefault()).format(TimeUtils.HUMAN_READABLE_FORMATTER)
            )
          } ?: emptyList()

        promise.complete(
          OperationResponse.success(
            data = JsonObject().put(
              "availableSlots",
              JsonArray(availableWindows.map { it.toJson() })
            ),
            message = "Found ${availableWindows.size} available slots for $date",
            metadata = ResponseMetadata(
              affectedRows = availableWindows.size,
              operationType = OperationType.SELECT,
              additionalInfo = JsonObject()
                .put("date", date)
                .put("dayOfWeek", dayOfWeek)
            )
          )
        )
      } catch (e: Exception) {
        promise.complete(
          OperationResponse.failure(
            error = OperationError(
              code = ErrorCode.UNKNOWN_ERROR,
              message = e.message ?: "Unknown error occurred"
            )
          )
        )
      }
    }

    return promise.future()
  }

  override fun findAvailableSlots(request: JsonObject): Future<OperationResponse> {
    val promise = Promise.promise<OperationResponse>()

    launch {
      try {
        val userIds = request.getJsonArray("userIds").map { it as String }
        val startDate = request.getString("startDate")
        val endDate = request.getString("endDate")
        val minDuration = request.getLong("minDuration", 30L) // default 30 minutes

        if (userIds.isEmpty()) {
          promise.complete(OperationResponse.failure(
            error = OperationError(
              code = ErrorCode.INVALID_INPUT,
              message = "At least one user ID is required"
            )
          ))
          return@launch
        }

        // Get availability for all users
        val availabilities = userIds.map { userId ->
          repository.getAvailabilityWindows(userId)
        }

        // Check if any user has no availability
        val usersWithNoAvailability = availabilities.filter {
          !it.success || it.data.isNullOrEmpty()
        }
        if (usersWithNoAvailability.isNotEmpty()) {
          promise.complete(OperationResponse.success(
            data = JsonObject().put("slots", JsonArray()),
            message = "No available slots - some users have no availability set"
          ))
          return@launch
        }

        // Convert dates to LocalDate
        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)

        // Find overlapping slots
        val slots = findOverlappingTimeSlots(
          userAvailabilities = availabilities.map { it.data!! },
          startDate = start,
          endDate = end,
          minDuration = Duration.ofMinutes(minDuration)
        )

        promise.complete(OperationResponse.success(
          data = JsonObject().put(
            "slots",
            JsonArray(slots.map { slot ->
              JsonObject()
                .put("start", slot.start.toString())
                .put("end", slot.end.toString())
            })
          ),
          message = if (slots.isEmpty())
            "No available slots found"
          else
            "Found ${slots.size} available slots",
          metadata = ResponseMetadata(
            affectedRows = slots.size,
            operationType = OperationType.SELECT,
            additionalInfo = JsonObject()
              .put("userCount", userIds.size)
              .put("dateRange", "$startDate to $endDate")
              .put("minDuration", minDuration)
          )
        ))
      } catch (e: Exception) {
        promise.complete(OperationResponse.failure(
          error = OperationError(
            code = ErrorCode.UNKNOWN_ERROR,
            message = e.message ?: "Unknown error occurred"
          )
        ))
      }
    }

    return promise.future()
  }


  private fun findOverlappingTimeSlots(
    userAvailabilities: List<List<AvailabilityWindowWithId>>,
    startDate: LocalDate,
    endDate: LocalDate,
    minDuration: Duration
  ): List<TimeSlot> {
    if (userAvailabilities.isEmpty() || userAvailabilities.any { it.isEmpty() }) {
      return emptyList()
    }

    // Generate dates between start and end dates (inclusive)
    val dates = generateSequence(startDate) { date ->
      date.plusDays(1).takeIf { !it.isAfter(endDate) }
    }.toList()

    return dates.flatMap { date ->
      // For each date, find overlapping slots for all users
      findDayOverlaps(date, userAvailabilities, minDuration)
    }.sortedBy { it.start }
  }

  private fun findDayOverlaps(
    date: LocalDate,
    userAvailabilities: List<List<AvailabilityWindowWithId>>,
    minDuration: Duration
  ): List<TimeSlot> {
    // Get windows for each user for this day of week
    val dayOfWeek = date.dayOfWeek.value
    val dayWindows = userAvailabilities.map { userWindows ->
      userWindows.filter { it.window.dayOfWeek == dayOfWeek }
    }

    // If any user has no availability on this day, return empty list
    if (dayWindows.any { it.isEmpty() }) {
      return emptyList()
    }

    // Convert availability windows to time slots for this specific date
    val userTimeSlots = dayWindows.map { windows ->
      windows.map { window ->
        TimeSlot(
          start = date.atTime(LocalTime.parse(window.window.startTime))
            .atZone(ZoneId.systemDefault()).format(TimeUtils.HUMAN_READABLE_FORMATTER),
          end = date.atTime(LocalTime.parse(window.window.endTime))
            .atZone(ZoneId.systemDefault()).format(TimeUtils.HUMAN_READABLE_FORMATTER)
        )
      }
    }

    // Find overlapping slots
    var overlappingSlots = userTimeSlots.first()
    for (userSlots in userTimeSlots.drop(1)) {
      overlappingSlots = findOverlappingSlots(overlappingSlots, userSlots)
      if (overlappingSlots.isEmpty()) {
        return emptyList()
      }
    }

    // Filter by minimum duration and merge adjacent slots
    return mergeAdjacentSlots(overlappingSlots)
      .filter { slot ->
        Duration.between(TimeUtils.toZonedDateTime(slot.start), TimeUtils.toZonedDateTime(slot.end)) >= minDuration
      }
  }

  private fun findOverlappingSlots(
    slots1: List<TimeSlot>,
    slots2: List<TimeSlot>
  ): List<TimeSlot> {
    val overlaps = mutableListOf<TimeSlot>()

    for (slot1 in slots1) {
      for (slot2 in slots2) {
        val overlap = findOverlap(slot1, slot2)
        if (overlap != null) {
          overlaps.add(overlap)
        }
      }
    }

    return overlaps
  }

  private fun findOverlap(slot1: TimeSlot, slot2: TimeSlot): TimeSlot? {
    // No overlap if one ends before other starts
    if (slot1.getEndTime().isBefore(slot2.getStartTime()) || slot2.getEndTime().isBefore(slot1.getStartTime())) {
      return null
    }

    // Find the overlap period
    val overlapStart = if (slot1.getStartTime().isAfter(slot2.getStartTime())) slot1.start else slot2.start
    val overlapEnd = if (slot1.getEndTime().isBefore(slot2.getEndTime())) slot1.end else slot2.end

    return TimeSlot(
      start = overlapStart,
      end = overlapEnd
    )
  }

  private fun mergeAdjacentSlots(slots: List<TimeSlot>): List<TimeSlot> {
    if (slots.isEmpty()) return emptyList()

    // Sort slots by start time
    val sortedSlots = slots.sortedBy { it.start }
    val mergedSlots = mutableListOf<TimeSlot>()

    var currentSlot = sortedSlots.first()

    for (nextSlot in sortedSlots.drop(1)) {
      if (currentSlot.getEndTime().isEqual(nextSlot.getStartTime()) ||
        currentSlot.getEndTime().isAfter(nextSlot.getStartTime())
      ) {
        // Merge slots
        currentSlot = TimeSlot(
          start = currentSlot.start,
          end = if (currentSlot.getEndTime().isAfter(nextSlot.getEndTime()))
            currentSlot.end else nextSlot.end
        )
      } else {
        // Add current slot and start new one
        mergedSlots.add(currentSlot)
        currentSlot = nextSlot
      }
    }

    mergedSlots.add(currentSlot)
    return mergedSlots
  }
}
