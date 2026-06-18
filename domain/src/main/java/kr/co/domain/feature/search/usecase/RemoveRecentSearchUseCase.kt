package kr.co.domain.feature.search.usecase

import kr.co.domain.feature.search.repository.SearchRepository
import javax.inject.Inject

class RemoveRecentSearchUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    suspend operator fun invoke(query: String) = searchRepository.removeRecentSearch(query)
}