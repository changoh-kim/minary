package kr.co.data.di.module

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.google.ai.client.generativeai.GenerativeModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.co.data.di.qualifier.GeminiApiKey
import kr.co.data.feature.auth.AuthRepositoryImpl
import kr.co.data.feature.calendar.repository.CalendarRepositoryImpl
import kr.co.data.feature.dashboard.repository.DashboardRepositoryImpl
import kr.co.data.feature.diary.repository.DiaryRepositoryImpl
import kr.co.data.feature.emotion.EmotionAnalysisAiDataSource
import kr.co.data.local.MinaryDatabase
import kr.co.data.local.dao.DiaryDao
import kr.co.domain.feature.auth.repository.AuthRepository
import kr.co.domain.feature.calendar.repository.CalendarRepository
import kr.co.domain.feature.dashboard.repository.DashboardRepository
import kr.co.domain.feature.diary.repository.DiaryRepository
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class MinaryDataModule {

    companion object {
        const val DATABASE_NAME = "minary_db"
        const val GEMINI_MODEL_NAME = "gemini-2.5-flash-lite"

        @Provides
        @Singleton
        fun provideAppDatabase(@ApplicationContext context: Context): MinaryDatabase {
            return Room.databaseBuilder(
                context,
                MinaryDatabase::class.java,
                DATABASE_NAME
            ).build()
        }

        @Provides
        @Singleton
        fun provideDiaryDao(minaryDatabase: MinaryDatabase): DiaryDao {
            return minaryDatabase.diaryDao()
        }

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
        fun provideEmotionAnalysisAiDataSource(generativeModel: GenerativeModel): EmotionAnalysisAiDataSource {
            return EmotionAnalysisAiDataSource(generativeModel)
        }

        @Provides
        @Singleton
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
            return WorkManager.getInstance(context)
        }
    }

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(impl: CalendarRepositoryImpl): CalendarRepository

    @Binds
    @Singleton
    abstract fun bindDiaryRepository(impl: DiaryRepositoryImpl): DiaryRepository

    @Binds
    @Singleton
    abstract fun bindDashBoardRepository(impl: DashboardRepositoryImpl): DashboardRepository
}