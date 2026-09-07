package com.ferg.awfulapp.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout

/**
 * Created by baka kaba on 15/08/2015.
 * 
 * 
 * 
 * This is a (hopefully) temporary extension of the SwipyRefreshLayout library,
 * to catch and swallow an exception that seems to be caused by an
 * [internal bug](https://code.google.com/p/android/issues/detail?id=64553).
 * 
 * 
 * 
 * When/if this is fixed, remove the same code from [com.ferg.awfulapp.SwipeLockViewPager] too thanks!
 */
class SwipyRefreshLayout : SwipyRefreshLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return antiCrashEventHandler(ev, true)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return antiCrashEventHandler(ev, false)
    }


    /**
     * Fix to avoid apparent bug in the support library, with infrequent crashing from an IAE.
     * (See [this issue](https://code.google.com/p/android/issues/detail?id=64553).)
     * 
     * @param ev           Motion event being passed
     * @param intercepting Set true when handling onInterceptTouchEvent
     * @return False if the exception was thrown, otherwise the result of the superclass call
     */
    private fun antiCrashEventHandler(ev: MotionEvent?, intercepting: Boolean): Boolean {
        var result = false
        try {
            result = if (intercepting) super.onInterceptTouchEvent(ev) else super.onTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        return result
    }
}
