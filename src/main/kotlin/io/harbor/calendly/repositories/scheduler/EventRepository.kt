package io.harbor.calendly.repositories.scheduler

import io.harbor.calendly.exceptions.BriefException
import io.harbor.calendly.repositories.BaseRepository
import io.harbor.calendly.repositories.models.ErrorCode
import io.harbor.calendly.repositories.models.OperationError
import io.harbor.calendly.repositories.models.OperationType
import io.harbor.calendly.repositories.models.RepositoryResult
import io.harbor.calendly.util.handleDatabaseException
import io.harbor.calendly.util.toEvent
import io.harbor.calendly.util.toJson
import io.harbor.calendly.util.toUUID
import io.harbor.calendly.utils.TimeUtils
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.Tuple
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

class EventRepository : BaseRepository() {

  companion object {
    val LOGGER = LoggerFactory.getLogger(EventRepository::class.java)
  }

  suspend fun getUserEvents(
    userId: String,
    startDate: String,
    endDate: String
  ): RepositoryResult<List<Event>> {
    return try {
      val result = pgPool.preparedQuery(
        """
                WITH user_events AS (
                    -- Events where user is organizer
                    SELECT
                        e.id,
                        e.title,
                        e.description,
                        e.start_time,
                        e.end_time,
                        e.organizer_id,
                        e.status,
                        e.metadata,
                        'organizer' as participation_type
                    FROM stg.events e
                    WHERE e.organizer_id = $1
                    AND e.start_time >= $2::timestamp
                    AND e.start_time <= $3::timestamp

                    UNION

                    -- Events where user is participant
                    SELECT
                        e.id,
                        e.title,
                        e.description,
                        e.start_time,
                        e.end_time,
                        e.organizer_id,
                        e.status,
                        e.metadata,
                        'participant' as participation_type
                    FROM stg.events e
                    JOIN stg.event_participants ep ON e.id = ep.event_id
                    WHERE ep.user_id = $1
                    AND e.start_time >= $2::timestamp
                    AND e.start_time <= $3::timestamp
                )
                SELECT
                    ue.*,
                    json_agg(
                        json_build_object(
                            'userId', ep.user_id,
                            'response', ep.response,
                            'notificationPreference', ep.notification_preference
                        )
                    ) as participants
                FROM user_events ue
                LEFT JOIN stg.event_participants ep ON ue.id = ep.event_id
                GROUP BY
                    ue.id,
                    ue.title,
                    ue.description,
                    ue.start_time,
                    ue.end_time,
                    ue.organizer_id,
                    ue.status,
                    ue.metadata,
                    ue.participation_type
                ORDER BY ue.start_time ASC
            """
      ).execute(
        Tuple.of(
          userId,
          LocalDateTime.parse(startDate, TimeUtils.DEFAULT_FORMATTER_UNI),
          LocalDateTime.parse(endDate, TimeUtils.DEFAULT_FORMATTER_UNI)
        )
      ).map { row -> row.toJson() }
        .coAwait()

      val events = result.map { row ->
        val ev = Event(row)
        ev.participants = row.getJsonArray("participants")
          .map { participant ->
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
          }
        ev.metadata = row.getJsonObject("metadata")?.let {
          EventMetadata(it)
        }
        ev
      }

      RepositoryResult.success(
        data = events,
        affectedRows = events.size,
        operation = OperationType.SELECT
      )
    } catch (e: Exception) {
      handleDatabaseException(e, OperationType.SELECT)
    }
  }

