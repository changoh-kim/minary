package kr.co.data.di.module.feature

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.data.feature.search.repository.SearchRepositoryImpl
import kr.co.domain.feature.search.repository.SearchRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface SearchModule {
    @Binds
    @Singleton
    fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository
}