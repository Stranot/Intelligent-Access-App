package com.example.indoor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class Call extends Activity{
	Button hangupButton;
	Button answerButton;
	private MediaPlayer mMediaPlayer;
	private WakeLock wakeLock;
	
	Socket clientSocket = new Socket();
	SocketClientThread socketClientThread =new SocketClientThread();//�µ�socketThread����
	//////////////////////////////////////////
	public String ip = "192.168.191.4";/////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public final int port = 25000;
	
	private static final String TAG = "Indoor call";
	private static final String HANGUP = "HangUp";
	private static final String INDOOR_HANGUP = "IndoorHangUp";
	private static final int CALL_STOP = 0;
	
	public final long TIMER = 45000;//��ʱ45s
	
	private Vibrator vibrator;
	
	TimerThread timer = new TimerThread();
	
	public static SQLiteDatabase db;
	private static int myClickCount;
		
	//���������ķ���
    private void playLocalFile() {        
        mMediaPlayer = MediaPlayer.create(this,R.raw.ring);       
        try {    
        	mMediaPlayer.prepare();       
        }catch (IllegalStateException e) {                   
        }catch (IOException e) { 
        }
        mMediaPlayer.start();  
        
        Log.d("Call", "Ringplay");
    
    	mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {    
    		public void onCompletion(MediaPlayer mp) {  
    			// TODO Auto-generated method stub  
    			// ѭ������  
    			try {  
    				mp.start();  
    			} catch (IllegalStateException e) {  
            	// TODO Auto-generated catch block  
    				e.printStackTrace();  
    			}  
    		}  
    	});                          

    }
    
   
    private Handler handler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
/*    		 //��ʱ45s���Զ��Ҷ�
    		case 1:
    			stopService(new Intent(Call.this,ScreenLockService.class));//�ر�ScreenLockService���ص���������
				Intent hangupIntent = new Intent(Call.this, Main.class);
				startActivity(hangupIntent);
				break;*/
			//�Ҷ�
    		case CALL_STOP:
				stopService(new Intent(Call.this,ScreenLockService.class));//�ر�ScreenLockService���ص���������
				Intent mainIntent = new Intent(Call.this, Main.class);
//				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(mainIntent);
				break;   	
    			
    		}
    	}
    };

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d("Call", "Create");
		
//		Starting main = new Starting();
		db = SQLiteDatabase.openOrCreateDatabase(Call.this.getFilesDir().toString()
				+ "/network.dbs", null);
//		main.db = db;
		//��ѯ����α�  
		try{
			Cursor cursor = db.query   ("tb_user",null,null,null,null,null,null); 
			cursor.getCount();
			myClickCount++;
			int i= myClickCount-1;
			if(cursor.getCount()!=0){
				ip = query(db,i);
			}
		}catch(Exception e){
//			createDb();
		}
		
		socketClientThread.start();
		
		playLocalFile();//����
		
		//��

        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);  
        long [] pattern = 
        	{500, 500,
        	500, 500,};   // ֹͣ ���� ֹͣ ����   
        vibrator.vibrate(pattern, 1); 