  suspend fun getConflictingEvents(
    userId: String,
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    excludeEventId: String? = null
  ): RepositoryResult<List<Event>> {
    return try {
      val params = mutableListOf<Any>(
        userId,
        startTime,
        endTime
      )

      var excludeClause = ""
      if (excludeEventId != null) {
        excludeClause = "AND e.id != $${params.size + 1}"
        params.add(excludeEventId)
      }

      val query = """
                SELECT e.*
                FROM stg.events e
                LEFT JOIN stg.event_participants ep ON e.id = ep.event_id
                WHERE (e.organizer_id = $1 OR ep.user_id = $1)
                AND e.status != 'CANCELLED'
                AND (
                    (e.start_time >= $2 AND e.start_time < $3)
                    OR (e.end_time > $2 AND e.end_time <= $3)
                    OR (e.start_time <= $2 AND e.end_time >= $3)
                )
                $excludeClause
            """

      val result = pgPool.preparedQuery(query)
        .execute(Tuple.from(params))
        .await()

      val events = result.map { row ->
        Event(
          id = row.getUUID("id").toString(),
          title = row.getString("title"),
          startTime = row.getLocalDateTime("start_time")
            .atZone(ZoneId.systemDefault())
            .toString(),
          endTime = row.getLocalDateTime("end_time")
            .atZone(ZoneId.systemDefault())
            .toString(),
          organizerId = row.getUUID("organizer_id").toString(),
          status = EventStatus.valueOf(row.getString("status")),
          participants = emptyList() // We don't need full participant details for conflict check
        )
      }

      RepositoryResult.success(
        data = events,
        affectedRows = events.size,
        operation = OperationType.SELECT
      )
    } catch (e: Exception) {
      handleDatabaseException(e, OperationType.SELECT)
    }
  }

  suspend fun updateParticipantResponse(
    eventId: String,
    userId: String,
    response: ParticipantResponse
  ): RepositoryResult<Event> {
    return try {
      // Use a transaction to ensure consistency
      pgPool.withTransaction { client ->
        // First verify the event exists and is active
        client.preparedQuery(
          """
                    SELECT
                        e.*,
                        json_agg(
                            json_build_object(
                                'userId', ep.user_id,
                                'response', ep.response,
                                'notificationPreference', ep.notification_preference
                            )
                        ) as participants
                    FROM stg.events e
                    LEFT JOIN stg.event_participants ep ON e.id = ep.event_id
                    WHERE e.id = $1
                    AND e.status != 'CANCELLED'
                    GROUP BY e.id
                """
        )
          .execute(Tuple.of(eventId))
          .compose { t ->
            if (t.rowCount() == 0) {
              return@compose Future.failedFuture(IllegalStateException("Event not found or is cancelled"))
            }
            client.preparedQuery(
              """
                    SELECT 1 FROM stg.event_participants
                    WHERE event_id = $1 AND user_id = $2
                """
            ).execute(Tuple.of(eventId, userId))
          }
          .compose { t ->
            if (t.rowCount() == 0) {
              return@compose Future.failedFuture(IllegalStateException("User is not a participant of this event"))
            }
            client.preparedQuery(
              """
                    UPDATE stg.event_participants
                    SET
                        response = $1,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE event_id = $2 AND user_id = $3
                    RETURNING *
                """
            ).execute(
              Tuple.of(
                response.name,
                eventId,
                userId
              )
            )
          }
          .compose { t ->
            client.preparedQuery(
              """
                    SELECT
                        e.*,
                        json_agg(
                            json_build_object(
                                'userId', ep.user_id,
                                'response', ep.response,
                                'notificationPreference', ep.notification_preference,
                                'updatedAt', ep.updated_at
                            )
                        ) as participants
                    FROM stg.events e
                    LEFT JOIN stg.event_participants ep ON e.id = ep.event_id
                    WHERE e.id = $1
                    GROUP BY e.id
                """
            ).execute(Tuple.of(eventId))
          }
      }
        .map { ev ->
          val eventRow = ev.toJson()[0]
          val event = Event(
            id = eventRow.getString("id"),
            title = eventRow.getString("title"),
            description = eventRow.getString("description"),
            startTime = TimeUtils.toUTCTimeString(eventRow.getString("start_time")),
            endTime = TimeUtils.toUTCTimeString(eventRow.getString("end_time")),
            organizerId = eventRow.getString("organizer_id"),
            status = EventStatus.valueOf(eventRow.getString("status")),
            participants = eventRow.getJsonArray("participants")
              .map { participant ->
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
              },
            metadata = eventRow.getJsonObject("metadata")?.let {
              EventMetadata(it)
            }
          )

          RepositoryResult.success(
            data = event,
            affectedRows = 1,
            operation = OperationType.UPDATE
          )
        }
        .onFailure { event ->
          RepositoryResult.failure<Event>(
            error = OperationError(
              code = ErrorCode.INVALID_INPUT,
              message = "User is not a participant of this event"
            ),
            operation = OperationType.UPDATE
          )
        }
        .coAwait()
    } catch (e: Exception) {
      handleDatabaseException(e, OperationType.UPDATE)
    }
  }

