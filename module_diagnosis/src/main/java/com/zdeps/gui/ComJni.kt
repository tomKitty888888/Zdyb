package com.zdeps.gui

import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.zdyb.ITaskCallback
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.KLog
import com.zdyb.module_diagnosis.service.DiagnosisService
import java.io.UnsupportedEncodingException
import kotlin.experimental.and

object ComJni {

    private lateinit var soPath: ByteArray
    private var currentPath: String? = null

    init {

    }
    fun init():ComJni{
        return this
    }
    private lateinit var mITaskCallback :ITaskCallback

    fun registerCallback(iTaskCallback: ITaskCallback){
        mITaskCallback = iTaskCallback
    }


    fun setExePath(path:String){
        soPath = path.toByteArray()
    }

    ///storage/emulated/0/zdeps/Diagnosis/Electronic/05Cummins/V4.008
    fun setCurrentPath(name:String){
        currentPath = name
        Log.v("ComJni", "setCurrentPath $name")
    }

    fun getExePath(): ByteArray {
        Log.v("ComJni", "GetAbsolutePath " + String(soPath))
        return soPath
    }

    /**
     * jni
     */
    fun getCurrentPath():ByteArray{
        println("----ComJni----->getCurrentPath")
        return currentPath?.trim()!!.toByteArray()
    }

    external fun nativeSetup()

    external fun getGUIBuf(offset: Int, len: Int): Boolean

    external fun getCOMBuf(offset: Int, len: Int): Boolean

    external fun setGUIBuf(offset: Int, dat: ByteArray?, len: Int): Boolean

    external fun setCOMBuf(offset: Int, dat: ByteArray?, len: Int): Boolean

    /**
     * c++ 反射调用 获取产品类型
     * @return
     */
    fun getProductType(): Int {

        val packageName = BaseApplication.getInstance().packageName
        println("packageName=$packageName")
        when(packageName){
            "com.zdeps.app2" -> { return 0x10003} //1018
            "com.zdeps.eps018" -> { return 0x10100} //农机
            "com.zdeps.dfle" -> {} //暂无指定
        }
        return 0x0
    }

    private var rxGUIData: ByteArray  = ByteArray(1024)

    fun genGUIBuf(data: ByteArray): Boolean {
        rxGUIData = ByteArray(data.size)
        System.arraycopy(data, 0, rxGUIData, 0, data.size)
        return true
    }
    private fun getGUIData(offset:Int, len:Int): ByteArray {
        if (!getGUIBuf(offset, len)) null
        return rxGUIData
    }

    fun PurgeComm(){
        println("----ComJni----->PurgeComm")
        DiagnosisService.mTransfer?.purge()
    }

    private fun send(dat: ByteArray, len: Int):Int {
        //println("----ComJni----->send=${android.os.Process.myPid()}") //进程切换
//        if (BaseApplication.usbConn != null) {
//            return BaseApplication.usbConn.sendData(dat)
//        }
        println("----ComJni----->send")
        var length = 0
        length = DiagnosisService.mTransfer?.sendData(dat, len)!!

        return length
    }
    private fun read(retlen:Int):ByteArray?{
        println("----ComJni----->read")
//        if (BaseApplication.usbConn != null) {
//            return BaseApplication.usbConn.readData(retlen)
//        }

        return DiagnosisService.mTransfer?.recvData(retlen)
    }

    /**
     * jni
     */
    @Synchronized
    fun sendData(dat: ByteArray, len: Int): Int {
        var res = 0
        res = send(dat,len)
        return res
    }

    /**
     * jni
     */
    fun recvData(retlen : Int):ByteArray {
        println("读数据长度 int=$retlen")

        val data = read(retlen)

        if (data != null){
            println("jni读串口得到的值="+ConvertUtils.bytes2HexString(data))
            return data
        }else{
            println("值为null??")
        }

        return ByteArray(0)
        //return read(temp) ?: return ByteArray(0)
    }

