package info.alkor.whereareyou.impl.persistence

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(PersonRecord::class), version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personRecords(): PersonDao
}
