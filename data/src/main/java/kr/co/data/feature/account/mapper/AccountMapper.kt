package kr.co.data.feature.account.mapper

import kr.co.data.feature.account.model.AccountModel
import kr.co.domain.feature.account.model.Account

object AccountMapper {
    fun Account.toAccountModel() = AccountModel(
        uid = uid,
        email = email,
    )

    fun AccountModel.toAccount() = Account(
        uid = uid,
        email = email,
    )
}
