package mutnemom.android.kotlindemo

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import mutnemom.android.kotlindemo.model.Product

class ProductsAdapter(private val products: ArrayList<Product>) : RecyclerView.Adapter<ProductsAdapter.ProductsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_item, parent, false)
        return ProductsViewHolder(view)
    }

    override fun getItemCount() = products.size

    override fun onBindViewHolder(holder: ProductsViewHolder, position: Int) {
        holder.title.text = products[position].title
    }


    class ProductsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageView>(R.id.product_item_image)
        val title = itemView.findViewById<TextView>(R.id.product_item_title)
    }
}
