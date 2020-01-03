package com.dxv.abivinTracklocationModule.uitls;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.dxv.abivinTracklocationModule.Constant;


public class SmartLog {
    private static final String TAG = "ABIVIN_TRACK";


    public static void logE(String message) {
//        if (Constant.DEBUG) {
        Log.e(TAG, message);
//        }
    }

    public static void logE(String message, Exception e) {
        if (Constant.DEBUG) {
            Log.e(TAG, message, e);
        }
    }

    public static void log(String message) {
        if (Constant.DEBUG) {
            Log.d(TAG, message);
        }
    }
}
