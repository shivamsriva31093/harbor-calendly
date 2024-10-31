package io.harbor.calendly.repositories.scheduler;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.harbor.calendly.repositories.scheduler.EventMetadata}.
 * NOTE: This class has been automatically generated from the {@link io.harbor.calendly.repositories.scheduler.EventMetadata} original class using Vert.x codegen.
 */
public class EventMetadataConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, EventMetadata obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "conferenceLink":
          break;
        case "customProperties":
          break;
        case "location":
          break;
        case "recurringPattern":
          break;
      }
    }
  }

  public static void toJson(EventMetadata obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(EventMetadata obj, java.util.Map<String, Object> json) {
    if (obj.getConferenceLink() != null) {
      json.put("conferenceLink", obj.getConferenceLink());
    }
    if (obj.getCustomProperties() != null) {
      json.put("customProperties", obj.getCustomProperties());
    }
    if (obj.getLocation() != null) {
      json.put("location", obj.getLocation());
    }
    if (obj.getRecurringPattern() != null) {
      json.put("recurringPattern", obj.getRecurringPattern().toJson());
    }
  }
}
