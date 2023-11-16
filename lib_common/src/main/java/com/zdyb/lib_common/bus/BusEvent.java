package com.zdyb.lib_common.bus;

public class BusEvent {

    public int what;
    public Object data;

    public BusEvent(int what) {
        this.what = what;
    }

    public BusEvent(int what, Object data) {
        this.what = what;
        this.data = data;
    }

    @Override
    public String toString() {
        return "BusEvent{" +
                "what=" + what +
                ", data=" + data +
                '}';
    }
}
