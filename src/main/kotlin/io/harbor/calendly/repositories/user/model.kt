package io.harbor.calendly.repositories.user

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject
import java.time.ZonedDateTime

// src/main/kotlin/com/harbor/calendar/model/User.kt
@DataObject(generateConverter = true)
data class User(
  val id: String? = null,
  val email: String,
  val timezone: String,
  val firstName: String,
  val lastName: String,
  val createdAt: ZonedDateTime? = null,
  val updatedAt: ZonedDateTime? = null
) {
  constructor(json: JsonObject) : this(
    id = json.getString("id"),
    email = json.getString("email"),
    timezone = json.getString("timezone"),
    firstName = json.getString("firstName"),
    lastName = json.getString("lastName")
  )

  fun toJson(): JsonObject = JsonObject()
    .put("id", id)
    .put("email", email)
    .put("timezone", timezone)
    .put("firstName", firstName)
    .put("lastName", lastName)
    .put("createdAt", createdAt?.toString())
    .put("updatedAt", updatedAt?.toString())

  fun isValid(): Boolean = email.isNotBlank() &&
    timezone.isNotBlank() &&
    firstName.isNotBlank() &&
    lastName.isNotBlank() &&
    email.matches(Regex(".+@.+\\..+"))
}
