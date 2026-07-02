package kr.co.data.di

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kr.co.core.common.logging.AppLogger
import kr.co.data.di.feature.AccountModule
import kr.co.data.di.feature.CalendarModule
import kr.co.data.di.feature.DashboardModule
import kr.co.data.di.feature.DiaryModule
import kr.co.data.di.feature.SearchModule
import kr.co.data.di.feature.SessionModule
import kr.co.data.di.feature.TimeModule
import kr.co.data.di.feature.UserModule
import kr.co.data.di.feature.UserProfileModule
import kr.co.data.di.feature.UserSettingsModule
import kr.co.data.di.service.RemoteConfigModule
import kr.co.data.service.image.ImageProcessorImpl
import kr.co.data.service.network.NetworkMonitorImpl
import kr.co.data.testing.AndroidFakeAppLogger
import kr.co.data.testing.BaseDataHiltInstrumentationTest
import kr.co.domain.service.image.ImageProcessor
import kr.co.domain.service.network.NetworkMonitor
import org.junit.Assert.assertTrue
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(
    AccountModule::class,
    CalendarModule::class,
    DashboardModule::class,
    DiaryModule::class,
    SearchModule::class,
    SessionModule::class,
    TimeModule::class,
    UserModule::class,
    UserProfileModule::class,
    UserSettingsModule::class,
    RemoteConfigModule::class,
    MinaryDataModule::class,
)
class DataServiceModuleHiltSmokeTest : BaseDataHiltInstrumentationTest() {
    @BindValue
    @JvmField
    val appLogger: AppLogger = AndroidFakeAppLogger()

    @Inject
    lateinit var imageProcessor: ImageProcessor

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Test
    fun service_modules_bind_domain_contracts_to_data_implementations() {
        assertTrue(imageProcessor is ImageProcessorImpl)
        assertTrue(networkMonitor is NetworkMonitorImpl)
    }
}
