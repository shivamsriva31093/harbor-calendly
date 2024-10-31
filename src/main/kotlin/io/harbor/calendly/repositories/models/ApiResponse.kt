package io.harbor.calendly.repositories.models

import io.harbor.calendly.util.DATA
import io.harbor.calendly.util.MESSAGE
import io.harbor.calendly.util.RESULTS_SIZE
import io.harbor.calendly.util.STATUS_CODE
import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

@DataObject(generateConverter = true)
class ApiResponse {
  var data: JsonArray = JsonArray()
  var statusCode: Int = 200
  var message: String = "Success"
  var apiStatus: Int = 200

  constructor(json: JsonObject) {
//    ApiResponseConverter.fromJson(json, this)
  }

  constructor(
    data: JsonArray = JsonArray(),
    statusCode: Int = 200,
    message: String = "Success",
    apiStatus: Int = 200
  ) {
    this.apiStatus = apiStatus
    this.data = data
    this.message = message
    this.apiStatus = apiStatus
    this.statusCode = statusCode
  }

  fun toJson(): JsonObject {
    return JsonObject()
      .put(STATUS_CODE, statusCode)
      .put(DATA, data)
      .put(RESULTS_SIZE, data.size())
      .put(MESSAGE, message)
  }
}
