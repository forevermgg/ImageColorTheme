package com.mgg.quantcolor

class ColorQuant {
    companion object {
        // Used to load the 'quantcolor' library on application startup.
        init {
            System.loadLibrary("ColorQuant")
        }

        @JvmStatic
        external fun colorQuant(imagePath: String): String
    }
}