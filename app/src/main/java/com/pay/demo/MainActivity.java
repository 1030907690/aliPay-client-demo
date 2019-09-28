package com.pay.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Button btn;

    /**下单地址**/
    private final String UNIFIED_ORDER_URL = "http://192.168.0.111:8080/pay/unifiedOrder";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.alipay_btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Runnable payRunnable = new Runnable() {

                    @Override
                    public void run() {


                        OkHttpClient client = new OkHttpClient();
                        //构造Request对象
                        //采用建造者模式，链式调用指明进行Get请求,传入Get的请求地址
                        Request request = new Request.Builder().get().url(UNIFIED_ORDER_URL).build();
                        Call call = client.newCall(request);
                        //异步调用并设置回调函数
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();

                            }

                            @Override
                            public void onResponse(Call call, final Response response) throws IOException {
                                final String responseStr = response.body().string();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d("responseStr ",responseStr);
                                        String orderInfo = responseStr;
                                        PayTask alipay = new PayTask(MainActivity.this);
                                        Map<String,String> result = alipay.payV2(orderInfo,true);

                                        Message msg = mHandler.obtainMessage();
                                        msg.what = 0;
                                        msg.obj = result;
                                        mHandler.sendMessage(msg);
                                    }
                                });
                            }
                        });


                    }
                };
                // 必须异步调用
                Thread payThread = new Thread(payRunnable);
                payThread.start();
            }
        });

    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Map<String,String> result = ((Map<String,String>) msg.obj);
            Toast.makeText(MainActivity.this, result.toString(),
                    Toast.LENGTH_LONG).show();
        };
    };
}
