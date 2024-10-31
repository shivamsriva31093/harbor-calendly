package io.harbor.calendly.repositories

import io.harbor.calendly.repositories.db.DatabaseClient
import io.vertx.sqlclient.Pool

abstract class BaseRepository {
  protected val pgPool: Pool
    get() = DatabaseClient.getPool()
}
