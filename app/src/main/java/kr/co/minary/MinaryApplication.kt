package kr.co.minary

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kr.co.minary.initializer.AppCheckInitializer
import javax.inject.Inject

@HiltAndroidApp
class MinaryApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // FirebaseApp 초기화
        FirebaseApp.initializeApp(this)
        // AppCheck 초기화
        AppCheckInitializer.initialize()
    }
}