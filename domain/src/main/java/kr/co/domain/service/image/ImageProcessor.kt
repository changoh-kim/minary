package kr.co.domain.service.image

import kr.co.core.common.result.AppResult

interface ImageProcessor {

    suspend fun resizeImage(
        sourceUrl: String,
        targetUrl: String,
        maxWidth: Int = 512,
        maxHeight: Int = 512
    ): AppResult<String>
}