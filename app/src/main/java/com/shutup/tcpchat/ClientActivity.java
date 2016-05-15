package com.shutup.tcpchat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
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
import java.net.Socket;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ClientActivity extends AppCompatActivity {

    @InjectView(R.id.chatContents)
    TextView mChatContents;
    @InjectView(R.id.msgContent)
    EditText mMsgContent;
    @InjectView(R.id.msgSendBtn)
    Button mMsgSendBtn;

    private Handler mHandler = null;
    private HandlerThread mHandlerThread = null;
    private ClientHandler mClientHandler = null;

    private BufferedReader mBufferedReader = null;
    private BufferedWriter mBufferedWriter = null;

    private Socket mSocket = null;
    private boolean isRun = false;
    private String server_addr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
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

        isRun = false;
    }
    private void initEvent() {
        Intent intent = getIntent();
        server_addr = intent.getStringExtra(Constants.SERVER_ADDR);
        //connect to the server
        //always read from the input stream
        isRun = true;
        new ClientThread().start();
    }

    @OnClick(R.id.msgSendBtn)
    public void onClick() {
        String msg = mMsgContent.getText().toString().trim();
        if (msg.length() >0 ){
            Message message = mClientHandler.obtainMessage();
            message.what = Constants.SEND_MSG;
            message.obj = msg;
            mClientHandler.sendMessage(message);
            //apped msg to chat contents
            String oldStr = mChatContents.getText().toString();
            String newStr = oldStr+"\n"+msg;
            mChatContents.setText(newStr);
            //clear the msg input
            mMsgContent.setText("");
        }
    }

    //connect to server and read msg from input stream
    class ClientThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                mSocket = new Socket(server_addr,8888);
                mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                mBufferedWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));

                mHandler = new Handler(getMainLooper(),new ReceiveMsgCallback());
                Message message = mHandler.obtainMessage();
                message.what = Constants.IS_CONNECTED;
                mHandler.sendMessage(message);
                //init the handler in msg send
                mHandlerThread = new HandlerThread("Client");
                mHandlerThread.start();
                mClientHandler = new ClientHandler(mHandlerThread.getLooper());

            } catch (IOException e) {
                e.printStackTrace();
            }
            while (isRun) {
                String msg = null;
                try {
                    msg = mBufferedReader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (msg != null) {
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
                Toast.makeText(ClientActivity.this, "Connected Server!", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    }

    class ClientHandler extends Handler{

        public ClientHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == Constants.SEND_MSG) {
                if (mBufferedWriter != null) {
                    String msgStr =  msg.obj + "\n";
                    try {
                        mBufferedWriter.write(msgStr);
                        mBufferedWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
