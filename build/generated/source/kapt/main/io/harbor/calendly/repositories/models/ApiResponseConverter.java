package io.harbor.calendly.repositories.models;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.harbor.calendly.repositories.models.ApiResponse}.
 * NOTE: This class has been automatically generated from the {@link io.harbor.calendly.repositories.models.ApiResponse} original class using Vert.x codegen.
 */
public class ApiResponseConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, ApiResponse obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "apiStatus":
          if (member.getValue() instanceof Number) {
            obj.setApiStatus(((Number)member.getValue()).intValue());
          }
          break;
        case "data":
          if (member.getValue() instanceof JsonArray) {
            obj.setData(((JsonArray)member.getValue()).copy());
          }
          break;
        case "message":
          if (member.getValue() instanceof String) {
            obj.setMessage((String)member.getValue());
          }
          break;
        case "statusCode":
          if (member.getValue() instanceof Number) {
            obj.setStatusCode(((Number)member.getValue()).intValue());
          }
          break;
      }
    }
  }

  public static void toJson(ApiResponse obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(ApiResponse obj, java.util.Map<String, Object> json) {
    json.put("apiStatus", obj.getApiStatus());
    if (obj.getData() != null) {
      json.put("data", obj.getData());
    }
    if (obj.getMessage() != null) {
      json.put("message", obj.getMessage());
    }
    json.put("statusCode", obj.getStatusCode());
  }
}
