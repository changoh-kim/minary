package kr.co.minary.logging

import timber.log.Timber

class MinaryDebugTree : Timber.DebugTree() {

    override fun createStackElementTag(element: StackTraceElement): String =
        "${super.createStackElementTag(element)}.${element.methodName}:${element.lineNumber}"
}
