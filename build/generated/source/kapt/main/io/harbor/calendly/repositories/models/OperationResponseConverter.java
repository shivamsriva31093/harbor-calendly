package io.harbor.calendly.repositories.models;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.harbor.calendly.repositories.models.OperationResponse}.
 * NOTE: This class has been automatically generated from the {@link io.harbor.calendly.repositories.models.OperationResponse} original class using Vert.x codegen.
 */
public class OperationResponseConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, OperationResponse obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "data":
          break;
        case "error":
          break;
        case "message":
          break;
        case "metadata":
          break;
        case "success":
          break;
      }
    }
  }

  public static void toJson(OperationResponse obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(OperationResponse obj, java.util.Map<String, Object> json) {
    if (obj.getData() != null) {
      json.put("data", obj.getData());
    }
    if (obj.getError() != null) {
      json.put("error", obj.getError().toJson());
    }
    if (obj.getMessage() != null) {
      json.put("message", obj.getMessage());
    }
    if (obj.getMetadata() != null) {
      json.put("metadata", obj.getMetadata().toJson());
    }
    json.put("success", obj.getSuccess());
  }
}
