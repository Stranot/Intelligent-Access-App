package com.example.lock;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;  
import android.view.View;  
import android.widget.Button;  
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;  

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.example.lock.VideoSurfaceView;
import com.example.lock.R;
  
public class Video extends Activity {  
/** Called when the activity is first created. */  
  
	Button openButton;  
	Button backButton;  
	VideoView videoView;  
	
	private static String clientId = "PiCommanderAndroid";
	private static MqttClient mqttClient;
	
    public static final String TOPIC = "PiCommander";
    public static final String TOPIC_STATUS = "PiCommanderStatus";
    public static final int QOS = 2;
    public static final int TIMEOUT = 3;
    public final String gpio_lock = "GPIO 10";
    public final String gpio_outdoor = "GPIO 11";
    public final String port = "1883";
//    public final String ip = "192.168.191.3";////////////////////////////////////////////////
    public String ip;
    
  //Ĭ�������ip��ַ
  	public static final String OUTIP_ADDRESS = "192.168.191.3";
  	public static final String OUTIP_ADDRESS_FILE = "outipAddressFile";
  	public static final String PASSWORD_FILE = "passwordFile";
  	public String password;
    
    private static final String LOG_TAG = "Video";
    
    //Thread��Handler���
    public static final int UPDATE_CON_THREAD_SUC = 0;
    public static final int UPDATE_CON_THREAD_FAIL = 1;
    public static final int UPDATE_DIA_THREAD = 2;
    
    public boolean openClick = false;
      
    private Handler handler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
    		//��ʾ����
    		case UPDATE_CON_THREAD_SUC:
    			Toast.makeText(Video.this, "���ӳɹ�",
    	                  Toast.LENGTH_SHORT).show();
    			break;
    		//��ʾ����ʧ��
    		case UPDATE_CON_THREAD_FAIL:
    			Toast.makeText(Video.this, "��ǰ���粻���ã��������Ƿ���wifi��ip��ַ�Ƿ�������ȷ",
    	    			Toast.LENGTH_LONG).show();
    			break;
    		//�������ŶԻ���
    		case UPDATE_DIA_THREAD:
    			
    			new AlertDialog.Builder(Video.this)    	             
	             
    		    .setMessage("��Ԫ¥�Ŵ򿪳ɹ�")  
    		    .setCancelable(false)
    		    .setPositiveButton("ȷ��",new DialogInterface.OnClickListener() {//���ȷ����ť  
    		    	@Override  
    		        public void onClick(DialogInterface dialog, int which) {//ȷ����ť����Ӧ�¼�  
    		        // TODO Auto-generated method stub  
    		                		
    		        stopService(new Intent(Video.this,ScreenLockService.class));//�ر�ScreenLockService���ص���������  
    		                		
    		        	Intent backIntent = new Intent(Video.this, Main.class);
    		            startActivity(backIntent); 
    		            }  
    		        })  	            
    		        .show();
    					
    			break;
    			
    		}
    	}
    };
    
	@Override  
	public void onCreate(Bundle savedInstanceState) {  
		super.onCreate(savedInstanceState); 
		
		//��SharedPreference�л�������ip
		SharedPreferences outip_settings = getSharedPreferences(OUTIP_ADDRESS_FILE, 0);
		ip = outip_settings.getString("outip_address", OUTIP_ADDRESS);
		// ȫ������
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.video_watch); 
//		videoView = (VideoView)this.findViewById(R.id.videoView);  
		
		// ����SurfaceView��ַ����
		VideoSurfaceView r = (VideoSurfaceView) findViewById(R.id.SurfaceViewVideo);
		if (r == null) {
			Log.e("r", "r == null");
		}
		r.setMontiorAddress("http://"+ip+":8090/?action=stream");
		Log.i("address", "setMontiorAddress complete");
		
		new Thread(new Runnable(){
			@Override
			public void run(){
				processConnect(ip, port);//���������
				if (mqttClient.isConnected()){
					Message message = new Message();
					message.what = UPDATE_CON_THREAD_SUC;
					handler.sendMessage(message);
				}
				else {
					Message message = new Message();
					message.what = UPDATE_CON_THREAD_FAIL;
					handler.sendMessage(message);
				}
			}
		}).start();
		
/*		if (!mqttClient.isConnected()) {
			Toast.makeText(this, "Connected",
                  Toast.LENGTH_LONG).show();
		}
		else Toast.makeText(this, "Connecting failed",
    			Toast.LENGTH_LONG).show();*/
		
		
