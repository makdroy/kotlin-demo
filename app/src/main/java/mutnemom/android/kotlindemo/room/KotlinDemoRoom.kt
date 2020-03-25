package mutnemom.android.kotlindemo.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [Member::class],
    version = 1,
    exportSchema = false
)
abstract class KotlinDemoRoom : RoomDatabase() {

    abstract fun memberDao(): MemberDao

    companion object {
        @Volatile
        private var INSTANCE: KotlinDemoRoom? = null

        fun getRoomInstance(context: Context, scope: CoroutineScope): KotlinDemoRoom =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: createRoomInstance(context, scope).also { INSTANCE = it }
            }

        private fun createRoomInstance(context: Context, scope: CoroutineScope): KotlinDemoRoom =
            Room
                .databaseBuilder(
                    context.applicationContext,
                    KotlinDemoRoom::class.java,
                    "kotlin_demo.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(KotlinDemoRoomCallback(scope))
                .build()
    }

    private class KotlinDemoRoomCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)

            INSTANCE?.let {
                scope.launch {
                    it.withTransaction {
                        /* call suspension functions from different DAOs inside a transaction */
                    }

                    populateDatabase(it.memberDao())
                }
            }
        }

        suspend fun populateDatabase(memberDao: MemberDao) {
            memberDao.deleteMembers()
        }

    }

}
