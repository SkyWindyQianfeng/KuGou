package com.xfdream.music.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.xfdream.music.R;

public class SystemSetting {
	/**
	 * ϵͳ���õı�����ļ���
	 * */
	public static final String PREFERENCE_NAME = "com.xfdream.music.system";
	/**
	 * SD�����ظ���Ŀ¼
	 * */
	public static final String DOWNLOAD_MUSIC_DIRECTORY="/MusicKnow/download_music/";
	/**
	 * SD�����ظ��Ŀ¼
	 * */
	public static final String DOWNLOAD_LYRIC_DIRECTORY="/MusicKnow/download_lyric/";
	/**
	 * SD������ר��ͼƬĿ¼
	 * */
	public static final String DOWNLOAD_ALBUM_DIRECTORY="/MusicKnow/download_album/";
	/**
	 * SD�����ظ���ͼƬĿ¼
	 * */
	public static final String DOWNLOAD_ARTIST_DIRECTORY="/MusicKnow/download_artist/";
	
	public static final String KEY_SKINID = "skin_id";
	
	public static final String KEY_PLAYER_ID="player_id";//����Id
	public static final String KEY_PLAYER_CURRENTDURATION="player_currentduration";//�Ѿ�����ʱ��
	public static final String KEY_PLAYER_MODE="player_mode";//����ģʽ
	public static final String KEY_PLAYER_FLAG="player_flag";//�����б�Flag
	public static final String KEY_PLAYER_PARAMETER="player_parameter";//�����б��ѯ����
	
	public static final String KEY_PLAYER_LATELY="player_lately";//�������(���汾�صģ���','�ָ�)
	
	public static final String KEY_ISSTARTUP="isStartup";//�Ƿ��Ǹ�����
	
	public static final String KEY_ISSCANNERTIP="isScannerTip";//�Ƿ���ʾҪɨ����ʾ
	
	public static final String KEY_AUTO_SLEEP="sleep";//��ʱ�ر�ʱ��
	
	public static final String KEY_BRIGHTNESS="brightness";//��Ļģʽ->1:����ģʽ 0:ҹ��ģʽ
	public static final float KEY_DARKNESS=0.1f;//ҹ��ģʽֵlevel
	
	/**
	 * Ƥ����Դ��ID����
	 * */
	public static final int[] SKIN_RESOURCES = { R.drawable.main_bg01,
			R.drawable.main_bg02, R.drawable.main_bg03, R.drawable.main_bg04,
			R.drawable.main_bg05, R.drawable.main_bg06 };

	private SharedPreferences settingPreference;
	
	public SystemSetting(Context context,boolean isWrite) {
		settingPreference = context.getSharedPreferences(PREFERENCE_NAME,
				isWrite?Context.MODE_WORLD_READABLE:Context.MODE_WORLD_WRITEABLE);
	}
	
	/**
	 * ��ȡ����
	 * */
	public String getValue(String key){
		return settingPreference.getString(key, null);
	}

	/**
	 * ���沥����Ϣ[0:����Id 1:�Ѿ�����ʱ�� 2:����ģʽ3:�����б�Flag4:�����б��ѯ���� 5:������ŵ�]
	 * */
	public void setPlayerInfo(String[] playerInfos){
		Editor it = settingPreference.edit();
		it.putString(KEY_PLAYER_ID, playerInfos[0]);
		it.putString(KEY_PLAYER_CURRENTDURATION, playerInfos[1]);
		it.putString(KEY_PLAYER_MODE, playerInfos[2]);
		it.putString(KEY_PLAYER_FLAG, playerInfos[3]);
		it.putString(KEY_PLAYER_PARAMETER, playerInfos[4]);
		it.putString(KEY_PLAYER_LATELY, playerInfos[5]);
		it.commit();
	}
	
	/**
	 * ��ȡƤ����ԴID
	 * */
	public int getCurrentSkinResId() {
		int skinIndex = settingPreference.getInt(KEY_SKINID, 0);
		if (skinIndex >= SKIN_RESOURCES.length) {
			skinIndex = 0;
		}
		return SKIN_RESOURCES[skinIndex];
	}

	/**
	 * ��ȡƤ��Id
	 * */
	public int getCurrentSkinId(){
		int skinIndex = settingPreference.getInt(KEY_SKINID, 0);
		if (skinIndex >= SKIN_RESOURCES.length) {
			skinIndex = 0;
		}
		return skinIndex;
	}
	
	/**
	 * ����Ƥ����ԴID
	 * */
	public void setCurrentSkinResId(int skinIndex) {
		Editor it = settingPreference.edit();
		it.putInt(KEY_SKINID, skinIndex);
		it.commit();
	}
	
	/**
	 * ���ü�ֵ
	 * */
	public void setValue(String key,String value){
		Editor it = settingPreference.edit();
		it.putString(key, value);
		it.commit();
	}
}