  suspend fun getEventById(eventId: String): RepositoryResult<Event> {
    return try {
      val result = pgPool.preparedQuery(
        """
           SELECT
               e.*,
               json_agg(
                   json_build_object(
                       'userId', ep.user_id,
                       'response', ep.response,
                       'notificationPreference', ep.notification_preference,
                       'updatedAt', ep.updated_at,
                       'responseHistory', ep.response_history
                   )
               ) as participants
           FROM stg.events e
           LEFT JOIN stg.event_participants ep ON e.id = ep.event_id
           WHERE e.id = $1
           GROUP BY e.id
       """
      ).execute(Tuple.of(eventId)).await()

      if (result.rowCount() == 0) {
        return RepositoryResult.failure(
          error = OperationError(
            code = ErrorCode.NOT_FOUND,
            message = "Event not found"
          ),
          operation = OperationType.SELECT
        )
      }

      val row = result.first()
      val event = Event(
        id = row.getUUID("id").toString(),
        title = row.getString("title"),
        description = row.getString("description"),
        startTime = row.getLocalDateTime("start_time")
          .atZone(ZoneId.systemDefault())
          .toString(),
        endTime = row.getLocalDateTime("end_time")
          .atZone(ZoneId.systemDefault())
          .toString(),
        organizerId = row.getUUID("organizer_id").toString(),
        status = EventStatus.valueOf(row.getString("status")),
        metadata = row.getJsonObject("metadata")?.let { EventMetadata(it) },
        participants = row.getJsonArray("participants")
          .map { Participant(it as JsonObject) }
      )

      RepositoryResult.success(
        data = event,
        affectedRows = 1,
        operation = OperationType.SELECT
      )
    } catch (e: Exception) {
      handleDatabaseException(e, OperationType.SELECT)
    }
  }

  suspend fun createEvent(event: Event): RepositoryResult<Event> {
    return try {
      pgPool.withTransaction { client ->
        // Check for overlapping active events for all participants
        client.preparedQuery(
          """
                SELECT e.id, e.title, e.start_time, e.end_time
                FROM stg.events e
                LEFT JOIN stg.event_participants ep ON e.id = ep.event_id
                WHERE (
                    e.organizer_id = ANY($1)
                    OR ep.user_id = ANY($1)
                )
                AND e.status != 'CANCELLED'
                AND tstzrange(e.start_time, e.end_time) && tstzrange($2, $3)
            """
        )
          .execute(
            Tuple.of(
              event.participants.map { UUID.fromString(it.userId) }.plus(UUID.fromString(event.organizerId)).toTypedArray(),
              TimeUtils.toZonedDateTime(event.startTime).toOffsetDateTime(),
              TimeUtils.toZonedDateTime(event.endTime).toOffsetDateTime(),
            )
          )
          .compose { overlapCheck ->
            if (overlapCheck.rowCount() > 0) {
              val conflictingEvent = overlapCheck.first()
              return@compose Future.succeededFuture<RepositoryResult<Event>>(
                RepositoryResult.failure(
                  error = OperationError(
                    code = ErrorCode.CONSTRAINT_VIOLATION,
                    message = "Overlapping active event found",
                    details = JsonObject()
                      .put("conflictingEventId", conflictingEvent.getUUID("id").toString())
                      .put("conflictingEventTitle", conflictingEvent.getString("title"))
                      .put(
                        "conflictingEventStart",
                        conflictingEvent.getLocalDateTime("start_time").toString()
                      )
                      .put(
                        "conflictingEventEnd",
                        conflictingEvent.getLocalDateTime("end_time").toString()
                      )
                  ),
                  operation = OperationType.INSERT
                )
              )
            }
            Future.succeededFuture()
          }
          .compose { t ->
            // Insert event
            client.preparedQuery(
              """
                INSERT INTO stg.events (
                    id, title, description, start_time, end_time,
                    organizer_id, status, metadata
                ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
                RETURNING *
            """
            ).execute(
              Tuple.of(
                event.id?.toUUID(),
                event.title,
                event.description,
                TimeUtils.toZonedDateTime(event.startTime).toOffsetDateTime(),
                TimeUtils.toZonedDateTime(event.endTime).toOffsetDateTime(),
                event.organizerId.toUUID(),
                event.status.name,
                event.metadata?.toJson()
              )
            )
          }
          .compose { t ->
            val batch = event.participants.map { participant ->
              Tuple.of(
                event.id?.toUUID(),
                participant.userId.toUUID(),
                participant.response.name,
                participant.notificationPreference.name
              )
            }.toList()
            client.preparedQuery(
              """
                    INSERT INTO stg.event_participants (
                        event_id, user_id, response, notification_preference
                    ) VALUES ($1, $2, $3, $4)
                """
            ).executeBatch(batch)
          }

      }
        .map { t ->
          RepositoryResult.success(
            data = event,
            affectedRows = 1,
            operation = OperationType.INSERT
          )
        }.coAwait()
    } catch (e: Exception) {
      handleDatabaseException(e, OperationType.INSERT)
    }
  }

