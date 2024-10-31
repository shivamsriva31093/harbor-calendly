package io.harbor.calendly.repositories.models;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.harbor.calendly.repositories.models.ResponseMetadata}.
 * NOTE: This class has been automatically generated from the {@link io.harbor.calendly.repositories.models.ResponseMetadata} original class using Vert.x codegen.
 */
public class ResponseMetadataConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, ResponseMetadata obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "additionalInfo":
          break;
        case "affectedRows":
          break;
        case "operationType":
          break;
        case "timestamp":
          break;
      }
    }
  }

  public static void toJson(ResponseMetadata obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(ResponseMetadata obj, java.util.Map<String, Object> json) {
    if (obj.getAdditionalInfo() != null) {
      json.put("additionalInfo", obj.getAdditionalInfo());
    }
    json.put("affectedRows", obj.getAffectedRows());
    if (obj.getOperationType() != null) {
      json.put("operationType", obj.getOperationType().name());
    }
    json.put("timestamp", obj.getTimestamp());
  }
}
