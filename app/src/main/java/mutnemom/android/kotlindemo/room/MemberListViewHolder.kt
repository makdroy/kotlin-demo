package mutnemom.android.kotlindemo.room

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.member_item.view.*

class MemberListViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun setData(member: Member) {
        itemView.txtId?.text = member.id.toString()
        itemView.txtName?.text = member.name
    }

}
