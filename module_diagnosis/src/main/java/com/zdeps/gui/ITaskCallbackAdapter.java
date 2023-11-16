package com.zdeps.gui;

import android.os.RemoteException;

import com.zdyb.ITaskCallback;

import org.jetbrains.annotations.NotNull;

public class ITaskCallbackAdapter extends ITaskCallback.Stub{
    @Override
    public boolean guiOpen() throws RemoteException {
        return false;
    }

    @Override
    public boolean guiClose() throws RemoteException {
        return false;
    }

    @Override
    public boolean dataInit(@NotNull byte tag) throws RemoteException {
        return false;
    }

    @Override
    public boolean addItemOne(@NotNull byte tag, @NotNull String pszMenu) throws RemoteException {
        return false;
    }


    @Override
    public boolean addItemTwo(@NotNull byte tag, @NotNull String key, @NotNull String value) throws RemoteException {
        return false;
    }

    @Override
    public boolean addItemThree(@NotNull byte tag,@NotNull String value1,@NotNull String value2,@NotNull String value3) throws RemoteException {
        return false;
    }

    @Override
    public boolean addItemChild(@NotNull byte tag, @NotNull String pszMenu) throws RemoteException {
        return false;
    }

    @Override
    public boolean addDataStream(@NotNull byte tag, @NotNull int index, @NotNull String key, String value) throws RemoteException {
        return false;
    }

    @Override
    public boolean addButton(@NotNull byte tag, @NotNull String name) throws RemoteException {
        return false;
    }

    @Override
    public boolean addHint(@NotNull byte tag, @NotNull String hint) throws RemoteException {
        return false;
    }

    @Override
    public boolean dataShow(@NotNull byte tag) throws RemoteException {
        return false;
    }

    @Override
    public byte[] getByteData(byte tag) throws RemoteException {
        return new byte[0];
    }


    @Override
    public long showDialog(@NotNull byte tag, byte type, String title, String msg, String imgPath,long color) throws RemoteException {

        return 1;
    }

    @Override
    public long destroyDialog() throws RemoteException {
        return 1;
    }

    @Override
    public void viewFinish() throws RemoteException {

    }
}
