package kr.co.data.di.module.feature

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.data.feature.calendar.repository.CalendarRepositoryImpl
import kr.co.domain.feature.calendar.repository.CalendarRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface CalendarModule {
    @Binds
    @Singleton
    fun bindCalendarRepository(impl: CalendarRepositoryImpl): CalendarRepository
}