  suspend fun updateEventStatus(
    eventId: String,
    status: EventStatus
  ): RepositoryResult<Event> {
    return try {
      val result = pgPool.preparedQuery(
        """
            UPDATE stg.events
            SET status = $1, updated_at = CURRENT_TIMESTAMP
            WHERE id = $2
            RETURNING *
        """
      ).execute(
        Tuple.of(
          status.name,
          eventId
        )
      ).await()

      if (result.rowCount() == 0) {
        RepositoryResult.failure(
          error = OperationError(
            code = ErrorCode.NOT_FOUND,
            message = "Event not found"
          ),
          operation = OperationType.UPDATE
        )
      } else {
        getEventById(eventId)
      }
    } catch (e: Exception) {
      handleDatabaseException(e, OperationType.UPDATE)
    }
  }

  suspend fun getEventsForUsers(
    userIds: List<String>,
    startDate: String,
    endDate: String
  ): RepositoryResult<List<Event>> {
    return try {
      pgPool.preparedQuery(
        """
            WITH user_events AS (
                SELECT e.*
                FROM stg.events e
                LEFT JOIN stg.event_participants ep ON e.id = ep.event_id
                WHERE (e.organizer_id = ANY($1) OR ep.user_id = ANY($1))
                AND e.start_time >= $2::timestamp
                AND e.start_time <= $3::timestamp
                AND e.status != 'CANCELLED'
            )
            SELECT
                ue.*,
                json_agg(
                    json_build_object(
                        'userId', ep.user_id,
                        'response', ep.response,
                        'notificationPreference', ep.notification_preference
                    )
                ) as participants
            FROM user_events ue
            LEFT JOIN stg.event_participants ep ON ue.id = ep.event_id
            GROUP BY ue.id
        """
      ).execute(
        Tuple.of(
          userIds.toTypedArray(),
          LocalDateTime.parse(startDate, TimeUtils.DEFAULT_FORMATTER_UNI),
          LocalDateTime.parse(endDate, TimeUtils.DEFAULT_FORMATTER_UNI)
        )
      ).map { t ->
        val events = t.map { row -> row.toEvent() }
        RepositoryResult.success(
          data = events,
          affectedRows = events.size,
          operation = OperationType.SELECT
        )
      }.coAwait()
    } catch (e: Exception) {
      handleDatabaseException(e, OperationType.SELECT)
    }
  }

