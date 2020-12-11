package info.alkor.whereareyou.impl.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PersonRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personRecords(): PersonDao

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
