package com.mgg.quantcolor

import android.os.Looper

class ColorQuant {
    companion object {
        // Used to load the 'quantcolor' library on application startup.
        init {
            System.loadLibrary("ColorQuant")
        }

        @JvmStatic
        fun getColorQuant(imagePath: String): String {
            require(Looper.getMainLooper().thread != Thread.currentThread()) {
                "currentThread is MainThread, please use it in WorkThread"
            }
            return colorQuant(imagePath)
        }

        @JvmStatic
        private external fun colorQuant(imagePath: String): String
    }
}