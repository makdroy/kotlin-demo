package mutnemom.android.kotlindemo.custom

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import mutnemom.android.kotlindemo.databinding.ActivityCustomShapeDemoBinding

/**
 * See [source](https://www.kodeco.com/9556022-drawing-custom-shapes-in-android)
 */
class CustomShapeDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomShapeDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCustomShapeDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}
