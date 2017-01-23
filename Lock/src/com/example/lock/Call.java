package com.example.lock;

import java.io.IOException;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
	
	public final long TIMER = 45000;//��ʱ45s
	
	private Vibrator vibrator;
	
	TimerThread timer = new TimerThread();
	
	private boolean isplayRing = false;
		
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
    	isplayRing = true;

    }
    
    //��ʱ45s���Զ��Ҷ�
    private Handler handler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
    		case 1:
    			stopService(new Intent(Call.this,ScreenLockService.class));//�ر�ScreenLockService���ص���������
				Intent hangupIntent = new Intent(Call.this, Main.class);
				startActivity(hangupIntent);
				break;
			default:
				break;
    			
    		}
    	}
    };

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d("Call", "Create");
		
		
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
		
//		playLocalFile();//����
		
		Log.d("Call", "Resume");
		acquireWakeLock();//��Ļ��/////////////////////////////////////////////////////////////////////
				
		hangupButton = (Button)this.findViewById(R.id.hangup);
		answerButton = (Button)this.findViewById(R.id.answer);
		
		//���¹Ҷ�button���ص�Main�,����ֻ��Ҷ�
		hangupButton.setOnClickListener(new Button.OnClickListener(){  
			public void onClick(View v) { 
				stopService(new Intent(Call.this,ScreenLockService.class));//�ر�ScreenLockService���ص���������
				Intent hangupIntent = new Intent(Call.this, Main.class);
				startActivity(hangupIntent);
				
				//���͹㲥��CallListenerServer,��ֻ��Ҷ�
				Intent intent = new Intent("android.com.lock.INDOORHANGUP");
				sendBroadcast(intent);
			}  
		});
		
		//���½�ͨbutton����video����,����ֻ��Ҷ�
		answerButton.setOnClickListener(new Button.OnClickListener(){  
			public void onClick(View v) {  
				Intent answerIntent = new Intent(Call.this, Video_open.class);
				startActivity(answerIntent);
				
				//���͹㲥��CallListenerServer,��ֻ��Ҷ�
				Intent intent = new Intent("android.com.lock.INDOORHANGUP");
				sendBroadcast(intent);
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
//    	mMediaPlayer.stop();  
//    	mMediaPlayer.release();
    	
    	releaseWakeLock();//////////////////////////////////////////////////////////////////////
    	
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
                msg.what = 1;  
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
    
}
