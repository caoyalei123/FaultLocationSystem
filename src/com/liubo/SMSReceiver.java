package com.liubo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.liubo.db.AlertDao;
import com.liubo.db.TowerDao;
import com.liubo.exception.TowerNotUniqueException;
import com.liubo.map.Constant;
import com.liubo.modal.Alert;
import com.liubo.modal.Tower;
import com.liubo.resolver.AlertResolver;

public class SMSReceiver extends BroadcastReceiver {

	Notification notification;
	NotificationManager notificationManager;
	Intent intent;
	PendingIntent pendingIntent ;
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		Object[] messages = (Object[]) bundle.get("pdus");
		SmsMessage smsMessage[] = new SmsMessage[messages.length];
		for(int i = 0 ; i < smsMessage.length ; i++ ){
			smsMessage[i] = SmsMessage.createFromPdu((byte[]) messages[i]);
		}
		Alert alert = AlertResolver.resolve(smsMessage[0]);
		if(alert == null){
			return;
		}
		Toast toast = Toast.makeText(context, alert.toString(),Toast.LENGTH_LONG);
		toast.show();
		Tower tower = null;
		{	
			TowerDao towerDao = new TowerDao(context);
			try {
				tower = towerDao.getTowerBySMSNumber(alert.getAddressNum());
			} catch (TowerNotUniqueException e) {
				Toast.makeText(context, "ϵͳ���󣺴��ڳ�ͻ�ĸ�����Ϣ���������ݡ�",Toast.LENGTH_LONG).show();
				e.printStackTrace();
				return ;
			}
			if(tower == null){
				Toast.makeText(context, alert.getAddressNum(), Toast.LENGTH_LONG).show();
				return;
			}
			towerDao.updateTowerStatusById(tower.getId(), Tower.Status.ERROR.value);
			
			notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			intent = new Intent(context,ActivityGroupDemo.class);
			intent.putExtra(Constant.Common.TOWER_ID, tower.getId());
			Log.e("towerid", tower.getId());
			intent.putExtra(Constant.Common.ACTIVITY_INDEX, "0");
	        pendingIntent = PendingIntent.getActivity(context , 0, intent, 0);
	        notification = new Notification();
	        notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.icon = R.drawable.ic_launcher;
			notification.tickerText = "����";
			notification.defaults = Notification.DEFAULT_SOUND;
			notification.setLatestEventInfo(context, "�������ϱ���", alert.toString(), pendingIntent);
			notificationManager.notify(0, notification);
			//�������Ѿ�������ı䵱ǰ��tower��״̬
			if (MapMainActivity.self != null) {
				Message msg =  new Message();
				msg.what = MapMainActivity.ADD_ALERT_TOWER;
				msg.getData().putString(Constant.Common.TOWER_ID, tower.getId());
				MapMainActivity.self.handler.sendMessage(msg);
			}
		}
		
		AlertDao alertDao = new AlertDao(context);
		alert.setTowerId(tower.getId());
		alertDao.save(alert);
		alertDao.close();
		Intent alertServiceIntent = new Intent(context, AlertService.class);
		context.startService(alertServiceIntent);
	}

}
