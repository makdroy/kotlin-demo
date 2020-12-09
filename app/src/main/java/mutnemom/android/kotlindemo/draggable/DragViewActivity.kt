package mutnemom.android.kotlindemo.draggable

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import mutnemom.android.kotlindemo.databinding.ActivityDragViewBinding

class DragViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDragViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDragViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgDraggable.apply { setOnTouchListener(OnDragTouchListener(this)) }
    }

}
