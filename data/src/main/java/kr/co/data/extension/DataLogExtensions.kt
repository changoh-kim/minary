package kr.co.data.extension

val Any.TAG: String
    get() = this::class.java.simpleName.let {
        if (it.length > 23) it.substring(0, 23) else it
    }