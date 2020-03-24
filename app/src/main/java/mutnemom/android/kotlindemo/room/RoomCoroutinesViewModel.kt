package mutnemom.android.kotlindemo.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RoomCoroutinesViewModel(application: Application) : AndroidViewModel(application) {

    private val memberRepo: MemberRepository
    val allMembers: LiveData<List<Member>>

    init {
        val memberDao = KotlinDemoRoom.getRoomInstance(application).memberDao()
        memberRepo = MemberRepository(memberDao)
        allMembers = memberRepo.members
    }

    fun insert(member: Member) = viewModelScope.launch { memberRepo.insert(member) }
}
