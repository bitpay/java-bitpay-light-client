package com.bitpay.sdk_light.util;

public class BitPayLogger {

    private static final int OFF = 0;
    private static final int INFO = 1;
    private static final int WARN = 2;
    private static final int ERR = 3;
    public static final int DEBUG = 4;

    private int _level;

    public BitPayLogger(int level) {
        _level = level;

        String strLevel;
        switch (level) {
            case 0:
                strLevel = "OFF";
                break;
            case 1:
                strLevel = "INFO";
                break;
            case 2:
                strLevel = "WARN";
                break;
            case 3:
                strLevel = "ERR";
                break;
            case 4:
            default:
                strLevel = "DEBUG";
                break;
        }
        System.out.println("Logging level set to: " + strLevel);
    }

    public void info(String message) {
        if (_level >= INFO) {
            System.out.println("INFO: " + message);
        }
    }

    public void warn(String message) {
        if (_level >= WARN) {
            System.out.println("WARN: " + message);
        }
    }

    public void err(String message) {
        if (_level >= ERR) {
            System.out.println("ERR: " + message);
        }
    }

    public void debug(String message) {
        if (_level >= DEBUG) {
            System.out.println("DEBUG: " + message);
        }
    }

}
