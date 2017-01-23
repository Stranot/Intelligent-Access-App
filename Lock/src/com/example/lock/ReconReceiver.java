package com.example.lock;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;

public class ReconReceiver extends BroadcastReceiver {
	
	private static final String TAG = "ReconReciever";
	
    @Override
    public void onReceive(final Context context, Intent intent) {
//     Bundle bundle = intent.getExtras();
//     String reconSig = bundle.getString("Reconnect");
     
   //���������ڻ�ʧ�ܣ���ʾ�Ի�����������SocketClientService����
     Log.d(TAG, "reconnectIntent: ");
//       if(reconSig.equals(RECONNECT)){
     AlertDialog.Builder build = new AlertDialog.Builder(context);    	             
     build.setTitle("�޷��������ڻ�");  	             
     build.setMessage("���������Ƿ������Լ�ip��ַ�Ƿ���ȷ����");  	            
     build.setPositiveButton("��������",new DialogInterface.OnClickListener() {//���ȷ����ť  
	          	@Override  
	          	public void onClick(DialogInterface dialog, int which) {//ȷ����ť����Ӧ�¼�  
	          		// TODO Auto-generated method stub  
	          		
	          		Intent socketIntent = new Intent(context, SocketClientService.class);
	          		socketIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	          		context.stopService(socketIntent);
	          		context.startService(socketIntent);
	          		dialog.dismiss();
	          	}  
	          });  	  
     build.setNegativeButton("����ip",new DialogInterface.OnClickListener() {//���ȡ����ť  
	          	@Override  
	          	public void onClick(DialogInterface dialog, int which) {//ȷ����ť����Ӧ�¼�  
	          		// TODO Auto-generated method stub  
	          		dialog.dismiss();
	    			Intent pairIntent = new Intent(context, Pair.class);
	    			pairIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    			context.startActivity(pairIntent);
	          	}  
	          });  	
     AlertDialog dialog = build.create();
     dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
	          

     dialog.show();
	        }
        
    //}
}
