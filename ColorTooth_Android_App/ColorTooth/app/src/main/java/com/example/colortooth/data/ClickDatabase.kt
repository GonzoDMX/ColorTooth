package com.example.colortooth.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Database(entities = arrayOf(ClickData::class), version = 1, exportSchema = false)
abstract class ClickDatabase : RoomDatabase(){

    abstract fun clickDao(): ClickDao

    private class ClickDatabaseCallback( private val scope: CoroutineScope) :
        RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.clickDao())

                }
            }
        }

        suspend fun populateDatabase(clickDao: ClickDao) {

            clickDao.deleteAll()
            /*
            // Add test data
            var click1 = ClickData( 0, 1, "Red", 255, 255, 0, 0)
            var click2 = ClickData( 0, 2, "Green", 255, 0, 255, 0)
            var click3 = ClickData( 0, 3, "Blue", 255, 0, 0, 255)
            clickDao.insert(click1)
            clickDao.insert(click2)
            clickDao.insert(click3)
             */
        }
    }

    companion object {

        @Volatile
        private var INSTANCE: ClickDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ClickDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                    ClickDatabase::class.java,
                    "click_database"
                        ).addCallback(ClickDatabaseCallback(scope)).build()
                INSTANCE = instance
                instance
            }
        }
    }
}