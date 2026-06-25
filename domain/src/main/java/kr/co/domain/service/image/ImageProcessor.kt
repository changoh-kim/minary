package kr.co.domain.service.image

import com.github.michaelbull.result.Result
import kr.co.core.common.error.DomainError

interface ImageProcessor {

    suspend fun resizeImage(
        sourceUrl: String,
        targetUrl: String,
        maxWidth: Int = 512,
        maxHeight: Int = 512
    ): Result<String, DomainError>
}