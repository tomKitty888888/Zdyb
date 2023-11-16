package com.ble;

import android.bluetooth.BluetoothDevice;

public interface DeviceCallback {
    void onDeviceConnected(BluetoothDevice device);
    void onDeviceDisconnected(BluetoothDevice device);
    void onMessage(String message);
    //void onError(String message);
    //void onConnectError(BluetoothDevice device, String message);
    void onConnecting();
}