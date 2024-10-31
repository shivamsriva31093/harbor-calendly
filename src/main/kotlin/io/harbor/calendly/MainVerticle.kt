package io.harbor.calendly

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.harbor.calendly.repositories.availability.AvailabilityVerticle
import io.harbor.calendly.repositories.db.DatabaseVerticle
import io.harbor.calendly.repositories.scheduler.SchedulerVerticle
import io.harbor.calendly.repositories.user.UserVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.logging.SLF4JLogDelegateFactory
import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.slf4j.LoggerFactory
import java.io.FileInputStream

class MainVerticle : CoroutineVerticle() {

  init {

    // set vertx logger delegate factory to slf4j

    // set vertx logger delegate factory to slf4j
    val logFactory = System.getProperty("org.vertx.logger-delegate-factory-class-name")
    if (logFactory == null) {
      System.setProperty("org.vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory::class.java.name)
    }
  }

  override suspend fun start() {
    try {
      deployVerticles()
    } catch (e: Exception) {
      LOGGER.error("Failed to deploy verticles: ${e.message}", e)
      throw e
    }
  }

  private fun deployVerticles() {
    // Deploy Database Verticle
    vertx.deployVerticle(
      DatabaseVerticle::class.java.name,
      DeploymentOptions().setConfig(config.getJsonObject("database"))
    ).compose { t ->
      LOGGER.info("DatabaseVerticle deployed successfully: ${t}")
      vertx.deployVerticle(
        SchedulerVerticle::class.java.name,
        DeploymentOptions().setConfig(config.getJsonObject("scheduler"))
      )
    }.compose { t ->
      LOGGER.info("SchedulerVerticle deployed successfully: ${t}")
      vertx.deployVerticle(
        AvailabilityVerticle::class.java.name,
        DeploymentOptions().setConfig(config.getJsonObject("availability"))
      )
    }
      .compose { t ->
      LOGGER.info("AvailabilityVerticle deployed successfully: ${t}")
      vertx.deployVerticle(
        UserVerticle::class.java.name,
        DeploymentOptions().setConfig(config.getJsonObject("user"))
      )
    }
      .compose { t ->
        LOGGER.info("UserVerticle deployed successfully: ${t}")
        vertx.deployVerticle(
          NotificationVerticle::class.java.name,
          DeploymentOptions().setConfig(config.getJsonObject("mail"))
        )
      }
      .compose { t ->
        LOGGER.info("NotificationVerticle deployed successfully: ${t}")
        vertx.deployVerticle(
          RestApiServerVerticle::class.java.name,
          DeploymentOptions().setConfig(config.getJsonObject("server"))
        )
      }
      .onSuccess { t ->
        LOGGER.info("RestApiServerVerticle deployed successfully: ${t}")
        LOGGER.info("All verticles deployed successfully")
      }
      .onFailure { event ->
        LOGGER.error("RestApiServerVerticle deployment failed: ${event.message}", event.cause)
      }

  }

  companion object {
    val LOGGER = LoggerFactory.getLogger(MainVerticle::class.java)!!

    @JvmStatic
    fun main(args: Array<String>) {
      val vertx = Vertx.vertx()
      vertx.deployVerticle(MainVerticle())
    }
  }
}
