package com.example.machenike.wifi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainSearch extends AppCompatActivity implements View.OnClickListener,PopupMenu.OnMenuItemClickListener{

    //显示听写结果
    private TextView textView;
    //语音听写对象
    private SpeechRecognizer speechRecognizer;
    //语音听写UI
    private RecognizerDialog recognizerDialog;
    //是否显示听写UI
    private boolean isShowDialog = true;
    //缓存
    private SharedPreferences sharedPreferences;
    //用hashmap存储听写结果
    private HashMap<String, String> hashMap = new LinkedHashMap<String, String>();
    //引擎类型（云端或本地）
    private String mEngineType = null;
    //函数返回值
    private int ret = 0;

    public static InputStream mInputStream = null;
    public static OutputStream mOutputStream = null;

    public static Socket mSocketClient = null;//socket只能在新线程里面建立连接

    private String MessageClient ="",BookInfo = "";
    private boolean IsSeverRunning =false;
    public static boolean LogInFlag = false;
    public static boolean LogInSuccessFlag = false;
    public static boolean SignUpSuccessFlag = false;
    private boolean IsSend = false;

    private static Thread mThreadClient = null;
    String test = "";

    ImageButton menubt ;
    Button search ;
    TextView RecBox;
    EditText SearchBar;
    private RecyclerView mRecyclerView;

    MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_search);
        menubt = findViewById(R.id.Menubt);
        search = findViewById(R.id.Searchbt);
        RecBox = findViewById(R.id.RecBox);
        SearchBar = findViewById(R.id.SearchBar);
        menubt.setOnClickListener(this);
        search.setOnClickListener(this);

        mRecyclerView = findViewById(R.id.recycler_view);
        List<ImageInfor> list = new ArrayList<>();
        list.add(new ImageInfor(R.mipmap.caiyilin, "蔡依林"));
        list.add(new ImageInfor(R.mipmap.ulinxinru, "林心如"));
        list.add(new ImageInfor(R.mipmap.caiyilin,"蔡依林"));
        list.add(new ImageInfor(R.mipmap.ulinxinru, "林心如"));
        list.add(new ImageInfor(R.mipmap.caiyilin,"蔡依林"));
        list.add(new ImageInfor(R.mipmap.ulinxinru, "林心如"));


        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        myAdapter = new MyAdapter(list);
        mRecyclerView.setAdapter(myAdapter);
        myAdapter.setOnItemClick(new OnItemClick(){
            @Override
            public void onItemClick(View view, int position) {
                //Toast.makeText(getApplication(),"点击了：" + position,Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "点击了"  + position, Toast.LENGTH_SHORT).show();
            }
        });

        initDate();
        requestAllPower();
    }


    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{
        private List<ImageInfor> list;
        MyAdapter(List<ImageInfor> list){
            this.list = list;
        }
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bookcard,viewGroup,false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
            myViewHolder.iv_backgroud.setBackgroundResource(list.get(i).getImageId());
            myViewHolder.tv_title.setText(list.get(i).getName());
            final int position = i;
            myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onItemClick != null){
                        onItemClick.onItemClick(view,position);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            private ImageView iv_backgroud;
            private TextView tv_title;
            public MyViewHolder(View itemView) {
                super(itemView);
                iv_backgroud = (ImageView) itemView.findViewById(R.id.picture);
                tv_title = (TextView) itemView.findViewById(R.id.name);
            }
        }

        private OnItemClick onItemClick;

        public void setOnItemClick(OnItemClick onItemClick) {
            this.onItemClick = onItemClick;
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.Menubt:
                Toast.makeText(getApplicationContext(), "菜单", Toast.LENGTH_SHORT).show();
                PopupMenu popup = new PopupMenu(this, view);//第二个参数是绑定的那个view
                //获取菜单填充器
                MenuInflater inflater = popup.getMenuInflater();
                //填充菜单
                inflater.inflate(R.menu.main, popup.getMenu());
                //绑定菜单项的点击事件
                popup.setOnMenuItemClickListener(this);
                popup.show(); //这一行代码不要忘记了
                break;
            case R.id.Searchbt:
                if(ConnectSever.Isconnected)
                {
                    if(IsSeverRunning)
                    {
                        Message msg = new Message();
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                    }
                    else
                    {
                        try {
                            mInputStream = mSocketClient.getInputStream();
                            mOutputStream = mSocketClient.getOutputStream();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mThreadClient = new Thread(mClientRunnable);
                        mThreadClient.start();

                        Message msg = new Message();
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "请先连接服务器", Toast.LENGTH_SHORT).show();
                }
                //Toast.makeText(getApplicationContext(), "搜索", Toast.LENGTH_SHORT).show();
                break;

        }
    }


    public Runnable	mClientRunnable	= new Runnable()
    {
        public void run(){
            try {

                byte[] buffer_byte = new byte[1024];
                JSONObject json = null;
                int op = 10;
                while(ConnectSever.Isconnected)
                {
                    IsSeverRunning = true;
                    if(!LogInFlag)
                    {
                        try
                        {
                            op = 10;
                            int N = mInputStream.read(buffer_byte,0,1024);
                            if(N != 0)
                            {
                                String readbuff  = new String(buffer_byte,"UTF-8");

                                try
                                {
                                    JSONObject jsonbuff = new JSONObject(readbuff);
                                    json = jsonbuff;
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
                                    case 0:
                                        MessageClient += "\n无搜索结果";

                                        break;

                                    case 1:
                                        MessageClient = "输入Json格式数据: \n" + readbuff + "\n";
                                        String name = json.getString("bookname");
                                        String url = json.getString("booklocation");
                                        String num = json.getString("num");
                                        MessageClient +="\n 书名：" + name + "\n 藏书馆地址：" + url +"\n 剩余数量："+ num;
                                        BookInfo = "书名：" + name + "\n 藏书馆地址：" + url +"\n 剩余数量："+ num;
                                        break;
                                }
                            }
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
                IsSeverRunning = false;//服务器监听线程正常结束，关闭标志位
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT).show();
                return;
            }
        }

    };



    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0)
            {
                RecBox.append(MessageClient + "\n");	// 刷新

                List<ImageInfor> list = new ArrayList<>();
                list.add(new ImageInfor(R.mipmap.elian, BookInfo));
                myAdapter = new MyAdapter(list);
                mRecyclerView.setAdapter(myAdapter);

                MessageClient = "";
                BookInfo = "";
            }
            else if(msg.what == 1)
            {
                if (ConnectSever.Isconnected)
                {
                    final String msgText =SearchBar.getText().toString();//取得编辑框中我们输入的内容
                    if(msgText.length()<=0)
                    {
                        Toast.makeText(getApplicationContext(), "发送内容不能为空！", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        try
                        {
                            RecBox.append("正在搜索《"+ msgText+"》...\n");	// 刷新

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Map<String, String> map = new HashMap<String, String>();
                                        map.clear();
                                        map.put("BookName",msgText);
                                        map.put("op","2");
                                        //将json转化为String类型
                                        JSONObject json = new JSONObject(map);
                                        String jsonString = "";
                                        test = jsonString = json.toString();
                                        byte[] jsonByte = jsonString.getBytes("Utf-8");
                                        mOutputStream.write(jsonByte);
                                        mOutputStream.flush();
                                        MessageClient = "输出Json格式数据: \n" + test;

                                        Message msg0 = new Message();
                                        msg0.what = 0;
                                        mHandler.sendMessage(msg0);

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


    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.LogIn:
                if(!LogInSuccessFlag)
                {
                    Intent intent = new Intent(this , LogIn.class);
                    startActivity(intent);
                    LogInFlag = true;  // 竖起此标志位代表进入注册登录界面
                }
                else
                {
                    Intent intent = new Intent(this , Userpage.class);
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.Setting:
                Intent intent1 = new Intent(this , ConnectSever.class);
                startActivity(intent1);
                break;

            case R.id.Test:
                Intent intent2 = new Intent(this , VoiceTest.class);
                startActivity(intent2);
                break;
            default:
                break;
        }
        return false;
    }


    private void initDate() {
        //初始化sdk 将自己申请的appid放到下面
        //此句代码应该放在application中的，这里为了方便就直接放代码中了
        SpeechUtility.createUtility(this, "appid=5c26ca6b");
        speechRecognizer = SpeechRecognizer.createRecognizer(this, initListener);
        recognizerDialog = new RecognizerDialog(this, initListener);
        sharedPreferences = getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
        Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
        //这里我直接将引擎类型设置为云端，因为本地需要下载讯飞语记，这里为了方便直接使用云端
        //有需要的朋友可以加个单选框 让用户选择云端或本地
        mEngineType = SpeechConstant.TYPE_CLOUD;

    }


    public void requestAllPower() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            }
        }
    }

    //开始听写
    public void start(View view) {
        SearchBar.setText("");
        hashMap.clear();
        setParams();

        if (isShowDialog) {

            recognizerDialog.setListener(dialogListener);
            recognizerDialog.show();

        } else {
            ret = speechRecognizer.startListening(recognizerListener);
            if (ret != ErrorCode.SUCCESS) {
                Log.e("tag", "听写失败,错误码" + ret);
            }
        }

    }

    //初始化监听器
    private InitListener initListener = new InitListener() {
        @Override
        public void onInit(int i) {
            if (i != ErrorCode.SUCCESS) {
                Log.e("tag", "初始化失败，错误码" + i);
            }
        }
    };

    //无UI监听器
    private RecognizerListener recognizerListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(final int i, byte[] bytes) {
            Log.e("tag", "返回数据大小" + bytes.length);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //toast.setText("当前音量" + i);
                }
            });
        }

        @Override
        public void onBeginOfSpeech() {
            Log.e("tag", "开始说话");
        }

        @Override
        public void onEndOfSpeech() {
            Log.e("tag", "结束说话");

        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            if (recognizerResult != null) {
                Log.e("tag", "听写结果：" + recognizerResult.getResultString());
                printResult(recognizerResult);

            }

        }

        @Override
        public void onError(SpeechError speechError) {
            Log.e("tag", "错误信息" + speechError.getPlainDescription(true));

        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //  if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //      String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //      Log.d(TAG, "session id =" + sid);
            //  }
        }
    };

    //有UI监听器
    private RecognizerDialogListener dialogListener = new RecognizerDialogListener() {
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            if (recognizerResult != null) {
                Log.e("tag", "听写结果：" + recognizerResult.getResultString());
                printResult(recognizerResult);

            }
        }

        @Override
        public void onError(SpeechError speechError) {
            Log.e("tag", speechError.getPlainDescription(true));

        }
    };

    //输出结果，将返回的json字段解析并在SearchBar中显示
    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        hashMap.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : hashMap.keySet()) {
            resultBuffer.append(hashMap.get(key));
        }

        SearchBar.setText(resultBuffer.toString());
    }

    private void setParams() {
        //清空参数
        speechRecognizer.setParameter(SpeechConstant.PARAMS, null);
        //设置引擎
        speechRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        //设置返回数据类型
        speechRecognizer.setParameter(SpeechConstant.RESULT_TYPE, "json");
        //设置中文 普通话
        speechRecognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        speechRecognizer.setParameter(SpeechConstant.ACCENT, "mandarin");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        speechRecognizer.setParameter(SpeechConstant.VAD_BOS,
                sharedPreferences.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        speechRecognizer.setParameter(SpeechConstant.VAD_EOS,
                sharedPreferences.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        speechRecognizer.setParameter(SpeechConstant.ASR_PTT,
                sharedPreferences.getString("iat_punc_preference", "0"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        speechRecognizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        speechRecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                Environment.getExternalStorageDirectory() + "/msc/iat.wav");

    }



}
