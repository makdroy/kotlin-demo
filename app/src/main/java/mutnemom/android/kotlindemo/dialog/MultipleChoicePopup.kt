package mutnemom.android.kotlindemo.dialog

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.popup_multiple_choice.*
import mutnemom.android.kotlindemo.R
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
        txtHeader?.text = dialogTitle
        setEvent()

        dialog?.apply {
//            requestWindowFeature(Window.FEATURE_NO_TITLE)
//            window?.setLayout(
//                WindowManager.LayoutParams.MATCH_PARENT,
//                WindowManager.LayoutParams.MATCH_PARENT
//            )
//
            window?.setBackgroundDrawableResource(R.drawable.bg_rounded_dialog)
//            setStyle(STYLE_NO_INPUT, android.R.style.Theme)
        }
    }

//    override fun onStart() {
//        super.onStart()
//        dialog?.window?.setLayout(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.MATCH_PARENT
//        )
//    }

    private fun setEvent() {
        btnChoiceOne?.setOnClickListener { toastMessage("ONE") }
        btnChoiceTwo?.setOnClickListener { toastMessage("TWO") }
    }

    private fun toastMessage(message: String) {
        if (!isAdded) return
        (requireActivity() as? AppCompatActivity)?.toast(message)
    }

}
