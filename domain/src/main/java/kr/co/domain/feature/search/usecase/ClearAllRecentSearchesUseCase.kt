package kr.co.domain.feature.search.usecase

import kr.co.domain.feature.search.repository.SearchRepository
import javax.inject.Inject

class ClearAllRecentSearchesUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    suspend operator fun invoke() = searchRepository.clearAll()
}