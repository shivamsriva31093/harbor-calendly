package io.harbor.calendly.repositories.user

import io.harbor.calendly.repositories.BaseService
import io.harbor.calendly.repositories.availability.AvailabilityServiceFactory
import io.harbor.calendly.repositories.models.ErrorCode
import io.harbor.calendly.repositories.models.OperationError
import io.harbor.calendly.repositories.models.OperationResponse
import io.harbor.calendly.repositories.models.toOperationResponse
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.dispatcher

// src/main/kotlin/com/harbor/calendar/service/UserServiceImpl.kt
class UserServiceImpl(private val vertx: Vertx) : BaseService(vertx, UserServiceFactory.SERVICE_NAME), UserService {
    private val repository = UserRepository()

    override fun createUser(request: JsonObject): Future<OperationResponse> {
        val promise = Promise.promise<OperationResponse>()

        launch {
            try {
                val user = User(request)

                if (!user.isValid()) {
                    promise.complete(OperationResponse.failure(
                        error = OperationError(
                            code = ErrorCode.INVALID_INPUT,
                            message = "Invalid user data"
                        )
                    ))
                    return@launch
                }

                val result = repository.createUser(user)
                val response = when {
                    result.success -> OperationResponse.success(
                        data = result.data?.toJson(),
                        message = "User created successfully"
                    )
                    else -> result.toOperationResponse()
                }

                promise.complete(response)
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

    override fun updateUser(userId: String, request: JsonObject): Future<OperationResponse> {
        val promise = Promise.promise<OperationResponse>()

        launch {
            try {
                val user = User(request)

                if (!user.isValid()) {
                    promise.complete(OperationResponse.failure(
                        error = OperationError(
                            code = ErrorCode.INVALID_INPUT,
                            message = "Invalid user data"
                        )
                    ))
                    return@launch
                }

                val result = repository.updateUser(userId, user)
                promise.complete(result.toOperationResponse())
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

    override fun getUser(userId: String): Future<OperationResponse> {
        val promise = Promise.promise<OperationResponse>()

        launch {
            try {
                val result = repository.getUser(userId)
                promise.complete(result.toOperationResponse())
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

    override fun deleteUser(userId: String): Future<OperationResponse> {
        val promise = Promise.promise<OperationResponse>()

        launch {
            try {
                val result = repository.deleteUser(userId)
                promise.complete(result.toOperationResponse())
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

    override fun getUserTimezone(userId: String): Future<OperationResponse> {
        val promise = Promise.promise<OperationResponse>()

        launch {
            try {
                val result = repository.getUser(userId)
                val response = when {
                    result.success -> OperationResponse.success(
                        data = JsonObject().put("timezone", result.data?.timezone),
                        message = "User timezone retrieved successfully"
                    )
                    else -> result.toOperationResponse()
                }
                promise.complete(response)
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
}
