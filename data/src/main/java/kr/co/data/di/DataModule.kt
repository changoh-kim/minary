package kr.co.data.di

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
import kr.co.data.local.AppDatabase
import kr.co.data.local.dao.DiaryDAO
import kr.co.data.repository.AuthRepositoryImpl
import kr.co.data.repository.CalendarRepositoryImpl
import kr.co.data.repository.DiaryRepositoryImpl
import kr.co.data.source.EmotionAnalysisAiDataSource
import kr.co.domain.repository.AuthRepository
import kr.co.domain.repository.CalendarRepository
import kr.co.domain.repository.DiaryRepository
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    companion object {
        @Provides
        @Singleton
        fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "minary_db"
            ).build()
        }

        @Provides
        @Singleton
        fun provideDiaryDao(appDatabase: AppDatabase): DiaryDAO {
            return appDatabase.diaryDao()
        }

        @Provides
        @Singleton
        fun provideGenerativeModel(@GeminiApiKey apiKey: String): GenerativeModel {
            return GenerativeModel(
                modelName = "gemini-2.5-flash-lite",
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
        fun provideWorkManager(
            @ApplicationContext context: Context
        ): WorkManager {
            return WorkManager.getInstance(context)
        }
    }
    /* FirebaseAuth 이전에 사용하던 의존성 주입
    @Binds
    abstract fun bindAuthTokenRepository(impl: AuthTokenRepositoryImpl): AuthTokenRepository*/

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(impl: CalendarRepositoryImpl): CalendarRepository

    @Binds
    @Singleton
    abstract fun bindDiaryRepository(impl: DiaryRepositoryImpl): DiaryRepository
}