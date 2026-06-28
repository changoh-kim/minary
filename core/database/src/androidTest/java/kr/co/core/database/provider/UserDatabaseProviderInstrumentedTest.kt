package kr.co.core.database.provider

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import kr.co.core.database.testing.BaseInstrumentationTest
import kr.co.core.storage.config.LocalStoragePathProvider
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test

class UserDatabaseProviderInstrumentedTest : BaseInstrumentationTest() {

    private val pathProvider = LocalStoragePathProvider()

    @After
    fun tearDownFiles() {
        context.deleteDatabase(pathProvider.getUserDatabaseName("default"))
        context.deleteDatabase(pathProvider.getUserDatabaseName("uid-test"))
    }

    @Test
    fun getDatabaseCachesPerCurrentUidAndDeletesDatabaseFiles() {
        val auth = mockk<FirebaseAuth>()
        val user = mockk<FirebaseUser>()
        var currentUser: FirebaseUser? = null
        every { user.uid } returns "uid-test"
        every { auth.currentUser } answers { currentUser }
        val provider = UserDatabaseProvider(context, auth, pathProvider)

        val defaultDatabase = provider.getDatabase()
        assertSame(defaultDatabase, provider.getDatabase())

        currentUser = user
        val userDatabase = provider.getDatabase()
        assertNotSame(defaultDatabase, userDatabase)
        assertSame(userDatabase, provider.getDatabase())

        provider.deleteDatabaseFile("uid-test")
        assertFalse(context.getDatabasePath(pathProvider.getUserDatabaseName("uid-test")).exists())

        userDatabase.close()
    }
}
