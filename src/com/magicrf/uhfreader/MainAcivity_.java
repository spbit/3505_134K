package com.magicrf.uhfreader;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.zyapi.CommonApi;
import android.zyapi.Conversion;

import com.android.hdhe125klf.reader.R;

public class MainAcivity_ extends Activity implements OnItemSelectedListener, OnClickListener {
	private final static String Tag = "MainActivity" ;
	CommonApi mCommonApi;  
	private String[] mode = {"in","out"};  

	private String[] seral = null; 
	
	private String[] baudrate = {"9600"
			};

	private Spinner mModeSpinner; 
	private Spinner mSerialSpinner;
	private Spinner mBaudrateSpinner;
	private Button mStateBtn;
	private Boolean isInput = false;
	private Boolean isOut = false;  
	private EditText mPinEdt;
	private TextView mStateTex;
	private Button mOnoffSerialBtn;
	private Button mSendBtn;
	private TextView mRecvTex;
	private EditText mSendValueEdi;
	private Button mCleanBtn;
	private CheckBox mHexbox;
	private int mComFd =-1;
	private String mCommPort = "/dev/ttyMT1";
	private int mBaudrate = 9600;
	private boolean isOpen = false;
	private final int MAX_RECV_BUF_SIZE = 512;
	private String pin;
	private byte [] recv;
	private final static int  SHOW_RECV_DATA = 18;
	private String strRead;
	private CheckBox mHexshow;
	private MediaPlayer player;

	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case SHOW_RECV_DATA:
				String s =(String) msg.obj;  
				if(s!=null){
					mRecvTex.append(s+"");
					Log.e("", "1111:"+s);
					player.start();
				}
				break;
      case 101:
//    	  Toast.makeText(MainActivity.this, "发送数据", 0).show();
    	  send(Conversion.HexString2Bytes("1234567"));
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jy_layout);
		getDevices();
		findviews();
		setlistner();
		
		mCommonApi=new CommonApi();
			
		mCommonApi.setGpioDir(5,0);
		mCommonApi.getGpioIn(5);
			
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mCommonApi.setGpioDir(5,1);
				int ret =mCommonApi.setGpioOut(5,1);
				
//				mCommonApi.setGpioDir(58,1);
//				mCommonApi.setGpioOut(58,1);
				
				if(ret == 0){   
					Toast.makeText(MainAcivity_.this, "Set Success" , Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(MainAcivity_.this, "Set Fail" , Toast.LENGTH_SHORT).show();
				}
			}
		}, 1000);
		
		player = MediaPlayer.create(getApplicationContext(), R.raw.msg);
		
