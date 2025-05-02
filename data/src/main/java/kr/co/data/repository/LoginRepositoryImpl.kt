package kr.co.data.repository

import kr.co.data.remote.RetrofitService
import kr.co.domain.repository.LoginRepository
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val retrofitService: RetrofitService,
) : LoginRepository {

    override suspend fun invoke(id: String, password: String): String {
        return retrofitService.login(id, password)
    }
}