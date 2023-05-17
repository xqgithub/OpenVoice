package com.shannon.openvoice.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shannon.openvoice.FunApplication

/**
 *
 * @Package:        com.shannon.openvoice.db
 * @ClassName:      AppDatabase
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/12 16:14
 */
@TypeConverters(Converters::class)
@Database(entities = [DraftModel::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun draftDao(): DraftDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    FunApplication.getInstance(),
                    AppDatabase::class.java,
                    "openVoice"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}