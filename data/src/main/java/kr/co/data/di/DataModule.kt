package kr.co.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.data.remote.RetrofitService
import kr.co.data.repository.AuthTokenRepositoryImpl
import kr.co.data.repository.LoginRepositoryImpl
import kr.co.domain.repository.AuthTokenRepository
import kr.co.domain.repository.LoginRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindLoginRepository(impl: LoginRepositoryImpl): LoginRepository

    @Binds
    abstract fun bindAuthTokenRepository(impl: AuthTokenRepositoryImpl): AuthTokenRepository
}