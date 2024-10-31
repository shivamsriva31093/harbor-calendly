package io.harbor.calendly.repositories.availability;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.harbor.calendly.repositories.availability.AvailabilityWindowWithId}.
 * NOTE: This class has been automatically generated from the {@link io.harbor.calendly.repositories.availability.AvailabilityWindowWithId} original class using Vert.x codegen.
 */
public class AvailabilityWindowWithIdConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, AvailabilityWindowWithId obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "id":
          break;
        case "window":
          break;
      }
    }
  }

  public static void toJson(AvailabilityWindowWithId obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(AvailabilityWindowWithId obj, java.util.Map<String, Object> json) {
    if (obj.getId() != null) {
      json.put("id", obj.getId());
    }
    if (obj.getWindow() != null) {
      json.put("window", obj.getWindow().toJson());
    }
  }
}
