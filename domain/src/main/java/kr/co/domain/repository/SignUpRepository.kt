package kr.co.domain.repository

interface SignUpRepository {
    suspend operator fun invoke(id: String, userName: String, password: String): Boolean
}