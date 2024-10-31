package io.harbor.calendly

import io.harbor.calendly.handlers.CorsHandler
import io.harbor.calendly.repositories.availability.AvailabilityService
import io.harbor.calendly.repositories.availability.AvailabilityServiceFactory
import io.harbor.calendly.repositories.availability.AvailabilityWindow
import io.harbor.calendly.repositories.models.ErrorCode
import io.harbor.calendly.repositories.models.OperationError
import io.harbor.calendly.repositories.models.OperationResponse
import io.harbor.calendly.repositories.models.OperationType
import io.harbor.calendly.repositories.scheduler.SchedulerService
import io.harbor.calendly.repositories.scheduler.SchedulerServiceFactory
import io.harbor.calendly.repositories.user.UserService
import io.harbor.calendly.repositories.user.UserServiceFactory
import io.harbor.calendly.utils.TimeUtils
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.coAwait
import kotlinx.coroutines.launch

class RestApiServerVerticle : CoroutineVerticle() {
  private lateinit var userService: UserService
  private lateinit var schedulerService: SchedulerService
  private lateinit var availabilityService: AvailabilityService

  override suspend fun start() {
    userService = UserServiceFactory.createProxy(vertx, UserServiceFactory.SERVICE_ADDRESS)
    schedulerService = SchedulerServiceFactory.createProxy(vertx, SchedulerServiceFactory.SERVICE_ADDRESS)
    availabilityService = AvailabilityServiceFactory.createProxy(vertx, AvailabilityServiceFactory.SERVICE_ADDRESS)

    val v1Router = Router.router(vertx)
    setupRoutes(v1Router)

    val router = Router.router(vertx)
    router.route("/api/v1/*").subRouter(v1Router)


    val port = config.getInteger("port", 8080)
    vertx.createHttpServer()
      .requestHandler(router)
      .listen(port)
      .coAwait()
  }

  private fun setupRoutes(router: Router) {
    router.route().handler(BodyHandler.create())
    router.route().handler {ev -> CorsHandler.getCorsHandler().handle(ev) }

    // User routes
    router.post("/users").handler { handleUserCreate(it) }
    router.get("/users/:userId").handler { handleGetUser(it) }
    router.put("/users/:userId").handler { handleUserUpdate(it) }
    router.delete("/users/:userId").handler { handleUserDelete(it) }
    router.get("/users/:userId/timezone").handler { handleGetUserTimezone(it) }

    // Availability routes
    router.post("/users/:userId/availability").handler { handleAddAvailability(it) }
    router.get("/users/:userId/availability").handler { handleGetAvailability(it) }
    router.put("/users/:userId/availability/:id").handler { handleUpdateAvailability(it) }
    router.delete("/users/:userId/availability/:id").handler { handleDeleteAvailability(it) }
    router.get("/schedule/overlap").handler { handleFindOverlap(it) }

    // Event routes
    router.post("/events").handler { handleCreateEvent(it) }
    router.get("/events/:eventId").handler { handleGetEvent(it) }
    router.put("/events/:eventId").handler { handleUpdateEvent(it) }
    router.delete("/events/:eventId").handler { handleCancelEvent(it) }
    router.get("/users/:userId/events").handler { handleGetUserEvents(it) }
    router.put("/events/:eventId/participants/:userId/response").handler { handleParticipantResponse(it) }
  }

  private fun handleUserCreate(ctx: RoutingContext) {
    launch {
      try {
        val response = userService.createUser(ctx.body().asJsonObject()).coAwait()
        sendResponse(ctx, response)
      } catch (e: Exception) {
        handleError(ctx, e)
      }
    }
  }

  private fun handleGetUser(ctx: RoutingContext) {
    launch {
      try {
        val userId = ctx.pathParam("userId")
        val response = userService.getUser(userId).coAwait()
        sendResponse(ctx, response)
      } catch (e: Exception) {
        handleError(ctx, e)
      }
    }
  }

  private fun handleUserUpdate(ctx: RoutingContext) {
    launch {
      try {
        val userId = ctx.pathParam("userId")
        val response = userService.updateUser(userId, ctx.body().asJsonObject()).coAwait()
        sendResponse(ctx, response)
      } catch (e: Exception) {
        handleError(ctx, e)
      }
    }
  }

  private fun handleFindOverlap(ctx: RoutingContext) {
    launch {
      try {
        val userIdsString = ctx.queryParams().get("userIds")
        if (userIdsString == null) {
          ctx.response()
            .setStatusCode(400)
            .end("""{"error": "userIds parameter is required"}""")
          return@launch
        }

        // Parse the JSON array
        val userIds = JsonArray(userIdsString)

        // Get other parameters
        val startDate = ctx.queryParams().get("startDate")
        val endDate = ctx.queryParams().get("endDate")

        // Validate required parameters
        if (startDate == null || endDate == null) {
          ctx.response()
            .setStatusCode(400)
            .end("""{"error": "startDate and endDate are required"}""")
          return@launch
        }

        // Convert JsonArray to List<String> if needed
        val userIdList = userIds.map { it as String }

        val request = JsonObject()
          .put("userIds", JsonArray(userIdList))
          .put("startDate", startDate)
          .put("endDate", endDate)

        val response = availabilityService.findOverlappingSlots(request).coAwait()
        sendResponse(ctx, response)
      } catch (e: Exception) {
        handleError(ctx, e)
      }
    }
  }

  private fun handleCreateEvent(ctx: RoutingContext) {
    launch {
      try {
        val response = schedulerService.scheduleEvent(ctx.body().asJsonObject()).coAwait()
        sendResponse(ctx, response)
      } catch (e: Exception) {
        handleError(ctx, e)
      }
    }
  }

