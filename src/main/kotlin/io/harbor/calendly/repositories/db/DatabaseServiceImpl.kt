package io.harbor.calendly.repositories.db

import io.harbor.calendly.util.toJson
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple

class DatabaseServiceImpl(
  private val vertx: Vertx,
  private val pgPool: SqlClient,
  readyHandler: Handler<AsyncResult<DatabaseService>>
) : DatabaseService {

  init {
    readyHandler.handle(Future.succeededFuture(this))
  }

  override fun findUserById(id: String): Future<List<JsonObject>> {
    return pgPool.preparedQuery("SELECT * FROM users WHERE id = $1")
      .execute(Tuple.of(id))
      .map { t -> t.toJson() }
  }
}