//		PlayRtspStream("rtsp://192.168.191.5:8554/video.sdp"); 
//		PlayRtspStream("android.resource://" + getPackageName() + "/" +R.raw.video);
		 
		openButton = (Button)this.findViewById(R.id.open);  
		openButton.setOnClickListener(new Button.OnClickListener(){  
		//���¿���button�����Ϳ�����Ϣ	
		public void onClick(View v) {  		
			
			openClick = true;
			
            final EditText et = new EditText(Video.this);
			et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
			new AlertDialog.Builder(Video.this)
			.setTitle("����������")
			.setView(et)
			.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {   				
				@Override      				
            	public void onClick(DialogInterface dialog, int which) {
					String inputPassword = et.getText().toString();
					SharedPreferences password_settings = getSharedPreferences(PASSWORD_FILE, 0);
					password = password_settings.getString("password", PASSWORD_FILE);
					Log.d("PassSet","input"+inputPassword);
					Log.d("PassSet","Real"+password);
					if(inputPassword.equals(password)){
						
						
						dialog.dismiss();
						String content = gpio_lock + "," + "OFF";						
						
						MqttMessage message = new MqttMessage(content.getBytes());
			            message.setQos(QOS);

			            try {
			                mqttClient.publish(TOPIC, message);
			            }
			            catch (MqttException me) {
			                Log.d(LOG_TAG, me.toString());
			            }
			            
			            new Thread(){
			            	 public void run() {
			            		 Log.d(LOG_TAG, "close door thread start");
			            		 try {
									Thread.sleep(5000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
			            		 Log.d(LOG_TAG, "close door thread run");
			            		 String content = gpio_lock + "," + "ON";						
			         			
			         			MqttMessage message = new MqttMessage(content.getBytes());
			                     message.setQos(QOS);

			                     try {
			                    	 
			                         mqttClient.publish(TOPIC, message);
			                     }
			                     catch (MqttException me) {
			                         Log.d(LOG_TAG, me.toString());
			                     }
			            	 }
			            }.start();
			            
					}	
					
					else{
						dialog.dismiss();
						new AlertDialog.Builder(Video.this)    	             
//		                .setTitle("����")  	             
		                .setMessage("����������������¿���")  	            
		                .setPositiveButton("ȷ��",new DialogInterface.OnClickListener() {//���ȷ����ť  
		                	@Override  
		                	public void onClick(DialogInterface dialog, int which) {//ȷ����ť����Ӧ�¼�  
		                		// TODO Auto-generated method stub  
		                		dialog.dismiss();
		                	}  
		                })  	            
		                .show();
					}
				}
			})
			.setNegativeButton("ȡ��", null)
			.show();
		}  
	});  
		
		//���ذ���back�����ص�������Main
		backButton = (Button)this.findViewById(R.id.back);  
		backButton.setOnClickListener(new Button.OnClickListener(){  
		public void onClick(View v) {  
//			finish();
			Intent backIntent = new Intent(Video.this, Main.class);
			startActivity(backIntent);
		}  
	});  
  
}  
	 
	//play video 
/*	private void PlayRtspStream(String Url){  
		videoView.setVideoURI(Uri.parse(Url));  
		videoView.requestFocus();  
		videoView.start();  
	}  */
	

	
	//���������
	private void processConnect(String brokerIp, String brokerPort) {
        String broker = "tcp://" + brokerIp + ":" + brokerPort;

        try {
            clientId = clientId + System.currentTimeMillis();

            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(true);
            mqttConnectOptions.setConnectionTimeout(TIMEOUT);

            mqttClient = new MqttClient(broker, clientId, new MemoryPersistence());
            mqttClient.setCallback(new MqttCallbackHandler());
            mqttClient.connect(mqttConnectOptions);
            mqttClient.subscribe(TOPIC_STATUS);

//            controllerCommandAdapter.refresh();
//            listenerCommandAdapter.refresh();

//            main.setBackgroundResource(R.drawable.background_enable);
//            connect_imageview.setImageResource(R.drawable.mq_connected);
            
//            Toast.makeText(this, "Connected",
//                    Toast.LENGTH_LONG).show();

        }
        catch (MqttException me) {
//        	Toast.makeText(this, "Connecting failed",
//        			Toast.LENGTH_LONG).show();
        }
	}
	
	//�ֹͣ���Ͽ�����
/*	public void onStop() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
            }
            catch (MqttException me) {
                Log.d(LOG_TAG, me.toString());
            }
        }

        super.onStop();
    }*/
	public void onDestroy() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
            }
            catch (MqttException me) {
                Log.d(LOG_TAG, me.toString());
            }
        }

        super.onDestroy();
	}
	
	
	//�Ŵ򿪺󣬵���ȷ�϶Ի���
	private class MqttCallbackHandler implements MqttCallback {

        @Override
        public void connectionLost(Throwable throwable) {
            Log.d(LOG_TAG, throwable.toString());
        }

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
            final String message = new String(mqttMessage.getPayload());
            final String[] messageArray = message.split(",");

            if (!(messageArray.length == 2)) {
                Log.d(LOG_TAG, "Message context malformed");
                return;
            }
            if(message.equals(gpio_lock + "," + "OFF")&&(openClick == true)){
            	Log.d(LOG_TAG, "message");
            	new Thread(new Runnable() {
            		@Override
            		public void run() {
            			Message message = new Message();
            			message.what = UPDATE_DIA_THREAD;
            			handler.sendMessage(message);
            		}
            
            	}).start();
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            //
        }

    }
	
	//���ε�Back��
	public boolean onKeyDown(int keyCode ,KeyEvent event){
		
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
			return true ;

		else
			return super.onKeyDown(keyCode, event);
		
	}
	
	
}  































