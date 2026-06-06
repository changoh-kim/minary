package kr.co.presentation.feature.account.mapper

import kr.co.domain.feature.account.model.Account
import kr.co.presentation.feature.account.model.AccountUiModel

object AccountUiModelMapper {

    fun Account.toAccountUiModel() = AccountUiModel(
        uid = uid,
        email = email,
    )

    fun AccountUiModel.toAccount() = Account(
        uid = uid,
        email = email,
    )
}
