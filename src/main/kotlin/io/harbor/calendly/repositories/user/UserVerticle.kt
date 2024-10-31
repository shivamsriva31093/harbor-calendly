package io.harbor.calendly.repositories.user

import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.serviceproxy.ServiceBinder

class UserVerticle : CoroutineVerticle() {
  private lateinit var userService: UserService

  override suspend fun start() {
    try {
      userService = UserServiceFactory.create(vertx)
      ServiceBinder(vertx)
        .setAddress(UserServiceFactory.SERVICE_ADDRESS)
        .register(UserService::class.java, userService)
    } catch (e: Exception) {
      throw e
    }
  }

}
