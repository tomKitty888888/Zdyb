package com.zdyb.lib_common.http.response;

public class Response<T> {

    private int code; // 返回的code
    private T data; // 具体的数据结果
    private String message;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
