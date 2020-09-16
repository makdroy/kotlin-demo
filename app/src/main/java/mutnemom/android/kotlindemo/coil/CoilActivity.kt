package mutnemom.android.kotlindemo.coil

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import kotlinx.android.synthetic.main.activity_coil.*
import mutnemom.android.kotlindemo.R

class CoilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coil)

        imgRoundedCorner?.load("https://homepages.cae.wisc.edu/~ece533/images/girl.png") {
            crossfade(true)
            placeholder(R.drawable.img_placeholder)
            transformations(RoundedCornersTransformation(12f))
        }

        imgCircleCrop?.load("https://homepages.cae.wisc.edu/~ece533/images/girl.png") {
            crossfade(true)
            placeholder(R.drawable.img_placeholder)
            transformations(CircleCropTransformation())
        }

        imgRoundedCornerResource?.load(R.drawable.sample) {
            crossfade(true)
            placeholder(R.drawable.img_placeholder)
            transformations(RoundedCornersTransformation(5f))
        }
    }

}

