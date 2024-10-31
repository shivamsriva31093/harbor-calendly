package io.harbor.calendly.handlers;

import io.vertx.core.http.HttpMethod;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CorsHandler {
  private static final String AllOWED_ORIGINS_PATTERN = "^(https:\\/\\/bildx\\.space)|(https:\\/\\/thebrieflyapp\\.com)|(https:\\/\\/apis\\.bildx\\.space)|(^|^http:\\/\\/)(localhost$|localhost:[0-9]{1,5})|(^|^http:\\/\\/)(192.168.29.{1,}:[0-9]{1,5})|(^|^http:\\/\\/)(192.168.1.{1,}:[0-9]{1,5})$";
  private static final Set<String> ALLOWED_HEADERS = new HashSet<>(Arrays.asList(
    "Authorization",
    "Content-Type",
    "Content-Disposition",
    "tenant-name"
  ));
  private static final Set<String> EXPOSED_HEADERS = new HashSet<>(Arrays.asList(
    "Authorization",
    "Content-Type",
    "Content-Disposition"
  ));

  public static io.vertx.ext.web.handler.CorsHandler getCorsHandler() {
    io.vertx.ext.web.handler.CorsHandler corsHandler =
      io.vertx.ext.web.handler.CorsHandler.create(AllOWED_ORIGINS_PATTERN);
    corsHandler.allowedMethod(HttpMethod.GET);
    corsHandler.allowedMethod(HttpMethod.POST);
    corsHandler.allowedMethod(HttpMethod.PUT);
    corsHandler.allowedMethod(HttpMethod.DELETE);
    corsHandler.allowedMethod(HttpMethod.PATCH);

    corsHandler.allowedHeaders(ALLOWED_HEADERS);
    corsHandler.exposedHeaders(EXPOSED_HEADERS);
    return corsHandler;
  }
}
