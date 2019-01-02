package com.example.machenike.wifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Client extends AppCompatActivity {

    private EditText mIp;
    private EditText mPort;
    private EditText sendbox;
    private TextView tv_1;

    static InputStream mInputStream = null;
    static OutputStream mOutputStream = null;

    private Thread mThreadClient = null;
    private Socket mSocketClient = null;

    private String MessageClient ="";
    private byte[] SendMassage = new byte[1024];

    private boolean ClientRuning = false;

    String mIpAddress = "";
    int mClientPort = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        mIp = findViewById(R.id.IP);
        mPort = findViewById(R.id.PORT);
        sendbox = findViewById(R.id.sendbox);
        tv_1 = findViewById(R.id.recbox_1);
        Toast.makeText(getApplicationContext(), "发送内容不能为空！", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0)
            {
                tv_1.append(MessageClient + "\n");	// 刷新
            }
            else if(msg.what == 1)
            {
                if ( ClientRuning && mSocketClient!=null )
                {
                    final String msgText =sendbox.getText().toString();//取得编辑框中我们输入的内容
                    if(msgText.length()<=0)
                    {
                        Toast.makeText(getApplicationContext(), "发送内容不能为空！", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        try
                        {
                            tv_1.append(msgText);	// 刷新
                            //SendMassage = msgText.getBytes("UTF-8");

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Map<String, String> map = new HashMap<String, String>();
                                        map.put("BookName",msgText);
                                        map.put("op","0");
                                        //将json转化为String类型
                                        JSONObject json = new JSONObject(map);
                                        String jsonString = "";
                                        jsonString = json.toString();
                                        //将String转化为byte[]
                                        //byte[] jsonByte = new byte[jsonString.length()+1];
                                        byte[] jsonByte = jsonString.getBytes();

                                        mOutputStream.write(jsonByte);
                                        mOutputStream.flush();
                                    }catch (IOException e){
                                        e.printStackTrace();
                                    }
                                }
                            }).start();

                        }
                        catch (Exception e)
                        {
                            // TODO: handle exception
                            Toast.makeText(getApplicationContext(), "发送异常：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "没有连接", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    public void Connect(View view)
    {
        mIpAddress = mIp.getText().toString();
        mClientPort = Integer.parseInt(mPort.getText().toString());
        if(mIpAddress != "" && mClientPort != 0)
        {
            Message msg = new Message();
            msg.what = 0;
            MessageClient = mIpAddress + mClientPort;//消息换行
            mHandler.sendMessage(msg);

            mThreadClient = new Thread(mClientRunnable);
            mThreadClient.start();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "",Toast.LENGTH_SHORT).show();

        }
    }

    public void SendOnClick(View view)
    {
        Message msg = new Message();
        msg.what = 1;
        mHandler.sendMessage(msg);
    }

    public Runnable	mClientRunnable	= new Runnable()
    {
        public void run(){
            try {
                //指定ip地址和端口号
                mSocketClient = new Socket(mIpAddress,mClientPort);
                if(mSocketClient != null)
                {
                    ClientRuning = true;
                    //获取输出流、输入流
                    mOutputStream = mSocketClient.getOutputStream();
                    mInputStream = mSocketClient.getInputStream();

                    Looper.prepare();
                    Toast.makeText(getApplicationContext(), "连接成功",Toast.LENGTH_SHORT).show();
                    Looper.loop();

                    byte[] buffer_byte = new byte[1024];

                    while(ClientRuning)
                    {
                        try
                        {
                            mInputStream.read(buffer_byte,0,1024);
                            MessageClient  = new String(buffer_byte,"UTF-8");

                            /*
                            JSONObject json = new JSONObject(strInputstream);
                            int op =Integer.parseInt((String)json.get("op"));
                            switch(op)
                            {
                                case 1:
                                    break;

                            }
                            */
                            Message msg0 = new Message();
                            msg0.what = 0;
                            mHandler.sendMessage(msg0);

                        }
                        catch (Exception e)
                        {
                            Toast.makeText(getApplicationContext(), "连接异常", Toast.LENGTH_SHORT).show();
                            MessageClient = "接收异常:" + e.getMessage() + "\n";//消息换行
                            Message msg = new Message();
                            msg.what = 0;
                            mHandler.sendMessage(msg);
                            return;
                        }
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT).show();
                return;
            }
        }

    };
}
