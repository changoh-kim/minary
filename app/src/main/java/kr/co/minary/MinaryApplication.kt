package kr.co.minary

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kr.co.minary.initializer.AppCheckInitializer
import kr.co.minary.logging.MinaryDebugTree
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MinaryApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(MinaryDebugTree())
        }
        // FirebaseApp 초기화
        FirebaseApp.initializeApp(this)
        // AppCheck 초기화
        AppCheckInitializer.initialize()
    }
}
