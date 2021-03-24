package com.example.twopager.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.OverScroller
import androidx.core.view.children
import kotlin.math.abs

class TwoPager(context: Context?, attrs: AttributeSet?) : ViewGroup(context, attrs) {

    val velocityTracker = VelocityTracker.obtain()
    val viewConfiguration = ViewConfiguration.get(context)
    val maxVelocity = viewConfiguration.scaledMaximumFlingVelocity
    val minVelocity = viewConfiguration.scaledMinimumFlingVelocity
    val pagingSlop = viewConfiguration.scaledPagingTouchSlop
    val overScroller = OverScroller(context)
    var scrolling = false
    var scrolled = 0
    var downX = 0
    var targetPage = 0
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            velocityTracker.clear()
        }
        velocityTracker.addMovement(ev)
        var result = false
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                scrolling = false
                downX = ev.x.toInt()
                scrolled = scrollX
            }
            MotionEvent.ACTION_MOVE -> {
                if (!scrolling) {
                    val dx = downX - ev.x
                    if (abs(dx) > pagingSlop) {
                        scrolling = true
                        parent.requestDisallowInterceptTouchEvent(true)
                        result = true
                    }
                }
            }

        }
        return result
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            velocityTracker.clear()
        }
        velocityTracker.addMovement(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x.toInt()
                scrolled = scrollX
            }
            MotionEvent.ACTION_MOVE -> {
                val dx =
                    (downX - event.x + scrolled).coerceAtLeast(0f).coerceAtMost(width.toFloat())
                scrollTo(dx.toInt(), 0)
            }
            MotionEvent.ACTION_UP -> {
                velocityTracker.computeCurrentVelocity(1000, maxVelocity.toFloat())
                val vx = velocityTracker.xVelocity
                val scrollX = scrollX
                targetPage = if (abs(vx) < minVelocity) {
                    if (scrollX < width / 2) {
                        0
                    } else {
                        1
                    }
                } else {
                    if (vx > 0) {
                        0
                    } else {
                        1
                    }
                }
                val distance = if (targetPage == 1) width - scrollX else -scrollX
                overScroller.startScroll(scrollX, 0, distance, 0)
                postInvalidateOnAnimation()
            }

        }
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var childLeft = 0
        var childTop = 0
        var childRight = width
        var childBottom = height
        for (child in children) {
            child.layout(childLeft, childTop, childRight, childBottom)
            childLeft += width
            childRight += width
        }
    }


    override fun computeScroll() {
        if (overScroller.computeScrollOffset()) {
            scrollTo(overScroller.currX, overScroller.currY)
            postInvalidateOnAnimation()
        }
    }
}