package kr.co.domain.usecase

import kr.co.domain.repository.LoginRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val loginRepository: LoginRepository
) {
    suspend operator fun invoke(id: String, password: String): String {
        return loginRepository(id, password)
    }
}
