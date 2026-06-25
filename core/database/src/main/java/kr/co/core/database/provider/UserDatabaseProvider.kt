package kr.co.core.database.provider

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kr.co.core.storage.config.LocalStoragePathProvider
import kr.co.core.database.database.UserDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDatabaseProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val pathProvider: LocalStoragePathProvider,
) {
    private var currentUid: String? = null
    private var databaseInstance: UserDatabase? = null

    @Synchronized
    fun getDatabase(): UserDatabase {
        val uid = firebaseAuth.currentUser?.uid ?: "default"

        if (databaseInstance == null || currentUid != uid) {
            databaseInstance?.close()

            databaseInstance = Room.databaseBuilder(
                context,
                UserDatabase::class.java,
                pathProvider.getUserDatabaseName(uid)
            ).build()

            currentUid = uid
        }
        return databaseInstance!!
    }

    fun deleteDatabaseFile(uid: String) {
        context.deleteDatabase(pathProvider.getUserDatabaseName(uid))

        // Room/SQLite 잔여 파일 강제 삭제
        context.getDatabasePath(pathProvider.getUserDatabaseName(uid)).delete()
        context.getDatabasePath("${pathProvider.getUserDatabaseName(uid)}-shm").delete()
        context.getDatabasePath("${pathProvider.getUserDatabaseName(uid)}-wal").delete()
    }
}