package com.dxv.abivinTracklocationModule;

public class Constant {
    public static final boolean DEBUG = false;

    public static final String REF_FIREBASE = "trackLocation";

    public static final int DISTANCE_CHANGE = DEBUG ? 0 : 30;
    public static final float MIN_SPEED = DEBUG ? 0 : 0.8f;
    public static final float MAX_SPEED = DEBUG ? 400 : 45f;
    public static final float MAX_ACCURACY = DEBUG ? 1000 : 40;
    public static final float MAX_OFFLINE_ACCURACY = 100;


    public static final long SMALL_INTERVAL = 30000;
    public static final long NORMAL_INTERVAL = 60000;
    public static final long BIG_INTERVAL = NORMAL_INTERVAL * 2;
}
