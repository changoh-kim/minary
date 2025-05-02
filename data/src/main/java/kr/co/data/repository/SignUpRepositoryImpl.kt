package kr.co.data.repository

import kr.co.data.remote.RetrofitService
import kr.co.domain.repository.SignUpRepository
import javax.inject.Inject

class SignUpRepositoryImpl @Inject constructor(
    val retrofitService: RetrofitService
) : SignUpRepository {

    override suspend fun invoke(
        id: String,
        userName: String,
        password: String
    ): Boolean {
        return retrofitService.signUp(id, userName, password)
    }
}