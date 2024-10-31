// src/main/kotlin/com/harbor/calendar/service/SchedulerService.kt
package io.harbor.calendly.repositories.scheduler

import io.harbor.calendly.repositories.models.OperationResponse
import io.vertx.codegen.annotations.ProxyGen
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject

@ProxyGen
interface SchedulerService {
    /**
     * Schedule a new event/meeting with multiple participants
     */
    fun scheduleEvent(request: JsonObject): Future<OperationResponse>

    /**
     * Find available time slots that work for all participants
     */
    fun findAvailableSlots(request: JsonObject): Future<OperationResponse>

    /**
     * Cancel a scheduled event
     */
    fun cancelEvent(eventId: String): Future<OperationResponse>

    /**
     * Update an existing event
     */
    fun updateEvent(eventId: String, request: JsonObject): Future<OperationResponse>

    /**
     * Get events for a user within a date range
     */
    fun getUserEvents(
        userId: String,
        startDate: String,
        endDate: String
    ): Future<OperationResponse>

    /**
     * Update a participant's response to an event
     */
    fun updateParticipantResponse(
        eventId: String,
        userId: String,
        response: String
    ): Future<OperationResponse>

    fun getEvent(eventId: String): Future<OperationResponse>

}

object SchedulerServiceFactory {
    const val SERVICE_NAME = "calendly-scheduler-service"
    const val SERVICE_ADDRESS = "calendly.scheduler.service"

    @JvmStatic
    fun create(vertx: Vertx): SchedulerService = SchedulerServiceImpl(vertx)

    @JvmStatic
    fun createProxy(vertx: Vertx, address: String): SchedulerService =
        SchedulerServiceVertxEBProxy(vertx, address)
}
