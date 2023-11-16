package com.zdeps.obd;

public class CommandUtils {

    public static String numToCommand(int type) {
        String command = new String("");
        switch (type) {
            case 1:
                command = "GetVCIState";
                break;
            case 2:
                command = "GetSoftwareVersion";
                break;
            case 3:
                command = "StartTest";
                break;

            case 4:
                command = "GetCarInfo";
                break;

            case 5:
                command = "GetOBDInfo";
                break;

            case 6:
                command = "GetDTCInfo";
                break;

            case 7:
                command = "GetSystemCheckState";
                break;

            case 8:
                command = "GetIUPR";
                break;

            case 9:
                command = "GetRTData";
                break;

            case 10:
                command = "GetDTC";
                break;

            case 11:
                command = "GetDTCMileage";
                break;

            case 12:
                command = "GetFreezeData";
                break;

            case 13:
                command = "GetVIN";
                break;

            case 14:
                command = "GetECUInfo";
                break;

            case 15:
                command = "GetLampState";
                break;
            case 16:
                command = "StopTest";
                break;
            case 17:
                command = "ScanVINCode";
                break;
            case 18:
                break;
            case 19:
                command = "GetRTInfo";
                break;
            case 100:
                command = "VciGetSerialNumber";
                break;
        }
        return command;
    }
}
