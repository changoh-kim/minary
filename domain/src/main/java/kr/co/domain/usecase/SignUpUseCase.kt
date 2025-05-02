package kr.co.domain.usecase

import kr.co.domain.repository.SignUpRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val signUpRepository: SignUpRepository,
) {
    suspend operator fun invoke(id: String, userName: String,password: String) : Boolean {
        return signUpRepository(id, userName, password)
    }
}