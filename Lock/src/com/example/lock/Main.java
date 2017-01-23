package com.example.lock;

import com.example.lock.R;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class Main extends Activity{
	Button pairButton;
	Button watchButton;
	Button passwordSetButton;
	ImageView image;
	
	private static final String RECONNECT = "Socket Reconnect";
	
	private static final String TAG = "Main";
	public static final String PASSWORD_FILE = "passwordFile";
  	public String password;
	
//	private MyReceiver receiver = null;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.main);
		
		pairButton = (Button)this.findViewById(R.id.pair);
		watchButton = (Button)this.findViewById(R.id.watch);
		image = (ImageView)this.findViewById(R.id.imageView1);
		passwordSetButton = (Button)this.findViewById(R.id.passwordSet);
		

		//�������button����Pair�
		pairButton.setOnClickListener(new Button.OnClickListener(){  
			public void onClick(View v) {  
				Intent pairIntent = new Intent(Main.this, Pair.class);
				startActivity(pairIntent);
			}  
		});
		
		//���¼���button����Video�
		watchButton.setOnClickListener(new Button.OnClickListener(){  
			public void onClick(View v) {  
				Intent watchIntent = new Intent(Main.this, Video.class);
				startActivity(watchIntent);
			}  
		});
		
		//���²���button����Call�
		image.setOnClickListener(new Button.OnClickListener(){  
			public void onClick(View v) {  
				Intent testCallIntent = new Intent(Main.this, Call.class);
				startActivity(testCallIntent);
			}  
		});
		
		//������������button�����������û
		passwordSetButton.setOnClickListener(new Button.OnClickListener(){  
			public void onClick(View v) {  
				
				final EditText et = new EditText(Main.this);
				et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    			new AlertDialog.Builder(Main.this)
    			.setTitle("�����������")
    			.setMessage("��ʼ����Ϊadmin")
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
    						Intent passwordSetIntent = new Intent(Main.this, PasswordSet.class);
    						startActivity(passwordSetIntent);
    						
    					}	
    					
    					else{
    						dialog.dismiss();
    						new AlertDialog.Builder(Main.this)    	             
//    		                .setTitle("����")  	             
    		                .setMessage("������������޷��޸�����")  	            
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
    			.setNegativeButton("ȡ��",new DialogInterface.OnClickListener() {//���ȡ����ť  
    				@Override  
    				public void onClick(DialogInterface dialog, int which) {//ȷ����ť����Ӧ�¼�  
    					// TODO Auto-generated method stub  
    					dialog.dismiss();
    				}  
    			})  	
    			.show();
				

			}  
		});
		
		//ע��㲥������
//		  receiver = new MyReceiver();
//		  IntentFilter filter=new IntentFilter();
//		  filter.addAction("com.example.lock.SocketClientService");
//		  Main.this.registerReceiver(receiver,filter);
	}
	public void onResume(){
		super.onResume();
//		startService(new Intent(Main.this, ScreenLockService.class));

	        
/*	        //���������ڻ�ʧ�ܣ���ʾ�Ի�����������SocketClientService����
	        Intent reconnectIntent = getIntent();
	        String reconSig = reconnectIntent.getStringExtra("Reconnect");
	        Log.d(TAG, "reconnectIntent: "+reconSig);
	        
	        if(reconSig.equals(RECONNECT)){
				new AlertDialog.Builder(Main.this)    	             
	          .setTitle("�޷��������ڻ�")  	             
	          .setMessage("���������Ƿ������Լ�ip��ַ�Ƿ���ȷ����")  	            
	          .setPositiveButton("��������",new DialogInterface.OnClickListener() {//���ȷ����ť  
	          	@Override  
	          	public void onClick(DialogInterface dialog, int which) {//ȷ����ť����Ӧ�¼�  
	          		// TODO Auto-generated method stub  
	          		
	          		Intent socketIntent = new Intent(Main.this, SocketClientService.class);
	          		stopService(socketIntent);
	          		startService(socketIntent);
	          		dialog.dismiss();
	          	}  
	          })  	  
	          .setNegativeButton("����ip",new DialogInterface.OnClickListener() {//���ȡ����ť  
	          	@Override  
	          	public void onClick(DialogInterface dialog, int which) {//ȷ����ť����Ӧ�¼�  
	          		// TODO Auto-generated method stub  
	          		dialog.dismiss();
	    			Intent pairIntent = new Intent(Main.this, Pair.class);
	    			startActivity(pairIntent);
	          	}  
	          })  	
	          .show();
	        }*/
//		processServiceConnect();
	} 
	
	public void onDestory(){
		super.onDestroy();
//		Main.this.unregisterReceiver(receiver);
	}
	
/*	 public class MyReceiver extends BroadcastReceiver {
	     @Override
	     public void onReceive(Context context, Intent intent) {
//	      Bundle bundle = intent.getExtras();
//	      String reconSig = bundle.getString("Reconnect");
	      
	    //���������ڻ�ʧ�ܣ���ʾ�Ի�����������SocketClientService����
	      Log.d(TAG, "reconnectIntent: ");
//	        if(reconSig.equals(RECONNECT)){
					new AlertDialog.Builder(Main.this)    	             
		          .setTitle("�޷��������ڻ�")  	             
		          .setMessage("���������Ƿ������Լ�ip��ַ�Ƿ���ȷ����")  	            
		          .setPositiveButton("��������",new DialogInterface.OnClickListener() {//���ȷ����ť  
		          	@Override  
		          	public void onClick(DialogInterface dialog, int which) {//ȷ����ť����Ӧ�¼�  
		          		// TODO Auto-generated method stub  
		          		
		          		Intent socketIntent = new Intent(Main.this, SocketClientService.class);
		          		stopService(socketIntent);
		          		startService(socketIntent);
		          		dialog.dismiss();
		          	}  
		          })  	  
		          .setNegativeButton("����ip",new DialogInterface.OnClickListener() {//���ȡ����ť  
		          	@Override  
		          	public void onClick(DialogInterface dialog, int which) {//ȷ����ť����Ӧ�¼�  
		          		// TODO Auto-generated method stub  
		          		dialog.dismiss();
		    			Intent pairIntent = new Intent(Main.this, Pair.class);
		    			startActivity(pairIntent);
		          	}  
		          })  	
		          .show();
		        }
	         
	     //}
	 }*/
	
	//���ε�Back��
	public boolean onKeyDown(int keyCode ,KeyEvent event){
		
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
			return true ;

		else
			return super.onKeyDown(keyCode, event);
		
	}
	

}
