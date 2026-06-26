package kr.co.domain.service.time

import kr.co.core.common.result.AppResult

/**
 * 서버 시간을 제공하는 인터페이스입니다.
 *
 * 멀티 디바이스 환경에서 Offline-firest 정책을 지원하며,
 * 사용자 기기의 시간 설정과 관계없이 서버(NTP) 시간을 제공합니다.
 * 데이터 동기화시 발생 할 수 있는 시간 충돌(LWW, Last Write Win)을 방지하기 위해 사용합니다.
 */
interface ServerTimeProvider {

    /**
     * NTP 서버와 통신하여 현재 기기 시간과 동기화합니다.
     */
    suspend fun sync(): AppResult<Unit>

    /**
     * 동기화된 서버의 현재 시간을 반환합니다.
     *
     * [sync] 이후 사용하세요.
     * 캐시를 이용해 오프라인 상태에서도 시간을 제공합니다.
     * 서버와 동기화되지 않고 호출하는 경우 [System.currentTimeMillis]를 반환합니다.
     */
    fun now(): Long
}