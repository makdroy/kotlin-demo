package mutnemom.android.kotlindemo.bottomsheet

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import mutnemom.android.kotlindemo.R

class OnTopButtonBottomSheet2 : BottomSheetDialogFragment() {

    private var buttonLayoutParams: ConstraintLayout.LayoutParams? = null
    private var collapsedMargin: Int = 0
    private var buttonHeight: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_on_top_button_bottom_sheet2, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        sheet.setOnShowListener { d ->
            (d as? BottomSheetDialog)
                ?.apply { setupRatio(this) }
        }

        return sheet
    }

    private fun setupRatio(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet = bottomSheetDialog
            .findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)

        bottomSheet!!.apply {
            val button = findViewById<Button>(R.id.btnSticky)
            buttonLayoutParams = button.layoutParams as ConstraintLayout.LayoutParams

            val sheetBehavior = BottomSheetBehavior.from(this)
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            layoutParams.height = Resources.getSystem().displayMetrics.heightPixels

            // setup bottom sheet
            sheetBehavior.skipCollapsed = false
            sheetBehavior.peekHeight = (this.layoutParams.height * .6f).toInt()
            sheetBehavior.isHideable = true

            // calculate button margin from top
            buttonHeight = button.height + 40 // bottom margin of button
            collapsedMargin = sheetBehavior.peekHeight - buttonHeight
            buttonLayoutParams!!.topMargin = collapsedMargin
            button.layoutParams = buttonLayoutParams

            // add callback
            sheetBehavior.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (slideOffset > 0) {
                        buttonLayoutParams!!.topMargin =
                            ((this@apply.layoutParams.height - sheetBehavior.peekHeight)
                                    * slideOffset + collapsedMargin).toInt()

                    } else {
                        buttonLayoutParams!!.topMargin = collapsedMargin
                    }

                    button.layoutParams = buttonLayoutParams
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                }
            })
        }
    }

}
