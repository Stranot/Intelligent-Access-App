package com.example.indoor;

import android.app.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

public class Main extends Activity{
	Button pairButton;
	Button watchButton;
	ImageView image;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);  
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		
		pairButton = (Button)this.findViewById(R.id.pair);
		watchButton = (Button)this.findViewById(R.id.watch);
//		testCallButton = (Button)this.findViewById(R.id.testCall);
		image = (ImageView)this.findViewById(R.id.imageView1);
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
		
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
		//���²���button����Call�
		image.setOnClickListener(new Button.OnClickListener(){  
			public void onClick(View v) {  
				Intent testCallIntent = new Intent(Main.this, Call.class);
				startActivity(testCallIntent);
			}  
		});
	}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void onResume(){
		super.onResume();
//		startService(new Intent(Main.this, ScreenLockService.class));
		 new Thread() {
	            @Override
	            public void run() {
	                startService(new Intent(Main.this, ListenService.class));
	            }
	        }.start();
//		processServiceConnect();
	} 
	
	//���ε�Back��
	public boolean onKeyDown(int keyCode ,KeyEvent event){
		
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
			return true ;

		else
			return super.onKeyDown(keyCode, event);
		
	}
	

}
