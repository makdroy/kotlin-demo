package mutnemom.android.kotlindemo.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import mutnemom.android.kotlindemo.R
import mutnemom.android.kotlindemo.databinding.LayoutEmptyListBinding
import mutnemom.android.kotlindemo.databinding.LayoutPersonListItemBinding

class PersonListAdapter(
    _list: List<Person> = emptyList()
) : ListAdapter<Person, RecyclerView.ViewHolder>(callback) {

    companion object {
        val callback = object : DiffUtil.ItemCallback<Person>() {
            override fun areContentsTheSame(oldItem: Person, newItem: Person): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: Person, newItem: Person): Boolean =
                oldItem.email == newItem.email
        }
    }

    var list: List<Person>
        get() = currentList
        set(value) {
            submitList(value)
        }

    init {
        list = _list
    }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).email == "--") 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            1 -> LayoutPersonListItemBinding
                .inflate(inflater, parent, false)
                .let { PersonListItem(it) }

            else -> LayoutEmptyListBinding
                .inflate(inflater, parent, false)
                .let { EmptyListViewHolder(it) }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0 && getItemViewType(position) == 0) {
            (holder as? EmptyListViewHolder)
                ?.setData(R.drawable.img_cross_fade_1)

        } else {
            (holder as? PersonListItem)
                ?.bind(getItem(position))
        }
    }

}
