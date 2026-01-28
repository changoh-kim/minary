package kr.co.presentation.ui.preview

import kr.co.domain.model.emotion.Emotion
import kr.co.presentation.ui.model.common.UiDiary
import kr.co.presentation.viewmodel.diary.DiaryState
import kr.co.presentation.viewmodel.diary.DiaryUiState
import java.time.LocalDate


object DiaryPreviewFactory {
    fun createDiaryState(mode: DiaryUiState) = DiaryState(
        mode = mode,
        diary = UiDiary(
            id = 0L,
            date = LocalDate.now(),
            title = "어느 화창한 화요일의 기록",
            content = "오늘은 점심 식사 후 가벼운 산책을 했다. 맑은 하늘과 시원한 바람 덕분에 복잡했던 머릿속이 한결 가벼워지는 기분이었다. 길가에 핀 작은 꽃들을 구경하며 걷다 보니 일상의 소중함을 다시금 느낄 수 있었다. 특별한 일은 없었지만, 이런 평범한 평화가 나를 미소 짓게 한다. 내일도 오늘처럼만 기분 좋은 하루가 되길 바라며 하루를 마무리한다.",
            emotion = Emotion.SATISFACTION
        ),
    )
}