    fun setTaskID(taskID: Long): Boolean {
        KLog.i("ComJni", "gettaskid " + String.format("%x", taskID))
        val dat: ByteArray = byteArrayOf(
            0x55,
            (0x02 and 0xff).toByte(),
            (0x01 and 0xff).toByte(),
            0x10, ((taskID shr 24) and 0xffL).toByte(),
            ((taskID shr 16) and 0xffL).toByte(),
            ((taskID shr 8) and 0xffL).toByte(),
            (taskID and 0xffL).toByte())
        return setGUIBuf(0, dat, dat.size)
    }

    /**
     * jni dvi数据库加载完成
     */
    fun LoadVdiStatusCallBack(flag :Boolean){
        println("----ComJni----->LoadVdiStatusCallBack--flag=${flag}")
    }

    fun IsWiredOrBluetooth():Int{
        //0:有线连接 1:蓝牙连接 2:未知连接

        return 0
    }



















    fun bytes2Str(dat: ByteArray): String {
        try {
            return String(dat, charset("GB2312"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return ""
    }


    fun getOC16(dat: ByteArray, pos: Int): Int {
        var ret = (dat[pos].toInt() and 0xff) * 256
        ret = ret or (dat[pos + 1].toInt() and 0xff)
        return ret
    }

    fun getOC32(dat: ByteArray, pos: Int): Long {
        var ret = dat[pos].toLong() shl 24
        ret = ret or (dat[pos + 1].toLong() shl 16)
        ret = ret or (dat[pos + 2].toLong() shl 8)
        ret = ret or (dat[pos + 3].toInt() and 0xff).toLong()
        return ret
    }

    fun splitStr(strings: String): Array<String> {
        var strings = strings
        KLog.d("ComJni", "splitStr $strings")
        val str: MutableList<String> = ArrayList()
        while (true) {
            if (TextUtils.isEmpty(strings)) break
            val index = strings.indexOf("@*@")
            strings = if (index < 0) {
                break
            } else if (index == 0) {
                strings.substring(index + 3)
            } else {
                str.add(strings.substring(0, index))
                strings.substring(index + 3)
            }
        }
        if (strings != "") str.add(strings)
        return str.toTypedArray()
    }

    fun SendMessage(msg: Long, wParam: Long, lParam: Long): Long{
        KLog.i("-------SendMessage-------wParam=${java.lang.Long.toHexString(wParam)},lParam=${java.lang.Long.toHexString(lParam)}")

        when(wParam){
            CMD.PARAM_GUI ->{
                val data: ByteArray = getGUIData(0, 18)
                if (data.isEmpty())return 0
                val type = data[2]  //类型
                val sType = data[3] //子类型
                KLog.i("type=${type},sType=${sType}")
                when(type){
                    CMD.FORM_GUI_OPEN ->{
                        KLog.d("GUI 已开启")
                    }
                    CMD.FORM_GUI_CLOSE ->{
                        KLog.d("GUI 已关闭")
                    }
                    CMD.FORM_QUIT ->{
                        KLog.d("GUI 退出诊断程序")
                    }
                    CMD.FORM_COMMAND ->{
                        KLog.d("GUI 与下位机进行通信")
                    }
                    CMD.FORM_REFALSE_DATA_DOWNLOAD ->{
                        KLog.d("GUI 刷写数据在线下载相关")
                        val pTypeLen = getOC32(getGUIData(7, 4), 0).toInt()
                        val pType = bytes2Str(getGUIData(11, pTypeLen))
                        val pFileNameLen = getOC32(getGUIData(11 + pTypeLen, 4), 0).toInt()
                        val pFileName = bytes2Str(getGUIData(15 + pTypeLen, pFileNameLen))
                        val szLocalPathLen = getOC32(getGUIData(15 + pTypeLen + pFileNameLen, 4), 0).toInt()
                        val szLocalPath = bytes2Str(getGUIData(19 + pTypeLen + pFileNameLen, szLocalPathLen))
//                        DiagnosisService.mITaskCallback.ReflashDataDownload(
//                            pType,
//                            pFileName,
//                            szLocalPath
//                        )
                    }
                    CMD.FORM_MENU_EX ->{
                        KLog.d("GUI EX菜单相关")
                        when(sType){
                            CMD.FORM_DATA_INIT ->{
                                KLog.d("GUI 菜单初始化")
                                val wMenuNumLen = getOC16(data, 4)
                                KLog.d("GUI 菜单列表数量=$wMenuNumLen")
                            }
                            CMD.FORM_DATA_ADD ->{
                                KLog.d("GUI 菜单添加")
                                var pszMenu = ""
                                var nCtrlFlag: Long = 0
                                val wFlagLen = getOC16(data, 4)
                                val wLen = getOC16(data, 6)
                                val szFlagBuff = getGUIData(8, wFlagLen)
                                nCtrlFlag = bytes2Str(szFlagBuff).toLong()
                                val szBuff = getGUIData(wFlagLen + 8, wLen)
                                pszMenu = bytes2Str(szBuff)
                                try {
                                    val vMenu: Array<String> = splitStr(pszMenu)
                                    for (item in vMenu){
                                        println(item)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    return 0
                                }
                            }
                            CMD.FORM_DATA_SHOW ->{
                                KLog.d("GUI 菜单显示")
                                DiagnosisService.mITaskCallback?.dataShow(CMD.FORM_MENU_EX)
                            }
                        }
                    }
                    CMD.FORM_MENU ->{
                        KLog.d("GUI 菜单相关")
                        when(sType){
                            CMD.FORM_DATA_INIT ->{
                                KLog.d("GUI 菜单初始化")
                                DiagnosisService.mITaskCallback?.dataInit(CMD.FORM_MENU)
                            }
                            CMD.FORM_DATA_ADD ->{
                                KLog.d("GUI 菜单添加")
                                val len = getOC16(data, 4)
                                val sData = getGUIData(6, len)
                                val pszMenu = bytes2Str(sData)
                                KLog.d("GUI 菜单添加--$pszMenu")
                                DiagnosisService.mITaskCallback?.addItemOne(CMD.FORM_MENU,pszMenu)
                            }
                            CMD.FORM_DATA_SHOW ->{
                                KLog.d("GUI 菜单显示")
                                DiagnosisService.mITaskCallback?.dataShow(CMD.FORM_MENU)
                            }
                        }
                    }
                    CMD.FORM_DTC ->{
                        KLog.d("GUI DTC菜单相关")
                        when(sType){
                            CMD.FORM_DATA_INIT ->{
                                KLog.d("GUI 菜单初始化")
                                DiagnosisService.mITaskCallback?.dataInit(CMD.FORM_DTC)
                            }
                            CMD.FORM_DATA_ADD ->{
                                KLog.d("GUI 菜单添加")
                                var pszId = ""
                                var pszDtc = ""
                                var pszEx = ""
                                val len1 = getOC16(data, 4)
                                var sdat = getGUIData(6, len1)
                                pszId = bytes2Str(sdat)
                                sdat = getGUIData(len1 + 7, 2)
                                var len2 = 0
                                len2 = getOC16(sdat, 0)
                                sdat = getGUIData(len1 + 9, len2)
                                pszDtc = bytes2Str(sdat)
                                sdat = getGUIData(len1 + 10 + len2, 2)
                                var len3 = 0
                                len3 = getOC16(sdat, 0)
                                sdat = getGUIData(len1 + 12 + len2, len3)
                                pszEx = bytes2Str(sdat)
                                //val id = pszId.toLong(10).toString()
                                if (TextUtils.isEmpty(pszEx))pszEx = ""
                                DiagnosisService.mITaskCallback?.addItemThree(CMD.FORM_DTC,pszId, pszDtc,pszEx)
                            }
                            CMD.FORM_DATA_ADD_DTC_ONE ->{
                                KLog.d("GUI 菜单添加-FORM_DATA_ADD_DTC_ONE")
                                var pszId = ""
                                var pszDtc = ""
                                var pszContent = ""
                                val len = getOC16(data, 4)
                                var sdat = getGUIData(6, len)
                                pszId = bytes2Str(sdat)
                                sdat = getGUIData(len + 7, 2)
                                var slen = 0
                                slen = getOC16(sdat, 0)
                                sdat = getGUIData(len + 9, slen)
                                pszDtc = bytes2Str(sdat)
                                sdat = getGUIData(len + 9 + slen, 2)
                                val clen = getOC16(sdat, 0)
                                sdat = getGUIData(len + 11 + slen, clen)
                                pszContent = bytes2Str(sdat)
                                val id = pszId.toLong(10).toString()
                                DiagnosisService.mITaskCallback?.addItemThree(CMD.FORM_DTC,id, pszDtc,pszContent)
                            }
                            CMD.FORM_DATA_SHOW ->{
                                KLog.d("GUI 菜单显示")
                                DiagnosisService.mITaskCallback?.dataShow(CMD.FORM_DTC)
                            }
                        }
                    }
                    CMD.FORM_CDS_SELECT ->{
                        KLog.d("GUI CDS数据流-查询-菜单相关")
                        when(sType){
                            CMD.FORM_DATA_INIT ->{
                                KLog.d("GUI 菜单初始化")
                                DiagnosisService.mITaskCallback?.dataInit(CMD.FORM_CDS_SELECT)
                                return 0
                            }
                            CMD.FORM_DATA_ADD ->{
                                KLog.d("GUI 菜单添加")
                                val len = getOC16(data, 4)
                                var sdat = getGUIData(6, len)
                                val pszName = bytes2Str(sdat)

                                sdat = getGUIData(len + 7, 2)
                                val slen = getOC16(sdat, 0)
                                sdat = getGUIData(len + 9, slen)
                                val pszUnit = bytes2Str(sdat)
                                DiagnosisService.mITaskCallback?.addItemTwo(CMD.FORM_CDS_SELECT,pszName,pszUnit)
                                return 0
                            }
                            CMD.FORM_CDS_SELECT_GET_ITEM ->{
                                KLog.d("GUI 获取需要读取的数据流")
                                val selectIds = DiagnosisService.mITaskCallback?.getByteData(CMD.FORM_CDS_SELECT)
                                if (null != selectIds){
                                    val tempData = ByteArray(selectIds.size+1)
                                    tempData[0] = selectIds.size.toByte()
                                    System.arraycopy(selectIds, 0, tempData, 1, selectIds.size)
                                    setGUIBuf(0x0, tempData, tempData.size)
                                }else{
                                    val tempData = ByteArray(1){0x00}
                                    setGUIBuf(0x0, tempData, tempData.size)
                                }

                            }
                            CMD.FORM_DATA_SHOW ->{
                                KLog.d("GUI 菜单显示")
                                DiagnosisService.mITaskCallback?.dataShow(CMD.FORM_CDS_SELECT)
                                return 0
                            }
                        }
                    }
                    CMD.FORM_CDS ->{
                        KLog.d("GUI CDS数据流菜单相关")
                        when(sType){
                            CMD.FORM_DATA_INIT ->{
                                KLog.d("GUI 菜单初始化--该初始化只会调用一次，后续反复添加和show")
                                DiagnosisService.mITaskCallback?.dataInit(CMD.FORM_CDS)
                                return 0
                            }
                            CMD.FORM_DATA_ADD ->{
                                KLog.d("GUI 菜单添加")
                                val wIndex: Int = data[4].toInt() and 0xff
                                val len = getOC16(data, 5)
                                var sdat = getGUIData(7, len)
                                val pszValue = bytes2Str(sdat)

                                sdat = getGUIData(len + 7, 2)
                                val slen = sdat[0] * 256 + sdat[1]
                                sdat = getGUIData(len + 9, slen)
                                val pszUnit = bytes2Str(sdat)
                                DiagnosisService.mITaskCallback?.addDataStream(CMD.FORM_CDS,wIndex,pszValue,pszUnit)
                                return 0
                            }
                            CMD.FORM_DATA_ADD_CDS_ONE ->{
                                KLog.d("GUI 菜单添加2")
                                val len = getOC16(data, 4)
                                var sdat = getGUIData(6, len)
                                val szCdsName = bytes2Str(sdat)
                                sdat = getGUIData(len + 7, 2)
                                val slen = getOC16(sdat, 0)
                                sdat = getGUIData(len + 9, slen)
                                val pszValue = bytes2Str(sdat)
                                sdat = getGUIData(len + slen + 10, 2)
                                val sslen = getOC16(sdat, 0)
                                sdat = getGUIData(len + slen + 12, sslen)
                                val pszUnit = bytes2Str(sdat)
                                DiagnosisService.mITaskCallback?.addItemThree(CMD.FORM_CDS,szCdsName,pszValue,pszUnit)
                                return 0
                            }
                            CMD.FORM_DATA_SHOW ->{
                                KLog.d("GUI 菜单显示")
                                DiagnosisService.mITaskCallback?.dataShow(CMD.FORM_CDS)
                                return 0
                            }
                        }
                    }
                    CMD.FORM_VER ->{
                        KLog.d("GUI VER菜单相关")
                        when(sType){
                            CMD.FORM_DATA_INIT ->{
                                KLog.d("GUI 菜单初始化")
                                DiagnosisService.mITaskCallback?.dataInit(CMD.FORM_VER)
                                return 0
                            }
                            CMD.FORM_DATA_ADD ->{
                                KLog.d("GUI 菜单添加")
                                val len = getOC16(data, 4)
                                var sdat = getGUIData(6, len)
                                val pszName = bytes2Str(sdat)
                                sdat = getGUIData(len + 7, 2)
                                val slen = getOC16(sdat, 0)
                                sdat = getGUIData(len + 9, slen)
                                val pszText = bytes2Str(sdat)
                                DiagnosisService.mITaskCallback?.addItemTwo(CMD.FORM_VER,pszName,pszText)
                                return 0
                            }
                            CMD.FORM_DATA_SHOW ->{
                                KLog.d("GUI 菜单显示")
                                DiagnosisService.mITaskCallback?.dataShow(CMD.FORM_VER)
                                return 0
                            }
                        }
                    }
                    CMD.FORM_ACT ->{
                        KLog.d("GUI ACT菜单相关")
                        when(sType){
                            CMD.FORM_DATA_INIT ->{
                                KLog.d("GUI 菜单初始化")
                                DiagnosisService.mITaskCallback?.dataInit(CMD.FORM_ACT)
                                return 0
                            }
                            CMD.FORM_DATA_ADD ->{
                                KLog.d("GUI 菜单添加")
                                val len = getOC16(data, 4)
                                var sdat = getGUIData(6, len)
                                val pszName = bytes2Str(sdat)

                                sdat = getGUIData(len + 7, 2)
                                val slen = getOC16(sdat, 0)
                                sdat = getGUIData(len + 9, slen)
                                val pszValue = bytes2Str(sdat)
                                sdat = getGUIData(len + slen + 10, 2)
                                val sslen = getOC16(sdat, 0)
                                sdat = getGUIData(len + slen + 12, sslen)
                                val pszUnit = bytes2Str(sdat)
                                DiagnosisService.mITaskCallback?.addItemThree(CMD.FORM_ACT,pszName,pszValue,pszUnit)
                                return 0
                            }
                            CMD.FORM_ACT_ADD_BUTTON ->{
                                KLog.d("GUI 按钮添加")
                                val len = getOC16(data, 5)
                                val sdat = getGUIData(7, len)
                                val butName = bytes2Str(sdat)
                                DiagnosisService.mITaskCallback?.addButton(CMD.FORM_ACT,butName)
                                return 0
                            }
                            CMD.FORM_ACT_ADD_PROMPT ->{
                                KLog.d("GUI 提示信息添加")
                                val len = getOC16(data, 4)
                                val sdat = getGUIData(6, len)
                                val pszText = bytes2Str(sdat)
                                DiagnosisService.mITaskCallback?.addHint(CMD.FORM_ACT,pszText)
                                return 0
                            }
                            CMD.FORM_DATA_SHOW ->{
                                KLog.d("GUI 菜单显示")
                                DiagnosisService.mITaskCallback?.dataShow(CMD.FORM_ACT)
                                return 0
                            }
                        }
                    }
                    CMD.FORM_LIST ->{
                        KLog.d("GUI LIST菜单相关")
                        when(sType){
                            CMD.FORM_DATA_INIT ->{
                                KLog.d("GUI 菜单初始化")
                            }
                            CMD.FORM_DATA_ADD ->{
                                KLog.d("GUI 菜单添加")

                            }
                            CMD.FORM_DATA_SHOW ->{
                                KLog.d("GUI 菜单显示")
                                DiagnosisService.mITaskCallback?.dataShow(CMD.FORM_LIST)
                            }
                        }
                    }
                    CMD.FORM_BAUD_RATE_CHANGE ->{
                        KLog.d("GUI 诊断系统设置波特率相关")

                    }
                    CMD.FORM_DTC_MULTI ->{
                        KLog.d("GUI DTC-多重-菜单相关")
                        when(sType){
                            CMD.FORM_DATA_INIT ->{
                                KLog.d("GUI 菜单初始化")
                            }
                            CMD.FORM_DATA_ADD ->{
                                KLog.d("GUI 菜单添加")

                            }
                            CMD.FORM_DATA_SHOW ->{
                                KLog.d("GUI 菜单显示")
                                DiagnosisService.mITaskCallback?.dataShow(CMD.FORM_DTC_MULTI)
                            }
                        }
                    }
                    CMD.FORM_MENU_CTRL ->{
                        KLog.d("GUI 可控制菜单相关")
                        when(sType){
                            CMD.FORM_DATA_INIT ->{
                                KLog.d("GUI 菜单初始化")
                            }
                            CMD.FORM_DATA_ADD ->{
                                KLog.d("GUI 菜单添加")

                            }
                            CMD.FORM_DATA_SHOW ->{
                                KLog.d("GUI 菜单显示")
                                DiagnosisService.mITaskCallback?.dataShow(CMD.FORM_MENU_CTRL)
                            }
                        }
                    }

                }
            }
            CMD.PARAM_COM ->{
                KLog.d("GUI 执行数据流----注意未实现")
//                var dat: ByteArray? = getCOMData(3, 2)
//                if (dat != null) {
//                    val len = getOC16(dat, 0)
//                    dat = getCOMData(0, len + 5)
//                }
            }
            CMD.PARAM_GUI_ROOT_PATH ->{
                KLog.d("GUI 获取root的路径")
                //get root path
                val dat: ByteArray = soPath
                val sdat = ByteArray(dat.size + 1)
                System.arraycopy(dat, 0, sdat, 0, dat.size)
                sdat[dat.size] = 0
                setGUIBuf(0, sdat, sdat.size)
            }
        }

        return 1
    }


    fun PostMessage(msg: Long,wParam: Long,lParam: Long):Long{
        KLog.d("-------PostMessage-------wParam=${java.lang.Long.toHexString(wParam)},lParam=${java.lang.Long.toHexString(lParam)}")
        KLog.d("进程id=${android.os.Process.myPid()}")
        DiagnosisService.mTransfer?.purge()
        when(wParam){
            CMD.PARAM_GUI ->{
                KLog.d("GUI PARAM_GUI")
                val data = getGUIData(0, 8)
                if (data.isEmpty())return 0
                val bMode: Byte = data[3]
                val tempPring = ByteArray(1)
                tempPring[0] = bMode
                val bModeString =  ConvertUtils.bytes2HexString(tempPring)
                println("bModeString=$bModeString")
                when(data[2]){
                    CMD.FORM_MSG ->{
                        KLog.d("GUI FORM_MSG-消息弹窗-${(bMode and 0xff.toByte())}")
                        if (bMode == CMD.MSG_MB_IMAGE){
                            KLog.d("GUI FORM_MSG-带图片的对话框")
                            val childData = getGUIData(0, 0XFFF0)
                            //带图片显示的框
                            val msgLen = getOC16(childData, 4)
                            val pathLen = getOC16(childData, 2000)
                            val msgByte = ByteArray(msgLen)
                            System.arraycopy(childData, 6, msgByte, 0, msgLen)
                            val imgByte = ByteArray(pathLen)
                            System.arraycopy(childData, 2002, imgByte, 0, pathLen)
                            val pszMessage = bytes2Str(msgByte)
                            val imagePath = bytes2Str(imgByte)
                            println("pszMessage=$pszMessage")
                            println("imagePath=$imagePath")
                        }else{
                            KLog.d("GUI FORM_MSG-文字对话框")
                            val len = getOC16(data, 4)
                            val dat1 = getGUIData(6, len)
                            val pszMessage = bytes2Str(dat1)
                            val dat2 = getGUIData(7 + len, 4)
                            val dwColor = dat2[0].toLong() shl 24 or (dat2[1].toLong() shl 16) or (dat2[2].toLong() shl 8) or data[2].toLong()
                            println("pszMessage=$pszMessage")
                            println("dwColor=$dwColor")
                            DiagnosisService.mITaskCallback?.showDialog(bMode,bMode,"",pszMessage,"",dwColor)
                            Thread.sleep(200)
                        }
                    }
                    CMD.FORM_INPUT ->{
                        KLog.d("GUI FORM_INPUT-输入框对话框弹窗-${(bMode and 0xff.toByte())}")
                        var len = getOC16(data, 4)
                        val dat1 = getGUIData(6, len)
                        val pszMsg = bytes2Str(dat1)

                        val dlen = getGUIData(2000, 2)
                        len = getOC16(dlen, 0)
                        val dat2 = getGUIData(2002, len)
                        val pszTip = bytes2Str(dat2)
                        //还有pszMin,pszMax 值没有使用到这里不做解析
                        DiagnosisService.mITaskCallback?.showDialog(CMD.FORM_INPUT,bMode,pszMsg, pszTip,"",0)
                    }
                    CMD.FORM_TITLE ->{
                        KLog.d("GUI FORM_TITLE-标题对话框弹窗")
                    }
                    CMD.FORM_FILEDIALOG ->{
                        KLog.d("GUI FORM_FILEDIALOG-文件对话框")
                        var szFilter = ""
                        var len = getOC16(data, 4)
                        val dat1 = getGUIData(6, len)
                        szFilter = bytes2Str(dat1)
                        var szPath = ""
                        val dlen = getGUIData(1000, 2)
                        len = getOC16(dlen, 0)
                        val dat2 = getGUIData(1002, len)
                        szPath = bytes2Str(dat2)
                        szPath = szPath.replace('\\', '/')
                        val bOpen = bMode > 0 // true 选择文件 false 输入文件名称

                        KLog.e("szPath=$szPath")
                        KLog.e("szFilter=$szFilter")
                        DiagnosisService.mITaskCallback?.showDialog(CMD.FORM_FILEDIALOG,bMode,szPath, szFilter,"",0)
                    }

                }
            }


            CMD.MSG_WAIT_WINDOW_CLOSE ->{
                KLog.d("GUI 消息窗口关闭")
                DiagnosisService.mITaskCallback?.destroyDialog()
                Thread.sleep(100)
            }
            CMD.PARAM_GUI_TASK_ID_BEGIN ->{
                KLog.d("GUI 任务ID开始")
            }
            CMD.PARAM_GUI_TASK_ID_END ->{
                KLog.d("GUI 任务ID结束")
                DiagnosisService.mITaskCallback?.viewFinish()
            }

        }

        return 1
    }
}