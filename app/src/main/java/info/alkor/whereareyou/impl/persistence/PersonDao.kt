package info.alkor.whereareyou.impl.persistence

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PersonDao {
    @Query("SELECT * FROM person ORDER BY name")
    fun all(): LiveData<List<PersonRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg persons: PersonRecord)

    @Delete
    fun delete(person: PersonRecord)

    @Query("SELECT * FROM person WHERE phone = :phone")
    fun getPersonByPhone(phone: String): PersonRecord?
}

@Entity(tableName = "person")
data class PersonRecord(
        @PrimaryKey val phone: String,
        val name: String?
)
