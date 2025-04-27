package kr.co.domain.usecase

import kr.co.domain.repository.LoginRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val loginRepository: LoginRepository
) {
    operator fun invoke(username: String, password: String): Result<String> {
        return loginRepository.invoke(username, password)
    }
}
