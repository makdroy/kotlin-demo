package mutnemom.android.kotlindemo

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import mutnemom.android.kotlindemo.databinding.ActivityTabLayoutDemoBinding
import mutnemom.android.kotlindemo.extensions.toast

class TabLayoutDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTabLayoutDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTabLayoutDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTabIndicator()
        setupTabDisplayMode()
    }

    private fun setupTabIndicator() {
        val names = listOf("one", "two", "three", "four", "five")
        for (i in 0 until 3) {
            val tab = binding.tabLayoutIndicator.newTab()
            tab.text = names.getOrNull(i)
            binding.tabLayoutIndicator.addTab(tab)
        }

        binding.tabLayoutIndicator.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                toast("-> show tab ${tab?.text}")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                toast("-> unselected tab ${tab?.text}")
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                toast("-> reselected tab ${tab?.text}")
            }
        })
    }

    private fun setupTabDisplayMode() {
        val names = listOf("one", "two", "three", "four", "five")
        for (i in 0 until 3) {
            val tab = binding.tabLayoutDisplayMode.newTab()
            tab.text = names.getOrNull(i)
            binding.tabLayoutDisplayMode.addTab(tab)
        }

        val layout = binding.tabLayoutDisplayMode.getChildAt(0) as? LinearLayout
        layout?.apply {
            showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
            dividerPadding = 28
            dividerDrawable = ContextCompat.getDrawable(
                this@TabLayoutDemoActivity,
                R.drawable.divider_vertical
            )
        }
    }

}
