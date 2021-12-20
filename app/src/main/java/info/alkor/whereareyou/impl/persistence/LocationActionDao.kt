package info.alkor.whereareyou.impl.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import info.alkor.whereareyou.model.action.Direction
import info.alkor.whereareyou.model.action.MessageId
import info.alkor.whereareyou.model.action.SendingStatus
import info.alkor.whereareyou.model.location.Provider
import java.util.*

@Dao
interface LocationActionDao {
    @Query("SELECT * FROM location_action ORDER BY id DESC")
    fun all(): LiveData<List<LocationActionRecord>>

    @Query("DELETE FROM location_action WHERE id = :id")
    fun deleteAction(id: MessageId)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAction(action: LocationActionRecord): MessageId

    @Query("UPDATE location_action SET status = :status WHERE id = :id")
    fun updateSendingStatus(id: MessageId, status: SendingStatus)

    @Query("UPDATE location_action SET progress = :progress WHERE id = :id")
    fun updateProgress(id: MessageId, progress: Float)

    @Query("SELECT * FROM location_action WHERE id = :id")
    fun findById(id: MessageId): LocationActionRecord?

    @Query("SELECT * FROM location_action WHERE phone = :phone AND isFinal = 0 ORDER BY id DESC LIMIT 1")
    fun findMatching(phone: String): LocationActionRecord?

    @Update
    fun updateAction(action: LocationActionRecord)
}

@Entity(tableName = "location_action")
data class LocationActionRecord(
        @PrimaryKey(autoGenerate = true)
        var id: MessageId?,
        var direction: Direction,
        var phone: String,
        var name: String?,
        @Embedded
        var location: LocationRecord?,
        var isFinal: Boolean,
        var status: SendingStatus,
        var progress: Float?
)

data class LocationRecord(
        val provider: Provider,
        val time: Date,
        @Embedded
        val coordinates: CoordinatesRecord,
        @Embedded(prefix = "alt_")
        val altitude: AccurateValueRecord?,
        @Embedded(prefix = "bear_")
        val bearing: AccurateValueRecord?,
        @Embedded(prefix = "speed_")
        val speed: AccurateValueRecord?
)

data class CoordinatesRecord(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Double? = null
)

data class AccurateValueRecord(
        val value: Double,
        val accuracy: Double?
)