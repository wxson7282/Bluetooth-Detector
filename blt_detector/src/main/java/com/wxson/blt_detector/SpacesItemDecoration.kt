package com.wxson.blt_detector

import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacesItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.apply {
            bottom = space
            left = space
            right = space
            if (parent.getChildLayoutPosition(view) == 0) {
                top = space
            }
        }
    }

    companion object {
        fun px2dp(dpValue: Float): Int {
            return (0.5f + dpValue * Resources.getSystem().displayMetrics.density).toInt()
        }
    }

}