//		mCommonApi.setGpioDir(79,0);
//		mCommonApi.getGpioIn(79);
//			
//		new Handler().postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				mCommonApi.setGpioDir(79,1);
//				int ret =mCommonApi.setGpioOut(79,1);
//				
//				if(ret == 0){   
//					Toast.makeText(MainAcivity_.this, "Set Success" , Toast.LENGTH_SHORT).show();
//				}else{
//					Toast.makeText(MainAcivity_.this, "Set Fail" , Toast.LENGTH_SHORT).show();
//				}
//			}
//		}, 1000);
		
	}

	private void findviews(){

		mModeSpinner = (Spinner) findViewById(R.id.gpiospinner1);
		mSerialSpinner = (Spinner) findViewById(R.id.serialspinner);
		mBaudrateSpinner =  (Spinner) findViewById(R.id.baudratespinner);

		ArrayAdapter<String> gpioadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mode);
		ArrayAdapter<String> serialadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, seral);
		ArrayAdapter<String> baudratedapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, baudrate);

		gpioadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		serialadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		baudratedapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		mModeSpinner.setAdapter(gpioadapter);
		mSerialSpinner.setAdapter(serialadapter);
		mBaudrateSpinner.setAdapter(baudratedapter);

		mStateBtn = (Button) findViewById(R.id.statebtn);  
		mPinEdt = (EditText) findViewById(R.id.gpiopin);
		mStateTex = (TextView) findViewById(R.id.statetex);
		mRecvTex = (TextView) findViewById(R.id.recvtex);
		mSendValueEdi = (EditText) findViewById(R.id.sendvalue);
		mCleanBtn = (Button) findViewById(R.id.cleanvalue);
		mHexbox = (CheckBox) findViewById(R.id.hex);
		mHexshow = (CheckBox) findViewById(R.id.showhex);
		
		mOnoffSerialBtn = (Button) findViewById(R.id.onoffserial);
		mOnoffSerialBtn.setOnClickListener(this);
		mSendBtn = (Button) findViewById(R.id.sendbtn);
		mSendBtn.setVisibility(View.INVISIBLE);

		mCommonApi= new CommonApi();
	}   

	private void setlistner(){
		mModeSpinner.setOnItemSelectedListener(this);
		mSerialSpinner.setOnItemSelectedListener(this);
		mBaudrateSpinner.setOnItemSelectedListener(this);
		mSendBtn.setOnClickListener(this);		
		mStateBtn.setOnClickListener(this);
		mCleanBtn.setOnClickListener(this);
	}  

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		switch (parent.getId()) {
		case R.id.gpiospinner1:
			if(position == 0){
				mStateBtn.setText("get"+"");
				isInput = true;
				isOut = false;  
			}else if(position == 1){
				mStateBtn.setText("set"+"");
				isInput = false;
				isOut = true;
			}
			break;  
		case R.id.serialspinner:
			mCommPort = seral[position];  
			break; 
		case R.id.baudratespinner: 
			mBaudrate = Integer.parseInt(baudrate[position]);
			break;
		default:			
			break;
		}


	}
	@Override  
	public void onNothingSelected(AdapterView<?> parent) {  
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.statebtn:
			pin = mPinEdt.getText().toString();  
			if(pin.equals("")){
				Toast.makeText(this, "please pin number",Toast.LENGTH_SHORT ).show();
			}else{ 
				if(isInput && !isOut ){  
					mCommonApi.setGpioDir(Integer.parseInt(pin),0);
					int state =  mCommonApi.getGpioIn(Integer.parseInt(pin));
					mStateTex.setText(state +"");
				}else {
					Log.d(Tag, "mCommonApi = "+mCommonApi);
					setstateDia();		
				}
			} 
			break; 
		case R.id.onoffserial:  
			if(!isOpen){
//				Log.d(Tag, "mCommPort = " +mCommPort+" mBaudrate =" +mBaudrate);
				
				mComFd = mCommonApi.openCom("/dev/ttyMT1", 9600, 8, 'N', 1);
				
				if(mComFd > 0){
					Toast.makeText(this, "Open the serial port successfully", Toast.LENGTH_SHORT).show();
					isOpen = true;
					mOnoffSerialBtn.setText("close"+"");  
					mSendBtn.setVisibility(View.VISIBLE);
					mBaudrateSpinner.setEnabled(false); 
					mSerialSpinner.setEnabled(false);
					
//					send(new byte[]{(byte) 0xaa});
					
					readData();
				}else{
					Toast.makeText(this, "Open the serial port fail", Toast.LENGTH_SHORT).show();
					isOpen = false;
				}  
			}else{
				mCommonApi.closeCom(mComFd);   
				mOnoffSerialBtn.setText("open"+"");
				mSendBtn.setVisibility(View.INVISIBLE);
				isOpen = false;
				mBaudrateSpinner.setEnabled(true);  
				mSerialSpinner.setEnabled(true);
			}
			break;
		case R.id.sendbtn:
			Log.d(Tag, "点了发送");
			sendData();
			break;    
		case R.id.cleanvalue:
			mRecvTex.setText("");
			break;
		default:  
			break;
		}
	}  

	private void sendData() {
		// TODO Auto-generated method stub
		Log.d(Tag, "进来sendData");
		try{
			Log.d(Tag, "进来String");
			String sendvalue = "$R#";
//			String sendvalue = mSendValueEdi.getText().toString();
//			if(mHexbox.isChecked()){  
//				
//				Log.d(Tag, "进来send");
//				send(Conversion.HexString2Bytes(sendvalue));
//			}else{ 
				send(sendvalue.getBytes());
//			}  
		}catch(Exception e){    
			e.printStackTrace();
		}
	}

	/**
	 *获取串口设备名称 
	 */
	public void getDevices() {
		Vector<File> mDevices = null;
		if (mDevices == null) {
			mDevices = new Vector<File>();
			File dev = new File("/dev");
			File[] files = dev.listFiles();  
//			for (int i = 0; i < files.length; i++) {
//				if (files[i].getAbsolutePath().startsWith("/dev/ttyM")||files[i].getAbsolutePath().startsWith("/dev/ttyG")) {
					mDevices.add(new File("/dev/ttyMT0"));  
					mDevices.add(new File("/dev/ttyMT1"));  
					mDevices.add(new File("/dev/ttyMT2"));  
					mDevices.add(new File("/dev/ttyMT3"));  
					mDevices.add(new File("/dev/ttyMT4"));  
//				}  
//			} 
		}    
		for(int i = 0;i < mDevices.size();i++){ 
			if(seral == null){
				seral = new String[ mDevices.size()];	
			}	  
			seral[i] = mDevices.get(i).toString();
		}
	}


	private void setstateDia() {  
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(MainAcivity_.this);
		builder.setTitle("请选择要设置的状态");
		final String[] states = {"高", "低"}; 
		builder.setItems(states, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				Log.d(Tag, "which = " +which);
				if(which == 0){
					mCommonApi.setGpioDir(Integer.parseInt(pin),1);
					int  ret = mCommonApi.setGpioOut(Integer.parseInt(pin),1);
					if(ret == 0){   
						Toast.makeText(MainAcivity_.this, "设置成功" , Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(MainAcivity_.this, "设置失败" , Toast.LENGTH_SHORT).show();
					}
				}else if(which == 1){   
					mCommonApi.setGpioDir(Integer.parseInt(pin),1); 
					int  ret = mCommonApi.setGpioOut(Integer.parseInt(pin),0); 
					if(ret == 0){
						Toast.makeText(MainAcivity_.this, "设置成功" , Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(MainAcivity_.this, "设置失败" , Toast.LENGTH_SHORT).show();
					}
				}  
			}
		});
		builder.show();
	}

	/**
	 * 读数据线程
	 */
	private void readData(){
		new Thread(){
			public void run(){
				while(isOpen){  
					int ret = 0;
					byte[] buf = new byte[MAX_RECV_BUF_SIZE+1];
					Log.d(Tag, "mComfd = " +mComFd);
					ret = mCommonApi.readComEx(mComFd, buf, MAX_RECV_BUF_SIZE, 0, 0);
					if (ret <= 0) {
						Log.d(Tag,"read failed!!!! ret:"+ret);
						try {
							sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						continue;
					}  
					recv = new byte[ret];
					System.arraycopy(buf, 0, recv, 0, ret);
//					if(mHexshow.isChecked()){
//						strRead = Conversion.Bytes2HexString(recv);
//					}else {    
						try {
							strRead = new String(recv,"gb2312");
							Log.e("", "strRead:"+strRead);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//					}	  
					Log.d(Tag,"~~~Read["+ret+"]:"+strRead);  
					if(strRead!=null){
						Message msg = handler.obtainMessage(SHOW_RECV_DATA);
						msg.obj=strRead;
						msg.sendToTarget();
					}
				}				 
			}
		}.start();
	}

	/**
	 * 发送数据
	 */
	private void send(byte[]data){
		if(data==null)return;
		if(mComFd>0){
//			Log.d(Tag,"发送数据"+"data = "+data);  
//			mCommonApi.writeCom(mComFd, data, data.length);
//			Log.d(Tag,"发送数据~~~~finish");
//			
//			Message msg = new Message();
//			msg.what = 101;  
//			handler.sendMessageDelayed(msg, 60000); //1分钟发一次
		} 
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();  
		//close readThread
		handler.removeMessages(0);
//		if(mComFd>0){
		    mCommonApi.setGpioDir(5,0);
		    mCommonApi.setGpioOut(5,0);
//		    mCommonApi.setGpioDir(58,0);
//		    mCommonApi.setGpioOut(58,0);
		
//	    mCommonApi.setGpioDir(79,0);
//	    mCommonApi.setGpioOut(79,0);
			mCommonApi.closeCom(mComFd);
			isOpen =false;
			Toast.makeText(this, "exit", 0).show();
//		}  
	}            

}
