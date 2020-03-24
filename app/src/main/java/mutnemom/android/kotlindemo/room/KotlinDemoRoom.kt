package mutnemom.android.kotlindemo.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

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

        fun getRoomInstance(context: Context): KotlinDemoRoom =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: createRoomInstance(context).also { INSTANCE = it }
            }

        private fun createRoomInstance(context: Context): KotlinDemoRoom =
            Room
                .databaseBuilder(
                    context.applicationContext,
                    KotlinDemoRoom::class.java,
                    "kotlin_demo.db"
                )
                .fallbackToDestructiveMigration()
                .build()
    }

}
