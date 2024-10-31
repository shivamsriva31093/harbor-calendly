package io.harbor.calendly.repositories.models;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.harbor.calendly.repositories.models.OperationError}.
 * NOTE: This class has been automatically generated from the {@link io.harbor.calendly.repositories.models.OperationError} original class using Vert.x codegen.
 */
public class OperationErrorConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, OperationError obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "code":
          break;
        case "details":
          break;
        case "message":
          break;
      }
    }
  }

  public static void toJson(OperationError obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(OperationError obj, java.util.Map<String, Object> json) {
    if (obj.getCode() != null) {
      json.put("code", obj.getCode().name());
    }
    if (obj.getDetails() != null) {
      json.put("details", obj.getDetails());
    }
    if (obj.getMessage() != null) {
      json.put("message", obj.getMessage());
    }
  }
}
