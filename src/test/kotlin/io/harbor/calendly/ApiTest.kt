package io.harbor.calendly

import io.harbor.calendly.utils.FileUtils
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class ApiTest {


    companion object {
      private lateinit var webClient: WebClient
      private lateinit var deploymentId: String
      @JvmStatic
      @BeforeAll
      fun setup(vertx: Vertx, testContext: VertxTestContext) {
        val config = FileUtils.getJsonFromFile("config/app.config.json")
        val deploymentOptions = DeploymentOptions().setConfig(config)

        vertx.deployVerticle(MainVerticle(), deploymentOptions)
          .onComplete { ar ->
            if (ar.succeeded()) {
              deploymentId = ar.result()
              webClient = WebClient.create(vertx)
              testContext.completeNow()
            } else {
              testContext.failNow(ar.cause())
            }
          }
      }

      @JvmStatic
      @AfterAll
      fun tearDown(vertx: Vertx, testContext: VertxTestContext) {
        vertx.undeploy(deploymentId)
          .onComplete {
            vertx.close().onComplete { testContext.completeNow() }
          }
      }

    }




    @Test
    fun `should create and get event`(vertx: Vertx, testContext: VertxTestContext) {
        // Given
        val eventRequest = JsonObject()
            .put("title", "API Test Meeting")
            .put("startTime", "2024-01-01T10:00:00Z")
            .put("endTime", "2024-01-01T11:00:00Z")
            .put("organizerId", "user1")
            .put("participants", JsonArray().add(
                JsonObject()
                    .put("userId", "user2")
                    .put("response", "PENDING")
            ))

        // When

        webClient.postAbs("http://localhost:8080/api/v1/events")
            .sendJsonObject(eventRequest)
            .onComplete(testContext.succeeding { response ->
                testContext.verify {
                    assertEquals(201, response.statusCode())
                    val eventId = response.bodyAsJsonObject()
                        .getJsonObject("data")
                        .getString("id")

                    // Then get the created event
                    webClient.getAbs("http://localhost:8080/api/v1/events/$eventId")
                        .send()
                        .onComplete(testContext.succeeding { getResponse ->
                            testContext.verify {
                                assertEquals(200, getResponse.statusCode())
                                val event = getResponse.bodyAsJsonObject()
                                    .getJsonObject("data")
                                assertEquals("API Test Meeting", event.getString("title"))
                                testContext.completeNow()
                            }
                        })
                }
            })
    }

    @Test
    fun `should find available slots`(vertx: Vertx, testContext: VertxTestContext) {
        // Given
        val userIds = JsonArray().add("user1").add("user2")
        val startDate = "2024-01-01"
        val endDate = "2024-01-07"

        // When
        webClient.getAbs("http://localhost:8080/api/v1/schedule/overlap")
            .addQueryParam("userIds", userIds.encode())
            .addQueryParam("startDate", startDate)
            .addQueryParam("endDate", endDate)
            .send()
            .onComplete(testContext.succeeding { response ->
                testContext.verify {
                    assertEquals(200, response.statusCode())
                    val slots = response.bodyAsJsonObject()
                        .getJsonObject("data")
                        .getJsonArray("slots")
                    assertNotNull(slots)
                    testContext.completeNow()
                }
            })
    }
}
