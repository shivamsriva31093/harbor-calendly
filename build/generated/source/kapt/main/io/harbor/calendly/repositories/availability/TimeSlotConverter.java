package io.harbor.calendly.repositories.availability;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.harbor.calendly.repositories.availability.TimeSlot}.
 * NOTE: This class has been automatically generated from the {@link io.harbor.calendly.repositories.availability.TimeSlot} original class using Vert.x codegen.
 */
public class TimeSlotConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, TimeSlot obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "end":
          break;
        case "start":
          break;
      }
    }
  }

  public static void toJson(TimeSlot obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(TimeSlot obj, java.util.Map<String, Object> json) {
    if (obj.getEnd() != null) {
      json.put("end", obj.getEnd());
    }
    if (obj.getStart() != null) {
      json.put("start", obj.getStart());
    }
  }
}
