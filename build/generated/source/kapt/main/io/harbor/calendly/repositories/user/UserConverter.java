package io.harbor.calendly.repositories.user;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.harbor.calendly.repositories.user.User}.
 * NOTE: This class has been automatically generated from the {@link io.harbor.calendly.repositories.user.User} original class using Vert.x codegen.
 */
public class UserConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, User obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "email":
          break;
        case "firstName":
          break;
        case "id":
          break;
        case "lastName":
          break;
        case "timezone":
          break;
        case "valid":
          break;
      }
    }
  }

  public static void toJson(User obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(User obj, java.util.Map<String, Object> json) {
    if (obj.getEmail() != null) {
      json.put("email", obj.getEmail());
    }
    if (obj.getFirstName() != null) {
      json.put("firstName", obj.getFirstName());
    }
    if (obj.getId() != null) {
      json.put("id", obj.getId());
    }
    if (obj.getLastName() != null) {
      json.put("lastName", obj.getLastName());
    }
    if (obj.getTimezone() != null) {
      json.put("timezone", obj.getTimezone());
    }
    json.put("valid", obj.isValid());
  }
}
