package com.xfdream.music.recevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.xfdream.music.service.MediaPlayerManager;

/**
 * ����״̬�㲥�����������¶���ʱ����ͣ����
 * */
public class EarphoneStateRecevier extends BroadcastReceiver {

	/**
	 * ��ʾӦ�ó�����Ƶ�ź�������Ƶ����ı仯����á����ӡ���
	 * ���磬���γ�һ�����߶�������Ͽ�һ��֧��A2DP����Ƶ��������
	 * ���intent�ͻᱻ���ͣ�����Ƶϵͳ���Զ��л���Ƶ��·����������
	 * �յ����intent�󣬿�����Ƶ����Ӧ�ó���ῼ����ͣ����С������������ʩ
	 * ������������������ʹ�û����档
	 * */
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
			context.startService(new Intent(MediaPlayerManager.SERVICE_ACTION)
			.putExtra("flag", MediaPlayerManager.SERVICE_MUSIC_PAUSE));
        }
	}

}
