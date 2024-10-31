package io.harbor.calendly.repositories.db

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.pgclient.PgBuilder
import io.vertx.pgclient.PgConnectOptions
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.PoolOptions
import java.util.concurrent.atomic.AtomicReference

object DatabaseClient {
  private val poolRef = AtomicReference<Pool>()

  fun createPool(vertx: Vertx, config: JsonObject): Pool {
    return poolRef.updateAndGet { existing ->
      existing ?: initPool(vertx, config)
    }
  }

  fun getPool(): Pool {
    return poolRef.get() ?: throw IllegalStateException(
      "Database pool not initialized. Call createPool first."
    )
  }

  private fun initPool(vertx: Vertx, config: JsonObject): Pool {
    val connectOptions = PgConnectOptions()
      .setPort(config.getInteger("port", 5432))
      .setHost(config.getString("host", "localhost"))
      .setDatabase(config.getString("database", "calendar_db"))
      .setUser(config.getString("user", "postgres"))
      .setPassword(config.getString("password", "postgres"))
      // Connection pool tuning
      .setConnectTimeout(config.getInteger("connectTimeout", 5000))
      .setIdleTimeout(config.getInteger("idleTimeout", 60000))
      // Other recommended settings
      .setCachePreparedStatements(true)
      .setPreparedStatementCacheMaxSize(config.getInteger("preparedStatementCacheSize", 250))
      .setPreparedStatementCacheSqlLimit(config.getInteger("preparedStatementCacheSqlLimit", 2048))

    val poolOptions = PoolOptions()
      .setMaxSize(config.getInteger("poolMaxSize", 5))
      .setMaxWaitQueueSize(config.getInteger("maxWaitQueueSize", 1000))
      .setIdleTimeout(config.getInteger("poolIdleTimeout", 30))
      .setConnectionTimeout(config.getInteger("connectionTimeout", 2000))

    return PgBuilder.pool()
      .with(poolOptions)
      .connectingTo(connectOptions)
      .using(vertx)
      .build()
  }

  fun close() {
    poolRef.get()?.close()
    poolRef.set(null)
  }
}


class DatabaseVerifier(private val pool: Pool) {

  suspend fun verifyConnection(): Boolean {
    return try {
      // Try a simple query to verify connection
      pool.query("SELECT 1").execute().coAwait()
      true
    } catch (e: Exception) {
      false
    }
  }

  suspend fun verifyTables(): Set<String?> {
    val missingTables = mutableListOf<String>()

    val expectedTables = setOf(
      "users",
      "availability_settings",
      "events",
      "event_participants"
    )

    val existingTables = pool.query("""
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema = 'public'
        """).execute().coAwait()
      .map { row -> row.getString("table_name") }
      .toSet()

    return expectedTables - existingTables
  }
}
