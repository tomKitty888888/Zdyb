package com.zdyb.module_diagnosis.help

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.util.Log
import com.blankj.utilcode.util.ToastUtils
import com.tarek360.instacapture.Instacapture
import com.tarek360.instacapture.listener.SimpleScreenCapturingListener
import com.zdyb.lib_common.base.KLog
import com.zdyb.lib_common.utils.PathManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import io.reactivex.rxjava3.core.Observable

object CaptureHelp {

    var mDisposable : Disposable? = null
    fun capture(activity:Activity){

        mDisposable?.dispose()
        mDisposable =
            io.reactivex.Observable.create<String> {obit ->
                Instacapture.capture(activity, object : SimpleScreenCapturingListener() {
                    override fun onCaptureComplete(bitmap: Bitmap) {
                        //Your code here..
                        saveScreenshotToPicturesFolder(activity,bitmap, Consumer {
                            if (it){
                                KLog.i("截图成功")
                                obit.onNext("截图成功")
                            }
                        })
                    }
                })

            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    ToastUtils.showShort(it)
                },{
                    it.printStackTrace()
                })



    }

    fun saveScreenshotToPicturesFolder(context: Context?, image: Bitmap,consumer : Consumer<Boolean>) {
        // File bitmapFile = getOutputMediaFile(filename,baseFolder);
        val timeStamp = SimpleDateFormat("ddMMyyyy_HHmmss").format(Date())

        val bitmapFile: File = File(PathManager.getScreenshots() + timeStamp + ".png")

        // FileUtils.createFolder(baseFolder);
        try {
            FileUtils.touch(bitmapFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            val fos = FileOutputStream(bitmapFile)
            image.compress(Bitmap.CompressFormat.PNG, 90, fos)
            fos.close()

            // Initiate media scanning to make the image available in gallery apps
            MediaScannerConnection.scanFile(
                context,
                arrayOf(bitmapFile.path),
                arrayOf("image/jpeg"),
                null
            )
            consumer.accept(true)
        } catch (e: FileNotFoundException) {
            Log.d(ContentValues.TAG, "File not found: " + e.message)
        } catch (e: IOException) {
            Log.d(ContentValues.TAG, "Error accessing file: " + e.message)
        }
    }
}