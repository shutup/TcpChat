package com.shutup.tcpchat;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ServerActivity extends AppCompatActivity {

    @InjectView(R.id.msgContent)
    EditText mMsgContent;
    @InjectView(R.id.msgSendBtn)
    Button mMsgSendBtn;
    @InjectView(R.id.chatContents)
    TextView mChatContents;

    private Handler mHandler = null;

    private HandlerThread mHandlerThread = null;
    private ServerHandler serverHandler = null;

    private ServerSocket mServerSocket = null;
    private Socket mSocket = null;

    private BufferedReader mBufferedReader = null;
    private BufferedWriter mBufferedWriter = null;
    private boolean isRun = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        ButterKnife.inject(this);
        initEvent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBufferedWriter != null) {
            try {
                mBufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mBufferedReader != null) {
            try {
                mBufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mServerSocket != null) {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        isRun = false;
    }

    private void initEvent() {
        //start the server thread listen
        isRun = true;
        new ServerThread().start();
    }

    @OnClick(R.id.msgSendBtn)
    public void onClick() {
        String msg = mMsgContent.getText().toString().trim();
        if (msg.length() != 0) {
            sendMsg(msg);
        }
    }

    private void sendMsg(String msg) {
        Message message = serverHandler.obtainMessage();
        message.what = Constants.SEND_MSG;
        message.obj = msg;
        serverHandler.sendMessage(message);
        //apped msg to chat contents
        String oldStr = mChatContents.getText().toString();
        String newStr = oldStr+"\n"+msg ;
        mChatContents.setText(newStr);
        //clear the msg input
        mMsgContent.setText("");
    }

    public BufferedReader getBufferedReader() {
        return mBufferedReader;
    }

    public BufferedWriter getBufferedWriter() {
        return mBufferedWriter;
    }

    /**
     *
     */
    class ServerThread extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                mServerSocket = new ServerSocket(8888);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (isRun) {
                try {
                    mSocket = mServerSocket.accept();
                    mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    mBufferedWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
                    //the server send thread
                    mHandlerThread = new HandlerThread("Server");
                    mHandlerThread.start();
                    serverHandler = new ServerHandler(ServerActivity.this, mHandlerThread.getLooper());
                    //the server receive thread
                    mHandler = new Handler(getMainLooper(),new ReceiveMsgCallback());
                    new ServerReceiveThread().start();

                    Message message = mHandler.obtainMessage();
                    message.what = Constants.IS_CONNECTED;
                    mHandler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ServerReceiveThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (isRun) {
                String msg = "";
                try {
                    if (mBufferedReader == null){
                        continue;
                    }
                    msg = mBufferedReader.readLine();
                    if (msg == null) {
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (msg.trim().length() > 0) {
                    Message message = mHandler.obtainMessage();
                    message.what = Constants.RECEIVE_MSG;
                    message.obj = msg;
                    mHandler.sendMessage(message);
                }
            }
        }
    }

    class ReceiveMsgCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == Constants.RECEIVE_MSG) {
                String oldMsgs = mChatContents.getText().toString();
                String newMsgs = oldMsgs+"\n"+msg.obj;
                mChatContents.setText(newMsgs);
            }else if (msg.what == Constants.IS_CONNECTED) {
                mMsgSendBtn.setEnabled(true);
                Toast.makeText(ServerActivity.this, "Client Connected!", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    }
}
