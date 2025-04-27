package kr.co.minary

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MinaryApplication : Application()