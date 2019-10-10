package mutnemom.android.kotlindemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_recycler_view.*
import mutnemom.android.kotlindemo.model.Product

class RecyclerViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)

        val products = arrayListOf<Product>()
        for (i in 0..100) {
            products.add(Product("Organic Apple", "http://via.placeholder.com/200x200", 1.99))
        }

        recycler_view_recycler.apply {
            layoutManager = LinearLayoutManager(this@RecyclerViewActivity)
            adapter = ProductsAdapter(products)
        }
    }
}
