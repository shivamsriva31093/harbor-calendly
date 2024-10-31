package io.harbor.calendly

import io.harbor.calendly.repositories.user.User
import io.harbor.calendly.repositories.user.UserService
import io.harbor.calendly.repositories.user.UserServiceFactory
import io.vertx.core.json.JsonObject
import io.vertx.ext.mail.MailClient
import io.vertx.ext.mail.MailConfig
import io.vertx.ext.mail.MailMessage
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.templ.handlebars.HandlebarsTemplateEngine
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class NotificationVerticle : CoroutineVerticle() {
  private lateinit var mailClient: MailClient
  private lateinit var userService: UserService
  private lateinit var templateEngine: HandlebarsTemplateEngine

  override suspend fun start() {
    // Initialize mail client
    val mailConfig = config
    mailClient = MailClient.createShared(
      vertx,
      MailConfig().apply {
        hostname = mailConfig.getString("host")
        port = mailConfig.getInteger("port")
        username = mailConfig.getString("username")
        password = mailConfig.getString("password")
      }
    )

    // Initialize user service
    userService = UserServiceFactory.createProxy(vertx, UserServiceFactory.SERVICE_ADDRESS)

    // Initialize template engine
    templateEngine = HandlebarsTemplateEngine.create(Vertx.newInstance(vertx))

    // Register event bus handlers
    vertx.eventBus().consumer<JsonObject>("notifications") { message ->
      handleNotification(message.body())
    }
  }

  private fun handleNotification(notification: JsonObject) {
    launch {
      try {
        when (notification.getString("type")) {
          "EVENT_INVITATION" -> sendEventInvitation(notification)
          "EVENT_UPDATE" -> sendEventUpdate(notification)
          "EVENT_CANCELLED" -> sendEventCancellation(notification)
          "RESPONSE_UPDATE" -> sendResponseUpdate(notification)
          "ALL_RESPONDED" -> sendAllRespondedNotification(notification)
        }
      } catch (e: Exception) {
        println("Failed to send notification: ${e.message}")
      }
    }
  }

  private suspend fun sendEventInvitation(notification: JsonObject) {
    val userId = notification.getString("userId")
    val user = getUserDetails(userId)

    val context = JsonObject()
      .put("userName", "${user.firstName} ${user.lastName}")
      .put("eventTitle", notification.getString("eventTitle"))
      .put("startTime", formatDateTime(notification.getString("startTime")))
      .put("endTime", formatDateTime(notification.getString("endTime")))
      .put("organizerName", getOrganizerName(notification.getString("organizer")))
      .put("eventId", notification.getString("eventId"))

    templateEngine.rxRender(context, "templates/event-invitation.hbs")
      .subscribe { buffer ->
        launch {
          sendEmail(
            to = user.email,
            subject = "Event Invitation: ${notification.getString("eventTitle")}",
            html = buffer.toString()
          )
        }
      }
  }

  private suspend fun sendEventUpdate(notification: JsonObject) {
    val userId = notification.getString("userId")
    val user = getUserDetails(userId)
    val changes = notification.getJsonObject("changes")

    val context = JsonObject()
      .put("userName", "${user.firstName} ${user.lastName}")
      .put("eventTitle", notification.getString("eventTitle"))
      .put("changes", formatChanges(changes))
      .put("requiresResponse", notification.getBoolean("isTimeChanged", false))
      .put("eventId", notification.getString("eventId"))

    templateEngine.rxRender(context, "templates/event-update.hbs")
      .subscribe { buffer ->
        launch {
          sendEmail(
            to = user.email,
            subject = "Event Updated: ${notification.getString("eventTitle")}",
            html = buffer.toString()
          )
        }
      }


  }

  private suspend fun sendEventCancellation(notification: JsonObject) {
    val userId = notification.getString("userId")
    val user = getUserDetails(userId)

    val context = JsonObject()
      .put("userName", "${user.firstName} ${user.lastName}")
      .put("eventTitle", notification.getString("eventTitle"))
      .put("startTime", formatDateTime(notification.getString("startTime")))
      .put("organizerName", getOrganizerName(notification.getString("organizer")))

    templateEngine.rxRender(context, "templates/event-cancellation.hbs")
      .subscribe { html ->
        launch {
          sendEmail(
            to = user.email,
            subject = "Event Cancelled: ${notification.getString("eventTitle")}",
            html = html.toString()
          )
        }
      }
  }

  private suspend fun sendResponseUpdate(notification: JsonObject) {
    val organizerId = notification.getString("organizerId")
    val organizer = getUserDetails(organizerId)
    val responder = getUserDetails(notification.getString("userId"))

    val context = JsonObject()
      .put("organizerName", "${organizer.firstName} ${organizer.lastName}")
      .put("responderName", "${responder.firstName} ${responder.lastName}")
      .put("eventTitle", notification.getString("eventTitle"))
      .put("response", notification.getString("response"))
      .put("eventId", notification.getString("eventId"))

    templateEngine.rxRender(context, "templates/response-update.hbs")
      .subscribe { html ->
        launch {
          sendEmail(
            to = organizer.email,
            subject = "Event Response Update: ${notification.getString("eventTitle")}",
            html = html.toString()
          )
        }
      }


  }

  private suspend fun sendAllRespondedNotification(notification: JsonObject) {
    val organizerId = notification.getString("organizerId")
    val organizer = getUserDetails(organizerId)
    val responses = notification.getJsonObject("responses")

    val context = JsonObject()
      .put("organizerName", "${organizer.firstName} ${organizer.lastName}")
      .put("eventTitle", notification.getString("eventTitle"))
      .put("acceptedCount", responses.getInteger("ACCEPTED", 0))
      .put("declinedCount", responses.getInteger("DECLINED", 0))
      .put("tentativeCount", responses.getInteger("TENTATIVE", 0))
      .put("eventId", notification.getString("eventId"))

    templateEngine.rxRender(context, "templates/all-responded.hbs")
      .subscribe { html ->
        launch {
          sendEmail(
            to = organizer.email,
            subject = "All Participants Responded: ${notification.getString("eventTitle")}",
            html = html.toString()
          )
        }
      }


  }

  private suspend fun sendEmail(to: String, subject: String, html: String) {
    val message = MailMessage().apply {
      from = config.getString("from")
      setTo(to)
      this.subject = subject
      this.html = html
    }

    try {
      mailClient.sendMail(message).coAwait()
    } catch (e: Exception) {
      println("Failed to send email to $to: ${e.message}")
      throw e
    }
  }

  private suspend fun getUserDetails(userId: String): User {
    val response = userService.getUser(userId).coAwait()
    if (!response.success || response.data == null) {
      throw IllegalStateException("Failed to get user details for $userId")
    }
    return User(response.data)
  }

  private fun formatDateTime(isoDateTime: String): String {
    return DateTimeFormatter
      .ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a z")
      .format(ZonedDateTime.parse(isoDateTime))
  }

  private fun formatChanges(changes: JsonObject): String {
    val changedFields = mutableListOf<String>()

    if (changes.containsKey("time")) {
      val time = changes.getJsonObject("time")
      changedFields.add(
        "Time changed from ${formatDateTime(time.getJsonObject("old").getString("start"))} " +
          "to ${formatDateTime(time.getJsonObject("new").getString("start"))}"
      )
    }

    if (changes.containsKey("title")) {
      val title = changes.getJsonObject("title")
      changedFields.add(
        "Title changed from \"${title.getString("old")}\" " +
          "to \"${title.getString("new")}\""
      )
    }

    if (changes.containsKey("participants")) {
      val participants = changes.getJsonObject("participants")
      val added = participants.getJsonArray("added").size()
      val removed = participants.getJsonArray("removed").size()
      if (added > 0) changedFields.add("$added new participants added")
      if (removed > 0) changedFields.add("$removed participants removed")
    }

    return changedFields.joinToString("\n")
  }

  private suspend fun getOrganizerName(organizerId: String): String {
    val organizer = getUserDetails(organizerId)
    return "${organizer.firstName} ${organizer.lastName}"
  }
}
