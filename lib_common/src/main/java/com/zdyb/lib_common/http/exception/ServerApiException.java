package com.zdyb.lib_common.http.exception;

public class ServerApiException extends Exception {
    private String code;
    private String displayMessage;



    public ServerApiException(String code, String displayMessage) {
        this.code = code;
        if (code.equals("04")){
            this.displayMessage = displayMessage;
        }else {
            this.displayMessage = getErrMsg(code);
        }

    }

    public ServerApiException(String code, String message, String displayMessage) {
        super(message);
        this.code = code;
        this.displayMessage = displayMessage;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public String getErrMsg(String message){
        switch (message){
            case "FAILD":
                message = "失败";
                break;
            case "SUCCESS":
                message = "成功";
                break;
            case "2":
                message = "数据为空";
                break;
            case "5":
                message = "密码错误";
                break;
            case "6":
                message = "不存在";
                break;
            case "7":
                message = "已存在";
                break;
            case "8":
                message = "用户名或者密码错误";
                break;
            case "9":
                message = "结果为空";
                break;
            case "11":
                message = "用户名或密码为空";
                break;
            case "12":
                message = "用户名不存在";
                break;
            case "13":
                message = "手机号已存在";
                break;
            case "20":
                message = "角色不存在";
                break;
            case "21":
                message = "验证码错误";
                break;
            case "30":
                message = "文件大小过大";
                break;
            case "31":
                message = "文件格式不支持";
                break;
            case "22":
                message = "超时";
                break;
            case "33":
                message = "参数错误";
                break;
        }
        return message;
    }
}
