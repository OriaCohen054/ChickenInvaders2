package com.example.chickeninvaders.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object VibrationUtil {
    fun vibrate(context: Context, duration: Long = 200L) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(duration)
            }
        }
    }
}