//        vibrator.vibrate(TIMER);
		
		timer.start();//��ʱ��ʼ
		/*����ȫ�����ޱ���*/
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				   WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.call);	
				
		acquireWakeLock();//��Ļ��//////////////////////////////////////////////////////////////////////
	}
	
	public void onResume(){
		super.onResume();
		
		Log.d("Call", "Resume");
		acquireWakeLock();//��Ļ��/////////////////////////////////////////////////////////////////////
		
//		playLocalFile();//����
		
		//��ʱ45s�Զ��Ҷ�
		
		
		
		hangupButton = (Button)this.findViewById(R.id.hangup);
		answerButton = (Button)this.findViewById(R.id.answer);
		
		//���¹Ҷ�button���ص�Main���������Ϣ�Ҷ��ֻ�
		hangupButton.setOnClickListener(new Button.OnClickListener(){  
			public void onClick(View v) { 
				stopService(new Intent(Call.this,ScreenLockService.class));//�ر�ScreenLockService���ص���������
				Intent hangupIntent = new Intent(Call.this, Main.class);
				startActivity(hangupIntent);
				
				//������Ϣ�Ҷ��ֻ�
				if(clientSocket.isConnected()&&(!clientSocket.isClosed())){
	  	            try {
	  	            	//��ȡ����� 
	  					OutputStream out = clientSocket.getOutputStream();	  		            
	  		          //������Ϣ  
	  		           out.write((HANGUP+"\n").getBytes());  
	  		           out.flush();  

	  				} catch (IOException e) {
	  					// TODO Auto-generated catch block
	  					e.printStackTrace();
	  				}  

	  		    }
			}  
		});
		
		//���½�ͨbutton����video���棬������Ϣ�Ҷ��ֻ�
		answerButton.setOnClickListener(new Button.OnClickListener(){  
			public void onClick(View v) {  
				Intent answerIntent = new Intent(Call.this, Video_open.class);
				startActivity(answerIntent);
				
				//������Ϣ�Ҷ��ֻ�
				if(clientSocket.isConnected()&&(!clientSocket.isClosed())){
	  	            try {
	  	            	//��ȡ����� 
	  					OutputStream out = clientSocket.getOutputStream();	  		            
	  		          //������Ϣ  
	  		           out.write((HANGUP+"\n").getBytes());  
	  		           out.flush();  

	  				} catch (IOException e) {
	  					// TODO Auto-generated catch block
	  					e.printStackTrace();
	  				}  

	  		    }
			}  
		});
	} 
	
    public void onStop() {  
    	super.onStop();  
        // TODO Auto-generated method stub  
        // ����ֹͣʱֹͣ�������ֲ��ͷ���Դ  
//    	mMediaPlayer.stop();  
    	mMediaPlayer.release();
    	vibrator.cancel();
    	
    	timer.exit = true;
    	releaseWakeLock();/////////////////////////////////////////////////////////////////////
    	
    } 
    
    public void onDestroy() {  
    	super.onDestroy();  
        // TODO Auto-generated method stub  
        // ����ֹͣʱֹͣ�������ֲ��ͷ���Դ  
 //   	mMediaPlayer.stop();  
 //   	mMediaPlayer.release();
    	//�Ͽ�����
    	try {
    		clientSocket.close();
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	releaseWakeLock();//////////////////////////////////////////////////////////////////////
    	db.close();
    } 
    
	//���ε�Back��
	public boolean onKeyDown(int keyCode ,KeyEvent event){
		
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
			return true ;

		else
			return super.onKeyDown(keyCode, event);
		
	}
    
    
    private void acquireWakeLock() {
    	if (wakeLock == null) {
//    		Log.d("Acquiring wake lock", null);
    		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    		wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getCanonicalName());
    		wakeLock.acquire();
    	}

    	}


    private void releaseWakeLock() {
    	if (wakeLock !=null && wakeLock.isHeld()) {
    		wakeLock.release();
    		wakeLock =null;
    	}

    }
    
    //��ʱ45s�Ҷ�
    class TimerThread extends Thread{
    	public volatile boolean exit = false; 
    	public volatile int i = 0; 
		public void run(){
			while(!exit){
			try {  
                Thread.sleep(1);  
                i++;
                if(i >= TIMER){
                Message msg = new Message();  
                msg.what = CALL_STOP;  
                handler.sendMessage(msg);  
                System.out.println("send...");
                i = 0;
                }
            } catch (Exception e) {  
                // TODO Auto-generated catch block  
                e.printStackTrace();  
                System.out.println("thread error...");  
            }
			}
		}
    }
    
  //Socket����߳���
  	class SocketClientThread extends Thread {  
  		public void run(){
  			Log.d(TAG, "TreadStart");
			String line = null;  
			InputStream input;  
  			//���ӷ����� ���������ӳ�ʱΪ5��  
//  			clientSocket = new Socket();  
              try {
  				clientSocket.connect(new InetSocketAddress(ip, port), 5000);/////////////////////////////

  			} catch (IOException e) {
  				// TODO Auto-generated catch block
  				e.printStackTrace();
  			}
  		    if(clientSocket.isConnected()&&(!clientSocket.isClosed())){
  		    	Log.d(TAG, "Connected");
  		    	
//  		    	WriteThread writeThread =new WriteThread();
//  		    	writeThread.start();
  		    	
  		    	try {  

  					input = clientSocket.getInputStream();  
  					BufferedReader bff = new BufferedReader(  
  							new InputStreamReader(input));  
  					Log.d(TAG, "Try start");
  					
//  	            	Log.d(TAG, line); 
  					//���չҶ��źţ��Ҷ�
  					if ((line = bff.readLine()) != null) {            	
  						Log.d(TAG, line); 
  						if(line.equals(INDOOR_HANGUP)){
  							Message msg = new Message();
  							msg.what = CALL_STOP;
  							handler.sendMessage(msg);
  						}
  						
  					}  
  					else Log.d(TAG, "No input");
  					//�ر����������  
  					bff.close();  
  					input.close();  
  					clientSocket.close();  
  	  
  				} catch (IOException e) {  
//  	        		Log.d(TAG, "No input");
  					e.printStackTrace();  
  				}  

  		    }
  		    else {
  		    	Log.d(TAG, "Connecting failed");

  		    }
  		    		   

              

  		}
  	}
  	
	private String query(SQLiteDatabase db,int i )  
	{  
	   
		String pass = null;
	   //��ѯ����α�  
	   Cursor cursor = db.query   ("tb_user",null,null,null,null,null,null); 
	   int j,k;
	   //����ܵ���������
	   j=cursor.getCount();
	   k=i%j;
	   if(cursor.moveToFirst()) {  
		   		//�ƶ���ָ����¼ 
		   		cursor.move(k);  
	            //����û���  
//	            String username=cursor.getString(cursor.getColumnIndex("name")); 
	            pass=cursor.getString(cursor.getColumnIndex("password")); 
	            //����û���Ϣ  
	           System.out.println(pass);  
	           final String[] strs = new String[] {pass};
	           //����
//	           MAClist.setAdapter(new ArrayAdapter<String>(this,
//	                   android.R.layout.simple_list_item_1,strs));
	           
	       }  
	   
	   return pass;
	 }   
	
	public void createDb() {
		db.execSQL("create table tb_user( name varchar(30) primary key,password varchar(30))");
	}
    
}
