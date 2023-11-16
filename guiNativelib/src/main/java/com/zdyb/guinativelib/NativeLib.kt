package com.zdyb.guinativelib

class NativeLib {

    /**
     * A native method that is implemented by the 'guinativelib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'guinativelib' library on application startup.
        init {
            System.loadLibrary("guinativelib")
        }
    }
}