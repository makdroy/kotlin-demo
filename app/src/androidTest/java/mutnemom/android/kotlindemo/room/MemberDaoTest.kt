package mutnemom.android.kotlindemo.room

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MemberDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var memberDao: MemberDao
    private lateinit var db: KotlinDemoRoom

    @Before
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext()

        /* using in-memory database
        and allowing main thread queries for testing */

        db = Room.inMemoryDatabaseBuilder(context, KotlinDemoRoom::class.java)
            .allowMainThreadQueries()
            .build()

        memberDao = db.memberDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetMemberName() = runBlocking {
        val testName = "test man"
        val member = Member(name = testName)
        memberDao.insertMember(member)
        val allMembers = memberDao.getMembers().waitForValue()
        assertEquals(allMembers[0].name, testName)
    }

    @Test
    @Throws(Exception::class)
    fun getAllMembers() = runBlocking {
        val testName1 = "insert first"
        val testName2 = "insert second"

        val member1 = Member(name = testName1)
        val member2 = Member(name = testName2)

        memberDao.insertMember(member1)
        memberDao.insertMember(member2)

        val allMembers = memberDao.getMembers().waitForValue()
        assertEquals(allMembers[0].name, testName2)
        assertEquals(allMembers[1].name, testName1)
    }

    @Test
    @Throws(Exception::class)
    fun deleteAll() = runBlocking {
        val testName1 = "delete first"
        val testName2 = "delete second"

        val member1 = Member(name = testName1)
        val member2 = Member(name = testName2)

        memberDao.insertMember(member1)
        memberDao.insertMember(member2)
        memberDao.deleteMembers()

        val allMembers = memberDao.getMembers().waitForValue()
        assertTrue(allMembers.isEmpty())
    }

}