  suspend fun updateEvent(eventId: String, event: Event): RepositoryResult<Event> {
    val promise = Promise.promise<RepositoryResult<Event>>()
    try {
      pgPool.withTransaction { client ->
        // First check if event exists and user has permission
        client.preparedQuery(
          """
               SELECT organizer_id, status
               FROM events
               WHERE id = $1
           """
        ).execute(Tuple.of(eventId))
          .compose { existingEvent ->
            if (existingEvent.rowCount() == 0) {
              return@compose Future.succeededFuture(
                RepositoryResult.failure<Event>(
                  error = OperationError(
                    code = ErrorCode.NOT_FOUND,
                    message = "Event not found"
                  ),
                  operation = OperationType.UPDATE
                )
              )
            }
            val row = existingEvent.first()
            if (row.getString("status") == EventStatus.CANCELLED.name) {
              return@compose Future.succeededFuture(
                RepositoryResult.failure<Event>(
                  error = OperationError(
                    code = ErrorCode.NOT_FOUND,
                    message = "Cannot update cancelled event"
                  ),
                  operation = OperationType.UPDATE
                )
              )
            }
            client.preparedQuery(
              """
               UPDATE events
               SET
                   title = $1,
                   description = $2,
                   start_time = $3,
                   end_time = $4,
                   status = $5,
                   metadata = $6,
                   updated_at = CURRENT_TIMESTAMP
               WHERE id = $7
               RETURNING *
           """
            ).execute(
              Tuple.of(
                event.title,
                event.description,
                LocalDateTime.parse(event.startTime, TimeUtils.DEFAULT_FORMATTER_UNI),
                LocalDateTime.parse(event.endTime, TimeUtils.DEFAULT_FORMATTER_UNI),
                event.status.name,
                event.metadata?.toJson(),
                eventId
              )
            ).map { rows -> rows.toJson() }
          }
          .compose { t ->
            // Update participants if provided
            if (event.participants.isNotEmpty()) {
              // First, remove participants that are no longer in the list
              val currentParticipantIds = event.participants.map { it.userId }
              return@compose client.preparedQuery(
                """
                   DELETE FROM event_participants
                   WHERE event_id = $1
                   AND user_id != ALL($2)
               """
              ).execute(Tuple.of(eventId, currentParticipantIds.toTypedArray()))
                .compose { t1 ->
                  val batch = event.participants.map { participant ->
                    Tuple.of(
                      eventId,
                      participant.userId,
                      participant.response.name,
                      participant.notificationPreference.name
                    )
                  }.toList()
                  client.preparedQuery(
                    """
                       INSERT INTO event_participants (
                           event_id, user_id, response, notification_preference
                       ) VALUES ($1, $2, $3, $4)
                       ON CONFLICT (event_id, user_id)
                       DO UPDATE SET
                           notification_preference = EXCLUDED.notification_preference,
                           updated_at = CURRENT_TIMESTAMP
                   """
                  ).executeBatch(batch)
                }
            }
            return@compose Future.succeededFuture()
          }
          .compose { t ->
            client.preparedQuery(
              """
               SELECT
                   e.*,
                   json_agg(
                       json_build_object(
                           'userId', ep.user_id,
                           'response', ep.response,
                           'notificationPreference', ep.notification_preference,
                           'updatedAt', ep.updated_at,
                           'responseHistory', ep.response_history
                       )
                   ) as participants
               FROM events e
               LEFT JOIN event_participants ep ON e.id = ep.event_id
               WHERE e.id = $1
               GROUP BY e.id
           """
            ).execute(Tuple.of(eventId))
          }
          .map { updatedEvent ->
            RepositoryResult.success(
              data = updatedEvent.first().toEvent(),
              affectedRows = 1,
              operation = OperationType.UPDATE
            )
          }
      }
        .onSuccess { event ->
          promise.complete(event)
        }
        .onFailure { event ->
          promise.complete(
            handleDatabaseException(
              BriefException(ErrorCode.UNKNOWN_ERROR.ordinal, event.cause!!),
              OperationType.UPDATE
            )
          )
        }
    } catch (e: Exception) {
      promise.complete(handleDatabaseException(e, OperationType.UPDATE))
    }

    return promise.future().coAwait()
  }

  suspend fun resetParticipantResponses(eventId: String) {
    try {
      pgPool.preparedQuery(
        """
           UPDATE event_participants
           SET
               response = 'PENDING',
               updated_at = CURRENT_TIMESTAMP,
               response_history = COALESCE(response_history, ARRAY[]::jsonb[]) ||
                   jsonb_build_object(
                       'response', response,
                       'timestamp', CURRENT_TIMESTAMP,
                       'reason', 'EVENT_TIME_CHANGED'
                   )::jsonb
           WHERE event_id = $1
           AND response != 'PENDING'
       """
      ).execute(Tuple.of(eventId)).await()
    } catch (e: Exception) {
      LOGGER.info("Failed to reset participant responses: ${e.message}")
    }
  }
}
