package kr.co.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kr.co.core.common.state.SyncStatus
import java.time.YearMonth

@Entity(tableName = "diary_sync_metadata")
data class DiarySyncMetadataEntity(

    @PrimaryKey
    val yearMonth: YearMonth,
    val status: SyncStatus,
    val lastSyncedAt: Long = System.currentTimeMillis()
)