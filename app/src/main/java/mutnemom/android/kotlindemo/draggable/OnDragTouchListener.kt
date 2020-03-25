package mutnemom.android.kotlindemo.draggable

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View

class OnDragTouchListener(
    private val mView: View,
    private val parent: View = mView.parent as View,
    private val mOnDragActionListener: OnDragActionListener? = null
) : View.OnTouchListener {

    interface OnDragActionListener {
        fun onDragStart(v: View?)
        fun onDragEnd(v: View?)
    }

    private var isInitialized = false
    private var isDragging = false

    private var width = 0
    private var xWhenAttached = .0f
    private var dX = .0f

    private var height = 0
    private var yWhenAttached = .0f
    private var dY = .0f

    private var maxLeft = .0f
    private var maxRight = .0f
    private var maxBottom = .0f
    private var maxTop = .0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (isDragging) {
            val bounds = FloatArray(4)
            // LEFT
            bounds[0] = event!!.rawX + dX
            if (bounds[0] < maxLeft) {
                bounds[0] = maxLeft
            }
            // RIGHT
            bounds[2] = bounds[0] + width
            if (bounds[2] > maxRight) {
                bounds[2] = maxRight
                bounds[0] = bounds[2] - width
            }
            // TOP
            bounds[1] = event.rawY + dY
            if (bounds[1] < maxTop) {
                bounds[1] = maxTop
            }
            // BOTTOM
            bounds[3] = bounds[1] + height
            if (bounds[3] > maxBottom) {
                bounds[3] = maxBottom
                bounds[1] = bounds[3] - height
            }

            when (event.action) {
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> onDragFinish()
                MotionEvent.ACTION_MOVE -> mView.animate().x(bounds[0]).y(bounds[1]).setDuration(
                    0
                ).start()
            }

            return true
        } else {
            event?.apply {
                when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        isDragging = true
                        if (!isInitialized) {
                            updateBounds()
                        }

                        dX = v!!.x - rawX
                        dY = v.y - rawY

                        mOnDragActionListener?.onDragStart(mView)

                        return true
                    }
                }
            }
        }

        return false
    }

    private fun updateBounds() {
        updateViewBounds()
        updateParentBounds()
        isInitialized = true
    }

    private fun updateViewBounds() {
        width = mView.width
        xWhenAttached = mView.x
        dX = .0f

        height = mView.height
        yWhenAttached = mView.y
        dY = .0f
    }

    private fun updateParentBounds() {
        maxLeft = .0f
        maxRight = maxLeft + parent.width

        maxTop = .0f
        maxBottom = maxTop + parent.height
    }

    private fun onDragFinish() {
        mOnDragActionListener?.onDragEnd(mView)
        isDragging = false
        dX = .0f
        dY = .0f
    }

}