  private fun handleParticipantResponse(ctx: RoutingContext) {
    launch {
      try {
        val eventId = ctx.pathParam("eventId")
        val userId = ctx.pathParam("userId")
        val response = ctx.body().asJsonObject().getString("response")

        val result = schedulerService.updateParticipantResponse(
          eventId, userId, response
        ).coAwait()

        sendResponse(ctx, result)
      } catch (e: Exception) {
        handleError(ctx, e)
      }
    }
  }

  private fun handleUserDelete(ctx: RoutingContext) {
    launch {
      try {
        val userId = ctx.pathParam("userId")
        val response = userService.deleteUser(userId).coAwait()
        sendResponse(ctx, response)
      } catch (e: Exception) {
        handleError(ctx, e)
      }
    }
  }

  private fun handleGetUserTimezone(ctx: RoutingContext) {
    launch {
      try {
        val userId = ctx.pathParam("userId")
        val response = userService.getUserTimezone(userId).coAwait()
        sendResponse(ctx, response)
      } catch (e: Exception) {
        handleError(ctx, e)
      }
    }
  }

  private fun handleAddAvailability(ctx: RoutingContext) {
    launch {
      try {
        val userId = ctx.pathParam("userId")
        val window = AvailabilityWindow(ctx.body().asJsonObject())
        val response = availabilityService.addAvailabilityWindow(userId, window).coAwait()
        sendResponse(ctx, response)
      } catch (e: Exception) {
        handleError(ctx, e)
      }
    }
  }

  private fun handleGetAvailability(ctx: RoutingContext) {
    launch {
      try {
        val userId = ctx.pathParam("userId")
        val response = availabilityService.getAvailabilityWindows(userId).coAwait()
        sendResponse(ctx, response)
      } catch (e: Exception) {
        handleError(ctx, e)
      }
    }
  }

  private fun handleUpdateAvailability(ctx: RoutingContext) {
    launch {
      try {
        val userId = ctx.pathParam("userId")
        val windowId = ctx.pathParam("id")
        val window = AvailabilityWindow(ctx.body().asJsonObject())
        val response = availabilityService.updateAvailabilityWindow(userId, windowId, window).coAwait()
        sendResponse(ctx, response)
      } catch (e: Exception) {
        handleError(ctx, e)
      }
    }
  }

  private fun handleDeleteAvailability(ctx: RoutingContext) {
    launch {
      try {
        val userId = ctx.pathParam("userId")
        val windowId = ctx.pathParam("id")
        val response = availabilityService.removeAvailabilityWindow(userId, windowId).coAwait()
        sendResponse(ctx, response)
      } catch (e: Exception) {
        handleError(ctx, e)
      }
    }
  }

  private fun handleGetEvent(ctx: RoutingContext) {
    launch {
      try {
        val eventId = ctx.pathParam("eventId")
        val response = schedulerService.getEvent(eventId).coAwait()
        sendResponse(ctx, response)
      } catch (e: Exception) {
        handleError(ctx, e)
      }
    }
  }

  private fun handleUpdateEvent(ctx: RoutingContext) {
    launch {
      try {
        val eventId = ctx.pathParam("eventId")
        val response = schedulerService.updateEvent(eventId, ctx.body().asJsonObject()).coAwait()
        sendResponse(ctx, response)
      } catch (e: Exception) {
        handleError(ctx, e)
      }
    }
  }

  private fun handleCancelEvent(ctx: RoutingContext) {
    launch {
      try {
        val eventId = ctx.pathParam("eventId")
        val response = schedulerService.cancelEvent(eventId).coAwait()
        sendResponse(ctx, response)
      } catch (e: Exception) {
        handleError(ctx, e)
      }
    }
  }

  private fun handleGetUserEvents(ctx: RoutingContext) {
    launch {
      try {
        val userId = ctx.pathParam("userId")
        val startDate = ctx.queryParam("startDate").firstOrNull()
          ?: TimeUtils.now().format(TimeUtils.HUMAN_READABLE_FORMATTER)
        val endDate = ctx.queryParam("endDate").firstOrNull()
          ?: TimeUtils.now().plusDays(7).format(TimeUtils.HUMAN_READABLE_FORMATTER)

        val response = schedulerService.getUserEvents(userId, startDate, endDate).coAwait()
        sendResponse(ctx, response)
      } catch (e: Exception) {
        handleError(ctx, e)
      }
    }
  }

  private fun sendResponse(ctx: RoutingContext, response: OperationResponse) {
    val statusCode = when {
      response.success -> when(response.metadata.operationType) {
        OperationType.INSERT -> 201
        OperationType.DELETE -> 204
        else -> 200
      }
      else -> when(response.error?.code) {
        ErrorCode.NOT_FOUND -> 404
        ErrorCode.INVALID_INPUT -> 400
        ErrorCode.DUPLICATE_ENTRY -> 409
        ErrorCode.CONSTRAINT_VIOLATION -> 422
        else -> 500
      }
    }

    ctx.response()
      .setStatusCode(statusCode)
      .putHeader("Content-Type", "application/json")
      .end(response.toJson().encode())
  }

  private fun handleError(ctx: RoutingContext, e: Exception) {
    val response = OperationResponse.failure(
      error = OperationError(
        code = ErrorCode.UNKNOWN_ERROR,
        message = e.message ?: "Unknown error occurred"
      )
    )
    sendResponse(ctx, response)
  }
}
