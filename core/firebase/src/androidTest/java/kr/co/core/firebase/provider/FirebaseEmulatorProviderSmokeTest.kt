package kr.co.core.firebase.provider

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.memoryCacheSettings
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import kr.co.core.firebase.testing.BaseInstrumentationTest
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FirebaseEmulatorProviderSmokeTest : BaseInstrumentationTest() {

    private companion object {
        const val APP_NAME = "core-firebase-test"
        const val EMULATOR_HOST = "10.0.2.2"
        const val AUTH_PORT = 9099
        const val FIRESTORE_PORT = 8080
        const val FUNCTIONS_PORT = 5001
        const val STORAGE_PORT = 9199
        const val REGION_SEOUL = "asia-northeast3"
    }

    private var firebaseApp: FirebaseApp? = null

    @After
    fun tearDownFirebaseApp() {
        firebaseApp?.delete()
        firebaseApp = null
    }

    @Test
    fun providersCreateReferencesWithEmulatorConfiguredSdkInstances() {
        val app = initializeTestFirebaseApp()
        val auth = FirebaseAuth.getInstance(app).apply {
            useEmulator(EMULATOR_HOST, AUTH_PORT)
        }
        val firestore = FirebaseFirestore.getInstance(app).apply {
            useEmulator(EMULATOR_HOST, FIRESTORE_PORT)
            firestoreSettings = firestoreSettings {
                setLocalCacheSettings(memoryCacheSettings { })
            }
        }
        val functions = FirebaseFunctions.getInstance(app, REGION_SEOUL).apply {
            useEmulator(EMULATOR_HOST, FUNCTIONS_PORT)
        }
        val storage = FirebaseStorage.getInstance(app).apply {
            useEmulator(EMULATOR_HOST, STORAGE_PORT)
        }

        val authProvider = FirebaseAuthProvider(auth)
        val firestoreProvider = FirebaseFirestoreProvider(firestore)
        assertNull(authProvider.currentUser)
        authProvider.signOut()
        assertNotNull(firestoreProvider.getUserProfileRef("uid-test"))
        assertNotNull(firestoreProvider.getUserSettingsRef("uid-test"))
        assertNotNull(firestoreProvider.getDiariesRef("uid-test"))
        assertNotNull(FirebaseFunctionsProvider(functions).getCheckEmailAvailabilityCallable())
        assertNotNull(FirebaseStorageProvider(storage).getUserProfilePhotoRef("uid-test"))
    }

    private fun initializeTestFirebaseApp(): FirebaseApp {
        val options = FirebaseOptions.Builder()
            .setProjectId("minary-2c818")
            .setApplicationId("1:000000000000:android:coretest")
            .setApiKey("test-api-key")
            .setStorageBucket("minary-2c818.appspot.com")
            .build()
        return FirebaseApp.initializeApp(context, options, APP_NAME).also {
            firebaseApp = it
        }
    }
}
