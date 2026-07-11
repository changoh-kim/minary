package kr.co.data.di

import android.content.Context
import androidx.work.WorkManager
import com.google.ai.client.generativeai.GenerativeModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.co.core.di.qualifier.GeminiApiKey
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface MinaryDataModule {

    companion object {
        const val GEMINI_MODEL_NAME = "gemini-3.1-flash-lite"

        @Provides
        @Singleton
        fun provideGenerativeModel(@GeminiApiKey apiKey: String): GenerativeModel {
            return GenerativeModel(
                modelName = GEMINI_MODEL_NAME,
                apiKey = apiKey,
            )
        }

        @Provides
        @Singleton
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
            return WorkManager.getInstance(context)
        }
    }
}