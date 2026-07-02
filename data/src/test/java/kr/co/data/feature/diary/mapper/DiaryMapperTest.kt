package kr.co.data.feature.diary.mapper

import kr.co.core.common.model.Emotion
import kr.co.core.common.state.DiarySyncStatus
import kr.co.core.database.entity.DiaryEmotionEntity
import kr.co.core.database.entity.DiaryImageUrlEntity
import kr.co.data.feature.diary.mapper.DiaryMapper.toDiary
import kr.co.data.feature.diary.mapper.DiaryMapper.toDiaryDto
import kr.co.data.feature.diary.mapper.DiaryMapper.toDiaryWithRelations
import kr.co.data.feature.diary.model.DiaryDto
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DiaryMapperTest : BaseDataUnitTest() {
    @Test
    fun `maps domain diary to diary relations`() {
        assertEquals(DataFixtures.diaryWithRelations, DataFixtures.diary.toDiaryWithRelations())
    }

    @Test
    fun `maps diary relations to domain diary`() {
        assertEquals(DataFixtures.diary, DataFixtures.diaryWithRelations.toDiary())
    }

    @Test
    fun `maps diary dto to diary relations with requested sync status`() {
        val expected = DataFixtures.diaryWithRelations.copy(
            diary = DataFixtures.diaryEntity.copy(syncStatus = DiarySyncStatus.SYNCED),
        )

        val actual = DataFixtures.diaryDto.toDiaryWithRelations(DiarySyncStatus.SYNCED)

        assertEquals(expected, actual)
    }

    @Test
    fun `maps unknown diary dto emotion to unknown`() {
        val dto = DataFixtures.diaryDto.copy(
            emotions = listOf("JOY", "unknown-value-test"),
        )

        val actual = dto.toDiaryWithRelations(DiarySyncStatus.SYNCED)

        assertEquals(
            listOf(
                DiaryEmotionEntity(DataFixtures.DIARY_ID, Emotion.JOY),
                DiaryEmotionEntity(DataFixtures.DIARY_ID, Emotion.UNKNOWN),
            ),
            actual.emotions,
        )
    }

    @Test
    fun `throws when diary dto date is invalid`() {
        val dto = DataFixtures.diaryDto.copy(date = "invalid-date-test")

        assertThrows(IllegalArgumentException::class.java) {
            dto.toDiaryWithRelations(DiarySyncStatus.SYNCED)
        }
    }

    @Test
    fun `maps diary relations to dto`() {
        assertEquals(DataFixtures.diaryDto, DataFixtures.diaryWithRelations.toDiaryDto())
    }

    @Test
    fun `maps image urls by diary id`() {
        val actual = DataFixtures.diary.toDiaryWithRelations()

        assertEquals(
            listOf(DiaryImageUrlEntity(DataFixtures.DIARY_ID, DataFixtures.IMAGE_URL)),
            actual.imageUrls,
        )
    }

    @Test
    fun `keeps dto defaults stable when constructed with no args`() {
        val dto = DiaryDto()

        assertEquals("", dto.date)
        assertEquals("", dto.title)
        assertEquals("", dto.content)
        assertEquals(emptyList<String>(), dto.emotions)
        assertEquals(emptyList<String>(), dto.imageUrls)
    }
}
