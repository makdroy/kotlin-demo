package mutnemom.android.kotlindemo.gesture

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import mutnemom.android.kotlindemo.databinding.ActivityGestureBinding

class GestureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGestureBinding.inflate(layoutInflater)
        setContentView(MultiTouchCanvas(this))

        setEvent()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setEvent() {
        binding.touchArea.setOnTouchListener { _, _ ->
            Log.e("tt", "-> handle touch event")
            true
        }
    }

}
