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

package io.harbor.calendly.repositories.rxjava3.availability;

import io.vertx.rxjava3.RxHelper;
import io.vertx.rxjava3.ObservableHelper;
import io.vertx.rxjava3.FlowableHelper;
import io.vertx.rxjava3.impl.AsyncResultMaybe;
import io.vertx.rxjava3.impl.AsyncResultSingle;
import io.vertx.rxjava3.impl.AsyncResultCompletable;
import io.vertx.rxjava3.WriteStreamObserver;
import io.vertx.rxjava3.WriteStreamSubscriber;
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


  public io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> addAvailabilityWindow(java.lang.String userId, io.harbor.calendly.repositories.availability.AvailabilityWindow window) { 
    io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> ret = rxAddAvailabilityWindow(userId, window);
    ret = ret.cache();
    ret.subscribe(io.vertx.rxjava3.SingleHelper.nullObserver());
    return ret;
  }

  public io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> rxAddAvailabilityWindow(java.lang.String userId, io.harbor.calendly.repositories.availability.AvailabilityWindow window) { 
    return AsyncResultSingle.toSingle(delegate.addAvailabilityWindow(userId, window), __value -> __value);
  }

  public io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> updateAvailabilityWindow(java.lang.String userId, java.lang.String windowId, io.harbor.calendly.repositories.availability.AvailabilityWindow window) { 
    io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> ret = rxUpdateAvailabilityWindow(userId, windowId, window);
    ret = ret.cache();
    ret.subscribe(io.vertx.rxjava3.SingleHelper.nullObserver());
    return ret;
  }

  public io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> rxUpdateAvailabilityWindow(java.lang.String userId, java.lang.String windowId, io.harbor.calendly.repositories.availability.AvailabilityWindow window) { 
    return AsyncResultSingle.toSingle(delegate.updateAvailabilityWindow(userId, windowId, window), __value -> __value);
  }

  public io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> removeAvailabilityWindow(java.lang.String userId, java.lang.String windowId) { 
    io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> ret = rxRemoveAvailabilityWindow(userId, windowId);
    ret = ret.cache();
    ret.subscribe(io.vertx.rxjava3.SingleHelper.nullObserver());
    return ret;
  }

  public io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> rxRemoveAvailabilityWindow(java.lang.String userId, java.lang.String windowId) { 
    return AsyncResultSingle.toSingle(delegate.removeAvailabilityWindow(userId, windowId), __value -> __value);
  }

  public io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> getAvailabilityWindows(java.lang.String userId) { 
    io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> ret = rxGetAvailabilityWindows(userId);
    ret = ret.cache();
    ret.subscribe(io.vertx.rxjava3.SingleHelper.nullObserver());
    return ret;
  }

  public io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> rxGetAvailabilityWindows(java.lang.String userId) { 
    return AsyncResultSingle.toSingle(delegate.getAvailabilityWindows(userId), __value -> __value);
  }

  public io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> findOverlappingSlots(io.vertx.core.json.JsonObject request) { 
    io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> ret = rxFindOverlappingSlots(request);
    ret = ret.cache();
    ret.subscribe(io.vertx.rxjava3.SingleHelper.nullObserver());
    return ret;
  }

  public io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> rxFindOverlappingSlots(io.vertx.core.json.JsonObject request) { 
    return AsyncResultSingle.toSingle(delegate.findOverlappingSlots(request), __value -> __value);
  }

  public io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> getUserAvailabilityForDate(java.lang.String userId, java.lang.String date) { 
    io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> ret = rxGetUserAvailabilityForDate(userId, date);
    ret = ret.cache();
    ret.subscribe(io.vertx.rxjava3.SingleHelper.nullObserver());
    return ret;
  }

  public io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> rxGetUserAvailabilityForDate(java.lang.String userId, java.lang.String date) { 
    return AsyncResultSingle.toSingle(delegate.getUserAvailabilityForDate(userId, date), __value -> __value);
  }

  public io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> findAvailableSlots(io.vertx.core.json.JsonObject request) { 
    io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> ret = rxFindAvailableSlots(request);
    ret = ret.cache();
    ret.subscribe(io.vertx.rxjava3.SingleHelper.nullObserver());
    return ret;
  }

  public io.reactivex.rxjava3.core.Single<io.harbor.calendly.repositories.models.OperationResponse> rxFindAvailableSlots(io.vertx.core.json.JsonObject request) { 
    return AsyncResultSingle.toSingle(delegate.findAvailableSlots(request), __value -> __value);
  }

  public static AvailabilityService newInstance(io.harbor.calendly.repositories.availability.AvailabilityService arg) {
    return arg != null ? new AvailabilityService(arg) : null;
  }

}
