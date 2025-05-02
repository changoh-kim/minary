package kr.co.domain.repository

interface LoginRepository {
    suspend operator fun invoke(id: String, password: String): String
}