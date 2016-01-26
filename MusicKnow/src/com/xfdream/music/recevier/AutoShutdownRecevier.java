package com.xfdream.music.recevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.xfdream.music.activity.BaseActivity;
import com.xfdream.music.data.SystemSetting;
import com.xfdream.music.service.DownLoadManager;
import com.xfdream.music.service.MediaPlayerManager;

public class AutoShutdownRecevier extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//���������ļ�
		new SystemSetting(context, true).setValue(SystemSetting.KEY_AUTO_SLEEP,"");
		
		//�رճ���
		context.sendBroadcast(new Intent(BaseActivity.BROADCASTRECEVIER_ACTON));
		//ֹͣ����
		context.startService(new Intent(MediaPlayerManager.SERVICE_ACTION).putExtra("flag", MediaPlayerManager.SERVICE_MUSIC_STOP));
		//ֹͣ����
		context.startService(new Intent(DownLoadManager.SERVICE_ACTION).putExtra("flag", DownLoadManager.SERVICE_DOWNLOAD_STOP));
	}

}
