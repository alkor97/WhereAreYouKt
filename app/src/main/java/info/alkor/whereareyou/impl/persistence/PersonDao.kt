package info.alkor.whereareyou.impl.persistence

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PersonDao {
    @Query("SELECT * from person")
    fun all(): LiveData<List<PersonRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg persons: PersonRecord)

    @Delete
    fun delete(person: PersonRecord)
}

@Entity(tableName = "person")
data class PersonRecord(
        @PrimaryKey val phone: String,
        val name: String?
)
