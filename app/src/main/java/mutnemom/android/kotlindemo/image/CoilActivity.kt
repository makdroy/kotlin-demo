package mutnemom.android.kotlindemo.image

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import mutnemom.android.kotlindemo.R
import mutnemom.android.kotlindemo.databinding.ActivityCoilBinding

class CoilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCoilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCoilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgRoundedCorner.load("https://homepages.cae.wisc.edu/~ece533/images/girl.png") {
            crossfade(true)
            placeholder(R.drawable.img_placeholder)
            transformations(RoundedCornersTransformation(12f))
        }

        binding.imgCircleCrop.load("https://homepages.cae.wisc.edu/~ece533/images/girl.png") {
            crossfade(true)
            placeholder(R.drawable.img_placeholder)
            transformations(CircleCropTransformation())
        }

        binding.imgRoundedCornerResource.load(R.drawable.sample) {
            crossfade(true)
            placeholder(R.drawable.img_placeholder)
            transformations(RoundedCornersTransformation(5f))
        }
    }

}
