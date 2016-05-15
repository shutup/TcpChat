package com.shutup.tcpchat;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by shutup on 16/5/15.
 */
public class ServerHandler extends Handler {

    private ServerActivity mServerActivity = null;

    public ServerHandler(Context context, Looper looper) {
        super(looper);
        mServerActivity = (ServerActivity) context;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg.what == Constants.SEND_MSG) {
            try {
                BufferedWriter bufferedWriter = mServerActivity.getBufferedWriter();
                if (bufferedWriter != null) {
                    bufferedWriter.write(msg.obj+"\n");
                    bufferedWriter.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
