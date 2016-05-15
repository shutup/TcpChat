package com.shutup.tcpchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.serverAddressInput)
    EditText mServerAddressInput;
    @InjectView(R.id.becomeClientBtn)
    Button mBecomeClientBtn;
    @InjectView(R.id.becomeServerBtn)
    Button mBecomeServerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }

    @OnClick({R.id.becomeClientBtn, R.id.becomeServerBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.becomeClientBtn:
                String ser_add = mServerAddressInput.getText().toString().trim();
                if (TextUtils.isEmpty(ser_add)){
                    Toast.makeText(this, "please input the server address!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, ClientActivity.class);
                intent.putExtra(Constants.SERVER_ADDR,ser_add);
                startActivity(intent);
                break;
            case R.id.becomeServerBtn:
                startActivity(new Intent(MainActivity.this, ServerActivity.class));
                break;
        }
    }
}
