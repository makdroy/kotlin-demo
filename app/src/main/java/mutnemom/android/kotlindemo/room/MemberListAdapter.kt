package mutnemom.android.kotlindemo.room

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mutnemom.android.kotlindemo.R

class MemberListAdapter(val context: Context) : RecyclerView.Adapter<MemberListViewHolder>() {

    private var members = emptyList<Member>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberListViewHolder =
        LayoutInflater.from(context).let {
            val view = it.inflate(R.layout.member_item, parent, false)
            MemberListViewHolder(view)
        }

    override fun onBindViewHolder(holder: MemberListViewHolder, position: Int) {
        holder.setData(members[position])
    }

    override fun getItemCount(): Int = members.size

    internal fun setMemberList(members: List<Member>) {
        this.members = members
        notifyDataSetChanged()
    }

}
