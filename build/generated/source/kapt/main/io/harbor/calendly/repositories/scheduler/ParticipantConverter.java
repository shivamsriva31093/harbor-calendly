package io.harbor.calendly.repositories.scheduler;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.harbor.calendly.repositories.scheduler.Participant}.
 * NOTE: This class has been automatically generated from the {@link io.harbor.calendly.repositories.scheduler.Participant} original class using Vert.x codegen.
 */
public class ParticipantConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Participant obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "notificationPreference":
          break;
        case "response":
          break;
        case "userId":
          break;
      }
    }
  }

  public static void toJson(Participant obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(Participant obj, java.util.Map<String, Object> json) {
    if (obj.getNotificationPreference() != null) {
      json.put("notificationPreference", obj.getNotificationPreference().name());
    }
    if (obj.getResponse() != null) {
      json.put("response", obj.getResponse().name());
    }
    if (obj.getUserId() != null) {
      json.put("userId", obj.getUserId());
    }
  }
}
