package kr.co.data.local.provider

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kr.co.data.local.config.LocalStoragePathProvider
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserInternalStorageProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val pathProvider: LocalStoragePathProvider
) {
    private var currentUid: String? = null
    private var userDirectoryInstance: File? = null

    private fun getUserDirectoryPath(uid: String): File {
        return File(context.filesDir, pathProvider.getUserDirectoryName(uid))
    }

    /**
     * 특정 사용자의 내부 저장소 디렉토리를 가져옵니다.
     */
    @Synchronized
    fun getUserDirectory(): File {
        val uid = firebaseAuth.currentUser?.uid ?: "default"

        if (userDirectoryInstance == null || currentUid != uid) {
            val dir = getUserDirectoryPath(uid)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            userDirectoryInstance = dir
            currentUid = uid
        }
        return userDirectoryInstance!!
    }

    /**
     * 특정 사용자의 내부 저장소 디렉토리와 모든 파일을 삭제합니다.
     */
    @Synchronized
    fun deleteUserDirectory(uid: String) {
        val dir = getUserDirectoryPath(uid)

        if (currentUid == uid) {
            userDirectoryInstance = null
            currentUid = null
        }

        if (dir.exists()) {
            dir.deleteRecursively() // 폴더 안의 파일들까지 한꺼번에 안전하게 삭제
        }
    }
}