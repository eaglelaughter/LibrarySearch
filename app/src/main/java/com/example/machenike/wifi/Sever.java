package com.example.machenike.wifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Sever extends AppCompatActivity {


    private EditText et;
    private TextView tv;
    private ImageView imageView;

    private Thread mThreadServer = null;
    private Socket mSocketServer = null;

    private Thread mSendThread = null;

    static BufferedReader mBufferedReaderServer	= null;
    static PrintWriter mPrintWriterServer = null;

    static InputStream mInputStream = null;
    static OutputStream mOutputStream = null;

    List<OutputStream> moutputStreams = new ArrayList<>(); //声明用于装载多输出流的列表

    static byte[] send_massage = new byte[1024];

    private  String recvMessageClient = "";
    private  String recvMessageServer = "";



    private ServerSocket serverSocket = null;
    private boolean serverRuning = false;
    private boolean send_flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sever);
        tv = findViewById(R.id.rec_box);
        et = findViewById(R.id.Send_et);
    }


    public void sendOnClick(View view)
    {

        Message msg = new Message();
        msg.what = 1;
        recvMessageServer = "client已经连接上！\n";
        mHandler.sendMessage(msg);

    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            if(msg.what == 0)
            {
                byte[] image ;

                tv.append(recvMessageServer + "\n");	// 刷新
            }
            else if(msg.what == 1)
            {
                if ( serverRuning && mSocketServer!=null )
                {
                    String msgText =et.getText().toString();//取得编辑框中我们输入的内容
                    if(msgText.length()<=0)
                    {
                        Toast.makeText(getApplicationContext(), "默认Toast样式",Toast.LENGTH_SHORT).show();

                    }
                    else
                    {
                        try
                        {
                            tv.append(msgText + " ");	// 显示发送的内容
                            send_massage = msgText.getBytes("UTF-8");

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        for ( int i = 0; i < moutputStreams.size(); i++)
                                        {
                                            moutputStreams.get(i).write(send_massage);
                                            moutputStreams.get(i).flush();
                                        }
                                    }catch (IOException e){
                                        e.printStackTrace();
                                    }
                                }
                            }).start();

                        }
                        catch (Exception e)
                        {
                            // TODO: handle exception
                            Toast.makeText(getApplicationContext(), "发送异常",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "没有连接", Toast.LENGTH_SHORT).show();
                }
                tv.append(recvMessageClient);	// 刷新

            }
            else if(msg.what == 5)
            {
                tv.append("发送成功\r\n");

            }
        }
    };




    public void creatOnClick(View view) {
        String w = "william's work ";
        tv.setText(w);
        if (serverRuning)
        {
            serverRuning = false;

            try {
                if(serverSocket!=null)
                {
                    serverSocket.close();
                    serverSocket = null;
                }
                if(mSocketServer!=null)
                {
                    mSocketServer.close();
                    mSocketServer = null;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mThreadServer.interrupt();
            tv.setText("信息:\n");
        }
        else
        {
            serverRuning = true;
            mThreadServer = new Thread(mcreateRunnable);
            mThreadServer.setPriority(Thread.MAX_PRIORITY);
            mThreadServer.start();



        }
    }

    public void display(String s)
    {
        recvMessageServer = s + "\n";//消息换行

        Message msg = new Message();
        msg.what = 0;
        mHandler.sendMessage(msg);
    }

    public Runnable	mcreateRunnable	= new Runnable()
    {
        public void run()
        {
            try
            {
                serverSocket = new ServerSocket(10010);
                int num = 0;
                while(true)
                {
                    getLocalIpAddress();//显示服务器端的IP和端口号

                    mSocketServer = serverSocket.accept();
                    if(mSocketServer != null) new ServerThread(mSocketServer,num).start();

                    num++;
                    display("client "+ num +"已经连接上");

                }
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                //e.printStackTrace();
                Message msg = new Message();
                msg.what = 0;
                recvMessageServer = "创建异常:" + e.getMessage() + e.toString() + "\n";//消息换行
                mHandler.sendMessage(msg);
                return;
            }
        }
    };

    class ServerThread extends Thread{
        InputStream input;
        OutputStream output;
        String user;
        Socket clientSocket;
        int num;

        ServerThread(Socket clientSocket,int i)
        {
            this.clientSocket=clientSocket;
            this.num=i;
        }

        public void run(){
            try{
                input = clientSocket.getInputStream();
                output = clientSocket.getOutputStream();
                moutputStreams.add(output);//将输出流添加至列表内
                //user=input.readLine();
                //output.println("hello,connect successful");
                //System.out.println("第"+num+"个用户"+user+"连接了!");
            }catch(IOException e){ }
            try {
                byte[] buffer_byte = new byte[1024];
                while(serverRuning)
                {
                    try
                    {
                        input.read(buffer_byte,0,1024);
                        String a = new String(buffer_byte,"UTF-8");
                        recvMessageServer = a;

                        Message msg0 = new Message();
                        msg0.what = 0;
                        mHandler.sendMessage(msg0);

                    }
                    catch (Exception e)
                    {
                        // Toast.makeText(mContext, "连接异常", Toast.LENGTH_SHORT).show();
                        recvMessageServer = "接收异常:" + e.getMessage() + "\n";//消息换行
                        Message msg = new Message();
                        msg.what = 0;
                        mHandler.sendMessage(msg);
                        return;
                    }
                }
            }catch(Exception e){
                return;}
            System.out.println(user+ "has disconnected.");
            try{
                clientSocket.close();
                input.close();
            }catch(Exception e){
                return;
            }
        }
    }




    public String getLocalIpAddress()
    {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress())
                    {
                        if(inetAddress.isSiteLocalAddress())
                        {
                            //serverSocket.getLocalPort();
                            recvMessageServer = "新客户端请连接IP："+inetAddress.getHostAddress()+": " + "10010"+ "\n";//获取本地IP
                        }
                    }
                }
            }
        }
        catch (SocketException ex) {
            recvMessageServer = "获取IP地址异常:" + ex.getMessage() + "\n";//消息换行
            Message msg = new Message();
            msg.what = 0;
            mHandler.sendMessage(msg);
        }
        Message msg = new Message();
        msg.what = 0;
        mHandler.sendMessage(msg);
        return null;
    }
}
