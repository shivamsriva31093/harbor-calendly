package io.harbor.calendly.handlers;

import io.harbor.calendly.exceptions.BriefException;
import io.harbor.calendly.util.ConstantsKt;
import io.harbor.calendly.util.ExtensionsKt;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonFailureHandler implements Handler<RoutingContext> {
  private final static Logger LOGGER = LoggerFactory.getLogger(CommonFailureHandler.class);

  public CommonFailureHandler() {
    super();
  }

  @Override
  public void handle(RoutingContext event) {
    Throwable inner = event.failure();
    String message;
    int code = 500;
    if (event.statusCode() > 0) {
      code = event.statusCode();
    }
    if (inner instanceof BriefException) {
      message = inner.getMessage();
      code = ((BriefException) inner).getCode();
    } else {
      message = ConstantsKt.MSG_ERR_GENERIC;
    }

    io.vertx.reactivex.core.http.HttpServerResponse httpServerResponse = event.response();
    int statusCode;
    if (HttpResponseStatus.valueOf(code).reasonPhrase().contains("Unknown Status (")) {
      statusCode = HttpResponseStatus.INTERNAL_SERVER_ERROR.code();
    } else {
      statusCode = code;
    }

    ExtensionsKt.toApiResponse(httpServerResponse, new JsonArray(), code, message, statusCode);
  }
}


