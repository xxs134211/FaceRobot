package com.example.facerobot;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;


public class LaunchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        //全屏
       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //加载启动界面
        setContentView(R.layout.activity_launch);

        Button buton2 =findViewById(R.id.button2);
        buton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivity(new Intent(LaunchActivity.this, Main.class));
                LaunchActivity.this.finish();
            }

        });


        //设置等待一段时间进入首页
        // Integer time = 5000;    //设置等待时间，单位为毫秒
        // Handler handler = new Handler();
        //当计时结束时，跳转至主界面
        //       handler.postDelayed(new Runnable() {
        //           @Override
//            public void run() {
//                startActivity(new Intent(LaunchActivity.this, Main.class));
//                LaunchActivity.this.finish();
//            }
//        }, time);
//    }
    }



    private void setLightMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            // 设置状态栏底色白色
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(Color.WHITE);
            // 设置状态栏字体黑色

            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

}