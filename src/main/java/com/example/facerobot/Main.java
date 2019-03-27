package com.example.facerobot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * <p>
 * Title: Calculator.java<／p>
 * <p>
 * Description: <／p>
 * <p>
 * Copyright: Copyright (c) 2014<／p>
 * 
 * @author Kevin Xu
 * @date Jan 7, 2014
 * @version 1.6
 */
@SuppressLint("ShowToast")
public class Main extends Activity {
//	private TextView tv_show;
	 // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_INPUT_MSG = 3;
    
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    private int btState = STATE_NONE;
	private final static int REQUEST_CODE=1000;  
	String cmd_name[] = new String[16];
	String cmd_content[] = new String[16];
	Button button[] = new Button[16];
	ToggleButton tog[] = new ToggleButton[16];
	int tog_flag[] = new int[16];
	TextView pos_text = null;
	TextView sudu_text = null;
	int tog_num = 0;
	int long_press_button_num = -1;
	SharedPreferences preference;
	SharedPreferences.Editor editor;
	private ToggleButton TbnBt;
	private Button btnBtSearch;
	private SeekBar mSeekBar1 = null;
	private SeekBar mSeekBar2 = null;
		//蓝牙相关
	private static BluetoothAdapter btAdapter = null;
	private static BluetoothDevice btDevice = null;
	private static BluetoothService btService = null;
	String message, inputMsgString="";
	byte[] outMsgBuffer, inputMsgBuffer;
	int msgLength, inputMsgLen = 0;
	private static String remoteDeviceAddress, remoteDeviceName;
	 //定时器相关
    private Handler myHandler=null;
    private Runnable myRunnable=null;
    private static final int HANDLER_TIME = 1000;
    public static final String PREFER_NAME = "com.iflytek.setting";
    private static String TAG = "motor";
    private static String TAG1 = "action";
	private Context context;

	// 语音听写对象

	private SpeechRecognizer mIat;

	// 语音听写UI

	private RecognizerDialog mIatDialog;

	// 用HashMap存储听写结果

	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();



	private Toast mToast;



	private Button btStart,btStop,btCancel;

	private EditText etContent;

	private SharedPreferences mSharedPreferences;

	private int ret = 0; // 函数调用返回值


	/**

	 * 初始化监听器。

	 */

	private InitListener mInitListener = new InitListener() {



		@Override

		public void onInit(int code) {

			Log.d("SpeechRecognizer init() code = " + code,"1");

			if (code != ErrorCode.SUCCESS) {

				showTip("初始化失败，错误码：" + code);

			}

		}

	};


	// 引擎类型

	private String mEngineType = SpeechConstant.TYPE_CLOUD;

	Toast toast;


