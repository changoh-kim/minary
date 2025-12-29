package kr.co.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.data.repository.AuthRepositoryImpl
import kr.co.data.repository.AuthTokenRepositoryImpl
import kr.co.data.repository.CalendarRepositoryImpl
import kr.co.domain.repository.AuthRepository
import kr.co.domain.repository.AuthTokenRepository
import kr.co.domain.repository.CalendarRepository
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    /* FirebaseAuth 이전에 사용하던 의존성 주입
    @Binds
    abstract fun bindAuthTokenRepository(impl: AuthTokenRepositoryImpl): AuthTokenRepository*/

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(impl: CalendarRepositoryImpl): CalendarRepository
}