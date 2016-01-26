package com.xfdream.music.recevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.xfdream.music.service.MediaPlayerManager;

/**
 * �绰״̬�㲥������������ʱ��ͣ����
 * */
public class PhoneStateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		if(telephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE){//���ǿ���״̬
			context.startService(new Intent(MediaPlayerManager.SERVICE_ACTION)
			.putExtra("flag", MediaPlayerManager.SERVICE_MUSIC_PAUSE));
		}
	}

}
