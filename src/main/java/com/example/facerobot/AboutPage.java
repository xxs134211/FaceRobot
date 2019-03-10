package com.example.facerobot;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;


public class AboutPage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(Color.WHITE);
        // 设置状态栏字体黑色

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.about_page);
    }
       // Button button = (Button)findViewById(R.id.button);
    public void onClick(View view)
    {
        String mTV="1342110378";
        Toast ntoast=Toast.makeText(AboutPage.this,mTV,Toast.LENGTH_LONG);
        String mTV1="12345678";
        ntoast.makeText(AboutPage.this,mTV1,Toast.LENGTH_SHORT).show();
    }






}
