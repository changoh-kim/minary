package kr.co.data.source

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import kr.co.domain.model.diary.DiaryData
import kr.co.domain.model.emotion.Emotion
import javax.inject.Inject


class EmotionAnalysisAiDataSource @Inject constructor(
    private val generativeModel: GenerativeModel
) {
    private val tag: String = this::class.java.simpleName

    suspend fun emotionAnalysis(diaryData: DiaryData): String {
        val emotions = Emotion.entries.joinToString(", ") { it.name }

        val prompt = """
                일기 제목과 내용을 분석하고 [$emotions] 목록 중 
                감정 1개만 선택해줘.
                결과는 반드시 선택한 감정 1개만 출력해줘.
                
                제목: ${diaryData.title}
                내용: ${diaryData.content}
            """.trimIndent()

        try {
            val response = generativeModel.generateContent(prompt)
            val emotion = response.text?.trim() ?: Emotion.UNKNOWN.name
            return emotion
        } catch (e: Exception){
            Log.e(tag, "emotionAnalysisAi: ${e.message}")
            return Emotion.UNKNOWN.name
        }
    }
}