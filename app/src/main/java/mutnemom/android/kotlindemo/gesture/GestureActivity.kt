package mutnemom.android.kotlindemo.gesture

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_gesture.*

class GestureActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_gesture)
//        setContentView(DrawingCanvas(this))
        setContentView(MultiTouchCanvas(this))

        setEvent()
    }

    private fun setEvent() {
        touchArea?.setOnTouchListener { v, event ->
            Log.e("tt", "-> handle touch event")
            true
        }
    }

}
