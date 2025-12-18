package com.rafid.navibus

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.rafid.navibus.data.model.HistoryTrip

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "navibus.db"
        private const val DATABASE_VERSION = 3 // Bump version for schema update

        // Users table
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"

        // Trip History table
        private const val TABLE_TRIP_HISTORY = "trip_history"
        private const val COLUMN_TRIP_ID = "trip_id"
        private const val COLUMN_TRIP_USER_EMAIL = "user_email" // Foreign key
        private const val COLUMN_START_HALTE = "start_halte"
        private const val COLUMN_DESTINATION_HALTE = "destination_halte"
        private const val COLUMN_BUS_CODE = "bus_code"
        private const val COLUMN_TIMESTAMP = "timestamp"

        @Volatile
        private var INSTANCE: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                val instance = DatabaseHelper(context)
                INSTANCE = instance
                instance
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EMAIL TEXT UNIQUE,
                $COLUMN_PASSWORD TEXT
            )
        """.trimIndent()
        db.execSQL(createUsersTable)

        val createTripHistoryTable = """
            CREATE TABLE $TABLE_TRIP_HISTORY (
                $COLUMN_TRIP_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TRIP_USER_EMAIL TEXT,
                $COLUMN_START_HALTE TEXT,
                $COLUMN_DESTINATION_HALTE TEXT,
                $COLUMN_BUS_CODE TEXT,
                $COLUMN_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()
        db.execSQL(createTripHistoryTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
             val createTripHistoryTable = """
                CREATE TABLE $TABLE_TRIP_HISTORY (
                    $COLUMN_TRIP_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_START_HALTE TEXT,
                    $COLUMN_DESTINATION_HALTE TEXT,
                    $COLUMN_BUS_CODE TEXT,
                    $COLUMN_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """.trimIndent()
            db.execSQL(createTripHistoryTable)
        }
         if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_TRIP_HISTORY ADD COLUMN $COLUMN_TRIP_USER_EMAIL TEXT")
        }
    }

    fun addUser(email: String, password: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
        }
        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

    fun checkUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val selection = "$COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?"
        val selectionArgs = arrayOf(email, password)
        db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null).use { cursor ->
            return cursor.count > 0
        }
    }

    fun addTripHistory(userEmail: String, start: String, destination: String, busCode: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TRIP_USER_EMAIL, userEmail)
            put(COLUMN_START_HALTE, start)
            put(COLUMN_DESTINATION_HALTE, destination)
            put(COLUMN_BUS_CODE, busCode)
        }
        val result = db.insert(TABLE_TRIP_HISTORY, null, values)
        return result != -1L
    }

    fun getAllTripsForUser(userEmail: String): List<HistoryTrip> {
        val tripList = mutableListOf<HistoryTrip>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_TRIP_HISTORY WHERE $COLUMN_TRIP_USER_EMAIL = ? ORDER BY $COLUMN_TIMESTAMP DESC"
        db.rawQuery(query, arrayOf(userEmail)).use { cursor ->
            while (cursor.moveToNext()) {
                val trip = HistoryTrip(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRIP_ID)),
                    startHalte = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_HALTE)),
                    destinationHalte = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESTINATION_HALTE)),
                    busCode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BUS_CODE)),
                    timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                )
                tripList.add(trip)
            }
        }
        return tripList
    }
}
