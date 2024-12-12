/*
 * Copyright (c) 18-8-13 上午9:48. create by User,email:godmarvin@163.com.
 */

package com.ble;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import com.hoho.android.usbserial.util.HexDump;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by kouten on 17-1-18.
 */

public class BluetoothCom {
    private static final int REQUEST_ENABLE_BT = 1111;

    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public BluetoothSocket mBtSocket = null;

    public String mBtAddr = "";
    BluetoothManager bluetoothManager;
    private BluetoothDevice device, devicePair;
    public static BluetoothCom bluetoothCom;
    private BluetoothAdapter bluetoothAdapter;
    protected DeviceCallback2 deviceCallback;
    private DiscoveryCallback discoveryCallback;
    private BluetoothCallback bluetoothCallback;
    private Context context;

    public BluetoothCom(Context context) {
        this.context = context;
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null) {
                bluetoothAdapter = bluetoothManager.getAdapter();
            }
        } else {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

    }

    public static BluetoothCom instance(Context context) {
        if (null == bluetoothCom) {
            Log.v("BluetoothCom","instance" +" "+ getCurProcessName(context));
            bluetoothCom = new BluetoothCom(context);
//            bluetoothCom = new SocketCom(context);
        }
        return bluetoothCom;
    }

    static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {

                return appProcess.processName;
            }
        }
        return null;
    }


    public void regDeviceListener() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(bluetoothReceiver, intentFilter);
    }

    public boolean isConnected() {
        if (mBtSocket == null) return false;
        return mBtSocket.isConnected();
    }

    public void setBluetoothAddr(String addr) {
        mBtAddr = addr;
    }

    public String getmBtAddr() {
        return mBtAddr;
    }

    public void showEnableDialog(Activity activity) {
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    public void enable() {
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            }
        }
    }

    public void disable() {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
            }
        }
    }

    public void onStop() {
        context.unregisterReceiver(bluetoothReceiver);
    }

    public void startScanning() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);

        context.registerReceiver(scanReceiver, filter);
        bluetoothAdapter.startDiscovery();
    }

    public void stopScanning() {
        context.unregisterReceiver(scanReceiver);
        bluetoothAdapter.cancelDiscovery();
    }



    public void connectAddr(String address) {
        regDeviceListener();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

        new ConnectThread(device, false).start();

    }


    private class ConnectThread extends Thread {
        ConnectThread(BluetoothDevice device, boolean insecureConnection) {
            BluetoothCom.this.device = device;
            try {
                if (insecureConnection) {
                    BluetoothCom.this.mBtSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                } else {
                    BluetoothCom.this.mBtSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                }


            } catch (IOException e) {
                if (deviceCallback != null) {
                    deviceCallback.onError(e.getMessage());
                }
            }
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();

            try {
                mBtSocket.connect();
                Log.v("BluetoothCom","蓝牙连接成功");
                if (deviceCallback != null) {
                    deviceCallback.onDeviceConnected(device);
                }
            } catch (final IOException e) {
                //连接失败就需要弹出蓝牙列表
                Log.v("BluetoothCom","蓝牙连接失败 "+Log.getStackTraceString(e));
                if (deviceCallback != null) {
                    deviceCallback.onConnectError(device, e.getMessage());
                }

                try {
                    mBtSocket.close();
                } catch ( IOException closeException) {
                    if (deviceCallback != null) {
                        deviceCallback.onError(closeException.getMessage());
                    }
                }catch (NullPointerException nullPointerException){
                    if (deviceCallback != null) {
                        deviceCallback.onError(nullPointerException.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 获取与当前序列号匹配的蓝牙列表
     * @return
     */
    public List<BluetoothDevice> getMatchPaireDevices(){
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList<BluetoothDevice> objects = new ArrayList<>();
        for (BluetoothDevice b:bondedDevices){
//            if (LinyingConfig.acceptSerialNum(b.getName())){
//                objects.add(b) ;
//            }
        }
        return objects;
    }

    public List<BluetoothDevice> getPairedDevices() {
        return new ArrayList<>(bluetoothAdapter.getBondedDevices());
    }

    public boolean isPairedDevice(String deviceName) {
        if (deviceName.isEmpty()) {
            return false;
        }
        for (BluetoothDevice bluetoothDevice : getPairedDevices()) {
            if (bluetoothDevice.getName().equals(deviceName)) {
                //ActiveObject.getInstance().setVciAddress(bluetoothDevice.getAddress());
                return true;
            }
        }
        return false;
    }

    public BluetoothDevice getPairedDevice(String bluetoothAddr) {
        if (bluetoothAddr.isEmpty()) {
            return null;
        }
        for (BluetoothDevice bluetoothDevice : getPairedDevices()) {
            if (bluetoothDevice.getAddress().equals(bluetoothAddr)) {
                return bluetoothDevice;
            }
        }
        return null;
    }

    public void pair(BluetoothDevice device) {
        context.registerReceiver(pairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        devicePair = device;
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (final Exception e) {
            if (discoveryCallback != null) {
                ThreadHelper.run(false, null, new Runnable() {
                    @Override
                    public void run() {
                        discoveryCallback.onError(e.getMessage());
                    }
                });
            }
        }
    }

    public void unpair(BluetoothDevice device) {
        context.registerReceiver(pairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        devicePair = device;
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (final Exception e) {
            if (discoveryCallback != null) {
                ThreadHelper.run(false, null, new Runnable() {
                    @Override
                    public void run() {
                        discoveryCallback.onError(e.getMessage());
                    }
                });
            }
        }
    }


    protected InputStream getIps() throws IOException {
        return mBtSocket.getInputStream();
    }

    protected OutputStream getOps() throws IOException {
        return mBtSocket.getOutputStream();
    }



    public  byte[] recvData(int retlen) {
        System.out.println("蓝牙读取长度="+retlen);
        if (!isConnected()) return null;
        try {
                InputStream inputStream =getIps();
                int len = inputStream.available();

                if (len <= 0) return null;
                if (retlen < len) len = retlen;
              //  len=len>1024?1024:len;
                byte[] dat = new byte[len];
            inputStream.read(dat,0,len);//大于1024 jni层就接受不到 java层倒是没有问题,但是为了兼容还是用1024吧 谢车驰
            System.out.println("返回读到了"+len);
                return dat;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public  byte[] recvData2(Callback cb) {
        if (!isConnected()) return null;
        try {
            InputStream inputStream =getIps();

            DataInputStream is = new DataInputStream(inputStream);

            String msg;
            byte[] bytes = new byte[8];
            while ((is.read(bytes))!=0){
                Log.v("BluetoothCom","recvData" +" "+" "+HexDump.toHexString(bytes));
                cb.handleRecvData(bytes);
            }
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public interface Callback{
        void handleRecvData(byte[] b);
    }


    //   long startTime;
    public  int sendData(byte[] dat, int len) {
        // startTime = System.currentTimeMillis();
        if (!isConnected()) return 0;
        try {
                OutputStream outStream = getOps();
                outStream.write(dat, 0, len);

        } catch (Exception e) {
            e.printStackTrace();
            //LogUtils.logVci("sendData "+Log.getStackTraceString(e));
            return 0;
        }
        //  MGLog.d("BLUETOOTH cost times:"+(System.currentTimeMillis()-startTime));
        return len;
    }

    public void close() {
        if (null != mBtSocket) {
            try {
                mBtSocket.close();
                mBtSocket=null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 清空里面的数据;
     */
    public int purge() {
        if (!isConnected()) return 0;
        InputStream inputStream;
        try {
            inputStream = getIps();
            int read=0;
            while (true) {
                if (inputStream.available() <= 0) break;
                int dat = inputStream.read();
                if (dat == -1) break;
                read+=dat;
            }
            return read;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 断开蓝牙
     */
    public void breakOffConnect() {
        if (mBtSocket != null) {
            {
                try {
                    mBtSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mBtSocket = null;
            }
        }
    }

    private BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                        if (state == BluetoothAdapter.STATE_OFF) {
                            if (discoveryCallback != null) {
                                ThreadHelper.run(false, null, new Runnable() {
                                    @Override
                                    public void run() {
                                        discoveryCallback.onError("Bluetooth turned off");
                                    }
                                });
                            }
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        if (discoveryCallback != null) {
                            ThreadHelper.run(false, null, new Runnable() {
                                @Override
                                public void run() {
                                    discoveryCallback.onDiscoveryStarted();
                                }
                            });
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        context.unregisterReceiver(scanReceiver);
                        if (discoveryCallback != null) {
                            ThreadHelper.run(false, null, new Runnable() {
                                @Override
                                public void run() {
                                    discoveryCallback.onDiscoveryFinished();
                                }
                            });
                        }
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (discoveryCallback != null) {
                            ThreadHelper.run(false, null, new Runnable() {
                                @Override
                                public void run() {
                                    discoveryCallback.onDeviceFound(device);
                                }
                            });
                        }
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver pairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("BLE", "PAIR RECEIVER");
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    context.unregisterReceiver(pairReceiver);
                    if (discoveryCallback != null) {
                        ThreadHelper.run(false, null, new Runnable() {
                            @Override
                            public void run() {
                                discoveryCallback.onDevicePaired(devicePair);
                            }
                        });
                    }
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    context.unregisterReceiver(pairReceiver);
                    if (discoveryCallback != null) {
                        ThreadHelper.run(false, null, new Runnable() {
                            @Override
                            public void run() {
                                discoveryCallback.onDeviceUnpaired(devicePair);
                            }
                        });
                    }
                }
            }
        }
    };

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (bluetoothCallback != null) {
                    ThreadHelper.run(false, null, new Runnable() {
                        @Override
                        public void run() {
                            switch (state) {
                                case BluetoothAdapter.STATE_OFF:
                                    bluetoothCallback.onBluetoothOff();
                                    break;
                                case BluetoothAdapter.STATE_TURNING_OFF:
                                    bluetoothCallback.onBluetoothTurningOff();
                                    break;
                                case BluetoothAdapter.STATE_ON:
                                    bluetoothCallback.onBluetoothOn();
                                    break;
                                case BluetoothAdapter.STATE_TURNING_ON:
                                    bluetoothCallback.onBluetoothTurningOn();
                                    break;
                                case BluetoothAdapter.STATE_CONNECTED:
//                                    deviceCallback.onDeviceConnected(device);
                                    break;
                                case BluetoothAdapter.STATE_DISCONNECTED:
                                    deviceCallback.onConnectError(device, "device line off");
                                    break;
                            }
                        }
                    });
                }
            }
            if (action != null && action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                breakOffConnect();
                deviceCallback.onConnectError(device, "device line off");
            }
            if (action != null && action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)) {
                breakOffConnect();
                deviceCallback.onConnectError(device, "device line off");
            }

        }
    };


    public void setDeviceCallback(DeviceCallback2 deviceCallback) {
        this.deviceCallback = deviceCallback;
    }

    public void removeCommunicationCallback() {
        this.deviceCallback = null;
    }

    public void setDiscoveryCallback(DiscoveryCallback discoveryCallback) {
        this.discoveryCallback = discoveryCallback;
    }

    public void removeDiscoveryCallback() {
        this.discoveryCallback = null;
    }

    public void setBluetoothCallback(BluetoothCallback bluetoothCallback) {
        this.bluetoothCallback = bluetoothCallback;
    }

    public void removeBluetoothCallback() {
        this.bluetoothCallback = null;
    }
}
