package io.harbor.calendly.repositories.scheduler

import io.harbor.calendly.repositories.BaseService
import io.harbor.calendly.repositories.availability.AvailabilityService
import io.harbor.calendly.repositories.availability.AvailabilityServiceFactory
import io.harbor.calendly.repositories.models.ErrorCode
import io.harbor.calendly.repositories.models.OperationError
import io.harbor.calendly.repositories.models.OperationResponse
import io.harbor.calendly.repositories.models.OperationType
import io.harbor.calendly.repositories.models.ResponseMetadata
import io.harbor.calendly.repositories.models.toOperationResponse
import io.harbor.calendly.utils.TimeUtils
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID

class SchedulerServiceImpl(
    private val vertx: Vertx,
    private val availabilityService: AvailabilityService = AvailabilityServiceFactory.createProxy(
        vertx,
        AvailabilityServiceFactory.SERVICE_ADDRESS
    )
) : BaseService(vertx, SchedulerServiceFactory.SERVICE_NAME), SchedulerService {

    private val eventRepository = EventRepository()

    override fun scheduleEvent(request: JsonObject): Future<OperationResponse> {
        val promise = Promise.promise<OperationResponse>()

        launch {
            try {
                val event = Event(request)

                // Validate event
                if (!event.isValid()) {
                    promise.complete(
                        OperationResponse.failure(
                            error = OperationError(
                                code = ErrorCode.INVALID_INPUT,
                                message = "Invalid event data"
                            )
                        )
                    )
                    return@launch
                }

                // Check participants' availability
                val availabilityRequest = JsonObject()
                    .put("userIds", event.participants.map { it.userId }.plus(event.organizerId))
                    .put("startDate", event.startTime.split("T")[0])
                    .put("endDate", event.endTime.split("T")[0])

                val availabilityResponse = availabilityService
                    .findAvailableSlots(availabilityRequest).coAwait()

                if (!isTimeSlotAvailable(event, availabilityResponse)) {
                    promise.complete(
                        OperationResponse.failure(
                            error = OperationError(
                                code = ErrorCode.CONSTRAINT_VIOLATION,
                                message = "Selected time slot is not available for all participants"
                            )
                        )
                    )
                    return@launch
                }

                // Create event
                val result = eventRepository.createEvent(
                    event.copy(
                        id = UUID.randomUUID().toString()
                    )
                )

                if (result.success) {
                    // Notify participants asynchronously
                    notifyParticipants(result.data!!)
                }

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

    override fun findAvailableSlots(request: JsonObject): Future<OperationResponse> {
        val promise = Promise.promise<OperationResponse>()

        launch {
            try {
                val userIds = request.getJsonArray("userIds").map { it as String }
                val startDate = request.getString("startDate")
                val endDate = request.getString("endDate")
                val duration = request.getLong("duration", 30L) // minutes

                // Get users' availability
                val availabilityResponse = availabilityService
                    .findAvailableSlots(request).coAwait()

                if (!availabilityResponse.success) {
                    promise.complete(availabilityResponse)
                    return@launch
                }

                // Get existing events for all users
                val existingEvents = eventRepository
                    .getEventsForUsers(userIds, startDate, endDate)

                // Filter available slots by existing events
                val availableSlots = filterSlotsByEvents(
                    availabilityResponse.data?.getJsonArray("slots") ?: JsonArray(),
                    existingEvents.data ?: emptyList(),
                    duration
                )

                promise.complete(
                    OperationResponse.success(
                        data = JsonObject().put("slots", JsonArray(availableSlots)),
                        message = "Found ${availableSlots.size} available slots",
                        metadata = ResponseMetadata(
                            additionalInfo = JsonObject()
                                .put("userCount", userIds.size)
                                .put("dateRange", "$startDate to $endDate")
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

    override fun cancelEvent(eventId: String): Future<OperationResponse> {
        val promise = Promise.promise<OperationResponse>()

        launch {
            try {
                val result = eventRepository.updateEventStatus(
                    eventId,
                    EventStatus.CANCELLED
                )

                if (result.success) {
                    // Notify participants asynchronously
                    notifyEventCancellation(result.data!!)
                }

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

    override fun updateEvent(
        eventId: String,
        request: JsonObject
    ): Future<OperationResponse> {
        val promise = Promise.promise<OperationResponse>()

        launch {
            try {
                val event = Event(request)

                // Validate event
                if (!event.isValid()) {
                    promise.complete(
                        OperationResponse.failure(
                            error = OperationError(
                                code = ErrorCode.INVALID_INPUT,
                                message = "Invalid event data"
                            )
                        )
                    )
                    return@launch
                }

                // If time changed, check availability
                val existingEvent = eventRepository.getEventById(eventId)
                if (!existingEvent.success || existingEvent.data == null) {
                    promise.complete(
                        OperationResponse.failure(
                            error = OperationError(
                                code = ErrorCode.NOT_FOUND,
                                message = "Event not found"
                            )
                        )
                    )
                    return@launch
                }

                if (event.startTime != existingEvent.data.startTime ||
                    event.endTime != existingEvent.data.endTime
                ) {

                    val availabilityRequest = JsonObject()
                        .put("userIds", event.participants.map { it.userId })
                        .put("startDate", event.startTime.split("T")[0])
                        .put("endDate", event.endTime.split("T")[0])

                    val availabilityResponse = availabilityService
                        .findAvailableSlots(availabilityRequest)
                        .coAwait()

                    if (!isTimeSlotAvailable(event, availabilityResponse)) {
                        promise.complete(
                            OperationResponse.failure(
                                error = OperationError(
                                    code = ErrorCode.CONSTRAINT_VIOLATION,
                                    message = "Selected time slot is not available for all participants"
                                )
                            )
                        )
                        return@launch
                    }
                }

                // Update event
                val result = eventRepository.updateEvent(eventId, event)

                if (result.success) {
                    // Notify participants asynchronously
                    notifyEventUpdate(result.data!!)
                }

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

    override fun getUserEvents(
        userId: String,
        startDate: String,
        endDate: String
    ): Future<OperationResponse> {
        val promise = Promise.promise<OperationResponse>()

        launch {
            try {
                // Validate date range
                val start = try {
                    LocalDateTime.parse(startDate)
                } catch (e: Exception) {
                    promise.complete(
                        OperationResponse.failure(
                            error = OperationError(
                                code = ErrorCode.INVALID_INPUT,
                                message = "Invalid start date format. Expected: yyyy-MM-dd'T'HH:mm:ss"
                            )
                        )
                    )
                    return@launch
                }

                val end = try {
                    LocalDateTime.parse(endDate)
                } catch (e: Exception) {
                    promise.complete(
                        OperationResponse.failure(
                            error = OperationError(
                                code = ErrorCode.INVALID_INPUT,
                                message = "Invalid end date format. Expected: yyyy-MM-dd'T'HH:mm:ss"
                            )
                        )
                    )
                    return@launch
                }

                if (end.isBefore(start)) {
                    promise.complete(
                        OperationResponse.failure(
                            error = OperationError(
                                code = ErrorCode.INVALID_INPUT,
                                message = "End date must be after start date"
                            )
                        )
                    )
                    return@launch
                }

                // Get events from repository
                val result = eventRepository.getUserEvents(userId, startDate, endDate)

                // Transform the result
                val response = when {
                    result.success -> {
                        val events = result.data ?: emptyList()
                        OperationResponse.success(
                            data = JsonObject()
                                .put("events", JsonArray(events.map { it.toJson() }))
                                .put("totalCount", events.size)
                                .put(
                                    "dateRange", JsonObject()
                                        .put("start", startDate)
                                        .put("end", endDate)
                                ),
                            message = if (events.isEmpty())
                                "No events found for the specified date range"
                            else
                                "Retrieved ${events.size} events",
                            metadata = ResponseMetadata(
                                affectedRows = events.size,
                                operationType = OperationType.SELECT,
                                additionalInfo = JsonObject()
                                    .put("userId", userId)
                                    .put("startDate", startDate)
                                    .put("endDate", endDate)
                            )
                        )
                    }

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


    override fun updateParticipantResponse(
        eventId: String,
        userId: String,
        response: String
    ): Future<OperationResponse> {
        val promise = Promise.promise<OperationResponse>()

        launch {
            try {
                // Validate response value
                val participantResponse = try {
                    ParticipantResponse.valueOf(response.uppercase())
                } catch (e: IllegalArgumentException) {
                    promise.complete(
                        OperationResponse.failure(
                            error = OperationError(
                                code = ErrorCode.INVALID_INPUT,
                                message = "Invalid response value. Allowed values: ${
                                    ParticipantResponse.values().joinToString()
                                }"
                            )
                        )
                    )
                    return@launch
                }

                // Update response in repository
                val result = eventRepository.updateParticipantResponse(
                    eventId = eventId,
                    userId = userId,
                    response = participantResponse
                )

                when {
                    result.success -> {
                        // Notify event organizer asynchronously
                        notifyResponseUpdate(
                            event = result.data!!,
                            userId = userId,
                            response = participantResponse
                        )

                        // If all participants have responded, notify organizer
                        checkAllResponded(result.data)

                        promise.complete(
                            OperationResponse.success(
                                data = JsonObject()
                                    .put("event", result.data.toJson())
                                    .put("updatedResponse", response),
                                message = "Successfully updated participant response",
                                metadata = ResponseMetadata(
                                    affectedRows = 1,
                                    operationType = OperationType.UPDATE,
                                    additionalInfo = JsonObject()
                                        .put("eventId", eventId)
                                        .put("userId", userId)
                                        .put("response", response)
                                )
                            )
                        )
                    }

                    else -> promise.complete(result.toOperationResponse())
                }
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

    private fun notifyResponseUpdate(
        event: Event,
        userId: String,
        response: ParticipantResponse
    ) {
        // Get user details and send notification to organizer
        vertx.eventBus().publish(
            "notifications",
            JsonObject()
                .put("type", "RESPONSE_UPDATE")
                .put("eventId", event.id)
                .put("userId", userId)
                .put("organizerId", event.organizerId)
                .put("response", response.name)
                .put("eventTitle", event.title)
        )
    }

    private fun checkAllResponded(event: Event) {
        val allResponded = event.participants.none {
            it.response == ParticipantResponse.PENDING
        }

        if (allResponded) {
            vertx.eventBus().publish(
                "notifications",
                JsonObject()
                    .put("type", "ALL_RESPONDED")
                    .put("eventId", event.id)
                    .put("organizerId", event.organizerId)
                    .put("eventTitle", event.title)
                    .put("responses", JsonObject().apply {
                        ParticipantResponse.values().forEach { response ->
                            put(response.name, event.participants.count {
                                it.response == response
                            })
                        }
                    })
            )
        }
    }

    override fun getEvent(eventId: String): Future<OperationResponse> {
        val promise = Promise.promise<OperationResponse>()

        launch {
            try {
                val result = eventRepository.getEventById(eventId)

                when {
                    result.success -> {
                        val event = result.data
                        promise.complete(
                            OperationResponse.success(
                                data = event?.toJson(),
                                message = "Event retrieved successfully",
                                metadata = ResponseMetadata(
                                    affectedRows = 1,
                                    operationType = OperationType.SELECT,
                                    additionalInfo = JsonObject()
                                        .put("eventId", eventId)
                                )
                            )
                        )
                    }

                    else -> promise.complete(result.toOperationResponse())
                }
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

    private fun isTimeSlotAvailable(
        event: Event,
        availabilityResponse: OperationResponse
    ): Boolean {
        if (!availabilityResponse.success || availabilityResponse.data == null) {
            return false
        }

        val availableSlots = availabilityResponse.data.getJsonArray("slots")
        val eventStart = TimeUtils.toZonedDateTime(event.startTime)
        val eventEnd = TimeUtils.toZonedDateTime(event.endTime)

        return availableSlots.any { slot ->
            val slotJson = slot as JsonObject
            val slotStart = TimeUtils.toZonedDateTime(slotJson.getString("start"))
            val slotEnd = TimeUtils.toZonedDateTime(slotJson.getString("end"))

            !eventStart.isBefore(slotStart) && !eventEnd.isAfter(slotEnd)
        }
    }

    private fun filterSlotsByEvents(
        availableSlots: JsonArray,
        existingEvents: List<Event>,
        duration: Long
    ): List<JsonObject> {
        return availableSlots.mapNotNull { slot ->
            val slotJson = slot as JsonObject
            val slotStart = TimeUtils.toZonedDateTime(slotJson.getString("start"))
            val slotEnd = TimeUtils.toZonedDateTime(slotJson.getString("end"))

            // Check if slot duration is sufficient
            if (Duration.between(slotStart, slotEnd).toMinutes() < duration) {
                return@mapNotNull null
            }

            // Check for conflicts with existing events
            val hasConflict = existingEvents.any { event ->
                val eventStart = TimeUtils.toZonedDateTime(event.startTime)
                val eventEnd = TimeUtils.toZonedDateTime(event.endTime)

                !slotStart.isAfter(eventEnd) && !slotEnd.isBefore(eventStart)
            }

            if (hasConflict) null else slotJson
        }
    }

    private fun notifyParticipants(event: Event) {
        event.participants.forEach { participant ->
            vertx.eventBus().send(
                "notifications",
                JsonObject()
                    .put("type", "EVENT_INVITATION")
                    .put("eventId", event.id)
                    .put("userId", participant.userId)
                    .put("eventTitle", event.title)
                    .put("startTime", event.startTime)
                    .put("endTime", event.endTime)
                    .put("organizer", event.organizerId)
            )
        }
    }

    private fun notifyEventCancellation(event: Event) {
        event.participants.forEach { participant ->
            vertx.eventBus().send(
                "notifications",
                JsonObject()
                    .put("type", "EVENT_CANCELLED")
                    .put("eventId", event.id)
                    .put("userId", participant.userId)
                    .put("eventTitle", event.title)
                    .put("startTime", event.startTime)
                    .put("organizer", event.organizerId)
            )
        }
    }

    // In SchedulerServiceImpl
    private fun notifyEventUpdate(event: Event) {
        // Get all current participants
        val participants = event.participants.map { it.userId }.toSet()

        // Notify about the update
        launch {
            try {
                // Get previous event details for comparison
                val previousEvent = eventRepository.getEventById(event.id!!)
                val changedFields = getChangedFields(previousEvent.data, event)
                val ob = JsonObject()
                    .put("type", "EVENT_UPDATE")
                    .put("eventId", event.id)
                    .put("eventTitle", event.title)
                    .put("organizer", event.organizerId)
                    .put("changes", JsonObject().apply {
                        changedFields.forEach { (field, value) ->
                            put(field, value)
                        }
                    })
                    .put("startTime", event.startTime)
                    .put("endTime", event.endTime)
                    .put("isTimeChanged", changedFields.containsKey("time"))
                participants.forEach { userId ->
                    vertx.eventBus().publish(
                        "notifications",
                        ob.put("userId", userId)
                    )
                }

                // If time changed, notify about required reconfirmation
                if (changedFields.containsKey("time")) {
                    eventRepository.resetParticipantResponses(event.id)
                }
            } catch (e: Exception) {
                println("Failed to send event update notifications: ${e.message}")
            }
        }
    }

    private fun getChangedFields(oldEvent: Event?, newEvent: Event): Map<String, Any> {
        if (oldEvent == null) return emptyMap()

        val changes = mutableMapOf<String, Any>()

        if (oldEvent.title != newEvent.title) {
            changes["title"] = JsonObject()
                .put("old", oldEvent.title)
                .put("new", newEvent.title)
        }

        if (oldEvent.description != newEvent.description) {
            changes["description"] = JsonObject()
                .put("old", oldEvent.description)
                .put("new", newEvent.description)
        }

        if (oldEvent.startTime != newEvent.startTime ||
            oldEvent.endTime != newEvent.endTime
        ) {
            changes["time"] = JsonObject()
                .put(
                    "old", JsonObject()
                        .put("start", oldEvent.startTime)
                        .put("end", oldEvent.endTime)
                )
                .put(
                    "new", JsonObject()
                        .put("start", newEvent.startTime)
                        .put("end", newEvent.endTime)
                )
        }

        // Check for participant changes
        val oldParticipants = oldEvent.participants.map { it.userId }.toSet()
        val newParticipants = newEvent.participants.map { it.userId }.toSet()

        val added = newParticipants - oldParticipants
        val removed = oldParticipants - newParticipants

        if (added.isNotEmpty() || removed.isNotEmpty()) {
            changes["participants"] = JsonObject()
                .put("added", JsonArray(added.toList()))
                .put("removed", JsonArray(removed.toList()))
        }

        return changes
    }


}
