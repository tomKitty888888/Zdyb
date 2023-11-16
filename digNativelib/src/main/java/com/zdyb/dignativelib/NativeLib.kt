package com.zdyb.dignativelib

class NativeLib {

    /**
     * A native method that is implemented by the 'dignativelib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'dignativelib' library on application startup.
        init {
            System.loadLibrary("dignativelib")
        }
    }
}