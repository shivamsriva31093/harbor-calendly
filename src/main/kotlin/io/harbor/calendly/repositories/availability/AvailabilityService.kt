package io.harbor.calendly.repositories.availability

import io.harbor.calendly.repositories.models.OperationResponse
import io.vertx.codegen.annotations.ProxyGen
import io.vertx.codegen.annotations.VertxGen
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.SqlClient
import org.jetbrains.annotations.NotNull

@ProxyGen
@VertxGen
interface AvailabilityService {
  fun addAvailabilityWindow(userId: String, window: AvailabilityWindow): Future<OperationResponse>
  fun updateAvailabilityWindow(userId: String, windowId: String, window: AvailabilityWindow): Future<OperationResponse>
  fun removeAvailabilityWindow(userId: String, windowId: String): Future<OperationResponse>
  fun getAvailabilityWindows(userId: String): Future<OperationResponse>
  fun findOverlappingSlots(request: JsonObject): Future<OperationResponse>
  fun getUserAvailabilityForDate(userId: String, date: String): Future<OperationResponse>
  fun findAvailableSlots(request: JsonObject): Future<OperationResponse>
}

object AvailabilityServiceFactory {
  const val SERVICE_NAME = "calendly-availability-service"
  const val SERVICE_ADDRESS = "calendly.availability.service"

  @JvmStatic
  fun create(
    @NotNull vertx: Vertx
  ): AvailabilityService {
    return AvailabilityServiceImpl(vertx)
  }

  @JvmStatic
  fun createProxy(vertx: Vertx, address: String): AvailabilityService {
    return AvailabilityServiceVertxEBProxy(vertx, address)
  }
}
