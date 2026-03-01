package kr.co.minary.di

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.data.di.qualifier.GeminiApiKey
import kr.co.minary.BuildConfig
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class MinaryAppModule {

    @Singleton
    @Binds
    abstract fun bindContext(application: Application): Context

    companion object {

        @Provides
        @Singleton
        @GeminiApiKey
        fun provideGeminiApiKey(): String {
            return BuildConfig.GEMINI_API_KEY
        }
    }
}