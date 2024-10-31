package io.harbor.calendly.repositories.availability

import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.serviceproxy.ServiceBinder

class AvailabilityVerticle : CoroutineVerticle() {
  private lateinit var availabilityService: AvailabilityService

  override suspend fun start() {
    try {
      val svc = AvailabilityServiceFactory.create(vertx)
      ServiceBinder(vertx)
        .setAddress(AvailabilityServiceFactory.SERVICE_ADDRESS)
        .register(AvailabilityService::class.java, svc)
    } catch (e: Exception) {
      throw e
    }
  }

}
