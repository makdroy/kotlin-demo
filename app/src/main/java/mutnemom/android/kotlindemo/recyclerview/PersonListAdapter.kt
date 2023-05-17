package mutnemom.android.kotlindemo.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import mutnemom.android.kotlindemo.databinding.LayoutPersonListItemBinding

class PersonListAdapter(
    _list: List<Person> = emptyList()
) : ListAdapter<Person, PersonListItem>(callback) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonListItem =
        LayoutInflater.from(parent.context)
            .let { LayoutPersonListItemBinding.inflate(it, parent, false) }
            .let { PersonListItem(it) }

    override fun onBindViewHolder(holder: PersonListItem, position: Int) {
        (holder as? PersonListItem)?.bind(getItem(position))
    }

}
