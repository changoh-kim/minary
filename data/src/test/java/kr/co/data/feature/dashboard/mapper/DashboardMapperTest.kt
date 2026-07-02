package kr.co.data.feature.dashboard.mapper

import kr.co.data.feature.dashboard.mapper.DashboardMapper.toDashboard
import kr.co.data.feature.dashboard.mapper.DashboardMapper.toDashboardModel
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DashboardMapperTest : BaseDataUnitTest() {
    @Test
    fun `maps dashboard model to domain dashboard`() {
        assertEquals(DataFixtures.dashboard, DataFixtures.dashboardModel.toDashboard())
    }

    @Test
    fun `maps domain dashboard to dashboard model`() {
        assertEquals(DataFixtures.dashboardModel, DataFixtures.dashboard.toDashboardModel())
    }
}
