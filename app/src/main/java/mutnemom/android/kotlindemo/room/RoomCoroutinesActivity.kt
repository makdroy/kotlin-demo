package mutnemom.android.kotlindemo.room

import android.os.Bundle
import android.widget.EditText
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
        btnInsert?.setOnClickListener { insertMember(editMemberName) }
        btnDelete?.setOnClickListener { deleteMember(editMemberName) }
    }

    private fun deleteMember(editText: EditText?) {
        editText?.apply {
            text?.toString()?.let { roomCoroutinesVm.delete(it) }
            text?.clear()
        }
    }

    private fun insertMember(editText: EditText?) {
        editText?.apply {
            text?.toString()?.let { roomCoroutinesVm.insert(Member(name = it)) }
            text?.clear()
        }
    }

    private fun setupViewModel() {
        roomCoroutinesVm = ViewModelProvider(this, RoomCoroutinesViewModelFactory(application))
            .get(RoomCoroutinesViewModel::class.java)

        roomCoroutinesVm.allMembers.observe(this, Observer { list ->
            list?.let { memberListAdapter.setMemberList(it) }
        })
    }

}
