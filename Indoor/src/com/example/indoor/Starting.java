package com.example.indoor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;

public class Starting extends Activity{
	
	public static SQLiteDatabase db;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState); 
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);	
		setContentView(R.layout.starting);
		
		db = SQLiteDatabase.openOrCreateDatabase(Starting.this.getFilesDir().toString()
				+ "/network.dbs", null);//�������ݿ⣬����Զ�˴�����mac��ip
		try{
			createDb();
		}catch(Exception e){
			Log.d("Start", "Db table exists");
		}
		
	}	
	
	public void onStart(){
		super.onStart();
		
//		Intent screenServiceIntent = new Intent(Starting.this, ScreenLockService.class);
//		startService(screenServiceIntent);
		
		if(!isWiFiActive()){
			new AlertDialog.Builder(Starting.this)    	             
//          .setTitle("����")  	      
			.setCancelable(false)
          .setMessage("�������wifi")  	            
          .setPositiveButton("ȷ��",new DialogInterface.OnClickListener() {//���ȷ����ť  
          	@Override  
          	public void onClick(DialogInterface dialog, int which) {//ȷ����ť����Ӧ�¼�  
          		// TODO Auto-generated method stub  
          		Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
          		startActivity(intent);
          	}  
          })  	  
          .setNegativeButton("ȡ��",new DialogInterface.OnClickListener() {//���ȡ����ť  
          	@Override  
          	public void onClick(DialogInterface dialog, int which) {//ȷ����ť����Ӧ�¼�  
          		// TODO Auto-generated method stub  
          		dialog.dismiss();
    			Intent mainIntent = new Intent(Starting.this, Main.class);
    			startActivity(mainIntent);
          	}  
          })  	
          .show();
		}
		else {
			new Thread() {
	            @Override
	            public void run() {
	            	try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    			Intent mainIntent = new Intent(Starting.this, Main.class);
	    			startActivity(mainIntent);
	            }
	        }.start();

		}
				
		
		//����socket����
		Intent socketIntent = new Intent(Starting.this, SocketServerService.class);
		startService(socketIntent);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		db.close();
	}

	
	public boolean isWiFiActive() {      
        ConnectivityManager connectivity = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);      
        if (connectivity != null) {      
            NetworkInfo[] infos = connectivity.getAllNetworkInfo();      
            if (infos != null) {      
                for(NetworkInfo ni : infos){  
                    if(ni.getTypeName().equals("WIFI") && ni.isConnected()){  
                        return true;  
                    }  
                }  
            }      
        }      
        return false;      
    }
	
	public void createDb() {
		db.execSQL("create table tb_user( name varchar(30) primary key,password varchar(30))");
	}
	
}
