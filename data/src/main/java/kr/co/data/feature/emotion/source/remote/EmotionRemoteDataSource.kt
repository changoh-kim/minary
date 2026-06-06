package kr.co.data.feature.emotion.source.remote

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import kr.co.data.extension.TAG
import kr.co.domain.common.extension.toEmotion
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.emotion.model.Emotion
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmotionRemoteDataSource @Inject constructor(
    private val generativeModel: GenerativeModel
) {
    suspend fun analysis(diary: Diary): List<Emotion> {
        val emotions = Emotion.entries.filter { it != Emotion.UNKNOWN }.joinToString(", ") { it.name }

        val prompt = """
                일기 제목과 내용을 분석하고 [$emotions] 목록 중 
                어울리는 감정을 최소 1개에서 최대 5개까지 선택해줘.
                결과는 반드시 선택한 감정들을 쉼표(,)로 구분해서 감정 이름만 출력해줘.
                다른 설명은 생략해줘.
                예시: JOY, SADNESS, CALMNESS
                
                제목: ${diary.title}
                내용: ${diary.content}
            """.trimIndent()

        try {
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text?.trim() ?: ""

            val selectedEmotions = responseText.split(",")
                .map { it.trim().toEmotion() }
                .filter { it != Emotion.UNKNOWN }
                .distinct()
                .take(5)

            return selectedEmotions.ifEmpty { listOf(Emotion.UNKNOWN) }
        } catch (e: Exception){
            Log.e(TAG, "emotionAnalysisAi: ${e.message}")
            return listOf(Emotion.UNKNOWN)
        }
    }
}