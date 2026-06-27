package kr.co.domain.testing.fake

import com.github.michaelbull.result.Ok
import kr.co.core.common.result.AppResult
import kr.co.domain.feature.user.repository.UserStorageRepository

class FakeUserStorageRepository(
    var deleteUserStorageResult: AppResult<Unit> = Ok(Unit),
) : UserStorageRepository {
    val deletedUids = mutableListOf<String>()

    override suspend fun deleteUserStorage(uid: String): AppResult<Unit> {
        deletedUids += uid
        return deleteUserStorageResult
    }
}
