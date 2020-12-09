package mutnemom.android.kotlindemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import mutnemom.android.kotlindemo.databinding.ActivityRecyclerViewBinding
import mutnemom.android.kotlindemo.model.Product

class RecyclerViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecyclerViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecyclerViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val products = arrayListOf<Product>()
        for (i in 0..100) {
            products.add(Product("Organic Apple", "http://via.placeholder.com/200x200", 1.99))
        }

        binding.root.apply {
            layoutManager = LinearLayoutManager(this@RecyclerViewActivity)
            adapter = ProductsAdapter(products)
        }
    }
}
