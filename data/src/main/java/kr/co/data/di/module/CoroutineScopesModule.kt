package kr.co.data.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kr.co.data.di.qualifier.ApplicationScope
import kr.co.data.di.qualifier.DefaultDispatcher
import kr.co.data.di.qualifier.IoDispatcher
import kr.co.data.di.qualifier.IoScope
import kr.co.data.di.qualifier.MainDispatcher
import kr.co.data.di.qualifier.MainScope
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object CoroutineScopesModule {

    /**
     * Dispatchers.Default를 사용하는 코루틴 스코프를 제공합니다.
     * 주로 CPU-bound 작업이나 비동기 작업에 사용됩니다.
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun providesCoroutineScope(
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
    ): CoroutineScope {
        return CoroutineScope(SupervisorJob() + defaultDispatcher)
    }

    /**
     * IO Dispatcher를 사용하는 코루틴 스코프를 제공합니다.
     * 주로 파일 입출력, 네트워크 통신 등 블로킹이 발생할 수 있는 I/O 작업에 사용됩니다.
     */
    @Provides
    @Singleton
    @IoScope
    fun providesIoScope(
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): CoroutineScope {
        return CoroutineScope(SupervisorJob() + ioDispatcher)
    }

    /**
     * Main Dispatcher를 사용하는 코루틴 스코프를 제공합니다.
     * 주로 UI와 직접 상호작용하는 작업에 사용됩니다.
     */
    @Provides
    @Singleton
    @MainScope
    fun providesMainScope(
        @MainDispatcher mainDispatcher: CoroutineDispatcher
    ): CoroutineScope {
        return CoroutineScope(SupervisorJob() + mainDispatcher)
    }
}