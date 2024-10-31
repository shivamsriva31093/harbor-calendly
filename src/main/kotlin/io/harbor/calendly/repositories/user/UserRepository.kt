package io.harbor.calendly.repositories.user

import io.harbor.calendly.exceptions.BriefException
import io.harbor.calendly.repositories.BaseRepository
import io.harbor.calendly.repositories.models.ErrorCode
import io.harbor.calendly.repositories.models.OperationError
import io.harbor.calendly.repositories.models.OperationType
import io.harbor.calendly.repositories.models.RepositoryResult
import io.harbor.calendly.util.handleDatabaseException
import io.vertx.core.Promise
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple
import java.time.ZoneId
import java.util.UUID

// src/main/kotlin/com/harbor/calendar/repository/UserRepository.kt
class UserRepository : BaseRepository() {
  suspend fun createUser(user: User): RepositoryResult<User> {
    return try {
      val result = pgPool.preparedQuery(
        """
                INSERT INTO stg.users (id, email, timezone, first_name, last_name, created_at)
                VALUES ($1, $2, $3, $4, $5, CURRENT_TIMESTAMP)
                RETURNING *
            """
      ).execute(
        Tuple.of(
          user.id ?: UUID.randomUUID().toString(),
          user.email,
          user.timezone,
          user.firstName,
          user.lastName
        )
      ).coAwait()

      RepositoryResult.success(
        data = result.first().toUser(),
        affectedRows = 1,
        operation = OperationType.INSERT
      )
    } catch (e: Exception) {
      handleDatabaseException(e, OperationType.INSERT)
    }
  }

  suspend fun updateUser(userId: String, user: User): RepositoryResult<User> {
    return try {
      val result = pgPool.preparedQuery(
        """
                UPDATE stg.users
                SET
                    email = $1,
                    timezone = $2,
                    first_name = $3,
                    last_name = $4,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = $5
                RETURNING *
            """
      ).execute(
        Tuple.of(
          user.email,
          user.timezone,
          user.firstName,
          user.lastName,
          userId
        )
      ).coAwait()

      if (result.rowCount() == 0) {
        RepositoryResult.failure(
          error = OperationError(
            code = ErrorCode.NOT_FOUND,
            message = "User not found"
          ),
          operation = OperationType.UPDATE
        )
      } else {
        RepositoryResult.success(
          data = result.first().toUser(),
          affectedRows = 1,
          operation = OperationType.UPDATE
        )
      }
    } catch (e: Exception) {
      handleDatabaseException(e, OperationType.UPDATE)
    }
  }

  suspend fun getUser(userId: String): RepositoryResult<User> {
    return try {
      val result = pgPool.preparedQuery(
        """
                SELECT * FROM stg.users WHERE id = $1
            """
      ).execute(Tuple.of(userId)).coAwait()

      if (result.rowCount() == 0) {
        RepositoryResult.failure(
          error = OperationError(
            code = ErrorCode.NOT_FOUND,
            message = "User not found"
          ),
          operation = OperationType.SELECT
        )
      } else {
        RepositoryResult.success(
          data = result.first().toUser(),
          affectedRows = 1,
          operation = OperationType.SELECT
        )
      }
    } catch (e: Exception) {
      handleDatabaseException(e, OperationType.SELECT)
    }
  }

  suspend fun deleteUser(userId: String): RepositoryResult<String> {
    val promise = Promise.promise<RepositoryResult<String>>()
    try {
      pgPool.withTransaction { client ->
        // Delete user's events and participations
        client.preparedQuery(
          """
                    DELETE FROM stg.event_participants WHERE user_id = $1
                """
        ).execute(Tuple.of(userId))
          .compose { id ->
            client.preparedQuery(
              """
                    DELETE FROM stg.events WHERE organizer_id = $1
                """
            ).execute(Tuple.of(userId))
          }
          .compose { id ->
            client.preparedQuery(
              """
                    DELETE FROM stg.users WHERE id = $1
                """
            ).execute(Tuple.of(userId))
          }
      }
        .onSuccess { event ->
          promise.complete(RepositoryResult.success(data = userId, affectedRows = 1, operation = OperationType.DELETE))
        }
        .onFailure { event ->
          promise.complete(
            handleDatabaseException<String>(
              BriefException(
                ErrorCode.DATABASE_ERROR.ordinal,
                event.cause!!
              ), OperationType.DELETE
            )
          )
        }
    } catch (e: Exception) {
      promise.complete(handleDatabaseException<String>(e, OperationType.DELETE))
    }

    return promise.future().coAwait()
  }
}

// Extension function for Row to User conversion
fun Row.toUser() = User(
  id = getUUID("id").toString(),
  email = getString("email"),
  timezone = getString("timezone"),
  firstName = getString("first_name"),
  lastName = getString("last_name"),
  createdAt = getLocalDateTime("created_at")?.atZone(ZoneId.systemDefault()),
  updatedAt = getLocalDateTime("updated_at")?.atZone(ZoneId.systemDefault())
)
