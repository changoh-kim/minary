package kr.co.domain.feature.search.usecase

import kr.co.domain.feature.search.repository.SearchRepository
import kr.co.domain.feature.time.service.ServerTimeProvider
import javax.inject.Inject

class AddRecentSearchUseCase @Inject constructor(
    private val searchRepository: SearchRepository,
    private val serverTime: ServerTimeProvider,
) {
    suspend operator fun invoke(query: String) {
        if (query.isBlank()) return

        searchRepository.addRecentSearch(query.trim(), serverTime.now())
    }
}