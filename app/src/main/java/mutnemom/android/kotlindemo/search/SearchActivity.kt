package mutnemom.android.kotlindemo.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import mutnemom.android.kotlindemo.R
import mutnemom.android.kotlindemo.databinding.ActivitySearchBinding
import mutnemom.android.kotlindemo.databinding.LayoutSearchItemBinding
import java.util.*

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val searchAdapter = SearchAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.apply {
            menuInflater.inflate(R.menu.search_menu, this)
            val item = findItem(R.id.action_search)
            val searchView = item.actionView as? SearchView
            searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    searchAdapter.filter.filter(newText)
                    return false
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }
            })
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun setupRecyclerView() {
        binding.recyclerSearch.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = searchAdapter
            addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
        }
    }

    inner class SearchItem(private val binding: LayoutSearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(str: String) {
            binding.txtItemIndex.text = str
        }
    }

    inner class SearchAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

        private val indexArr = (0..16).toList().toTypedArray()
        private val itemAll = indexArr.map { "item index: ${it.inc()}" }
        private val itemShow = mutableListOf<String>().apply { addAll(itemAll) }

        private val filter = object : Filter() {
            override fun performFiltering(chars: CharSequence?): FilterResults {
                val filteredList = arrayListOf<String>()
                if (chars.toString().isEmpty()) {
                    // add all item to result if not typing yet
                    filteredList.addAll(itemAll)
                } else {
                    val lc = Locale.US
                    itemAll.forEach {
                        if (it.toLowerCase(lc).contains(chars.toString().toLowerCase(lc))) {
                            filteredList.add(it)
                        }
                    }
                }

                return FilterResults().apply { values = filteredList }
            }

            override fun publishResults(chars: CharSequence?, results: FilterResults?) {
                itemShow.clear()
                itemShow.addAll(results!!.values as Collection<String>)
                notifyDataSetChanged()
            }
        }

        override fun getItemCount(): Int = itemShow.size
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            LayoutInflater.from(parent.context).let {
                val binding = LayoutSearchItemBinding.inflate(it, parent, false)
                SearchItem(binding)
            }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is SearchItem) {
                holder.setData(itemShow[position])
            }
        }

        override fun getFilter(): Filter {
            return filter
        }
    }

}

