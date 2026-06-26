package kr.co.core.common.logging

interface AppLogger {
    fun v(message: String, vararg args: Any?)

    fun d(message: String, vararg args: Any?)

    fun i(message: String, vararg args: Any?)

    fun w(message: String, vararg args: Any?)

    fun w(throwable: Throwable, message: String, vararg args: Any?)

    fun e(message: String, vararg args: Any?)

    fun e(throwable: Throwable, message: String, vararg args: Any?)
}
