package com.shutup.tcpchat;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by shutup on 16/5/15.
 */
public class NetWorkUtils {
    private ServerSocket mServerSocket = null;
    private Socket mSocket = null;
    private boolean isRun = false;
    private static int type = 0;
    private static NetWorkUtils sNetWorkUtils = null;

    private NetWorkUtils() {

    }

    public static void init(int type) {

    }

    public static void Send(String msg) {

    }

    class NetWorkThread extends Thread {
        @Override
        public void run() {
            super.run();

            while (isRun) {

            }
        }
    }
}
