package kr.co.core.common.result

import com.github.michaelbull.result.Result
import kr.co.core.common.error.DomainError

typealias AppResult<T> = Result<T, DomainError>
