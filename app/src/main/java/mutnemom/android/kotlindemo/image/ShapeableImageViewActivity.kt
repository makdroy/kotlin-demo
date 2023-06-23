package mutnemom.android.kotlindemo.image

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import mutnemom.android.kotlindemo.R
import mutnemom.android.kotlindemo.databinding.ActivityShapeableImageViewBinding

class ShapeableImageViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShapeableImageViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityShapeableImageViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgRoundedCornerTopLeft.load("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcShI-6CDnEvavbDIoaHElWYi9lX6f65ad7XP-97zYj2Kvyvv_GWVA48RsgRwq_1j5VJMD8&usqp=CAU") {
            crossfade(true)
            placeholder(R.drawable.img_placeholder)
        }

    }

}
