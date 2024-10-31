package io.harbor.calendly.repositories.user

import io.harbor.calendly.repositories.models.OperationResponse
import io.vertx.codegen.annotations.ProxyGen
import io.vertx.codegen.annotations.VertxGen
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject

@ProxyGen
@VertxGen
interface UserService {
  fun createUser(request: JsonObject): Future<OperationResponse>
  fun updateUser(userId: String, request: JsonObject): Future<OperationResponse>
  fun getUser(userId: String): Future<OperationResponse>
  fun deleteUser(userId: String): Future<OperationResponse>
  fun getUserTimezone(userId: String): Future<OperationResponse>
}

object UserServiceFactory {
  const val SERVICE_NAME = "calendly-user-service"
  const val SERVICE_ADDRESS = "calendly.user.service"

  @JvmStatic
  fun create(vertx: Vertx): UserService = UserServiceImpl(vertx)

  @JvmStatic
  fun createProxy(vertx: Vertx, address: String): UserService =
    UserServiceVertxEBProxy(vertx, address)

}
