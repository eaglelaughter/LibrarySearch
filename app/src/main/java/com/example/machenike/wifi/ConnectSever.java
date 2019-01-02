package com.example.machenike.wifi;

import android.content.Intent;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ConnectSever extends AppCompatActivity implements View.OnClickListener{

    public static InputStream mInputStream = null;
    public static OutputStream mOutputStream = null;

    private Thread mThreadClient = null;
    private Socket mSocketClient = null;

    public static boolean Isconnected = false;
    String mIpAddress = "";
    int mClientPort = 0;

    EditText IP,PORT;
    Button CONNECT;
    TextView TB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_sever);
        IP =findViewById(R.id.IP);
        PORT = findViewById(R.id.PORT);
        CONNECT = findViewById(R.id.ConnectBT);
        TB = findViewById(R.id.TB);

        CONNECT.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.ConnectBT:
                try
                {
                    mIpAddress = IP.getText().toString();
                    mClientPort = Integer.parseInt(PORT.getText().toString());
                    TB.append(mIpAddress+ ":" + mClientPort);
                }catch(Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "请填写正确", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mIpAddress != "" && mClientPort != 0)
                {
                    try {
                        //指定ip地址和端口号
                        mThreadClient = new Thread(mClientRunnable);
                        mThreadClient.start();


                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "IP或端口号不能为空",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public Runnable	mClientRunnable	= new Runnable()
    {
        public void run(){
            try {
                //指定ip地址和端口号
                MainSearch.mSocketClient = new Socket(mIpAddress,mClientPort);
                if(MainSearch.mSocketClient != null)
                {

                    Isconnected = true;
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(), "连接成功",Toast.LENGTH_SHORT).show();
                    FinishPage();
                    Looper.loop();

                }

            } catch (Exception e) {
                e.printStackTrace();
                Looper.prepare();
                Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }

    };

    public void FinishPage()
    {
        finish();
    }
}
