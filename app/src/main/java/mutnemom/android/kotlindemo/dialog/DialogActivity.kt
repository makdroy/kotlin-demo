package mutnemom.android.kotlindemo.dialog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import mutnemom.android.kotlindemo.R
import mutnemom.android.kotlindemo.databinding.ActivityDialogBinding

class DialogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setEvent()
    }

    private fun setEvent() {
        binding.btnDialogFragment.setOnClickListener { showMultipleChoiceDialog() }
    }

    private fun showMultipleChoiceDialog() {
        MultipleChoicePopup
            .newInstance(resources.getString(R.string.txt_dialog_multiple_choice_title))
            .show(supportFragmentManager, "multiple-choice")
    }

}
