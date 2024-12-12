package com.hoho.android.usbserial.driver;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.util.Log;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FtdiCustomSerialPort implements UsbSerialDriver {
   public static FT_Device ftDev;
    public static final int readLength = 512;//gxf 2023-02-03 512
    private final UsbDevice mDevice;
    private final UsbSerialPort mPort;
   public static D2xxManager instance;

    public FtdiCustomSerialPort(UsbDevice mDevice) {
        this.mDevice = mDevice;
        this.mPort = new MySerialPort();
    }


    @Override
    public UsbDevice getDevice() {
        return mDevice;
    }

    @Override
    public List<UsbSerialPort> getPorts() {
        return Collections.singletonList(mPort);
    }




    class MySerialPort implements UsbSerialPort{

//            @Override
//    public int read(byte[] dest, int timeoutMillis) throws IOException {
//        synchronized(ftDev)
//        {
//
//            // check the amount of available data
//            int   iavailable = ftDev.getQueueStatus();
//            if (iavailable > 0) {
//
//                if(iavailable > readLength){
//                    iavailable = readLength;
//                }
//                // get the data
//                int read = ftDev.read(dest, iavailable);
//                Log.v("UI1018","read  "+read+" "+SerialInputOutputManager.byteArrayToHexStr(Arrays.copyOfRange(dest,0,iavailable)));
//                return read;
//            }else {
//
//            }
//            return iavailable;
//        }
//    }
//
//    @Override
//    public int write(byte[] src, int timeoutMillis) throws IOException {
//            Log.v("UI1018","write  "+ SerialInputOutputManager.byteArrayToHexStr(src));
//            // check the amount of available data
//        int       write=0;
//        synchronized(ftDev)
//        {
//            ftDev.setLatencyTimer((byte) 16);
//            write= ftDev.write(src, src.length);
//        }
//
//
//        return write;
//    }


        @Override
        public int read(byte[] dest, int timeoutMillis) throws IOException {

            // check the amount of available data
            int   iavailable = ftDev.getQueueStatus();
            if (iavailable > 0) {

                if(iavailable > readLength){
                    iavailable = readLength;
                }
                // get the data
                int read = ftDev.read(dest, iavailable);
                //Log.v("UI1018","read  "+read+" "+SerialInputOutputManager.byteArrayToHexStr(Arrays.copyOfRange(dest,0,iavailable)));//gxf
                return read;
            }else {

            }

            return iavailable;
        }

        @Override
        public int write(byte[] src, int timeoutMillis) throws IOException {
            // check the amount of available data
            int       write=0;
            ftDev.setLatencyTimer((byte) 16);
            write= ftDev.write(src, src.length);
            return write;
        }

        public void setParameters(int baud, int dataBit, int stopBit, int paritys,boolean flow) throws IOException {


            if (ftDev.isOpen() == false) {
                Log.v("ComJni","setParameters SetConfig: device not open");
                return;
            }
            byte dataBits= (byte) dataBit;
            byte stopBits= (byte) stopBit;
            byte parity= (byte) paritys;
            byte flowControl= (byte)( flow?1:0);

            // configure our port
            // reset to UART mode for 232 devices
            ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
            ftDev.setBaudRate(baud);
            switch (dataBits) {
                case 7:
                    dataBits = D2xxManager.FT_DATA_BITS_7;
                    break;
                case 8:
                    dataBits = D2xxManager.FT_DATA_BITS_8;
                    break;
                default:
                    dataBits = D2xxManager.FT_DATA_BITS_8;
                    break;
            }

            switch (stopBits) {
                case 1:
                    stopBits = D2xxManager.FT_STOP_BITS_1;
                    break;
                case 2:
                    stopBits = D2xxManager.FT_STOP_BITS_2;
                    break;
                default:
                    stopBits = D2xxManager.FT_STOP_BITS_1;
                    break;
            }

            switch (parity) {
                case 0:
                    parity = D2xxManager.FT_PARITY_NONE;
                    break;
                case 1:
                    parity = D2xxManager.FT_PARITY_ODD;
                    break;
                case 2:
                    parity = D2xxManager.FT_PARITY_EVEN;
                    break;
                case 3:
                    parity = D2xxManager.FT_PARITY_MARK;
                    break;
                case 4:
                    parity = D2xxManager.FT_PARITY_SPACE;
                    break;
                default:
                    parity = D2xxManager.FT_PARITY_NONE;
                    break;
            }

            ftDev.setDataCharacteristics(dataBits, stopBits, parity);

            short flowCtrlSetting;
            switch (flowControl) {
                case 0:
                    flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                    break;
                case 1:
                    flowCtrlSetting = D2xxManager.FT_FLOW_RTS_CTS;
                    break;
                case 2:
                    flowCtrlSetting = D2xxManager.FT_FLOW_DTR_DSR;
                    break;
                case 3:
                    flowCtrlSetting = D2xxManager.FT_FLOW_XON_XOFF;
                    break;
                default:
                    flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                    break;
            }


            ftDev.setFlowControl(flowCtrlSetting, (byte) 0x0b, (byte) 0x0d);
        }

        int baudRate=115200,dataBits=8,stopBits=1,parity=0;

        public int getBaudRate() {
            return baudRate;
        }

        public void setBaudRate(int baudRate) {
            this.baudRate = baudRate;
        }

        public int getDataBits() {
            return dataBits;
        }

        public void setDataBits(int dataBits) {
            this.dataBits = dataBits;
        }

        public int getStopBits() {
            return stopBits;
        }

        public void setStopBits(int stopBits) {
            this.stopBits = stopBits;
        }

        public int getParity() {
            return parity;
        }

        public void setParity(int parity) {
            this.parity = parity;
        }

        @Override
    public void setParameters(int baud, int dataBit, int stopBit, int paritys) throws IOException {
            setBaudRate(baud);
            setDataBits(dataBit);
            setStopBits(stopBit);
            setParity(paritys);
      setParameters(baud,dataBit,stopBit,paritys,false);
    }
        @Override
        public UsbSerialDriver getDriver() {
            return FtdiCustomSerialPort.this;
        }

        @Override
        public int getPortNumber() {
            return 0;
        }

        @Override
        public String getSerial() {
            return null;
        }

        @Override
        public void open(UsbDeviceConnection connection) throws IOException {

        }

        @Override
        public void close(Context context) throws IOException {
            ftDev.close();
        }


        @Override
        public boolean getCD() throws IOException {
            return false;
        }

        @Override
        public boolean getCTS() throws IOException {
            return false;
        }

        @Override
        public boolean getDSR() throws IOException {
            return false;
        }

        @Override
        public boolean getDTR() throws IOException {
            return false;
        }

        @Override
        public void setDTR(boolean value) throws IOException {

        }

        @Override
        public boolean getRI() throws IOException {
            return false;
        }

        @Override
        public boolean getRTS() throws IOException {
            return false;
        }

        @Override
        public void setRTS(boolean value) throws IOException {
           setParameters(getBaudRate(),getDataBits(),getStopBits(),getParity(),value);
        }

        @Override
        public boolean purgeHwBuffers(boolean flushRX, boolean flushTX) throws IOException {
            return false;
        }
    }

    public static Map<Integer, int[]> getSupportedDevices() {
        Log.v("UsbCom","getSupportedDevices ");
        final Map<Integer, int[]> supportedDevices = new LinkedHashMap<Integer, int[]>();
        supportedDevices.put(Integer.valueOf(UsbId.VENDOR_FTDI),
                new int[] {
                        UsbId.FTDI_FT232R,
                        UsbId.FTDI_FT231X,
                });
        return supportedDevices;
    }

}
