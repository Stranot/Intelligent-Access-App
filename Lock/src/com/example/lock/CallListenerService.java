package com.example.lock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class CallListenerService extends Service{
	
	public final int port = 25000;
	ServerSocket server = null;
	ServerAcceptThread serverAcceptThread =new ServerAcceptThread();
	Socket serverSocket = new Socket();
	private static final String TAG = "CallListenerService";
	
	private static final int CALL_START = 1;
	private static final int CALL_STOP_FROM_INDOOR = 2;
	private static final String HANGUP = "HangUp";
	private static final String INDOORHANGUP = "IndoorHangUp";
	
	private IntentFilter intentFilter;
	private IndoorHangupReceiver indoorHangupReceiver;
	
	private String ScreenStatus = "ON";
	
	private Handler handler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
    		//�������ӿ�ʼCall�
    		case CALL_START:
    			if(ScreenStatus.equals("ON")){
        			Intent callIntent = new Intent(CallListenerService.this, Call.class);
        			callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        			startActivity(callIntent);
        			  	
          	  }
          	//��Ļ��������ScreenLockService�����ɸ÷�����ScreenLock�
          	  else {
/*           		  Intent screenIntent = new Intent(getBaseContext(), ScreenLock.class);///////////////////////////////
          		  screenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          	  	  getApplication().startService(screenIntent);*/
          		  Intent screenLockIntent = new Intent(CallListenerService.this, ScreenLockService.class);
          		  screenLockIntent.putExtra("ScreenStatusJudge", ScreenStatus);
          	  	  startService(screenLockIntent);
          	  	  
          	  }
	    	break;	
    		
			case CALL_STOP_FROM_INDOOR:
/*				Intent mainIntent = new Intent(CallListenerService.this, Main.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(mainIntent);*/
				stopService(new Intent(CallListenerService.this,ScreenLockService.class));//�ر�ScreenLockService���ص���������
				Intent hangupIntent = new Intent(CallListenerService.this, Main.class);
				hangupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(hangupIntent);
				break;   		  

    		}
    	}
    };
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate(){
		super.onCreate();
		
		//ע��Ҷ����ڻ��㲥
		intentFilter = new IntentFilter();
		intentFilter.addAction("android.com.lock.INDOORHANGUP");
		indoorHangupReceiver = new IndoorHangupReceiver();
		registerReceiver(indoorHangupReceiver, intentFilter);
		
		/*ע��㲥*/
		IntentFilter mScreenOnFilter = new IntentFilter("android.intent.action.SCREEN_ON");
		CallListenerService.this.registerReceiver(mScreenOnReceiver, mScreenOnFilter);
		
		/*ע��㲥*/
		IntentFilter mScreenOffFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
		CallListenerService.this.registerReceiver(mScreenOffReceiver, mScreenOffFilter);
		
		Log.d(TAG, "Create");
		serverAcceptThread.start();//ѭ����������
		
	}
	
	public void onDestory(){
		super.onDestroy();
//		stopThread = true;//�ر��߳�
		
		//�Ͽ�����
		try {
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
class ServerAcceptThread extends Thread {  
		
		public void run(){
			try {
				server = new ServerSocket(port);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
/*			try {
				server.setSoTimeout(11000);
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
			while(!(server.isClosed())){
				try {
					
					serverSocket = server.accept();
					new Thread(new readRunable(serverSocket)).start(); 
					Message msg = new Message();
					msg.what = CALL_START;
					handler.sendMessage(msg);
//					new Thread(new HeartRunable(serverSocket)).start();  
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public class readRunable implements Runnable {  
	  
		Socket serverSocket = null;  
  
		public readRunable(Socket serverSocket) {  
			this.serverSocket = serverSocket;  
		}  
  
		@Override  
		public void run() {  
			// ��android�ͻ������hello worild  
			Log.d(TAG, "Thread Run");
			String line = null;  
			InputStream input;  
			OutputStream output;  
//    	    String str = "hello world!";  
			try {  

				input = serverSocket.getInputStream();  
				BufferedReader bff = new BufferedReader(  
						new InputStreamReader(input));  
				Log.d(TAG, "Try start");

				
//            	Log.d(TAG, line); 
				//���չҶ��źţ��Ҷ�
				if ((line = bff.readLine()) != null) {            	
					Log.d(TAG, line); 
					if(line.equals(HANGUP)){
						Message msg = new Message();
						msg.what = CALL_STOP_FROM_INDOOR;//���յ��Ҷ��ź���ͬ
						handler.sendMessage(msg);
					}
					
				}  
				else Log.d(TAG, "No input");
				//�ر����������  

				bff.close();  
				input.close();  
				serverSocket.close();  
  
			} catch (IOException e) {  
//        		Log.d(TAG, "No input");
				e.printStackTrace();  
			}  
  
		}  
	} 

	
	
	class IndoorHangupReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			try {
	            	//��ȡ����� 
				   OutputStream out = serverSocket.getOutputStream();	  		            
		          //������Ϣ �ԹҶϷֻ�
		           out.write((INDOORHANGUP+"\n").getBytes());  
		           out.flush();  

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
		}
		
	}
	
	//��Ļ�����Ĺ㲥,����Ҫ����Ĭ�ϵ���������
		private BroadcastReceiver mScreenOnReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context , Intent intent) {
				
	            Log.i(TAG, intent.getAction());

				if(intent.getAction().equals("android.intent.action.SCREEN_ON")){
					Log.i(TAG, "----------------- android.intent.action.SCREEN_ON------");
					ScreenStatus = "ON";
				}
			}
			
		};
		
		//��Ļ�䰵/�����Ĺ㲥 �� ����Ҫ����KeyguardManager����Ӧ����ȥ�����Ļ����
		private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context , Intent intent) {
				String action = intent.getAction() ;
				
			    Log.i(TAG, intent.toString());
			    
				if(action.equals("android.intent.action.SCREEN_OFF")){
					ScreenStatus = "OFF";
				}
			}
			
		};
	
	

}

