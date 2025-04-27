package kr.co.data.repository

import kr.co.data.remote.RetrofitService
import kr.co.domain.repository.LoginRepository
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val retrofitService: RetrofitService,
) : LoginRepository {

    override fun invoke(username: String, password: String): Result<String> {
        return retrofitService.login(username, password)
    }
}