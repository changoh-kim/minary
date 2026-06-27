package kr.co.domain.testing

import com.github.michaelbull.result.annotation.UnsafeResultErrorAccess
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import kr.co.core.common.error.DomainError
import kr.co.core.common.result.AppResult
import org.junit.jupiter.api.Assertions.assertEquals

@OptIn(UnsafeResultValueAccess::class, UnsafeResultErrorAccess::class)
fun <T> AppResult<T>.assertOk(): T =
    when {
        isOk -> value
        isErr -> throw AssertionError("Expected Ok but was Err($error)")
        else -> throw AssertionError("Expected Ok but result state was unknown")
    }

fun <T> AppResult<T>.assertOk(expected: T): T {
    val actual = assertOk()
    assertEquals(expected, actual)
    return actual
}

@OptIn(UnsafeResultValueAccess::class, UnsafeResultErrorAccess::class)
fun <T> AppResult<T>.assertErr(): DomainError =
    when {
        isErr -> error
        isOk -> throw AssertionError("Expected Err but was Ok($value)")
        else -> throw AssertionError("Expected Err but result state was unknown")
    }

fun <T> AppResult<T>.assertErr(expected: DomainError): DomainError {
    val actual = assertErr()
    assertEquals(expected, actual)
    return actual
}
