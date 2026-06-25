package kr.co.data.di.feature

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.data.feature.diary.repository.DiaryRepositoryImpl
import kr.co.data.feature.diary.sync.DiarySyncStateRepositoryImpl
import kr.co.data.feature.diary.sync.DiarySyncWorkManager
import kr.co.data.feature.diary.sync.FirestoreDiaryRealtimeSyncManager
import kr.co.data.feature.diary.sync.FirestoreDiarySyncManager
import kr.co.domain.feature.diary.repository.DiaryRepository
import kr.co.domain.feature.diary.sync.DiarySyncStateRepository
import kr.co.domain.feature.diary.sync.DiaryRealtimeSyncManager
import kr.co.domain.feature.diary.sync.DiarySyncManager
import kr.co.domain.feature.diary.sync.DiarySyncScheduler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DiaryModule {
    // repository
    @Binds
    @Singleton
    fun bindDiaryRepository(impl: DiaryRepositoryImpl): DiaryRepository

    @Binds
    @Singleton
    fun bindDiarySyncStateRepository(impl: DiarySyncStateRepositoryImpl): DiarySyncStateRepository

    // service
    @Binds
    @Singleton
    fun bindDiarySyncScheduler(impl: DiarySyncWorkManager): DiarySyncScheduler

    @Binds
    @Singleton
    fun bindDiarySyncManager(impl: FirestoreDiarySyncManager): DiarySyncManager

    @Binds
    @Singleton
    fun bindDiaryRealtimeSyncManager(impl: FirestoreDiaryRealtimeSyncManager): DiaryRealtimeSyncManager
}