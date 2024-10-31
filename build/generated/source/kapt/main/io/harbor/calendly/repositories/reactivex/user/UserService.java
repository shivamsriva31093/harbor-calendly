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

package io.harbor.calendly.repositories.reactivex.user;

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


@RxGen(io.harbor.calendly.repositories.user.UserService.class)
public class UserService {

  @Override
  public String toString() {
    return delegate.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserService that = (UserService) o;
    return delegate.equals(that.delegate);
  }
  
  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  public static final TypeArg<UserService> __TYPE_ARG = new TypeArg<>(    obj -> new UserService((io.harbor.calendly.repositories.user.UserService) obj),
    UserService::getDelegate
  );

  private final io.harbor.calendly.repositories.user.UserService delegate;
  
  public UserService(io.harbor.calendly.repositories.user.UserService delegate) {
    this.delegate = delegate;
  }

  public UserService(Object delegate) {
    this.delegate = (io.harbor.calendly.repositories.user.UserService)delegate;
  }

  public io.harbor.calendly.repositories.user.UserService getDelegate() {
    return delegate;
  }


  public io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> createUser(io.vertx.core.json.JsonObject request) { 
    io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> ret = delegate.createUser(request).map(val -> val);
    return ret;
  }

  public io.reactivex.Single<io.harbor.calendly.repositories.models.OperationResponse> rxCreateUser(io.vertx.core.json.JsonObject request) { 
    return AsyncResultSingle.toSingle($handler -> {
      createUser(request).onComplete($handler);
    });
  }

  public io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> updateUser(java.lang.String userId, io.vertx.core.json.JsonObject request) { 
    io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> ret = delegate.updateUser(userId, request).map(val -> val);
    return ret;
  }

  public io.reactivex.Single<io.harbor.calendly.repositories.models.OperationResponse> rxUpdateUser(java.lang.String userId, io.vertx.core.json.JsonObject request) { 
    return AsyncResultSingle.toSingle($handler -> {
      updateUser(userId, request).onComplete($handler);
    });
  }

  public io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> getUser(java.lang.String userId) { 
    io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> ret = delegate.getUser(userId).map(val -> val);
    return ret;
  }

  public io.reactivex.Single<io.harbor.calendly.repositories.models.OperationResponse> rxGetUser(java.lang.String userId) { 
    return AsyncResultSingle.toSingle($handler -> {
      getUser(userId).onComplete($handler);
    });
  }

  public io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> deleteUser(java.lang.String userId) { 
    io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> ret = delegate.deleteUser(userId).map(val -> val);
    return ret;
  }

  public io.reactivex.Single<io.harbor.calendly.repositories.models.OperationResponse> rxDeleteUser(java.lang.String userId) { 
    return AsyncResultSingle.toSingle($handler -> {
      deleteUser(userId).onComplete($handler);
    });
  }

  public io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> getUserTimezone(java.lang.String userId) { 
    io.vertx.core.Future<io.harbor.calendly.repositories.models.OperationResponse> ret = delegate.getUserTimezone(userId).map(val -> val);
    return ret;
  }

  public io.reactivex.Single<io.harbor.calendly.repositories.models.OperationResponse> rxGetUserTimezone(java.lang.String userId) { 
    return AsyncResultSingle.toSingle($handler -> {
      getUserTimezone(userId).onComplete($handler);
    });
  }

  public static UserService newInstance(io.harbor.calendly.repositories.user.UserService arg) {
    return arg != null ? new UserService(arg) : null;
  }

}
