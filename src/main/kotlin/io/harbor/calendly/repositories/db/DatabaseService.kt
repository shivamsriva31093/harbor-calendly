package io.harbor.calendly.repositories.db

import io.vertx.codegen.annotations.ProxyGen
import io.vertx.codegen.annotations.VertxGen
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.SqlClient
import org.jetbrains.annotations.NotNull

@ProxyGen
interface DatabaseService {
    fun findUserById(id: String): Future<List<JsonObject>>
}

object DatabaseServiceFactory {
  const val SERVICE_NAME = "calendly-database-service"
  const val SERVICE_ADDRESS = "calendly.database.service"

  @JvmStatic
  fun create(
    @NotNull vertx: Vertx,
    @NotNull pgPool: SqlClient,
    readyHandler: Handler<AsyncResult<DatabaseService>>
  ): DatabaseService {
    return DatabaseServiceImpl(vertx, pgPool, readyHandler)
  }

//  @JvmStatic
//  fun createProxy(vertx: Vertx, address: String): co.indata.newbyte.repository.services.reactivex.DatabaseService {
//
//  }
}
