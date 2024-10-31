package io.harbor.calendly.repositories.scheduler

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject
import io.vertx.core.json.JsonArray
import java.time.ZonedDateTime

@DataObject(generateConverter = true)
data class Event(
  val id: String? = null,
  val title: String,
  val description: String? = null,
  val startTime: String,  // ISO-8601 format
  val endTime: String,    // ISO-8601 format
  val organizerId: String,
  var participants: List<Participant>,
  var metadata: EventMetadata? = null,
  val status: EventStatus = EventStatus.SCHEDULED
) {
  constructor(json: JsonObject) : this(
    id = json.getString("id"),
    title = json.getString("title"),
    description = json.getString("description"),
    startTime = json.getString("startTime"),
    endTime = json.getString("endTime"),
    organizerId = json.getString("organizerId"),
    participants = json.getJsonArray("participants", JsonArray())
      .map { Participant(it as JsonObject) },
    metadata = json.getJsonObject("metadata")?.let { EventMetadata(it) },
    status = EventStatus.valueOf(json.getString("status", "SCHEDULED"))
  )

  fun toJson(): JsonObject = JsonObject()
    .put("id", id)
    .put("title", title)
    .put("description", description)
    .put("startTime", startTime)
    .put("endTime", endTime)
    .put("organizerId", organizerId)
    .put("participants", JsonArray(participants.map { it.toJson() }))
    .put("metadata", metadata?.toJson())
    .put("status", status.name)

  fun isValid(): Boolean {
    return try {
      val start = ZonedDateTime.parse(startTime)
      val end = ZonedDateTime.parse(endTime)
      title.isNotBlank() &&
        !end.isBefore(start) &&
        participants.isNotEmpty()
    } catch (e: Exception) {
      false
    }
  }
}

@DataObject(generateConverter = true)
data class Participant(
  val userId: String,
  val response: ParticipantResponse = ParticipantResponse.PENDING,
  val notificationPreference: NotificationPreference = NotificationPreference.EMAIL
) {
  constructor(json: JsonObject) : this(
    userId = json.getString("userId"),
    response = ParticipantResponse.valueOf(
      json.getString("response", "PENDING")
    ),
    notificationPreference = NotificationPreference.valueOf(
      json.getString("notificationPreference", "EMAIL")
    )
  )

  fun toJson(): JsonObject = JsonObject()
    .put("userId", userId)
    .put("response", response.name)
    .put("notificationPreference", notificationPreference.name)
}

@DataObject(generateConverter = true)
data class EventMetadata(
  val location: String? = null,
  val conferenceLink: String? = null,
  val recurringPattern: RecurringPattern? = null,
  val customProperties: JsonObject? = null
) {
  constructor(json: JsonObject) : this(
    location = json.getString("location"),
    conferenceLink = json.getString("conferenceLink"),
    recurringPattern = json.getJsonObject("recurringPattern")
      ?.let { RecurringPattern(it) },
    customProperties = json.getJsonObject("customProperties")
  )

  fun toJson(): JsonObject = JsonObject()
    .put("location", location)
    .put("conferenceLink", conferenceLink)
    .put("recurringPattern", recurringPattern?.toJson())
    .put("customProperties", customProperties)
}

@DataObject(generateConverter = true)
data class RecurringPattern(
  val frequency: RecurringFrequency,
  val interval: Int = 1,
  val endDate: String? = null,
  val occurrences: Int? = null
) {
  constructor(json: JsonObject) : this(
    frequency = RecurringFrequency.valueOf(
      json.getString("frequency", "NONE")
    ),
    interval = json.getInteger("interval", 1),
    endDate = json.getString("endDate"),
    occurrences = json.getInteger("occurrences")
  )

  fun toJson(): JsonObject = JsonObject()
    .put("frequency", frequency.name)
    .put("interval", interval)
    .put("endDate", endDate)
    .put("occurrences", occurrences)
}

enum class EventStatus {
  SCHEDULED,
  CANCELLED,
  COMPLETED,
  RESCHEDULED
}

enum class ParticipantResponse {
  PENDING,
  ACCEPTED,
  DECLINED,
  TENTATIVE
}

enum class NotificationPreference {
  EMAIL,
  SMS,
  BOTH,
  NONE
}

enum class RecurringFrequency {
  NONE,
  DAILY,
  WEEKLY,
  MONTHLY,
  YEARLY
}

data class UpdatedEventResponse(
  val event: Event,
  val responseStats: JsonObject,
  val updatedParticipant: UpdatedParticipant
)

data class UpdatedParticipant(
  val userId: String,
  val newResponse: ParticipantResponse,
  val previousResponse: ParticipantResponse,
  val updatedAt: ZonedDateTime
)
