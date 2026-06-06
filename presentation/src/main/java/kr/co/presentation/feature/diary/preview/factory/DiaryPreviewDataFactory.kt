package kr.co.presentation.feature.diary.preview.factory

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kr.co.domain.feature.emotion.model.Emotion
import kr.co.presentation.R
import kr.co.presentation.common.state.LoadState
import kr.co.presentation.feature.diary.model.DiaryUiModel
import kr.co.presentation.feature.diary.preview.model.DiaryPreviewData
import kr.co.presentation.feature.diary.viewmodel.DiaryScreenState
import java.time.LocalDate
import java.util.UUID


object DiaryPreviewDataFactory {

    @Composable
    fun createDiary(
        diaryPreviewData: DiaryPreviewData,
        context: Context = LocalContext.current
    ): DiaryScreenState {
        val (titleResId, contentResId) = getDiaryStringResources(diaryPreviewData.emotion)

        return DiaryScreenState(
            screenMode = diaryPreviewData.screenMode,
            diaryLoadState = LoadState.Success(
                DiaryUiModel(
                    id = UUID.randomUUID().toString(),
                    date = LocalDate.now(),
                    title = context.getString(titleResId),
                    content = context.getString(contentResId),
                    emotions = listOf(diaryPreviewData.emotion)
                )
            )
        )
    }

    @Composable
    fun createDiary(
        emotion: Emotion,
        context: Context = LocalContext.current
    ): DiaryUiModel {
        val (titleResId, contentResId) = getDiaryStringResources(emotion)

        return DiaryUiModel(
            id = UUID.randomUUID().toString(),
            date = LocalDate.now(),
            title = context.getString(titleResId),
            content = context.getString(contentResId),
            emotions = listOf(emotion)
        )
    }

    fun createDiaries(context: Context): ImmutableList<DiaryUiModel> {
        return Emotion.entries.map { emotion ->
            val (titleRes, contentRes) = getDiaryStringResources(emotion)
            DiaryUiModel(
                id = UUID.randomUUID().toString(),
                date = LocalDate.now(),
                title = context.getString(titleRes),
                content = context.getString(contentRes),
                emotions = listOf(emotion)
            )
        }.toImmutableList()
    }

    private fun getDiaryStringResources(emotion: Emotion): Pair<Int, Int> {
        return when (emotion) {
            // 1. 긍정 & 활력
            Emotion.JOY -> R.string.preview_diary_title_joy to R.string.preview_diary_content_joy
            Emotion.EXCITEMENT -> R.string.preview_diary_title_excitement to R.string.preview_diary_content_excitement
            Emotion.TRIUMPH -> R.string.preview_diary_title_triumph to R.string.preview_diary_content_triumph
            Emotion.AMUSEMENT -> R.string.preview_diary_title_amusement to R.string.preview_diary_content_amusement

            // 2. 사랑 & 애정
            Emotion.ROMANCE -> R.string.preview_diary_title_romance to R.string.preview_diary_content_romance
            Emotion.ADORATION -> R.string.preview_diary_title_adoration to R.string.preview_diary_content_adoration
            Emotion.SEXUAL_DESIRE -> R.string.preview_diary_title_sexual_desire to R.string.preview_diary_content_sexual_desire

            // 3. 차분함 & 심미
            Emotion.CALMNESS -> R.string.preview_diary_title_calmness to R.string.preview_diary_content_calmness
            Emotion.SATISFACTION -> R.string.preview_diary_title_satisfaction to R.string.preview_diary_content_satisfaction
            Emotion.AESTHETIC_APPRECIATION -> R.string.preview_diary_title_aesthetic_appreciation to R.string.preview_diary_content_aesthetic_appreciation
            Emotion.ENTRANCEMENT -> R.string.preview_diary_title_entrancement to R.string.preview_diary_content_entrancement

            // 4. 존경 & 관심
            Emotion.ADMIRATION -> R.string.preview_diary_title_admiration to R.string.preview_diary_content_admiration
            Emotion.AWE -> R.string.preview_diary_title_awe to R.string.preview_diary_content_awe
            Emotion.INTEREST -> R.string.preview_diary_title_interest to R.string.preview_diary_content_interest

            // 5. 슬픔 & 그리움
            Emotion.SADNESS -> R.string.preview_diary_title_sadness to R.string.preview_diary_content_sadness
            Emotion.NOSTALGIA -> R.string.preview_diary_title_nostalgia to R.string.preview_diary_content_nostalgia
            Emotion.SYMPATHY -> R.string.preview_diary_title_sympathy to R.string.preview_diary_content_sympathy
            Emotion.EMPATHETIC_PAIN -> R.string.preview_diary_title_empathetic_pain to R.string.preview_diary_content_empathetic_pain

            // 6. 부정 & 경계
            Emotion.ANGER -> R.string.preview_diary_title_anger to R.string.preview_diary_content_anger
            Emotion.FEAR -> R.string.preview_diary_title_fear to R.string.preview_diary_content_fear
            Emotion.HORROR -> R.string.preview_diary_title_horror to R.string.preview_diary_content_horror
            Emotion.ANXIETY -> R.string.preview_diary_title_anxiety to R.string.preview_diary_content_anxiety

            // 7. 복합 & 모호
            Emotion.CONFUSION -> R.string.preview_diary_title_confusion to R.string.preview_diary_content_confusion
            Emotion.BOREDOM -> R.string.preview_diary_title_boredom to R.string.preview_diary_content_boredom
            Emotion.AWKWARDNESS -> R.string.preview_diary_title_awkwardness to R.string.preview_diary_content_awkwardness
            Emotion.DISGUST -> R.string.preview_diary_title_disgust to R.string.preview_diary_content_disgust
            Emotion.ENVY -> R.string.preview_diary_title_envy to R.string.preview_diary_content_envy

            // 8. 욕구
            Emotion.CRAVING -> R.string.preview_diary_title_craving to R.string.preview_diary_content_craving

            // 9. 미정 및 기본값
            Emotion.UNKNOWN -> R.string.preview_diary_title_unknown to R.string.preview_diary_content_unknown
        }
    }
}