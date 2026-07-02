package kr.co.data.feature.emotion.source.remote

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kr.co.core.common.model.Emotion
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import kr.co.data.testing.fake.FakeAppLogger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EmotionRemoteDataSourceTest : BaseDataUnitTest() {
    private val logger = FakeAppLogger()
    private val generativeModel = mockk<GenerativeModel>()
    private val source = EmotionRemoteDataSource(
        logger = logger,
        generativeModel = generativeModel,
    )

    @Test
    fun `analysis parses known emotions removes duplicates and ignores unknown tokens`() = runDataTest {
        coEvery { generativeModel.generateContent(any<String>()) } returns response(
            text = "JOY, SADNESS, JOY, INVALID_TOKEN, UNKNOWN",
        )

        val actual = source.analysis(DataFixtures.diary)

        assertEquals(listOf(Emotion.JOY, Emotion.SADNESS), actual)
    }

    @Test
    fun `analysis limits selected emotions to five`() = runDataTest {
        coEvery { generativeModel.generateContent(any<String>()) } returns response(
            text = "JOY, SADNESS, CALMNESS, ANGER, ANXIETY, TIREDNESS",
        )

        val actual = source.analysis(DataFixtures.diary)

        assertEquals(
            listOf(Emotion.JOY, Emotion.SADNESS, Emotion.CALMNESS, Emotion.ANGER, Emotion.ANXIETY),
            actual,
        )
    }

    @Test
    fun `analysis returns unknown when response has no known emotions`() = runDataTest {
        coEvery { generativeModel.generateContent(any<String>()) } returns response(
            text = "INVALID_TOKEN",
        )

        val actual = source.analysis(DataFixtures.diary)

        assertEquals(listOf(Emotion.UNKNOWN), actual)
    }

    @Test
    fun `analysis returns unknown and logs when model throws`() = runDataTest {
        coEvery { generativeModel.generateContent(any<String>()) } throws IllegalStateException("failure-test")

        val actual = source.analysis(DataFixtures.diary)

        assertEquals(listOf(Emotion.UNKNOWN), actual)
        assertEquals("Failed to analyze emotion", logger.errorThrowables.single().second)
    }

    private fun response(text: String?): GenerateContentResponse =
        mockk {
            every { this@mockk.text } returns text
        }
}
