package io.harbor.calendly.repositories.scheduler;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.harbor.calendly.repositories.scheduler.Event}.
 * NOTE: This class has been automatically generated from the {@link io.harbor.calendly.repositories.scheduler.Event} original class using Vert.x codegen.
 */
public class EventConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Event obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "description":
          break;
        case "endTime":
          break;
        case "id":
          break;
        case "metadata":
          if (member.getValue() instanceof JsonObject) {
            obj.setMetadata(new io.harbor.calendly.repositories.scheduler.EventMetadata((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "organizerId":
          break;
        case "participants":
          if (member.getValue() instanceof JsonArray) {
            java.util.ArrayList<io.harbor.calendly.repositories.scheduler.Participant> list =  new java.util.ArrayList<>();
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof JsonObject)
                list.add(new io.harbor.calendly.repositories.scheduler.Participant((io.vertx.core.json.JsonObject)item));
            });
            obj.setParticipants(list);
          }
          break;
        case "startTime":
          break;
        case "status":
          break;
        case "title":
          break;
        case "valid":
          break;
      }
    }
  }

  public static void toJson(Event obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(Event obj, java.util.Map<String, Object> json) {
    if (obj.getDescription() != null) {
      json.put("description", obj.getDescription());
    }
    if (obj.getEndTime() != null) {
      json.put("endTime", obj.getEndTime());
    }
    if (obj.getId() != null) {
      json.put("id", obj.getId());
    }
    if (obj.getMetadata() != null) {
      json.put("metadata", obj.getMetadata().toJson());
    }
    if (obj.getOrganizerId() != null) {
      json.put("organizerId", obj.getOrganizerId());
    }
    if (obj.getParticipants() != null) {
      JsonArray array = new JsonArray();
      obj.getParticipants().forEach(item -> array.add(item.toJson()));
      json.put("participants", array);
    }
    if (obj.getStartTime() != null) {
      json.put("startTime", obj.getStartTime());
    }
    if (obj.getStatus() != null) {
      json.put("status", obj.getStatus().name());
    }
    if (obj.getTitle() != null) {
      json.put("title", obj.getTitle());
    }
    json.put("valid", obj.isValid());
  }
}
