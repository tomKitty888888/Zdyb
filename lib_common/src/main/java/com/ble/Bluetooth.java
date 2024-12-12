package com.ble;

import static android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.zdyb.lib_common.base.BaseApplication;
import com.zdyb.lib_common.bus.BusEvent;
import com.zdyb.lib_common.bus.EventType;
import com.zdyb.lib_common.bus.RxBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Omar on 14/07/2015.
 */
public class Bluetooth {
    private static final int REQUEST_ENABLE_BT = 1111;

    private Activity activity;
    private Context context;
    private UUID uuid;
    static BlockingQueue queue = new LinkedBlockingQueue();
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private BluetoothDevice device, devicePair;
    private BufferedReader input;
    private OutputStream out;
    private InputStream inputStream;

    private DeviceCallback deviceCallback;
    private DiscoveryCallback discoveryCallback;
    private BluetoothCallback bluetoothCallback;

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    private boolean connected;

    private boolean runOnUi;

    private Bluetooth(Context context) {
        initialize(context, UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
    }

    public static Bluetooth bluetooth;

    public static Bluetooth instance(Context context) {

        if (bluetooth == null) {
            bluetooth = new Bluetooth(context);
        }
        return bluetooth;
    }

    public Bluetooth(Context context, UUID uuid) {
        initialize(context, uuid);
    }

    private void initialize(Context context, UUID uuid) {
        this.context = context;
        this.uuid = uuid;
        this.deviceCallback = null;
        this.discoveryCallback = null;
        this.bluetoothCallback = null;
        this.connected = false;
        this.runOnUi = false;


    }

    public void onStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null) {
                bluetoothAdapter = bluetoothManager.getAdapter();
            }
        } else {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        context.registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        intentFilter.addAction(ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(deviceReceiver, intentFilter);
    }

    /**
     * 查看手机是否已经连接其他蓝牙设备
     *  -1表示无设备连接
     */
    public int getConnectDeviceState(){
        int a2dp = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP); // 可操控蓝牙设备，如带播放暂停功能的蓝牙耳机
        int headset = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET); // 蓝牙头戴式耳机，支持语音输入输出
        int health = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEALTH); // 蓝牙穿戴式设备
        int GATT = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.GATT);
        Log.e("lqq","a2dp="+a2dp+",headset="+headset+",health="+health);
        // 查看是否蓝牙是否连接到三种设备的一种，以此来判断是否处于连接状态还是打开并没有连接的状态
        int flag = -1;
        if (a2dp == BluetoothProfile.STATE_CONNECTED) {
            flag = a2dp;
        } else if (headset == BluetoothProfile.STATE_CONNECTED) {
            flag = headset;
        } else if (health == BluetoothProfile.STATE_CONNECTED) {
            flag = health;
        }
        return flag;
    }

    public void onPause() {
        context.unregisterReceiver(bluetoothReceiver);
        context.unregisterReceiver(deviceReceiver);
    }

    public void onFinish() {
        context.unregisterReceiver(bluetoothReceiver);
        context.unregisterReceiver(deviceReceiver);
        if (isConnected()) {
            disconnect();
        }
    }


    public void enable() {
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                if (ActivityCompat.checkSelfPermission(BaseApplication.getInstance(), "android.permission.BLUETOOTH_CONNECT") != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
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

    public void purgeQuere() {
        queue.clear();
    }

    public void purge() {
        if (!isConnected()) return;
        InputStream inputStream = null;
        try {
            inputStream = getSocket().getInputStream();
            while (true) {
                if (inputStream.available() <= 0) break;
                byte[] ret = new byte[inputStream.available()];
                int dat = inputStream.read(ret, 0, inputStream.available());

                if (dat == -1) break;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {

        }
    }

    public BluetoothSocket getSocket() {
        return socket;
    }

    public BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public boolean isEnabled() {
        if (bluetoothAdapter != null) {
            return bluetoothAdapter.isEnabled();
        }
        return false;
    }

    public void setCallbackOnUI(Activity activity) {
        this.activity = activity;
        this.runOnUi = true;
    }



    public void connectToAddress(String address, boolean insecureConnection) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        connectToDevice(device, insecureConnection);
    }

    public void connectToAddress(String address) {
        connectToAddress(address, false);
    }

    public void connectToName(String name, boolean insecureConnection) {
        for (BluetoothDevice blueDevice : bluetoothAdapter.getBondedDevices()) {
            if (blueDevice.getName().equals(name)) {
                connectToDevice(blueDevice, insecureConnection);
                return;
            }
        }
    }

    public void connectToName(String name) {
        connectToName(name, false);
    }

    public void connectToDevice(BluetoothDevice device, boolean insecureConnection) {
        new ConnectThread(device, insecureConnection).start();
    }

    public void connectToDevice(BluetoothDevice device) {
        connectToDevice(device, false);
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public int send(byte[] msg, int len) {
        //Log.v("BT_send:", SerialInputOutputManager.byteArrayToHexStr(msg));
        OutputStream out;
        try {
            out = socket.getOutputStream();
            out.write(msg, 0, len);//Sending as UTF-8 as default
            // out.flush();
        } catch (final IOException e) {
            e.printStackTrace();
            connected = false;
            if (e.getMessage().equals("socket closed")){
                RxBus.getDefault().post(new BusEvent(EventType.OBD_BLE_SOCKET_CLOSED_ERR));
            }
        }
        return msg.length;
    }

    public byte[] recvDataQuere(int retlen) {

        int len = queue.size();
        if (len >= retlen) len = retlen;
        byte[] rdat = new byte[len];
        for (int i = 0; i < rdat.length; i++) {
            rdat[i] = (byte) queue.poll();
        }
        //Log.v("BT_recv:", SerialInputOutputManager.byteArrayToHexStr(rdat));
        return rdat;
    }

    public byte[] recvData(int retlen) {
        InputStream inputStream;
        try {
            inputStream = socket.getInputStream();
            int len = inputStream.available();
            if (len <= 0) return null;
            if (len > retlen) len = retlen;
            byte[] dat = new byte[len];
            for (int i = 0; i < len; i++) {
                dat[i] = (byte) inputStream.read();
            }
            return dat;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //读完所有的数据
    public byte[] recvData() {
        int len = queue.size();
        byte[] rdat = new byte[len];
        for (int i = 0; i < rdat.length; i++) {
            rdat[i] = (byte) queue.poll();
        }
        //Log.v("BT_recv:", SerialInputOutputManager.byteArrayToHexStr(rdat));
        return rdat;
    }

    public List<BluetoothDevice> getPairedDevices() {
        return new ArrayList<>(bluetoothAdapter.getBondedDevices());
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

    public void pair(BluetoothDevice device) {
        context.registerReceiver(pairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        devicePair = device;
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (final Exception e) {
            if (discoveryCallback != null) {

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
            }
        }
    }


    private class ReceiveThread extends Thread implements Runnable {
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            try {
                while (connected) {
                    sleep(5);
                    if ((bytes = inputStream.read(buffer)) > 0) {

                        for (int i = 0; i < bytes; i++) {
                            queue.offer(buffer[i]);
                        }
                    }
                }
            } catch (final Exception e) {
                connected = false;
                if (deviceCallback != null) {
                    deviceCallback.onMessage(e.getMessage());
                }
            } finally {
                purgeQuere();
            }
        }
    }

    private class ConnectThread extends Thread {
        ConnectThread(BluetoothDevice device, boolean insecureConnection) {
            Bluetooth.this.device = device;
            try {
                if (insecureConnection) {
                    Bluetooth.this.socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                } else {
                    Bluetooth.this.socket = device.createRfcommSocketToServiceRecord(uuid);
                }
            } catch (IOException e) {
                e.printStackTrace();
               /* if (deviceCallback != null) {
                    deviceCallback.onError(e.getMessage());
                }*/
            }
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();

            try {
                if (deviceCallback != null) {
                    ThreadHelper.run(runOnUi, activity, new Runnable() {
                        @Override
                        public void run() {
                            deviceCallback.onConnecting();
                        }
                    });
                }
                socket.connect();
                inputStream = socket.getInputStream();
                input = new BufferedReader(new InputStreamReader(inputStream));
                connected = true;

                new ReceiveThread().start();

                if (deviceCallback != null) {
                    ThreadHelper.run(runOnUi, activity, new Runnable() {
                        @Override
                        public void run() {
                            deviceCallback.onDeviceConnected(device);
                        }
                    });
                }
            } catch (final IOException e) {
                e.printStackTrace();
                if (deviceCallback != null) {
                    ThreadHelper.run(runOnUi, activity, new Runnable() {
                        @Override
                        public void run() {
                            deviceCallback.onDeviceDisconnected(device);
                        }
                    });
                }

                try {
                    socket.close();
                } catch (final IOException closeException) {
                    e.printStackTrace();
                   /* if (deviceCallback != null) {
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                deviceCallback.onError(closeException.getMessage());
                            }
                        });
                    }*/
                }
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
                                ThreadHelper.run(runOnUi, activity, new Runnable() {
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
                            ThreadHelper.run(runOnUi, activity, new Runnable() {
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
                            ThreadHelper.run(runOnUi, activity, new Runnable() {
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
                            ThreadHelper.run(runOnUi, activity, new Runnable() {
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
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
                            @Override
                            public void run() {
                                discoveryCallback.onDevicePaired(devicePair);
                            }
                        });
                    }
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    context.unregisterReceiver(pairReceiver);
                    if (discoveryCallback != null) {
                        ThreadHelper.run(runOnUi, activity, new Runnable() {
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

    private final BroadcastReceiver deviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                //  final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (deviceCallback != null) {
                    switch (action) {
                        case ACTION_ACL_DISCONNECTED:
                            connected = false;
                            deviceCallback.onDeviceDisconnected(device);
                            break;
                       /* case ACTION_ACL_CONNECTED:
                            connected = true;
                            deviceCallback.onDeviceConnected(device);
                            break;*/
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
                    ThreadHelper.run(runOnUi, activity, new Runnable() {
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
                            }
                        }
                    });
                }

            }
        }
    };

    public void setDeviceCallback(DeviceCallback deviceCallback) {
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