package kr.co.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.data.repository.AuthRepositoryImpl
import kr.co.data.repository.AuthTokenRepositoryImpl
import kr.co.domain.repository.AuthRepository
import kr.co.domain.repository.AuthTokenRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

//    @Binds
//    abstract fun bindAuthTokenRepository(impl: AuthTokenRepositoryImpl): AuthTokenRepository

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}