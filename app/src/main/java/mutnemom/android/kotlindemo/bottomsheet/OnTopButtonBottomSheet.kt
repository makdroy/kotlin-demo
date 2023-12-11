package mutnemom.android.kotlindemo.bottomsheet

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import mutnemom.android.kotlindemo.R

class OnTopButtonBottomSheet : BottomSheetDialogFragment() {

    private var mFrameButton: FrameLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_on_top_button_bottom_sheet, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        sheet.setOnShowListener { d ->
            (d as? BottomSheetDialog)
                ?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                ?.also { sheet ->
                    mFrameButton = sheet.findViewById(R.id.frameStickyButton)

                    sheet.layoutParams?.height = Resources.getSystem().displayMetrics.heightPixels
                    BottomSheetBehavior.from(sheet).apply {
                        skipCollapsed = false
                        isHideable = true
                        peekHeight = (sheet.layoutParams.height * .6f).toInt()
                        state = BottomSheetBehavior.STATE_COLLAPSED

                        val yAxisInCollapseState = peekHeight - (mFrameButton?.height ?: 0)
                        val restOffPeekHeight = sheet.height - peekHeight

                        addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                            override fun onStateChanged(bottomSheet: View, newState: Int) {
                            }

                            override fun onSlide(v: View, slideOffset: Float) {
                                val positionY = if (slideOffset > 0) {
                                    (restOffPeekHeight * slideOffset) + yAxisInCollapseState
                                } else {
                                    yAxisInCollapseState.toFloat()
                                }

                                mFrameButton?.animate()
                                    ?.y(positionY)
                                    ?.setDuration(0)
                                    ?.start()
                            }
                        })

                        mFrameButton?.animate()
                            ?.y(yAxisInCollapseState.toFloat())
                            ?.setDuration(0)
                            ?.start()
                    }
                }
        }

        return sheet
    }

}
