@file:Suppress("PackageName")

package mutnemom.android.kotlindemo.recyclerview

import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import mutnemom.android.kotlindemo.databinding.LayoutEmptyListBinding

class EmptyListViewHolder(
    private val binding: LayoutEmptyListBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun setData(@DrawableRes icon: Int) {
        binding.txtPlaceholder
            .setCompoundDrawablesWithIntrinsicBounds(0, icon, 0, 0)
    }

}
