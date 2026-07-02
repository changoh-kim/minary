package kr.co.data.testing

import android.annotation.SuppressLint
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.impl.WorkManagerImpl
import androidx.work.impl.model.WorkSpec
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import java.util.concurrent.TimeUnit

@SuppressLint("RestrictedApi")
abstract class BaseWorkManagerSchedulerTest : BaseDataInstrumentationTest() {
    @Before
    fun setUpWorkManager() {
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @After
    fun tearDownWorkManager() {
        WorkManagerTestInitHelper.closeWorkDatabase()
        WorkManagerImpl.setDelegate(null)
    }

    protected fun workSpecsForUniqueWork(name: String): List<WorkSpec> {
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(name)
            .get(5, TimeUnit.SECONDS)

        val workManager = WorkManagerImpl.getInstance(context)
        val workSpecDao = workManager.workDatabase.workSpecDao()
        return workSpecDao.getWorkSpecIdAndStatesForName(name)
            .mapNotNull { workSpecDao.getWorkSpec(it.id) }
    }

    protected fun onlyWorkSpecForUniqueWork(name: String): WorkSpec {
        val specs = workSpecsForUniqueWork(name)
        assertEquals(1, specs.size)
        return specs.single()
    }

    protected fun assertUniqueWorkCancelled(name: String) {
        val specs = workSpecsForUniqueWork(name)
        assertEquals(true, specs.isNotEmpty())
        specs.forEach { spec ->
            assertEquals(WorkInfo.State.CANCELLED, spec.state)
        }
    }
}
