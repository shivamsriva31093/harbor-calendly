package io.harbor.calendly.repositories.db

import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.serviceproxy.ServiceBinder

class DatabaseVerticle : CoroutineVerticle() {
  override suspend fun start() {
    val dbConfig = config

    // Initialize database pool
    val dbPool = DatabaseClient.createPool(vertx, config)

    // Verify database connection
    val dbVerifier = DatabaseVerifier(dbPool)
    if (!dbVerifier.verifyConnection()) {
      throw IllegalStateException("Could not establish database connection")
    }

    try {
      DatabaseServiceFactory.create(vertx, dbPool) {
        if (it.succeeded()) {
          println("Database initialized successfully")
          ServiceBinder(vertx)
            .setAddress(DatabaseServiceFactory.SERVICE_ADDRESS)
            .register(DatabaseService::class.java, it.result())
          println("Database initialized successfully")
        } else {
          println("Database initialization failed")
          throw RuntimeException("Database initialization failed")
        }
      }
    } catch (e: Exception) {
      throw e
    }


  }

  override suspend fun stop() {
    DatabaseClient.close()
  }
}
