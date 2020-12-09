package mutnemom.android.kotlindemo.room

import androidx.recyclerview.widget.RecyclerView
import mutnemom.android.kotlindemo.databinding.MemberItemBinding

class MemberListViewHolder(private val binding: MemberItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun setData(member: Member) {
        binding.txtId.text = member.id.toString()
        binding.txtName.text = member.name
    }

}
