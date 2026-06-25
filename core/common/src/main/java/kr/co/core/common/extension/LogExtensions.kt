package kr.co.core.common.extension

val Any.TAG: String
    get() = this::class.java.simpleName.let {
        // 최대 23자 제한
        if (it.length > 23) it.substring(0, 23) else it
    }
