package kr.co.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kr.co.core.database.entity.DiarySyncMetadataEntity
import java.time.YearMonth

@Dao
interface DiarySyncMetadataDao {

    @Query("""
        SELECT * FROM diary_sync_metadata
        WHERE yearMonth = :yearMonth 
    """)
    fun getMetadataFlow(yearMonth: YearMonth): Flow<DiarySyncMetadataEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: DiarySyncMetadataEntity)
}