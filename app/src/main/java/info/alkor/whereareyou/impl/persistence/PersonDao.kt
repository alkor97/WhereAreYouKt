package info.alkor.whereareyou.impl.persistence

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PersonDao {
    @Query("SELECT * from person")
    fun all(): LiveData<List<PersonRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg persons: PersonRecord)

    @Delete
    suspend fun delete(person: PersonRecord)

    @Query("SELECT * FROM person WHERE phone = :phone")
    suspend fun getPersonByPhone(phone: String): PersonRecord?
}

@Entity(tableName = "person")
data class PersonRecord(
        @PrimaryKey val phone: String,
        val name: String?
)
