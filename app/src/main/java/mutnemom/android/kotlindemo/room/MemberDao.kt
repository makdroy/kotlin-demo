package mutnemom.android.kotlindemo.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MemberDao {

    @Transaction
    @Query("SELECT * FROM members ORDER BY id DESC")
    fun getMembers(): LiveData<List<Member>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMember(member: Member)

    @Update
    fun updateMember(member: Member)

    @Delete
    fun deleteMember(member: Member)

    @Query("DELETE FROM members")
    fun deleteMembers()

    @Transaction
    fun setLoggedInMember(loggedInMember: Member) {
        deleteMember(loggedInMember)
        insertMember(loggedInMember)
    }

}
