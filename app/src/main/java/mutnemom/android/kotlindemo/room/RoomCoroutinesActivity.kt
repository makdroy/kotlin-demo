package mutnemom.android.kotlindemo.room

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_room_coroutines.*
import mutnemom.android.kotlindemo.R

class RoomCoroutinesActivity : AppCompatActivity() {

    private lateinit var roomCoroutinesVm: RoomCoroutinesViewModel
    private val memberListAdapter = MemberListAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_coroutines)

        setupRecyclerView()
        setupInputForm()
        setupViewModel()
    }

    private fun setupRecyclerView() {
        recyclerMember?.layoutManager = LinearLayoutManager(this)
        recyclerMember?.adapter = memberListAdapter
    }

    private fun setupInputForm() {
        btnInsert?.setOnClickListener { insertMember(editMemberName?.text?.toString()) }
        btnDelete?.setOnClickListener { deleteMember(editMemberName?.text?.toString()) }
    }

    private fun deleteMember(name: String?) {
        name?.let { roomCoroutinesVm.delete(name) }
    }

    private fun insertMember(name: String?) {
        name?.let { roomCoroutinesVm.insert(Member(name = name)) }
    }

    private fun setupViewModel() {
        roomCoroutinesVm = ViewModelProvider(this, RoomCoroutinesViewModelFactory(application))
            .get(RoomCoroutinesViewModel::class.java)

        roomCoroutinesVm.allMembers.observe(this, Observer { list ->
            list?.let { memberListAdapter.setMemberList(it) }
        })
    }

}
