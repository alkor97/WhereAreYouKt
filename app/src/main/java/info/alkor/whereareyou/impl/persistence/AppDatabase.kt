package info.alkor.whereareyou.impl.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [PersonRecord::class, LocationActionRecord::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personRecords(): PersonDao
    abstract fun locationActionRecords(): LocationActionDao

    companion object {
        @Volatile
        private var db: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var current = db
                if (current == null) {
                    current = Room.databaseBuilder(context, AppDatabase::class.java, "WhereAreYouDb")
                            .fallbackToDestructiveMigration()
                            .build()
                    db = current
                }
                return current
            }
        }
    }
}
