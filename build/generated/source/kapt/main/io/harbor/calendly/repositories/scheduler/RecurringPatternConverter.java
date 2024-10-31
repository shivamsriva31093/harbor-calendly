package io.harbor.calendly.repositories.scheduler;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.harbor.calendly.repositories.scheduler.RecurringPattern}.
 * NOTE: This class has been automatically generated from the {@link io.harbor.calendly.repositories.scheduler.RecurringPattern} original class using Vert.x codegen.
 */
public class RecurringPatternConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, RecurringPattern obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "endDate":
          break;
        case "frequency":
          break;
        case "interval":
          break;
        case "occurrences":
          break;
      }
    }
  }

  public static void toJson(RecurringPattern obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(RecurringPattern obj, java.util.Map<String, Object> json) {
    if (obj.getEndDate() != null) {
      json.put("endDate", obj.getEndDate());
    }
    if (obj.getFrequency() != null) {
      json.put("frequency", obj.getFrequency().name());
    }
    json.put("interval", obj.getInterval());
    if (obj.getOccurrences() != null) {
      json.put("occurrences", obj.getOccurrences());
    }
  }
}
