package mutnemom.android.kotlindemo.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import mutnemom.android.kotlindemo.R
import mutnemom.android.kotlindemo.databinding.PopupMultipleChoiceBinding
import mutnemom.android.kotlindemo.extensions.toast

class MultipleChoicePopup : DialogFragment() {

    companion object {
        private const val KEY_DIALOG_TITLE = "dialog-title"

        @JvmStatic
        fun newInstance(title: String) = MultipleChoicePopup()
            .apply {
                arguments = Bundle().apply {
                    putString(KEY_DIALOG_TITLE, title)
                }
            }
    }

    private lateinit var binding: PopupMultipleChoiceBinding
    private lateinit var dialogTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialogTitle = arguments
            ?.getString(KEY_DIALOG_TITLE, "default header")
            ?: "default header"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.popup_multiple_choice, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtHeader.text = dialogTitle
        setEvent()

        dialog?.apply {
            window?.setBackgroundDrawableResource(R.drawable.bg_rounded_dialog)
        }
    }

    private fun setEvent() {
        binding.btnChoiceOne.setOnClickListener { toastMessage("ONE") }
        binding.btnChoiceTwo.setOnClickListener { toastMessage("TWO") }
    }

    private fun toastMessage(message: String) {
        if (!isAdded) return
        (requireActivity() as? AppCompatActivity)?.toast(message)
    }

}
