package kr.co.minary.di

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.core.common.logging.AppLogger
import kr.co.core.di.qualifier.GeminiApiKey
import kr.co.minary.BuildConfig
import kr.co.minary.logging.TimberAppLogger
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MinaryAppModule {

    @Singleton
    @Binds
    abstract fun bindContext(application: Application): Context

    @Singleton
    @Binds
    abstract fun bindAppLogger(logger: TimberAppLogger): AppLogger

    companion object {

        @Provides
        @Singleton
        @GeminiApiKey
        fun provideGeminiApiKey(): String {
            return BuildConfig.GEMINI_API_KEY
        }
    }
}
