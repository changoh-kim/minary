package kr.co.core.firebase.provider

import com.google.firebase.functions.FirebaseFunctions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFunctionsProvider @Inject constructor(
    private val functions: FirebaseFunctions,
) {
    companion object {
        const val CALL_NAME_CHECK_EMAIL_AVAILABILITY = "checkEmailAvailability"
        const val CALL_NAME_CREATE_ACCOUNT_AND_USER_DATA = "createAccountAndUserData"
        const val CALL_NAME_DELETE_ACCOUNT_AND_USER_DATA = "deleteAccountAndUserData"
    }

    fun getCheckEmailAvailabilityCallable() =
        functions.getHttpsCallable(CALL_NAME_CHECK_EMAIL_AVAILABILITY)

    fun getCreateAccountAndUserDataCallable() =
        functions.getHttpsCallable(CALL_NAME_CREATE_ACCOUNT_AND_USER_DATA)

    fun getDeleteAccountAndUserDataCallable() =
        functions.getHttpsCallable(CALL_NAME_DELETE_ACCOUNT_AND_USER_DATA)
}