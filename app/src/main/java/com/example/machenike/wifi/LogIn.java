package com.example.machenike.wifi;

import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class LogIn extends AppCompatActivity implements View.OnClickListener{


    private EditText User ,Password;
    private Button SingUp ,LogIn;

    private OutputStream Output;
    private InputStream Input;

    private Thread LogInThread;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        User = findViewById(R.id.NameET);
        Password = findViewById(R.id.PasswordET);
        SingUp = findViewById(R.id.SignUpBT);
        LogIn = findViewById(R.id.LogInBT);
        SingUp.setOnClickListener(this);
        LogIn.setOnClickListener(this);
        try
        {
            if (ConnectSever.Isconnected)
            {
                Output = MainSearch.mSocketClient.getOutputStream();
                Input = MainSearch.mSocketClient.getInputStream();
                LogInThread = new Thread(LogInRunable);
                LogInThread.start();
                if(MainSearch.LogInSuccessFlag) this.finish();
            }

        }catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "获取输出流失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public Runnable	LogInRunable = new Runnable()
    {
        public void run(){
            try {
                String recbuff;
                byte[] buffer_byte = new byte[1024];
                JSONObject json = null;
                int op = 10;
                while(ConnectSever.Isconnected && MainSearch.LogInFlag)
                {
                    try
                    {
                        Input.read(buffer_byte,0,1024);
                        recbuff  = new String(buffer_byte,"UTF-8");

                        try
                        {
                            json = new JSONObject(recbuff);
                            op = json.getInt("op");
                        }catch (Exception e)
                        {
                            // TODO: handle exception
                            Looper.prepare();
                            Toast.makeText(getApplicationContext(), "JSON数据解释失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }

                        switch(op)
                        {
                            case 3:
                                if(json.getInt("isSuccess") == 1)
                                {
                                    if(MainSearch.SignUpSuccessFlag)
                                    {
                                        Looper.prepare();
                                        Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_SHORT).show();
                                        FinishPage();
                                        Looper.loop();
                                        MainSearch.LogInFlag = false;
                                        MainSearch.SignUpSuccessFlag = true;
                                    }
                                    else
                                    {
                                        Looper.prepare();
                                        Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_SHORT).show();
                                        Looper.loop();
                                    }

                                }
                                else
                                {
                                    Looper.prepare();
                                    Toast.makeText(getApplicationContext(), "注册失败", Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }
                                break;

                            case 4:
                                if(json.getInt("IsLogedIn") == 1)
                                {
                                    if(!MainSearch.LogInSuccessFlag)
                                    {
                                        Looper.prepare();
                                        Toast.makeText(getApplicationContext(), "登陆成功", Toast.LENGTH_SHORT).show();
                                        FinishPage();
                                        MainSearch.LogInFlag = false;
                                        MainSearch.LogInSuccessFlag = true;
                                        Looper.loop();

                                    }
                                    else
                                    {
                                        Looper.prepare();
                                        Toast.makeText(getApplicationContext(), "用户已登陆", Toast.LENGTH_SHORT).show();
                                        Looper.loop();
                                    }
                                }
                                else
                                {
                                    Looper.prepare();
                                    Toast.makeText(getApplicationContext(), "用户名或密码错误", Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }
                                break;
                        }

                    }
                    catch (Exception e)
                    {
                        Toast.makeText(getApplicationContext(), "连接异常", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT).show();
                return;
            }
        }

    };

    public void FinishPage()
    {
        finish();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.SignUpBT:
                if (ConnectSever.Isconnected)
                {
                    final String name =User.getText().toString();//取得编辑框中我们输入的内容
                    final String password =Password.getText().toString();//取得编辑框中我们输入的内容
                    if(name.length()<=0 || password.length()<=0)
                    {
                        Toast.makeText(getApplicationContext(), "发送内容不能为空！", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        try
                        {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Map<String, String> map = new HashMap<String, String>();
                                        map.clear();
                                        map.put("op","1");
                                        map.put("name",name);
                                        map.put("password",password);
                                        //map.put("BookName","无限恐怖");
                                        //map.put("op","2");
                                        //将json转化为String类型
                                        JSONObject json = new JSONObject(map);
                                        String jsonString = "";
                                        jsonString = json.toString();
                                        byte[] jsonByte = jsonString.getBytes("Utf-8");

                                        Output.write(jsonByte);
                                        Output.flush();
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
                break;
            case R.id.LogInBT:
                if (ConnectSever.Isconnected)
                {

                    final String name =User.getText().toString();//取得编辑框中我们输入的内容
                    final String password =Password.getText().toString();//取得编辑框中我们输入的内容
                    if(name.length()<=0 || password.length()<=0)
                    {
                        Toast.makeText(getApplicationContext(), "发送内容不能为空！", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        try
                        {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Map<String, String> map = new HashMap<String, String>();
                                        map.clear();
                                        map.put("op","0");
                                        map.put("name",name);
                                        map.put("password",password);
                                        //map.put("BookName","无限恐怖");
                                        //map.put("op","2");
                                        //将json转化为String类型
                                        JSONObject json = new JSONObject(map);
                                        String jsonString = "";
                                        jsonString = json.toString();
                                        byte[] jsonByte = jsonString.getBytes("Utf-8");

                                        Output.write(jsonByte);
                                        Output.flush();
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
                break;

        }
    }
}
