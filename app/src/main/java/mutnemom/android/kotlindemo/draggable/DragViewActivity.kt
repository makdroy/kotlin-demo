package mutnemom.android.kotlindemo.draggable

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_drag_view.*
import mutnemom.android.kotlindemo.R

class DragViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drag_view)

        imgDraggable?.apply { setOnTouchListener(OnDragTouchListener(this)) }
    }

}
