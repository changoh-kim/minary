package kr.co.domain.repository

interface LoginRepository {

    fun invoke(username: String, password: String): Result<String>
}