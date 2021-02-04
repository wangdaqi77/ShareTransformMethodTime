package com.demo.app.utils

import android.util.Log


interface MethodDuration {
    fun onBefore()
    fun onAfter()
}


class MethodDurationImpl(private val className: String, private val methodName: String):
    MethodDuration {
    private var startTime: Long = -1L
    override fun onBefore() {
        startTime = System.currentTimeMillis()
    }

    override fun onAfter() {
        val endTime = System.currentTimeMillis() - startTime
        Log.e("MethodTimeImpl", "$className#${methodName}, run total time:$endTime")
        // TODO 进行统计或者上报警告...
    }
}