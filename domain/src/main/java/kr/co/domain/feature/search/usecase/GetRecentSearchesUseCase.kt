package kr.co.domain.feature.search.usecase

import kotlinx.coroutines.flow.Flow
import kr.co.domain.feature.search.repository.SearchRepository
import javax.inject.Inject

class GetRecentSearchesUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    operator fun invoke(): Flow<List<String>> = searchRepository.getRecentSearches()
}