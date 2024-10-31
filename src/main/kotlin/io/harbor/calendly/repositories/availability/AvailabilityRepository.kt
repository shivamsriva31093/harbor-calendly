
import io.harbor.calendly.repositories.BaseRepository
import io.harbor.calendly.repositories.availability.AvailabilityWindow
import io.harbor.calendly.repositories.availability.AvailabilityWindowWithId
import io.harbor.calendly.repositories.models.ErrorCode
import io.harbor.calendly.repositories.models.OperationError
import io.harbor.calendly.repositories.models.OperationType
import io.harbor.calendly.repositories.models.RepositoryResult
import io.harbor.calendly.util.handleDatabaseException
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.Tuple
import java.time.LocalTime
import java.util.*

class AvailabilityRepository(): BaseRepository() {

    suspend fun addAvailabilityWindow(
        userId: String,
        window: AvailabilityWindow
    ): RepositoryResult<String> {
        return try {
            val windowId = UUID.randomUUID().toString()

            val result = pgPool.preparedQuery("""
                INSERT INTO stg.availability_settings (
                    id, user_id, day_of_week, start_time, end_time
                ) VALUES ($1, $2, $3, $4, $5)
                ON CONFLICT (user_id, day_of_week, start_time, end_time)
                DO NOTHING
                RETURNING id
            """).execute(Tuple.of(
                windowId,
                userId,
                window.dayOfWeek,
                LocalTime.parse(window.startTime),
                LocalTime.parse(window.endTime)
            )).coAwait()

            when {
                result.rowCount() > 0 -> RepositoryResult.success(
                    data = windowId,
                    affectedRows = 1,
                    operation = OperationType.INSERT
                )
                else -> RepositoryResult.failure(
                    error = OperationError(
                        code = ErrorCode.DUPLICATE_ENTRY,
                        message = "Availability window already exists"
                    ),
                    operation = OperationType.INSERT
                )
            }
        } catch (e: Exception) {
            handleDatabaseException(e, OperationType.INSERT)
        }
    }

    suspend fun removeAvailabilityWindow(
        userId: String,
        windowId: String
    ): RepositoryResult<Unit> {
        return try {
            val result = pgPool.preparedQuery("""
                DELETE FROM stg.availability_settings
                WHERE id = $1 AND user_id = $2
            """).execute(Tuple.of(windowId, userId)).await()

            when {
                result.rowCount() > 0 -> RepositoryResult.success(
                    affectedRows = result.rowCount(),
                    operation = OperationType.DELETE
                )
                else -> RepositoryResult.failure(
                    error = OperationError(
                        code = ErrorCode.NOT_FOUND,
                        message = "Availability window not found"
                    ),
                    operation = OperationType.DELETE
                )
            }
        } catch (e: Exception) {
            handleDatabaseException(e, OperationType.DELETE)
        }
    }

    suspend fun updateAvailabilityWindow(
        userId: String,
        windowId: String,
        window: AvailabilityWindow
    ): RepositoryResult<Unit> {
        return try {
            val result = pgPool.preparedQuery("""
                UPDATE stg.availability_settings
                SET day_of_week = $1, start_time = $2, end_time = $3
                WHERE id = $4 AND user_id = $5
                RETURNING id
            """).execute(Tuple.of(
                window.dayOfWeek,
                window.startTime,
                window.endTime,
                windowId,
                userId
            )).await()

            when {
                result.rowCount() > 0 -> RepositoryResult.success(
                    affectedRows = result.rowCount(),
                    operation = OperationType.UPDATE
                )
                else -> RepositoryResult.failure(
                    error = OperationError(
                        code = ErrorCode.NOT_FOUND,
                        message = "Availability window not found"
                    ),
                    operation = OperationType.UPDATE
                )
            }
        } catch (e: Exception) {
            handleDatabaseException(e, OperationType.UPDATE)
        }
    }

    suspend fun getAvailabilityWindows(
        userId: String
    ): RepositoryResult<List<AvailabilityWindowWithId>> {
        return try {
            val result = pgPool.preparedQuery("""
                SELECT id, day_of_week, start_time, end_time
                FROM stg.availability_settings
                WHERE user_id = $1
                ORDER BY day_of_week, start_time
            """).execute(Tuple.of(UUID.fromString(userId))).coAwait()

            RepositoryResult.success(
                data = result.map { row ->
                    AvailabilityWindowWithId(
                        id = row.getUUID("id").toString(),
                        window = AvailabilityWindow(
                            dayOfWeek = row.getInteger("day_of_week"),
                            startTime = row.getLocalTime("start_time").toString(),
                            endTime = row.getLocalTime("end_time").toString()
                        )
                    )
                },
                affectedRows = result.rowCount(),
                operation = OperationType.SELECT
            )
        } catch (e: Exception) {
            handleDatabaseException(e, OperationType.SELECT)
        }
    }


}
