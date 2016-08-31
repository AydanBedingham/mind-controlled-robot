package com.usbhosttest.utils;

public class ThreadUtil {

    public static void sleep(int timeout){
        try {
            Thread.sleep(timeout);
        } catch(Exception ex){}
    }

}
