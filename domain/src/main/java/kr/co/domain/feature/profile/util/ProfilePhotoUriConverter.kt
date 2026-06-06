package kr.co.domain.feature.profile.util

import javax.inject.Inject

/**
 * 사용자 프로필 사진의 URI와 파일 경로 간의 변환을 담당하는 유틸리티 클래스입니다.
 * Coil 캐시 버스팅을 위한 타임스탬프 파라미터 추가 및 제거 로직을 중앙 집중화합니다.
 */
class ProfilePhotoUriConverter @Inject constructor() {
    companion object {
        private const val PREFIX_FILE = "file://"
        private const val PARAM_TIMESTAMP = "?t="
    }

    /**
     * 로컬 파일 경로를 임시 URI(file://...?t=timestamp)로 변환합니다.
     */
    fun wrapToTempUri(filePath: String): String {
        return "$filePath?t=${System.currentTimeMillis()}"
        /*return "file://$filePath?t=${System.currentTimeMillis()}"*/
    }

    /**
     * URI가 임시 프로필 사진 패턴(file://...?t=...)인지 확인합니다.
     */
    fun isTempUri(uri: String): Boolean {
        return uri.startsWith(PREFIX_FILE) && uri.contains(PARAM_TIMESTAMP)
    }

    /**
     * 임시 URI에서 순수 파일 경로를 추출합니다.
     * @return 임시 URI 패턴이 아닌 경우 null을 반환합니다.
     */
    fun extractPathFromTempUri(uri: String): String? {
        if (!isTempUri(uri)) return null
        
        return uri.removePrefix(PREFIX_FILE)
            .substringBefore(PARAM_TIMESTAMP)
    }
}
