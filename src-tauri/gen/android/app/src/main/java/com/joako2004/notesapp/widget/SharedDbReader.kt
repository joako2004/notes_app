package com.joako2004.notesapp.widget

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.Process
import android.util.Log
import java.io.File
import java.io.IOException

class SharedDbReader(private val context: Context) {

    fun readPendingTaskTitles(): List<String> {
        val dbFile = context.getDatabasePath(DB_NAME)
        logProcessInfo(dbFile)

        if (!dbFile.exists()) {
            Log.i(TAG, "db_exists=false")
            Log.i(TAG, "pending_count=0")
            return emptyList()
        }
        Log.i(TAG, "db_exists=true")

        val readOnlyDatabase = openDatabase(dbFile, SQLiteDatabase.OPEN_READONLY, "READONLY")
        if (readOnlyDatabase != null) {
            try {
                return readOnlyDatabase.use { database -> readTitles(database) }
            } catch (error: SQLiteException) {
                Log.i(TAG, "open_mode=READONLY read failed: ${error.message}")
            }
        }

        val readWriteDatabase = openDatabase(dbFile, SQLiteDatabase.OPEN_READWRITE, "READWRITE")
        if (readWriteDatabase == null) {
            Log.i(TAG, "pending_count=0")
            return emptyList()
        }

        return try {
            readWriteDatabase.use { database ->
                try {
                    database.execSQL("PRAGMA query_only = ON")
                    Log.i(TAG, "query_only=ON")
                } catch (queryOnlyError: SQLiteException) {
                    Log.i(TAG, "query_only failed: ${queryOnlyError.message}")
                }
                readTitles(database)
            }
        } catch (error: SQLiteException) {
            Log.i(TAG, "open_mode=READWRITE read failed: ${error.message}")
            Log.i(TAG, "pending_count=0")
            emptyList()
        }
    }

    private fun openDatabase(dbFile: File, flags: Int, label: String): SQLiteDatabase? =
        try {
            SQLiteDatabase.openDatabase(dbFile.absolutePath, null, flags).also {
                Log.i(TAG, "open_mode=$label")
            }
        } catch (error: SQLiteException) {
            Log.i(TAG, "open_mode=$label failed: ${error.message}")
            null
        }

    private fun readTitles(database: SQLiteDatabase): List<String> {
        logDatabaseInfo(database)
        return queryPendingTitles(database)
    }

    private fun logProcessInfo(dbFile: File) {
        Log.i(TAG, "pid=${Process.myPid()}")
        Log.i(TAG, "process=${readProcessName()}")
        Log.i(TAG, "rust_lib_loaded=${rustLibraryLoaded()}")
        Log.i(TAG, "db_path=${dbFile.absolutePath}")
    }

    private fun readProcessName(): String =
        try {
            File(PROC_CMDLINE).readText().substringBefore('\u0000')
        } catch (error: IOException) {
            Log.i(TAG, "cmdline_read_failed=${error.message}")
            "unknown"
        }

    private fun rustLibraryLoaded(): Boolean =
        try {
            File(PROC_MAPS).useLines { lines -> lines.any { it.contains(RUST_LIBRARY) } }
        } catch (error: IOException) {
            Log.i(TAG, "maps_read_failed=${error.message}")
            false
        }

    private fun logDatabaseInfo(database: SQLiteDatabase) {
        logSingleValue(database, "SELECT sqlite_version()", "sqlite_version")
        logSingleValue(database, "PRAGMA journal_mode", "journal_mode")
    }

    private fun logSingleValue(database: SQLiteDatabase, query: String, label: String) {
        try {
            database.rawQuery(query, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    Log.i(TAG, "$label=${cursor.getString(0)}")
                } else {
                    Log.i(TAG, "$label=<no rows>")
                }
            }
        } catch (error: SQLiteException) {
            Log.i(TAG, "${label}_error=${error.message}")
        }
    }

    private fun queryPendingTitles(database: SQLiteDatabase): List<String> {
        val titles = mutableListOf<String>()
        database.rawQuery(PENDING_TASKS_QUERY, null).use { cursor ->
            while (cursor.moveToNext()) {
                titles += cursor.getString(0)
            }
        }
        Log.i(TAG, "pending_count=${titles.size}")
        return titles
    }

    companion object {
        private const val TAG = "SQLITESPIKE"
        private const val DB_NAME = "notes_spike.db"
        private const val PROC_CMDLINE = "/proc/self/cmdline"
        private const val PROC_MAPS = "/proc/self/maps"
        private const val RUST_LIBRARY = "libnotes_app_lib"
        private const val PENDING_TASKS_QUERY =
            "SELECT title FROM tasks WHERE status = 'pending' AND deleted_at IS NULL " +
                "ORDER BY updated_at ASC, title ASC"
    }
}
