package kr.co.data.remote

import javax.inject.Inject

class RetrofitService @Inject constructor() {

    suspend fun login(id: String, password: String): String {
        return ""
    }

    suspend fun signUp(id: String, userName: String, password: String): Boolean {
        return true
    }
}
