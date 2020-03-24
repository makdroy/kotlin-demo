package mutnemom.android.kotlindemo.room

import androidx.lifecycle.LiveData

class MemberRepository(private val memberDao: MemberDao) {

    val members: LiveData<List<Member>> = memberDao.getMembers()

    suspend fun insert(member: Member) {
        memberDao.insertMember(member)
    }

    suspend fun update(member: Member) {
        memberDao.updateMember(member)
    }

    suspend fun delete(member: Member) {
        memberDao.deleteMember(member)
    }

    suspend fun deleteAll() {
        memberDao.deleteMembers()
    }

    suspend fun setLoggedInMember(member: Member) {
        memberDao.setLoggedInMember(member)
    }

}
