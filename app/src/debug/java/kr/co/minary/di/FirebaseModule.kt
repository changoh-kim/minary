package kr.co.minary.di

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.memoryCacheSettings
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.presentation.common.extension.TAG
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    private const val REGION_SEOUL = "asia-northeast3"
    private const val EMULATOR_HOST = "10.0.2.2"
    private const val AUTH_PORT = 9099
    private const val FIRESTORE_PORT = 8080
    private const val FUNCTIONS_PORT = 5001
    private const val STORAGE_PORT = 9199

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth.apply {
        useEmulator(EMULATOR_HOST, AUTH_PORT)
        Log.d(TAG, "Firebase Auth emulator connected")
    }

    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore.apply {
        useEmulator(EMULATOR_HOST, FIRESTORE_PORT)
        Log.d(TAG, "Firebase Firestore emulator connected")

        firestoreSettings = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings { })
        }
    }

    @Singleton
    @Provides
    fun provideFirebaseFunctions(): FirebaseFunctions = Firebase.functions(REGION_SEOUL).apply {
        useEmulator(EMULATOR_HOST, FUNCTIONS_PORT)
        Log.d(TAG, "Firebase Functions emulator connected")
    }

    @Singleton
    @Provides
    fun provideFirebaseStorage(): FirebaseStorage = Firebase.storage.apply {
        useEmulator(EMULATOR_HOST, STORAGE_PORT)
        Log.d(TAG, "Firebase Storage emulator connected")
    }

    @Singleton
    @Provides
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig = Firebase.remoteConfig
}
