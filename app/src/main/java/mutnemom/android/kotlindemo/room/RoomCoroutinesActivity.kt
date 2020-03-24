package mutnemom.android.kotlindemo.room

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_room_coroutines.*
import mutnemom.android.kotlindemo.R

class RoomCoroutinesActivity : AppCompatActivity() {

    private val memberListAdapter = MemberListAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_coroutines)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        recyclerMember?.layoutManager = LinearLayoutManager(this)
        recyclerMember?.adapter = memberListAdapter
    }

}
