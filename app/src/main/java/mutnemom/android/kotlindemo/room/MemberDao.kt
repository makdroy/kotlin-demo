package mutnemom.android.kotlindemo.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MemberDao {

    @Query("SELECT * FROM members ORDER BY id DESC")
    fun getMembers(): LiveData<List<Member>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member)

    @Update
    suspend fun updateMember(member: Member)

    @Delete
    suspend fun deleteMember(member: Member)

    @Query("DELETE FROM members")
    suspend fun deleteMembers()

    @Transaction
    suspend fun setLoggedInMember(loggedInMember: Member) {
        deleteMember(loggedInMember)
        insertMember(loggedInMember)
    }

}
