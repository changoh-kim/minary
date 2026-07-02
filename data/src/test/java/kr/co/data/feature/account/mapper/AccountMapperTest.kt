package kr.co.data.feature.account.mapper

import kr.co.data.feature.account.mapper.AccountMapper.toAccount
import kr.co.data.feature.account.mapper.AccountMapper.toAccountModel
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AccountMapperTest : BaseDataUnitTest() {
    @Test
    fun `maps account model to domain account`() {
        val model = DataFixtures.accountModel

        val actual = model.toAccount()

        assertEquals(DataFixtures.account, actual)
    }

    @Test
    fun `maps domain account to account model`() {
        val account = DataFixtures.account

        val actual = account.toAccountModel()

        assertEquals(DataFixtures.accountModel, actual)
    }
}
