package com.example.facerobot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Login extends Activity {
	Button yes;
	Button no;
	EditText cmd_name;
	EditText cmd_content;
	public final static int RESULT_CODE=1000;  
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		setTitle("\u3000\u3000\u3000\u3000\u3000\u3000"+"表情代码设置");
		yes = (Button) findViewById(R.id.yes_button);
		no  = (Button) findViewById(R.id.cancle_button);
		cmd_name = (EditText)findViewById(R.id.cmd_name);
		cmd_content = (EditText)findViewById(R.id.cmd_content);
		Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        String str=bundle.getString("cmd_name");
        cmd_name.setText(str);
        str=bundle.getString("cmd_content"); 
        cmd_content.setText(str);
		yes.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent();
	            intent.putExtra("cmd_name", cmd_name.getText().toString());  
	            intent.putExtra("cmd_content", cmd_content.getText().toString());  
	            setResult(RESULT_CODE, intent);  
	            finish();  
			}
		});
		no.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent();
	            intent.putExtra("cmd_name1", cmd_name.getText().toString());  
	            intent.putExtra("cmd_content1", cmd_content.getText().toString());  
	            setResult(-1, intent);  
	            finish();  
			}
		});
	}
	
}
