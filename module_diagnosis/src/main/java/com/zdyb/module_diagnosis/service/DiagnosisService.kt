package com.zdyb.module_diagnosis.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.zdeps.diag.DiagJni
import com.zdeps.gui.ComJni
import com.zdyb.ITaskBinder
import com.zdyb.ITaskCallback
import com.zdyb.app.Transfer
import com.zdyb.lib_common.base.KLog

class DiagnosisService :Service(){


    //var diagJni: DiagJni? = null
    var mServiceRunnable :ServiceRunnablee? = null


    companion object{
        var mTransfer: Transfer? = null
        var mITaskCallback: ITaskCallback? = null
        var comJni: ComJni = ComJni.init()
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    private val mBinder: ITaskBinder.Stub = object : ITaskBinder.Stub() {

        override fun registerCallback(cb: ITaskCallback) {
            mITaskCallback = cb
        }

        override fun unregisterCallback(cb: ITaskCallback) {
            mITaskCallback = null
        }

        override fun run(soLibsAbsolutePath: String, versionPath: String) {
            KLog.i("正常进入run")
            DiagJni.loadPathing = soLibsAbsolutePath
            //注意：setExePath() 只能传递：版本路径/libdiag-lib.so 否则gui中效验过不了（历史遗留问题）
            // "/storage/emulated/0/zdeps/Diagnosis/Electronic/05Cummins/V4.008/libdiag-lib.so"

            ComJni.setExePath(versionPath)
            mServiceRunnable = ServiceRunnablee(comJni,soLibsAbsolutePath)
            val thread = Thread(mServiceRunnable, "DIAGNOS")
            thread.start()
        }


        override fun registerTransferCallback(cb: Transfer) {
            mTransfer = cb
        }

        override fun unregisterTransferCallback(cb: Transfer) {
            mTransfer = null
        }

        override fun setTaskID(taskID: Long) {
            comJni.setTaskID(taskID)
        }

        /**
         * 固定偏移量为6，value：0xFF 退出， 6 查询数据流
         */
        override fun setMenuValue(value: Byte) {
            val dat = ByteArray(1)
            dat[0] = value
            comJni.setGUIBuf(6, dat, dat.size)
            println("--setMenuValue-->$value")
        }

        /**
         * 通用的设置
         * offset 偏移量，对应具体功能：
         *          3对应弹框按键;
         *          2000 输入的文件名称;
         *
         * value
         */
        override fun setCommonValue(offset: Int, value: Byte) {

            val dat = ByteArray(1)
            dat[0] = value
            comJni.setGUIBuf(offset, dat, dat.size)
            KLog.i("send---GUIBuf--->offset=$offset   value=${value}")
        }

        /**
         * 通用的设置
         * offset 偏移量，对应具体功能：
         *          6对应输入String值，备注：所有的String值都以\u0000结尾;
         *
         */
        override fun setCommonValueToArray(offset: Int, value: String) {
            val dat = value.toByteArray()
            comJni.setGUIBuf(offset, dat, dat.size)
            KLog.i("send---setCommonValueToArray--->offset=$offset   value=${value}")
        }

        override fun getProcessPid(): Int {
            KLog.i("结束DiagnosisService进程")
            return android.os.Process.myPid()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        KLog.i("DiagnosisService--->onDestroy")
    }
}