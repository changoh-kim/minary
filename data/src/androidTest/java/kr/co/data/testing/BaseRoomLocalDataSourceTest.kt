package kr.co.data.testing

import androidx.room.Room
import io.mockk.every
import io.mockk.mockk
import kr.co.core.database.database.UserDatabase
import kr.co.core.database.provider.UserDatabaseProvider
import org.junit.After
import org.junit.Before
import java.util.concurrent.Executor

abstract class BaseRoomLocalDataSourceTest : BaseDataInstrumentationTest() {
    protected lateinit var database: UserDatabase
    protected lateinit var databaseProvider: UserDatabaseProvider

    private val directExecutor = Executor { command -> command.run() }

    @Before
    fun setUpDatabase() {
        database = Room.inMemoryDatabaseBuilder(context, UserDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor(directExecutor)
            .setTransactionExecutor(directExecutor)
            .build()

        databaseProvider = mockk()
        every { databaseProvider.getDatabase() } returns database
    }

    @After
    fun tearDownDatabase() {
        if (::database.isInitialized) {
            database.close()
        }
    }
}
