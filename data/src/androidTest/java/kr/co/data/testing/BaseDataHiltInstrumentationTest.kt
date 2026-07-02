package kr.co.data.testing

import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.Before
import org.junit.Rule

abstract class BaseDataHiltInstrumentationTest : BaseDataInstrumentationTest() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUpHilt() {
        hiltRule.inject()
    }
}
