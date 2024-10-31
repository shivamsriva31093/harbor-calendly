package io.harbor.calendly.repositories.availability;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.harbor.calendly.repositories.availability.AvailabilityWindow}.
 * NOTE: This class has been automatically generated from the {@link io.harbor.calendly.repositories.availability.AvailabilityWindow} original class using Vert.x codegen.
 */
public class AvailabilityWindowConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, AvailabilityWindow obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "dayOfWeek":
          break;
        case "endTime":
          break;
        case "startTime":
          break;
        case "valid":
          break;
      }
    }
  }

  public static void toJson(AvailabilityWindow obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(AvailabilityWindow obj, java.util.Map<String, Object> json) {
    json.put("dayOfWeek", obj.getDayOfWeek());
    if (obj.getEndTime() != null) {
      json.put("endTime", obj.getEndTime());
    }
    if (obj.getStartTime() != null) {
      json.put("startTime", obj.getStartTime());
    }
    json.put("valid", obj.isValid());
  }
}
