package com.zdeps.obd;

import android.annotation.SuppressLint;

import com.zdeps.bean.OBDBean;
import com.zdeps.gui.OBDComJni;
import com.zdyb.lib_common.base.KLog;
import com.zdyb.lib_common.bluetooth.BluetoothManager;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.functions.Consumer;

/**
 * data/日期 2019-10-15
 * user/人员 谢俊平
 * desc/描述 （初版）封装comjni类，优化数据发送和接受。
 * * data/日期 2019-12-5
 * user/人员 谢俊平
 * desc/描述 过滤1939广播数据
 */
@SuppressLint("HandlerLeak")
public class DiagAbs implements DiagInface {

    private static DiagAbs diagAbs;

    private static boolean rtinfoFlag = false;

    public DiagAbs() {
        OBDComJni.INSTANCE.init();
    }

    public static DiagAbs getInstance(){
        if (diagAbs == null){
            diagAbs = new DiagAbs();
        }
        return diagAbs;
    }

    private String protocol = "";
    private String J1939 = "08";//j1939 广播过滤。
    private String protocolDom = "Protocol";

    protected ExecutorService threadPoolExecutor = Executors.newSingleThreadExecutor();

    private static String GB2312 = "GB2312";


    @Override
    public int commInit() {
        return OBDComJni.INSTANCE.CommInit();
    }

    @Override
    public byte[] recvData(int retlen) {

        return BluetoothManager.INSTANCE.readData(retlen);
    }

    @Override
    public int sendData(byte[] dat) {

        BluetoothManager.INSTANCE.send(dat);
        KLog.i("send:" + bytesToHexString(dat));
        return dat.length;
    }

    @Override
    public void purgeData() {
        //diagPurgeData();
        BluetoothManager.INSTANCE.purgeData();
    }


    /**
     * 异步返回String字符串
     * @param data
     * @return
     */
    public DiagAbs DiagRequestByXMLString(final String data, Consumer<OBDBean> consumer) {

//        threadPoolExecutor.submit(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });


        try {
            KLog.d("DiagAbs",data);
            StringBuffer sendData = new StringBuffer();
            sendData.append("ZDEPS").append(String.format("%06d", data.length() + 12)).append(data).append("#");
            //获取jni返回的数据
            byte[] resultByte = OBDComJni.INSTANCE.RequestByXMLString(sendData.toString());
            if (resultByte != null) {
                String resultData = new String(resultByte, "GB2312");
                resultData = resultData.substring(resultData.indexOf("<"), resultData.length() - 1);

                //解析xml
                handleInceptLogData(resultData);
                consumer.accept(parse(resultData));
            } else {
                throw new Exception("resultByte is null");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }



    /**
     * 关闭正在执行的线程
     */
    public void shutdownThreadPool() {
        threadPoolExecutor.shutdown();
    }


    public static String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = String.format("%02x ", bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }


    /**
     * @param inceptData
     */
    public void handleInceptLogData(String inceptData) {

        if (inceptData.contains(protocolDom)) {
            protocol =  inceptData.substring(inceptData.indexOf(protocolDom)+protocolDom.length()+1, inceptData.lastIndexOf(protocolDom)-2);
            System.out.println("protocolDom:"+protocol);
            if (protocol.equals(J1939)) {
                rtinfoFlag = true;
            }else {
                rtinfoFlag = false;
            }
        }
        KLog.i(inceptData);
    }

    private OBDBean parse(String xml){
        OBDBean obdBean = new OBDBean();
        try {
            org.dom4j.Document document = DocumentHelper.parseText(xml);
            Element rootElement = document.getRootElement();
            Element cmd = rootElement.element("cmd");
            Element code = rootElement.element("code");
            Element msg = rootElement.element("msg");

            System.out.println("cmd="+cmd.getData().toString() +"     code="+code.getData().toString() +"    msg="+msg.getData().toString());

            obdBean.setCmd(cmd.getData().toString());
            obdBean.setCode(code.getData().toString());
            obdBean.setMsg(msg.getData().toString());
            List<OBDBean.ObdData> obdDataList = new ArrayList<>();
            Element data = rootElement.element("data");
            if (data == null){
                return obdBean;
            }
            for (Iterator i = data.elementIterator(); i.hasNext();){
                Element next = (Element) i.next();
                String name = next.getName();

                if (obdBean.cmd.equals("GetDTCInfo")){ //取故障中屏蔽掉,以下都是负责人周江要求屏蔽的字段
                    if ("Unsettled".equals(name)
                            || name.contains("UnsetDTC")
                            || name.contains("UnsetDetail")
                            || "Mileage".equals(name)
                            || "MILTime".equals(name)){
                        //println("屏蔽掉未决故障代码数量，只显示当前故障和永久故障")
                        continue;
                    }
                }else if (obdBean.cmd.equals("GetSystemCheckState")){ //就绪状态中屏蔽DISD
                    if ("DISD".equals(name)){
                        continue;
                    }
                }else if (obdBean.cmd.equals("GetCarInfo")){ //取车辆信息 屏蔽VINHex  OBDCode
                    if ("VINHex".equals(name) || "OBDCode".equals(name)){
                        continue;
                    }
                }
                obdDataList.add(new OBDBean.ObdData(next.getName(),next.getData(),OBDCmd.INSTANCE.description(next.getName()),OBDCmd.INSTANCE.getUnit(next.getName())));
            }
            obdBean.setData(obdDataList);

        }catch (DocumentException e){
            e.printStackTrace();
            String cmdString = "cmd";
            if (xml.contains(cmdString)){
                String cmdData =  xml.substring(xml.indexOf(cmdString)+cmdString.length()+1, xml.lastIndexOf(cmdString)-2);
                obdBean.setMsg("parse err:"+cmdData);
            }

        }
        return obdBean;
    }


}