	//读写权限 具体权限加在字符串里面
	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.RECORD_AUDIO,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.READ_EXTERNAL_STORAGE
	};

	//请求状态码
	private static int REQUEST_PERMISSION_CODE = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		//透明导航栏
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

		//设置状态栏背景颜色
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//		getWindow().setStatusBarColor(Color.WHITE);
//		// 设置状态栏字体黑色
//		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
		//得到当前界面的装饰视图

		if (ActivityCompat.checkSelfPermission(Main.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(Main.this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
		}




		toast  = Toast.makeText(this, "hello", Toast.LENGTH_SHORT);
        TabHost m = (TabHost)findViewById(R.id.tabhost);
        m.setup();  
          
        LayoutInflater i= LayoutInflater.from(this);
        i.inflate(R.layout.activity_main, m.getTabContentView());
        i.inflate(R.layout.activity_main1, m.getTabContentView());//动态载入XML，而不需要Activity
          
        m.addTab(m.newTabSpec("tab1").setIndicator("表情组控制").setContent(R.id.LinearLayout01));
        m.addTab(m.newTabSpec("tab2").setIndicator("单一器官控制").setContent(R.id.LinearLayout02));
		preference = getSharedPreferences("record", MODE_PRIVATE);
		editor = preference.edit();

		initData();

		findViewById();

		setOnclickListener();

		if (preference.getString("cmd_name[0]", null) == null)
		{
			editor.putString("cmd_name[0]", "表情1");
			editor.putString("cmd_name[1]", "表情2");
			editor.putString("cmd_name[2]", "表情3");
			editor.putString("cmd_name[3]", "表情4");
			
			editor.putString("cmd_name[4]", "表情5");
			editor.putString("cmd_name[5]", "表情6");
			editor.putString("cmd_name[6]", "表情7");
			editor.putString("cmd_name[7]", "表情8");
			
			editor.putString("cmd_name[8]", "表情9");
			editor.putString("cmd_name[9]", "表情10");
			editor.putString("cmd_name[10]", "表情11");
			editor.putString("cmd_name[11]", "表情12");
			
			editor.putString("cmd_name[12]", "表情13");
			editor.putString("cmd_name[13]", "表情14");
			editor.putString("cmd_name[14]", "复位");
			editor.putString("cmd_name[15]", "停止");
			
			editor.putString("cmd_content[0]", "#1GC1");
			editor.putString("cmd_content[1]", "#2GC1");
			editor.putString("cmd_content[2]", "#3GC1");
			editor.putString("cmd_content[3]", "#4GC1");
			
			editor.putString("cmd_content[4]", "#5GC1");
			editor.putString("cmd_content[5]", "#6GC1");
			editor.putString("cmd_content[6]", "#7GC1");
			editor.putString("cmd_content[7]", "#8GC1");
			
			editor.putString("cmd_content[8]", "#9GC1");
			editor.putString("cmd_content[9]", "#10GC1");
			editor.putString("cmd_content[10]", "#11GC1");
			editor.putString("cmd_content[11]", "#12GC1");
			
			editor.putString("cmd_content[12]", "#13GC1");
			editor.putString("cmd_content[13]", "#14GC1");
			editor.putString("cmd_content[14]", "#15GC1");
			editor.putString("cmd_content[15]", "#16GC1");
			editor.commit();
		}
		cmd_name[0] = preference.getString("cmd_name[0]", null);
		cmd_name[1] = preference.getString("cmd_name[1]", null);
		cmd_name[2] = preference.getString("cmd_name[2]", null);
		cmd_name[3] = preference.getString("cmd_name[3]", null);
		cmd_name[4] = preference.getString("cmd_name[4]", null);
		cmd_name[5] = preference.getString("cmd_name[5]", null);
		cmd_name[6] = preference.getString("cmd_name[6]", null);
		cmd_name[7] = preference.getString("cmd_name[7]", null);
		cmd_name[8] = preference.getString("cmd_name[8]", null);
		cmd_name[9] = preference.getString("cmd_name[9]", null);
		cmd_name[10] = preference.getString("cmd_name[10]", null);
		cmd_name[11] = preference.getString("cmd_name[11]", null);
		cmd_name[12] = preference.getString("cmd_name[12]", null);
		cmd_name[13] = preference.getString("cmd_name[13]", null);
		cmd_name[14] = preference.getString("cmd_name[14]", null);
		cmd_name[15] = preference.getString("cmd_name[15]", null);
		
		cmd_content[0] = preference.getString("cmd_content[0]", null);
		cmd_content[1] = preference.getString("cmd_content[1]", null);
		cmd_content[2] = preference.getString("cmd_content[2]", null);
		cmd_content[3] = preference.getString("cmd_content[3]", null);
		cmd_content[4] = preference.getString("cmd_content[4]", null);
		cmd_content[5] = preference.getString("cmd_content[5]", null);
		cmd_content[6] = preference.getString("cmd_content[6]", null);
		cmd_content[7] = preference.getString("cmd_content[7]", null);
		cmd_content[8] = preference.getString("cmd_content[8]", null);
		cmd_content[9] = preference.getString("cmd_content[9]", null);
		cmd_content[10] = preference.getString("cmd_content[10]", null);
		cmd_content[11] = preference.getString("cmd_content[11]", null);
		cmd_content[12] = preference.getString("cmd_content[12]", null);
		cmd_content[13] = preference.getString("cmd_content[13]", null);
		cmd_content[14] = preference.getString("cmd_content[14]", null);
		cmd_content[15] = preference.getString("cmd_content[15]", null);
		initView();

		mToast = (Toast) Toast.makeText(this, "", Toast.LENGTH_SHORT);
		mSharedPreferences = getSharedPreferences(PREFER_NAME, Activity.MODE_PRIVATE);

		//蓝牙初始化
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		TbnBt = (ToggleButton)findViewById(R.id.TbnBt);
		if(btAdapter.getState() == BluetoothAdapter.STATE_ON){
			TbnBt.setChecked(true);
		}else {
			TbnBt.setChecked(false);
		}
		TbnBt.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(TbnBt.isChecked()) {
					if(btAdapter.getState() == BluetoothAdapter.STATE_OFF) {
						btAdapter.enable();
			            //setUpBtServer();
			            
					} 
				} else {
					btAdapter.disable();
					//destroyBtServer();
				}
				
			}
		});
		btnBtSearch = (Button)findViewById(R.id.BtnBtSearch);
		btnBtSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(btAdapter.getState() == BluetoothAdapter.STATE_OFF) {
	        		DisplayToast("蓝牙未打开！");//TODO
	        	} else {
	            	Intent serverIntent = new Intent(Main.this, DeviceListActivity.class);
	                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	        	}
			}
		});
	}

	private void initView() {
		button[0] =findViewById(R.id.btn0);
		button[0].setText(cmd_name[0]);
		button[0].setOnClickListener(new key_press());
		button[0].setOnLongClickListener(new key_longpress());
		button[1] = findViewById(R.id.btn1);
		button[1].setText(cmd_name[1]);
		button[1].setOnClickListener(new key_press());
		button[1].setOnLongClickListener(new key_longpress());
		button[2] = findViewById(R.id.btn2);
		button[2].setText(cmd_name[2]);
		button[2].setOnClickListener(new key_press());
		button[2].setOnLongClickListener(new key_longpress());
		button[3] = findViewById(R.id.btn3);
		button[3].setText(cmd_name[3]);
		button[3].setOnClickListener(new key_press());
		button[3].setOnLongClickListener(new key_longpress());
		button[4] = findViewById(R.id.btn4);
		button[4].setText(cmd_name[4]);
		button[4].setOnClickListener(new key_press());
		button[4].setOnLongClickListener(new key_longpress());
		button[5] = findViewById(R.id.btn5);
		button[5].setText(cmd_name[5]);
		button[5].setOnClickListener(new key_press());
		button[5].setOnLongClickListener(new key_longpress());
		button[6] = findViewById(R.id.btn6);
		button[6].setText(cmd_name[6]);
		button[6].setOnClickListener(new key_press());
		button[6].setOnLongClickListener(new key_longpress());
		button[7] = findViewById(R.id.btn7);
		button[7].setText(cmd_name[7]);
		button[7].setOnClickListener(new key_press());
		button[7].setOnLongClickListener(new key_longpress());
		button[8] = findViewById(R.id.btn8);
		button[8].setText(cmd_name[8]);
		button[8].setOnClickListener(new key_press());
		button[8].setOnLongClickListener(new key_longpress());
		button[9] = findViewById(R.id.btn9);
		button[9].setText(cmd_name[9]);
		button[9].setOnClickListener(new key_press());
		button[9].setOnLongClickListener(new key_longpress());
		button[10] = findViewById(R.id.btn_A);
		button[10].setText(cmd_name[10]);
		button[10].setOnClickListener(new key_press());
		button[10].setOnLongClickListener(new key_longpress());
		button[11] = findViewById(R.id.btn_B);
		button[11].setText(cmd_name[11]);
		button[11].setOnClickListener(new key_press());
		button[11].setOnLongClickListener(new key_longpress());
		button[12] = findViewById(R.id.btn_C);
		button[12].setText(cmd_name[12]);
		button[12].setOnClickListener(new key_press());
		button[12].setOnLongClickListener(new key_longpress());
		button[13] = findViewById(R.id.btn_D);
		button[13].setText(cmd_name[13]);
		button[13].setOnClickListener(new key_press());
		button[13].setOnLongClickListener(new key_longpress());
		button[14] = findViewById(R.id.btn_E);
		button[14].setText(cmd_name[14]);
		button[14].setOnClickListener(new key_press());
		button[14].setOnLongClickListener(new key_longpress());
		button[15] = findViewById(R.id.btn_F);
		button[15].setText(cmd_name[15]);
		button[15].setOnClickListener(new key_press());
		button[15].setOnLongClickListener(new key_longpress());
		
		tog[0] = (ToggleButton) findViewById(R.id.tog1);
		tog[0].setOnClickListener(new tog_key_press());
		
		tog[1] = (ToggleButton) findViewById(R.id.tog2);
		tog[1].setOnClickListener(new tog_key_press());
		
		tog[2] = (ToggleButton) findViewById(R.id.tog3);
		tog[2].setOnClickListener(new tog_key_press());
		
		tog[3] = (ToggleButton) findViewById(R.id.tog4);
		tog[3].setOnClickListener(new tog_key_press());
		
		tog[4] = (ToggleButton) findViewById(R.id.tog5);
		tog[4].setOnClickListener(new tog_key_press());
		
		tog[5] = (ToggleButton) findViewById(R.id.tog6);
		tog[5].setOnClickListener(new tog_key_press());
		
		tog[6] = (ToggleButton) findViewById(R.id.tog7);
		tog[6].setOnClickListener(new tog_key_press());
		
		tog[7] = (ToggleButton) findViewById(R.id.tog8);
		tog[7].setOnClickListener(new tog_key_press());
		
		tog[8] = (ToggleButton) findViewById(R.id.tog9);
		tog[8].setOnClickListener(new tog_key_press());
		
		tog[9] = (ToggleButton) findViewById(R.id.tog10);
		tog[9].setOnClickListener(new tog_key_press());
		
		tog[10] = (ToggleButton) findViewById(R.id.tog11);
		tog[10].setOnClickListener(new tog_key_press());
		
		tog[11] = (ToggleButton) findViewById(R.id.tog12);
		tog[11].setOnClickListener(new tog_key_press());
		
		tog[12] = (ToggleButton) findViewById(R.id.tog13);
		tog[12].setOnClickListener(new tog_key_press());
		
		tog[13] = (ToggleButton) findViewById(R.id.tog14);
		tog[13].setOnClickListener(new tog_key_press());
		
		tog[14] = (ToggleButton) findViewById(R.id.tog15);
		tog[14].setOnClickListener(new tog_key_press());
		
		tog[15] = (ToggleButton) findViewById(R.id.tog16);
		tog[15].setOnClickListener(new tog_key_press());
		pos_text = (TextView) findViewById(R.id.pos_text);
		sudu_text = (TextView) findViewById(R.id.sudu_text);
		mSeekBar1  = (SeekBar)findViewById(R.id.seekBar_pos);
		mSeekBar2  = (SeekBar)findViewById(R.id.seekBar_sudu);
		// 绑定进度条
		mSeekBar1.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			
			String cmd = null;
			int process1;
			int angle = 0;
			@Override
			public void onProgressChanged(SeekBar arg0, int process, boolean arg2) {
//				if(btState == STATE_CONNECTED)
				{
					
					if (tog_num != 0 && tog[tog_num-1].isChecked() == true)
					{
						//process1 = mSeekBar2.getProgress()*10;
						cmd = "#" + tog_num + "P" + (process + 500) + "T"+ 100 ;
						SendDnCmd(cmd);
						System.out.println("## tog_num = " + tog_num + " " + (tog[tog_num-1].isChecked()));
						angle = (process - 1000) * 90 / 1000;
						pos_text.setText("角度:"+angle);
						
					}
					else {
						DisplayToast("请选择控制舵机！");
					}
					
				}	
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

				System.out.println("拖动开始...");
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				System.out.println("拖动停止...");
//				if(false == mRunStatus){
//					Toast.makeText(MainActivity.this, "开关没有打开！", Toast.LENGTH_SHORT).show();
//				}
			}
			
			} );
		mSeekBar2.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			int sudu = 0;
			@Override
			public void onProgressChanged(SeekBar arg0, int process, boolean arg2) {
//				if(btState == STATE_CONNECTED)
				{
					
						sudu = process;
						sudu_text.setText("速度:"+sudu);
						
					
				}	
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

				System.out.println("拖动开始...");
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				System.out.println("拖动停止...");
//				if(false == mRunStatus){
//					Toast.makeText(Main.this, "开关没有打开！", Toast.LENGTH_SHORT).show();
//				}
			}
			
			} );
	}
	
	public class key_longpress implements OnLongClickListener
	{

	
		@Override
		public boolean onLongClick(View v) {
			// TODO Auto-generated method stub
			Button btn = (Button) v;
			
			switch (v.getId()) {
			case R.id.btn0:
				long_press_button_num = 0;		
				break;
			case R.id.btn1:
				long_press_button_num = 1;
				break;
			case R.id.btn2:
				long_press_button_num = 2;
				break;
			case R.id.btn3:long_press_button_num = 3;
				break;
			case R.id.btn4:long_press_button_num = 4;
				break;
			case R.id.btn5:long_press_button_num = 5;
				break;
			case R.id.btn6:long_press_button_num = 6;
				break;
			case R.id.btn7:long_press_button_num = 7;
				break;
			case R.id.btn8:long_press_button_num = 8;
				break;
			case R.id.btn9:long_press_button_num = 9;
				break;

			case R.id.btn_A:long_press_button_num = 10;
				
				break;
			case R.id.btn_B:long_press_button_num = 11;

				break;
			case R.id.btn_C:long_press_button_num = 12;
				
				break;
			case R.id.btn_D:long_press_button_num = 13;

				break;
			case R.id.btn_E:long_press_button_num = 14;

				break;
			case R.id.btn_F:long_press_button_num = 15;
				break;
			}
			Intent intent = new Intent(Main.this,Login.class);
			intent.putExtra("cmd_name", cmd_name[long_press_button_num]);  
			intent.putExtra("cmd_content", cmd_content[long_press_button_num]);  
            startActivityForResult(intent, REQUEST_CODE); 
			return false;
		}
		
		
	}
	public class tog_key_press implements OnClickListener
	{

		@Override
		public void onClick(View v) {
			
			ToggleButton btn = (ToggleButton) v;
				int id = 0;
				switch (v.getId()) {
				case R.id.tog1:
					id = 1;
					break;
				case R.id.tog2:
					id = 2;
					break;
				case R.id.tog3:
					id = 3;
					break;
				case R.id.tog4:
					id = 4;
					break;
				case R.id.tog5:id = 5;
					break;
				case R.id.tog6:id = 6;
					break;
				case R.id.tog7:id = 7;
					break;
				case R.id.tog8:id = 8;
					break;
				case R.id.tog9:id = 9;
					break;
				case R.id.tog10:id = 10;
					break;

				case R.id.tog11:id = 11;
					
					break;
				case R.id.tog12:id = 12;

					break;
				case R.id.tog13:id = 13;
					break;
				case R.id.tog14:id = 14;
					break;
				case R.id.tog15:id = 15;
					break;
				case R.id.tog16:id = 16;
					break;
				
				default:
					break;
				}
				if (id> 0 && id != tog_num && tog_num != 0)
				{
					tog[tog_num-1].setChecked(false);
					
				}
				if (id > 0)
					tog_num  = id;
				//SendDnCmd(cmd_content[id]);
			
		}
		
	}
	public class key_press implements OnClickListener
	{

		@Override
		public void onClick(View v) {
			
				Button btn = (Button) v;
				int id = 0;
				switch (v.getId()) {
				case R.id.btn0:
					id = 0;
					 //System.out.println("short press");
					break;
				case R.id.btn1:
					id = 1;
					break;
				case R.id.btn2:
					id = 2;
					break;
				case R.id.btn3:
					id = 3;
					break;
				case R.id.btn4:id = 4;
					break;
				case R.id.btn5:id = 5;
					break;
				case R.id.btn6:id = 6;
					break;
				case R.id.btn7:id = 7;
					break;
				case R.id.btn8:id = 8;
					break;
				case R.id.btn9:id = 9;
					break;

				case R.id.btn_A:id = 10;
					
					break;
				case R.id.btn_B:id = 11;

					break;
				case R.id.btn_C:id = 12;
					
					break;
				case R.id.btn_D:id = 13;

					break;
				case R.id.btn_E:id = 14;

					break;
				case R.id.btn_F:id = 15;

					break;
				
				default:
					break;
				}
				SendDnCmd(cmd_content[id]);
			
		}
		
	}
	@Override
    public void onStart() {
        super.onStart();
       
        if (btAdapter.isEnabled()) {
            if (btService == null) {
            	setUpBtServer();
            }
        }
        //DisplayToast("onStart");
    }
	private void setUpBtServer() {
		if(btService == null){
			btService = new BluetoothService(this, btHandler);	
		}
	}
	@Override
	protected synchronized void onResume() {
		super.onResume();
		
		//开启蓝牙服务
		if (btService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (btService.getState() == BluetoothService.STATE_NONE) {
              // Start the Bluetooth chat services
            	btService.start();
            }
		} else {
			if (btAdapter.isEnabled()) {
	            if (btService == null) {
	            	setUpBtServer();
	            	if (btService.getState() == BluetoothService.STATE_NONE) {
	                    // Start the Bluetooth chat services
	                  	btService.start();
	                  }
	            }
	        }
		}
		//DisplayToast("onResume");
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(btService != null){
			destroyBtServer();
		}
		//btAdapter.disable();//关闭APP关闭蓝牙
		//DisplayToast("onDestroy");
		super.onDestroy();
	}
	private void destroyBtServer() {
		if(btService != null){
			btService.stop();
			btService = null;
		}
	}
	private final Handler btHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                //if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
                	//DisplayToast("已连接");
                	btState = STATE_CONNECTED;
                	btnBtSearch.setText("已连接");
                	btnBtSearch.setTextColor(Color.GREEN);
                	break;
                case BluetoothService.STATE_CONNECTING:
                	//DisplayToast("正在连接。。。");
                	btState = STATE_CONNECTING;
                	btnBtSearch.setText("正在连接...");
                	btnBtSearch.setTextColor(Color.YELLOW);
                    break;
                case BluetoothService.STATE_LISTEN:
                case BluetoothService.STATE_NONE:
                	//DisplayToast("无连接");
                	btState = STATE_NONE;  
                	btnBtSearch.setText("无法连接");
                	btnBtSearch.setTextColor(Color.RED);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                //byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                //String writeMessage = new String(writeBuf);
                //mConversationArrayAdapter.add("Me:  " + writeMessage);
            	//EdtDownCmd.setHint("send success!");
            	
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                inputMsgString += readMessage;
                String temp = "";
                if('>' == inputMsgString.charAt(inputMsgString.length()-1)) {
               // if(inputMsgString.contains(">")) {	
                	//DisplayToast("OK");
                	//DisplayToast(inputMsgString);
//                	if('<' == inputMsgString.charAt(0) && '.' == inputMsgString.charAt(3) ) {
//                		if(null != EdtUpCmd && null != ChkUpCmd && !ChkUpCmd.isChecked()){
//                			//EdtUpCmd.setText(inputMsgString);
//                			temp = inputMsgString.substring(1, 6) + " Mpa";	
//                			DisplayUpCmd(temp);
//                		}
//                	}
//                	inputMsgString = "";
                }
            break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                //Toast.makeText(getApplicationContext(), "Connected to "
                //              + remoteDeviceName, Toast.LENGTH_SHORT).show();
            break;
            case MESSAGE_TOAST:
                //Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                //               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
	
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
            	
            	btAdapter.cancelDiscovery();
            	remoteDeviceName = intent.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            	
                // Get the BLuetoothDevice object
            	remoteDeviceAddress = remoteDeviceName.substring(remoteDeviceName.length()-17);
            	remoteDeviceName = remoteDeviceName.substring(0, remoteDeviceName.length()-18);
            	btDevice = btAdapter.getRemoteDevice(remoteDeviceAddress);
            	
            	// Attempt to connect to the device
            	setUpBtServer();
            	btService.connect(btDevice);   
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                //Bluetooth is now enabled, so set up a chat session
            	//setUpBtServer();
            	
            } else {
            }
            break;
            
	    case REQUEST_INPUT_MSG:
	    break;
	    case REQUEST_CODE:
	    	  if (resultCode == 1000) {
	                //Bluetooth is now enabled, so set up a chat session
	            	//setUpBtServer();	         					   
					  String str;
					  Bundle bundle=intent.getExtras();
					  if (bundle == null)
						  return;
					  String name =bundle.getString("cmd_name");
					  String content =bundle.getString("cmd_content");
					  if (name == null || content == null)
						  return;
					  cmd_name[long_press_button_num] = name;
					  str = "cmd_name[" + long_press_button_num + "]";
					  editor.putString(str, name);
					  
					  cmd_content[long_press_button_num] = content;
					  button[long_press_button_num] .setText(name); 
					  str = "cmd_content[" + long_press_button_num + "]";
					  editor.putString(str, content);
					editor.commit();
	    	  }
	    	break;
        }//end of switch
        //DisplayToast("onActivityResult");
        
    }
    
   
	  //公用接口
  	public void DisplayToast(String str){
  		toast.setText(str);
      	//toast.setGravity(Gravity.TOP, 0, 220);
      	toast.show();
      	
      }
  	public void SendDnCmd(String str) {
		if(btState == STATE_CONNECTED) {
			outMsgBuffer = (str + "\r\n").getBytes();
			btService.write(outMsgBuffer);
			
		} else {
			DisplayToast("蓝牙未连接！");
		}
		System.out.println(str+ "\r\n");
	}
	 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId()==R.id.action_about) {
			openAboutPage();
		       return  true;}
			else
				openHelpPage();
			   return  true;

		}



	private void openAboutPage() {
		Intent intent = new Intent(Main.this, AboutPage.class);
		startActivity(intent);
	}
	private void openHelpPage() {
		Intent intent = new Intent(Main.this, HelpPage.class);
		startActivity(intent);
	}
	private void showTip(final String str) {
		toast.setText(str);
		//toast.setGravity(Gravity.TOP, 0, 220);
		toast.show();
	}
	private void setOnclickListener(){

		btStart.setOnClickListener(new View.OnClickListener() {

			@Override

			public void onClick(View v) {

				checkSoIsInstallSucceed();

				etContent.setText(null);// 清空显示内容

				mIatResults.clear();

				// 设置参数

				setParam();

				boolean isShowDialog = mSharedPreferences.getBoolean(

						getString(R.string.pref_key_iat_show), true);

				if (isShowDialog) {

					// 显示听写对话框

					mIatDialog.setListener(mRecognizerDialogListener);

					mIatDialog.show();

					showTip(getString(R.string.text_begin));

				} else {

					// 不显示听写对话框

					ret = mIat.startListening(mRecognizerListener);

					if (ret != ErrorCode.SUCCESS) {

						showTip("听写失败,错误码：" + ret);

					} else {

						showTip(getString(R.string.text_begin));

					}

				}

			}

		});








	}



	private void findViewById(){

		btStart = (Button) findViewById(R.id.btn_start);



		etContent = (EditText) findViewById(R.id.et_content);

	}



	private void initData(){

		context = Main.this;

		// 初始化识别无UI识别对象

		// 使用SpeechRecognizer对象，可根据回调消息自定义界面；

		mIat = SpeechRecognizer.createRecognizer(context, mInitListener);

		// 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer

		// 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源

		mIatDialog = new RecognizerDialog(context, mInitListener);

		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

		mSharedPreferences = getSharedPreferences(Main.PREFER_NAME,

				Activity.MODE_PRIVATE);

	}



	private void checkSoIsInstallSucceed(){

		if( null == mIat ){

			// 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688

			this.showTip( "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化" );

			return;

		}

	}







	/**

	 * 参数设置

	 *

	 * @param

	 * @return

	 */

	public void setParam() {

		// 清空参数

		mIat.setParameter(SpeechConstant.PARAMS, null);



		// 设置听写引擎

		mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);

		// 设置返回结果格式

		mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");



		String lag = mSharedPreferences.getString("iat_language_preference",

				"mandarin");

		if (lag.equals("en_us")) {

			// 设置语言

			mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");

		} else {

			// 设置语言

			mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");

			// 设置语言区域

			mIat.setParameter(SpeechConstant.ACCENT, lag);

		}



		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理

		mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));



		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音

		mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));



		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点

		mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "0"));



		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限

		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效

		mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");

		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");

	}



	/**

	 * 听写UI监听器

	 */

	private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {

		public void onResult(RecognizerResult results, boolean isLast) {

			printResult(results);

		}



		/**

		 * 识别回调错误.

		 */

		public void onError(SpeechError error) {

			showTip(error.getPlainDescription(true));

		}



	};







	/**

	 * 听写监听器。

	 */

	private RecognizerListener mRecognizerListener = new RecognizerListener() {



		@Override

		public void onBeginOfSpeech() {

			// 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入

			showTip("开始说话");

		}



		@Override

		public void onError(SpeechError error) {

			// Tips：

			// 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。

			// 如果使用本地功能（语记）需要提示用户开启语记的录音权限。

			showTip(error.getPlainDescription(true));

		}



		@Override

		public void onEndOfSpeech() {

			// 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入

			showTip("结束说话");

		}



		@Override

		public void onResult(RecognizerResult results, boolean isLast) {

			Log.d(results.getResultString(),"2");

			printResult(results);



			if (isLast) {

				// TODO 最后的结果

			}

		}



		@Override

		public void onVolumeChanged(int volume, byte[] data) {

			showTip("当前正在说话，音量大小：" + volume);

			Log.d("返回音频数据："+data.length,"3");

		}



		@Override

		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因

			// 若使用本地能力，会话id为null

			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {

			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);

			//		Log.d(TAG, "session id =" + sid);

			//	}

		}

	};



	private void printResult(RecognizerResult results) {
		Log.i(TAG1, "执行printresult");

		String text = JsonParser.parseIatResult(results.getResultString());



		String sn = null;

		// 读取json结果中的sn字段

		try {

			JSONObject resultJson = new JSONObject(results.getResultString());

			sn = resultJson.optString("sn");

		} catch (JSONException e) {

			e.printStackTrace();

		}



		mIatResults.put(sn, text);



		StringBuffer resultBuffer = new StringBuffer();

		for (String key : mIatResults.keySet()) {

			resultBuffer.append(mIatResults.get(key));

		}
		String temp;
		temp = resultBuffer.toString();
		//showTip(temp);

		Log.d(TAG1, "接收到：" + temp);
		System.out.println("开始发送数据"+ "\r\n");


		if (temp.contains("微笑")) {
			SendDnCmd("#0GC1" );
			showTip("已开始执行");


		}else if (temp.contains("舵机")) {

			showTip("开始执行");
			SendDnCmd("#1GC1" );


		} else if (temp.contains("自动模式")) {

			showTip("已选择自动模式");



		} else if (temp.contains("手动模式")) {

			showTip("已选择手动模式");



		}


		 etContent.setText(resultBuffer.toString());


		etContent.setSelection(etContent.length());

	}
	private void delay(int ms){

		try {

			Thread.currentThread();

			Thread.sleep(ms);

		} catch (InterruptedException e) {

			e.printStackTrace();

		}

	}



}
