package com.jhoonpark.app.photoviewer.presentation.view.component

import android.util.Log
import android.view.View

class OnThrottleClickListener(
    private val clickListener: View.OnClickListener,
    private val interval: Long,
) : View.OnClickListener {
    companion object {
        const val TAG = "OnThrottleClickListener"
    }

    var clickable = true
        private set

    override fun onClick(v: View?) {
        if (clickable) {
            clickable = false
            v?.run {
                postDelayed({
                    clickable = true
                }, interval)
                clickListener.onClick(v)
            }
        } else {
            Log.d(TAG, "waiting for a while")
        }
    }
}

fun View.onThrottleClick(interval: Long = 500, action: (v: View) -> Unit) {
    val listener = View.OnClickListener { action(it) }
    setOnClickListener(OnThrottleClickListener(listener, interval))
}