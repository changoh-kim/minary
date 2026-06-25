package kr.co.data.di.feature

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.data.feature.account.service.FirebaseAccountService
import kr.co.domain.feature.account.service.AccountService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AccountModule {

    @Binds
    @Singleton
    fun bindAccountService(impl: FirebaseAccountService): AccountService
}