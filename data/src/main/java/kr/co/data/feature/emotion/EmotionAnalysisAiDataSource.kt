package kr.co.data.feature.emotion

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import kr.co.domain.common.extension.toEmotion
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.emotion.Emotion
import javax.inject.Inject


class EmotionAnalysisAiDataSource @Inject constructor(
    private val generativeModel: GenerativeModel
) {
    private val tag: String = this::class.java.simpleName

    suspend fun emotionAnalysis(diary: Diary): Emotion {
        val emotions = Emotion.entries.joinToString(", ") { it.name }

        val prompt = """
                일기 제목과 내용을 분석하고 [$emotions] 목록 중 
                감정 1개만 선택해줘.
                결과는 반드시 선택한 감정 1개만 출력해줘.
                
                제목: ${diary.title}
                내용: ${diary.content}
            """.trimIndent()

        try {
            val response = generativeModel.generateContent(prompt)
            val emotionName = response.text?.trim()

            return emotionName?.toEmotion() ?: Emotion.UNKNOWN
        } catch (e: Exception){
            Log.e(tag, "emotionAnalysisAi: ${e.message}")
            return Emotion.UNKNOWN
        }
    }
}