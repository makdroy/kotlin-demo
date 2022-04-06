package mutnemom.android.kotlindemo.room

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import mutnemom.android.kotlindemo.databinding.ActivityRoomCoroutinesBinding

class RoomCoroutinesActivity : AppCompatActivity() {

    private lateinit var roomCoroutinesVm: RoomCoroutinesViewModel
    private lateinit var binding: ActivityRoomCoroutinesBinding
    private val memberListAdapter = MemberListAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRoomCoroutinesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupInputForm()
        setupViewModel()
    }

    private fun setupRecyclerView() {
        binding.recyclerMember.layoutManager = LinearLayoutManager(this)
        binding.recyclerMember.adapter = memberListAdapter
    }

    private fun setupInputForm() {
        binding.btnInsert.setOnClickListener { insertMember(binding.editMemberName) }
        binding.btnDelete.setOnClickListener { deleteMember(binding.editMemberName) }
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

        roomCoroutinesVm.allMembers.observe(this) { list ->
            list?.let { memberListAdapter.setMemberList(it) }
        }
    }

}
