package mutnemom.android.kotlindemo.animations.transitions

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.TransitionManager
import kotlinx.android.synthetic.main.activity_transitions.*
import mutnemom.android.kotlindemo.R

class TransitionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transitions)
        setEvent()
    }

    private fun setEvent() {
        containerFirst?.let { parent ->
            btnFirst.setOnClickListener {
                TransitionManager.beginDelayedTransition(parent)
                txtFirst?.apply {
                    visibility = when (visibility) {
                        View.VISIBLE -> View.GONE
                        else -> View.VISIBLE
                    }
                }
            }
        }
    }

}
