package kr.co.data.testing

import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.memoryCacheSettings
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import java.util.UUID

abstract class BaseFirebaseEmulatorTest : BaseDataInstrumentationTest() {
    private var firebaseApp: FirebaseApp? = null
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null
    private var functions: FirebaseFunctions? = null
    private var storage: FirebaseStorage? = null

    protected val firebaseAuth: FirebaseAuth
        get() = requireNotNull(auth)

    protected val firebaseFirestore: FirebaseFirestore
        get() = requireNotNull(firestore)

    protected val firebaseFunctions: FirebaseFunctions
        get() = requireNotNull(functions)

    protected val firebaseStorage: FirebaseStorage
        get() = requireNotNull(storage)

    @Before
    fun setUpFirebaseEmulator() {
        assumeTrue(
            "Firebase Emulator Suite smoke tests require firebaseEmulatorEnabled=true.",
            isFirebaseEmulatorEnabled()
        )

        val app = FirebaseApp.initializeApp(context, firebaseOptions(), APP_NAME)
        firebaseApp = app

        auth = FirebaseAuth.getInstance(app).apply {
            useEmulator(firebaseEmulatorHost(), AUTH_PORT)
        }
        firestore = FirebaseFirestore.getInstance(app).apply {
            useEmulator(firebaseEmulatorHost(), FIRESTORE_PORT)
            firestoreSettings = firestoreSettings {
                setLocalCacheSettings(memoryCacheSettings { })
            }
        }
        functions = FirebaseFunctions.getInstance(app, REGION_SEOUL).apply {
            useEmulator(firebaseEmulatorHost(), FUNCTIONS_PORT)
        }
        storage = FirebaseStorage.getInstance(app).apply {
            useEmulator(firebaseEmulatorHost(), STORAGE_PORT)
        }
    }

    @After
    fun tearDownFirebaseEmulator() {
        if (isFirebaseEmulatorEnabled()) {
            runBlocking {
                runCatching {
                    auth?.currentUser?.delete()?.await()
                }
            }
        }
        firebaseApp?.delete()
        firebaseApp = null
        auth = null
        firestore = null
        functions = null
        storage = null
    }

    protected suspend fun createSignedInEmulatorUser(
        password: String = EMULATOR_PASSWORD,
    ): FirebaseUser {
        val result = firebaseAuth
            .createUserWithEmailAndPassword(uniqueSyntheticEmail(), password)
            .await()
        return requireNotNull(result.user)
    }

    protected fun uniqueEmulatorId(prefix: String): String =
        "$prefix-${UUID.randomUUID()}"

    private fun isFirebaseEmulatorEnabled(): Boolean =
        InstrumentationRegistry
            .getArguments()
            .getString(ARG_FIREBASE_EMULATOR_ENABLED)
            .toBoolean()

    private fun firebaseOptions() = FirebaseOptions.Builder()
        .setProjectId(PROJECT_ID)
        .setApplicationId(APPLICATION_ID)
        .setApiKey(API_KEY)
        .setStorageBucket(STORAGE_BUCKET)
        .build()

    private fun firebaseEmulatorHost(): String =
        InstrumentationRegistry
            .getArguments()
            .getString(ARG_FIREBASE_EMULATOR_HOST, defaultFirebaseEmulatorHost())

    private fun defaultFirebaseEmulatorHost(): String =
        if (isAndroidVirtualDevice()) {
            DEFAULT_ANDROID_VIRTUAL_DEVICE_HOST
        } else {
            DEFAULT_ADB_REVERSED_DEVICE_HOST
        }

    private fun isAndroidVirtualDevice(): Boolean =
        Build.FINGERPRINT.contains("generic", ignoreCase = true) ||
            Build.FINGERPRINT.contains("emulator", ignoreCase = true) ||
            Build.MODEL.contains("sdk", ignoreCase = true) ||
            Build.MODEL.contains("emulator", ignoreCase = true) ||
            Build.MANUFACTURER.contains("Genymotion", ignoreCase = true) ||
            Build.PRODUCT.contains("sdk", ignoreCase = true)

    private fun uniqueSyntheticEmail(): String =
        "${uniqueEmulatorId("user")}@example.test"

    protected companion object {
        const val ARG_FIREBASE_EMULATOR_ENABLED = "firebaseEmulatorEnabled"
        const val ARG_FIREBASE_EMULATOR_HOST = "firebaseEmulatorHost"
        const val APP_NAME = "data-firebase-emulator-test"
        const val PROJECT_ID = "minary-2c818"
        const val APPLICATION_ID = "1:000000000000:android:datatest"
        const val API_KEY = "AIzaSyA00000000000000000000000000000000"
        const val STORAGE_BUCKET = "minary-2c818.firebasestorage.app"
        const val EMULATOR_PASSWORD = "Password1!"
        const val REGION_SEOUL = "asia-northeast3"
        const val DEFAULT_ANDROID_VIRTUAL_DEVICE_HOST = "10.0.2.2"
        const val DEFAULT_ADB_REVERSED_DEVICE_HOST = "127.0.0.1"
        const val AUTH_PORT = 9099
        const val FIRESTORE_PORT = 8080
        const val FUNCTIONS_PORT = 5001
        const val STORAGE_PORT = 9199
    }
}
