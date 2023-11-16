package com.zdyb.lib_common.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class MyActivityContract<T>(var intent: Intent) : ActivityResultContract<String, T?>(){


    companion object{
        const val RESULT = "result"
    }

    override fun createIntent(context: Context, input: String): Intent {
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): T? {
        val result = intent?.getSerializableExtra(RESULT) as T
        if (resultCode == Activity.RESULT_OK){
            return result
        }

        return null
    }



}