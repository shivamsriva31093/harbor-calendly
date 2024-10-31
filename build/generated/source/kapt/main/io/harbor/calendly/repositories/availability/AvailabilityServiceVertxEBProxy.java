/*
* Copyright 2014 Red Hat, Inc.
*
* Red Hat licenses this file to you under the Apache License, version 2.0
* (the "License"); you may not use this file except in compliance with the
* License. You may obtain a copy of the License at:
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations
* under the License.
*/

package io.harbor.calendly.repositories.availability;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.Vertx;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Function;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.ServiceExceptionMessageCodec;
import io.vertx.serviceproxy.ProxyUtils;

import io.harbor.calendly.repositories.availability.AvailabilityWindow;
import io.harbor.calendly.repositories.models.OperationResponse;
import io.vertx.core.Future;
/*
  Generated Proxy code - DO NOT EDIT
  @author Roger the Robot
*/

@SuppressWarnings({"unchecked", "rawtypes"})
public class AvailabilityServiceVertxEBProxy implements AvailabilityService {
  private Vertx _vertx;
  private String _address;
  private DeliveryOptions _options;
  private boolean closed;

  public AvailabilityServiceVertxEBProxy(Vertx vertx, String address) {
    this(vertx, address, null);
  }

  public AvailabilityServiceVertxEBProxy(Vertx vertx, String address, DeliveryOptions options) {
    this._vertx = vertx;
    this._address = address;
    this._options = options;
    try {
      this._vertx.eventBus().registerDefaultCodec(ServiceException.class, new ServiceExceptionMessageCodec());
    } catch (IllegalStateException ex) {
    }
  }

  @Override
  public Future<OperationResponse> addAvailabilityWindow(String userId, AvailabilityWindow window){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("userId", userId);
    _json.put("window", window != null ? window.toJson() : null);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "addAvailabilityWindow");
    _deliveryOptions.getHeaders().set("action", "addAvailabilityWindow");
    return _vertx.eventBus().<JsonObject>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body() != null ? new io.harbor.calendly.repositories.models.OperationResponse((JsonObject)msg.body()) : null;
    });
  }
  @Override
  public Future<OperationResponse> updateAvailabilityWindow(String userId, String windowId, AvailabilityWindow window){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("userId", userId);
    _json.put("windowId", windowId);
    _json.put("window", window != null ? window.toJson() : null);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "updateAvailabilityWindow");
    _deliveryOptions.getHeaders().set("action", "updateAvailabilityWindow");
    return _vertx.eventBus().<JsonObject>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body() != null ? new io.harbor.calendly.repositories.models.OperationResponse((JsonObject)msg.body()) : null;
    });
  }
  @Override
  public Future<OperationResponse> removeAvailabilityWindow(String userId, String windowId){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("userId", userId);
    _json.put("windowId", windowId);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "removeAvailabilityWindow");
    _deliveryOptions.getHeaders().set("action", "removeAvailabilityWindow");
    return _vertx.eventBus().<JsonObject>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body() != null ? new io.harbor.calendly.repositories.models.OperationResponse((JsonObject)msg.body()) : null;
    });
  }
  @Override
  public Future<OperationResponse> getAvailabilityWindows(String userId){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("userId", userId);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "getAvailabilityWindows");
    _deliveryOptions.getHeaders().set("action", "getAvailabilityWindows");
    return _vertx.eventBus().<JsonObject>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body() != null ? new io.harbor.calendly.repositories.models.OperationResponse((JsonObject)msg.body()) : null;
    });
  }
  @Override
  public Future<OperationResponse> findOverlappingSlots(JsonObject request){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("request", request);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "findOverlappingSlots");
    _deliveryOptions.getHeaders().set("action", "findOverlappingSlots");
    return _vertx.eventBus().<JsonObject>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body() != null ? new io.harbor.calendly.repositories.models.OperationResponse((JsonObject)msg.body()) : null;
    });
  }
  @Override
  public Future<OperationResponse> getUserAvailabilityForDate(String userId, String date){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("userId", userId);
    _json.put("date", date);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "getUserAvailabilityForDate");
    _deliveryOptions.getHeaders().set("action", "getUserAvailabilityForDate");
    return _vertx.eventBus().<JsonObject>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body() != null ? new io.harbor.calendly.repositories.models.OperationResponse((JsonObject)msg.body()) : null;
    });
  }
  @Override
  public Future<OperationResponse> findAvailableSlots(JsonObject request){
    if (closed) return io.vertx.core.Future.failedFuture("Proxy is closed");
    JsonObject _json = new JsonObject();
    _json.put("request", request);

    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "findAvailableSlots");
    _deliveryOptions.getHeaders().set("action", "findAvailableSlots");
    return _vertx.eventBus().<JsonObject>request(_address, _json, _deliveryOptions).map(msg -> {
      return msg.body() != null ? new io.harbor.calendly.repositories.models.OperationResponse((JsonObject)msg.body()) : null;
    });
  }
}
