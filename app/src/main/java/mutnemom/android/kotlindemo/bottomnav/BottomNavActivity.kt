package mutnemom.android.kotlindemo.bottomnav

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import mutnemom.android.kotlindemo.R
import mutnemom.android.kotlindemo.databinding.ActivityBottomNavBinding

/**
 * See [Medium](https://proandroiddev.com/fragments-swapping-with-bottom-bar-ffbd265bd742)
 */
class BottomNavActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBottomNavBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBottomNavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications
            )
        )

        findNavController(R.id.nav_host_fragment).apply {
            setupActionBarWithNavController(this, appBarConfiguration)
            binding.bottomNav.setupWithNavController(this)
        }
    }
}
