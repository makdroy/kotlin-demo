package mutnemom.android.kotlindemo.dialog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_dialog.*
import mutnemom.android.kotlindemo.R

class DialogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog)
        setEvent()
    }

    private fun setEvent() {
        btnDialogFragment?.setOnClickListener { showMultipleChoiceDialog() }
    }

    private fun showMultipleChoiceDialog() {
        MultipleChoicePopup
            .newInstance(resources.getString(R.string.txt_dialog_multiple_choice_title))
            .show(supportFragmentManager, "multiple-choice")
    }

}
