package mutnemom.android.kotlindemo.recyclerview

import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import mutnemom.android.kotlindemo.R
import mutnemom.android.kotlindemo.databinding.LayoutPersonListItemBinding

class PersonListItem(private val binding: LayoutPersonListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(data: Person) {
        with(binding) {
            txtEmail.text = data.email
            txtName.text = data.name

            imgAvatar.load(data.avatar) {
                crossfade(true)
                placeholder(R.drawable.img_placeholder)
                transformations(CircleCropTransformation())
            }
        }
    }

}
