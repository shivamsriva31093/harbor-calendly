/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.harbor.calendly.repositories.reactivex.availability;

import io.vertx.reactivex.RxHelper;
import io.vertx.reactivex.ObservableHelper;
import io.vertx.reactivex.FlowableHelper;
import io.vertx.reactivex.impl.AsyncResultMaybe;
import io.vertx.reactivex.impl.AsyncResultSingle;
import io.vertx.reactivex.impl.AsyncResultCompletable;
import io.vertx.reactivex.WriteStreamObserver;
import io.vertx.reactivex.WriteStreamSubscriber;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Collectors;
import io.vertx.core.Handler;
import io.vertx.core.AsyncResult;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.lang.rx.RxGen;
import io.vertx.lang.rx.TypeArg;
import io.vertx.lang.rx.MappingIterator;


@RxGen(io.harbor.calendly.repositories.availability.AvailabilityService.class)
public class AvailabilityService {

  @Override
  public String toString() {
    return delegate.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AvailabilityService that = (AvailabilityService) o;
    return delegate.equals(that.delegate);
  }
  
  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  public static final TypeArg<AvailabilityService> __TYPE_ARG = new TypeArg<>(    obj -> new AvailabilityService((io.harbor.calendly.repositories.availability.AvailabilityService) obj),
    AvailabilityService::getDelegate
  );

  private final io.harbor.calendly.repositories.availability.AvailabilityService delegate;
  
  public AvailabilityService(io.harbor.calendly.repositories.availability.AvailabilityService delegate) {
    this.delegate = delegate;
  }

  public AvailabilityService(Object delegate) {
    this.delegate = (io.harbor.calendly.repositories.availability.AvailabilityService)delegate;
  }

  public io.harbor.calendly.repositories.availability.AvailabilityService getDelegate() {
    return delegate;
  }


  public io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> addAvailabilityWindow(java.lang.String userId, io.harbor.calendly.repositories.availability.AvailabilityWindow window) { 
    io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> ret = delegate.addAvailabilityWindow(userId, window).map(val -> val);
    return ret;
  }

  public io.reactivex.Single<io.harbor.calendly.repositories.models.OperationResponse> rxAddAvailabilityWindow(java.lang.String userId, io.harbor.calendly.repositories.availability.AvailabilityWindow window) { 
    return AsyncResultSingle.toSingle($handler -> {
      addAvailabilityWindow(userId, window).onComplete($handler);
    });
  }

  public io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> updateAvailabilityWindow(java.lang.String userId, java.lang.String windowId, io.harbor.calendly.repositories.availability.AvailabilityWindow window) { 
    io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> ret = delegate.updateAvailabilityWindow(userId, windowId, window).map(val -> val);
    return ret;
  }

  public io.reactivex.Single<io.harbor.calendly.repositories.models.OperationResponse> rxUpdateAvailabilityWindow(java.lang.String userId, java.lang.String windowId, io.harbor.calendly.repositories.availability.AvailabilityWindow window) { 
    return AsyncResultSingle.toSingle($handler -> {
      updateAvailabilityWindow(userId, windowId, window).onComplete($handler);
    });
  }

  public io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> removeAvailabilityWindow(java.lang.String userId, java.lang.String windowId) { 
    io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> ret = delegate.removeAvailabilityWindow(userId, windowId).map(val -> val);
    return ret;
  }

  public io.reactivex.Single<io.harbor.calendly.repositories.models.OperationResponse> rxRemoveAvailabilityWindow(java.lang.String userId, java.lang.String windowId) { 
    return AsyncResultSingle.toSingle($handler -> {
      removeAvailabilityWindow(userId, windowId).onComplete($handler);
    });
  }

  public io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> getAvailabilityWindows(java.lang.String userId) { 
    io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> ret = delegate.getAvailabilityWindows(userId).map(val -> val);
    return ret;
  }

  public io.reactivex.Single<io.harbor.calendly.repositories.models.OperationResponse> rxGetAvailabilityWindows(java.lang.String userId) { 
    return AsyncResultSingle.toSingle($handler -> {
      getAvailabilityWindows(userId).onComplete($handler);
    });
  }

  public io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> findOverlappingSlots(io.vertx.core.json.JsonObject request) { 
    io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> ret = delegate.findOverlappingSlots(request).map(val -> val);
    return ret;
  }

  public io.reactivex.Single<io.harbor.calendly.repositories.models.OperationResponse> rxFindOverlappingSlots(io.vertx.core.json.JsonObject request) { 
    return AsyncResultSingle.toSingle($handler -> {
      findOverlappingSlots(request).onComplete($handler);
    });
  }

  public io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> getUserAvailabilityForDate(java.lang.String userId, java.lang.String date) { 
    io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> ret = delegate.getUserAvailabilityForDate(userId, date).map(val -> val);
    return ret;
  }

  public io.reactivex.Single<io.harbor.calendly.repositories.models.OperationResponse> rxGetUserAvailabilityForDate(java.lang.String userId, java.lang.String date) { 
    return AsyncResultSingle.toSingle($handler -> {
      getUserAvailabilityForDate(userId, date).onComplete($handler);
    });
  }

  public io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> findAvailableSlots(io.vertx.core.json.JsonObject request) { 
    io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> ret = delegate.findAvailableSlots(request).map(val -> val);
    return ret;
  }

  public io.reactivex.Single<io.harbor.calendly.repositories.models.OperationResponse> rxFindAvailableSlots(io.vertx.core.json.JsonObject request) { 
    return AsyncResultSingle.toSingle($handler -> {
      findAvailableSlots(request).onComplete($handler);
    });
  }

  public static AvailabilityService newInstance(io.harbor.calendly.repositories.availability.AvailabilityService arg) {
    return arg != null ? new AvailabilityService(arg) : null;
  }

}
