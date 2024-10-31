package io.harbor.calendly.repositories.scheduler

import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.serviceproxy.ServiceBinder

class SchedulerVerticle : CoroutineVerticle() {
  private lateinit var schedulerService: SchedulerService

  override suspend fun start() {
    try {
      schedulerService = SchedulerServiceFactory.create(vertx)
      ServiceBinder(vertx)
        .setAddress(SchedulerServiceFactory.SERVICE_ADDRESS)
        .register(SchedulerService::class.java, schedulerService)
    } catch (e: Exception) {
      throw e
    }
  }

}
