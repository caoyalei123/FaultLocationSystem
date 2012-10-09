package com.liubo;

import java.util.HashMap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.IBinder;
import android.os.Vibrator;


public class AlertService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate() {
        sp = new SoundPool(2,AudioManager.STREAM_MUSIC,0);
        spMap = new HashMap<Integer,Integer>();
        spMap.put(1, sp.load(AlertService.this.getBaseContext() ,R.raw.alert, 1));
	}
	@Override
	public void onStart(Intent intent, int flag) {	
		new Thread() {
			public void run() {
	        	try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        long startTime = System.currentTimeMillis();
		        playSounds(AlertService.this.getBaseContext(), 1, -1);
		        while (System.currentTimeMillis() - startTime < 20 * 1000) {
		        	if (MapMainActivity.isFromNotify) {
		        		break;
		        	}
		        	try {
						Thread.sleep(60);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
        		sp.stop(1);
        		sp.release();
			}
		}.start();
	}
	 private SoundPool sp;
	 private HashMap<Integer,Integer> spMap;
    public void playSounds(Context context, int sound, int number){
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        am.setMode(AudioManager.MODE_RINGTONE);
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        am.setStreamMute(AudioManager.STREAM_MUSIC, false);
        am.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
        am.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);  
        vib.vibrate(10 * 1000); 

//        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//        float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);
//        float volumnRatio = audioCurrentVolumn/audioMaxVolumn;
        sp.play(spMap.get(sound), 0.9f, 0.9f, 1, number, 1);
    }
}
