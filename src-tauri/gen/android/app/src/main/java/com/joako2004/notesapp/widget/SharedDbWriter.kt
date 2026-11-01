package com.joako2004.notesapp.widget

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabaseLockedException
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteStatement
import android.os.Build
import android.os.Process
import android.os.SystemClock
import android.util.Log
import java.io.File

data class WriteOutcome(
    val rows: Int,
    val retries: Int,
    val elapsedMs: Long,
    val error: String? = null,
)

class SharedDbWriter(private val context: Context) {

    fun markDone(taskId: String): WriteOutcome {
        val updatedAt = nowSeconds()
        val outcome = write(
            operation = "mark_done",
            sql = MARK_DONE_SQL,
            bindArgs = arrayOf(updatedAt, taskId),
        )
        Log.i(
            TAG,
            "write op=mark_done id=$taskId updated=$updatedAt retries=${outcome.retries} " +
                "ms=${outcome.elapsedMs} pid=${Process.myPid()}",
        )
        return outcome
    }

    fun resetPending(): WriteOutcome {
        val updatedAt = nowSeconds()
        val outcome = write(
            operation = "reset",
            sql = RESET_SQL,
            bindArgs = arrayOf(updatedAt),
        )
        Log.i(
            TAG,
            "write op=reset updated=$updatedAt rows=${outcome.rows} " +
                "retries=${outcome.retries} ms=${outcome.elapsedMs} pid=${Process.myPid()}",
        )
        return outcome
    }

    private fun write(operation: String, sql: String, bindArgs: Array<Any>): WriteOutcome {
        val startedAt = SystemClock.elapsedRealtime()
        val dbFile = context.getDatabasePath(DB_NAME)
        if (!dbFile.exists()) {
            Log.i(TAG, "write op=$operation skipped db_exists=false pid=${Process.myPid()}")
            return WriteOutcome(
                rows = 0,
                retries = 0,
                elapsedMs = SystemClock.elapsedRealtime() - startedAt,
                error = "db_exists=false",
            )
        }

        var retries = 0
        while (true) {
            try {
                val rows = executeOnce(dbFile, sql, bindArgs)
                return WriteOutcome(
                    rows = rows,
                    retries = retries,
                    elapsedMs = SystemClock.elapsedRealtime() - startedAt,
                )
            } catch (locked: SQLiteDatabaseLockedException) {
                if (retries >= MAX_RETRIES) {
                    Log.e(TAG, "write op=$operation locked after retries=$retries", locked)
                    return WriteOutcome(
                        rows = 0,
                        retries = retries,
                        elapsedMs = SystemClock.elapsedRealtime() - startedAt,
                        error = locked.message,
                    )
                }
                retries += 1
                Log.i(
                    TAG,
                    "write op=$operation locked retry=$retries/$MAX_RETRIES " +
                        "pid=${Process.myPid()}",
                )
                SystemClock.sleep(RETRY_DELAY_MS)
            } catch (error: SQLiteException) {
                Log.e(TAG, "write op=$operation failed", error)
                return WriteOutcome(
                    rows = 0,
                    retries = retries,
                    elapsedMs = SystemClock.elapsedRealtime() - startedAt,
                    error = error.message,
                )
            }
        }
    }

    private fun executeOnce(dbFile: File, sql: String, bindArgs: Array<Any>): Int {
        // ENABLE_WRITE_AHEAD_LOGGING keeps the shared database in WAL. Without it,
        // the framework applies the platform default journal mode (manufacturer-defined)
        // when this is the only open connection.
        // https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase#ENABLE_WRITE_AHEAD_LOGGING
        val database = SQLiteDatabase.openDatabase(
            dbFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING,
        )
        try {
            configureBusyTimeout(database)
            database.beginTransaction()
            try {
                val rows = executeUpdate(database, sql, bindArgs)
                database.setTransactionSuccessful()
                return rows
            } finally {
                database.endTransaction()
            }
        } finally {
            logJournalMode(database)
            database.close()
        }
    }

    private fun logJournalMode(database: SQLiteDatabase) {
        try {
            database.rawQuery("PRAGMA journal_mode", null).use { cursor ->
                val mode = if (cursor.moveToFirst()) cursor.getString(0) else "<no rows>"
                Log.i(TAG, "write journal_mode=$mode pid=${Process.myPid()}")
            }
        } catch (error: SQLiteException) {
            Log.e(TAG, "write journal_mode failed pid=${Process.myPid()}", error)
        }
    }

    private fun executeUpdate(database: SQLiteDatabase, sql: String, bindArgs: Array<Any>): Int {
        val statement = database.compileStatement(sql)
        try {
            bindArgs.forEachIndexed { index, value ->
                when (value) {
                    is String -> statement.bindString(index + 1, value)
                    is Long -> statement.bindLong(index + 1, value)
                    else -> throw IllegalArgumentException(
                        "Unsupported bind argument at ${index + 1}: ${value::class.java.name}",
                    )
                }
            }
            return statement.executeUpdateDelete()
        } finally {
            statement.close()
        }
    }

    private fun configureBusyTimeout(database: SQLiteDatabase) {
        // Android has no public SQLiteDatabase busy-timeout setter. PRAGMA busy_timeout
        // applies per connection; execPerConnectionSQL is the documented way to apply it
        // to current and future connections on API 30+. On older APIs execSQL only covers
        // the write connection, so the retry loop above is the safety net.
        // https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase#execPerConnectionSQL(java.lang.String,java.lang.Object[])
        // https://www.sqlite.org/pragma.html#pragma_busy_timeout
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                database.execPerConnectionSQL(
                    "PRAGMA busy_timeout = $BUSY_TIMEOUT_MS",
                    emptyArray<Any>(),
                )
            } else {
                database.execSQL("PRAGMA busy_timeout = $BUSY_TIMEOUT_MS")
            }
            Log.i(TAG, "busy_timeout=$BUSY_TIMEOUT_MS api=${Build.VERSION.SDK_INT}")
        } catch (error: SQLiteException) {
            Log.e(TAG, "busy_timeout failed", error)
        }
    }

    private fun nowSeconds(): Long = System.currentTimeMillis() / 1_000L

    companion object {
        private const val TAG = "SQLITESPIKE"
        private const val DB_NAME = "notes_spike.db"
        private const val BUSY_TIMEOUT_MS = 2_000
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 200L
        private const val MARK_DONE_SQL =
            "UPDATE tasks SET status = 'done', updated_at = ? " +
                "WHERE id = ? AND status = 'pending' AND deleted_at IS NULL"
        private const val RESET_SQL =
            "UPDATE tasks SET status = 'pending', updated_at = ? WHERE deleted_at IS NULL"
    }
}
