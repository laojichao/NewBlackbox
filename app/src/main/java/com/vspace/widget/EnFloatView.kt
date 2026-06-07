package com.vspace.widget

import android.content.Context
import android.view.MotionEvent
import com.imuxuan.floatingview.FloatingMagnetView
import com.vspace.R

/**
 * Custom floating view that wraps a [RockerView] inside a draggable [FloatingMagnetView].
 *
 * Disables the rocker's movement while the floating view itself is being dragged
 * (ACTION_DOWN) and re-enables it when the touch is released (ACTION_UP).
 *
 * @param mContext the [Context] used to inflate the layout and initialize the floating view.
 */
class EnFloatView(mContext: Context) : FloatingMagnetView(mContext) {
    private var rockerView: RockerView? = null

    init {
        inflate(mContext, R.layout.view_float_rocker, this)
        initRockerView()
    }

    /**
     * Finds and caches the [RockerView] child widget.
     */
    private fun initRockerView() {
        rockerView = findViewById(R.id.rocker)
    }

    /**
     * Intercepts touch events to disable the rocker's movement while the floating
     * view is being dragged, preventing conflicting gestures between the two controls.
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            rockerView?.setCanMove(false)
        } else if (event?.action == MotionEvent.ACTION_UP) {
            rockerView?.setCanMove(true)
        }
        return super.onTouchEvent(event)
    }
}
