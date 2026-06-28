package kr.co.core.firebase.provider

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableReference
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class FirebaseFunctionsProviderTest {

    private val functions = mockk<FirebaseFunctions>()
    private val provider = FirebaseFunctionsProvider(functions)

    @Test
    fun `returns configured callable references`() {
        val checkEmail = mockk<HttpsCallableReference>()
        val createAccount = mockk<HttpsCallableReference>()
        val deleteAccount = mockk<HttpsCallableReference>()
        every { functions.getHttpsCallable("checkEmailAvailability") } returns checkEmail
        every { functions.getHttpsCallable("createAccountAndUserData") } returns createAccount
        every { functions.getHttpsCallable("deleteAccountAndUserData") } returns deleteAccount

        assertSame(checkEmail, provider.getCheckEmailAvailabilityCallable())
        assertSame(createAccount, provider.getCreateAccountAndUserDataCallable())
        assertSame(deleteAccount, provider.getDeleteAccountAndUserDataCallable())
    }
}
