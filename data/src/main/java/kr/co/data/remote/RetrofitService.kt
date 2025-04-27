package kr.co.data.remote

import javax.inject.Inject

class RetrofitService @Inject constructor() {

    fun login(username: String, password: String): Result<String> {
        return Result.success("Success")
    }
}
