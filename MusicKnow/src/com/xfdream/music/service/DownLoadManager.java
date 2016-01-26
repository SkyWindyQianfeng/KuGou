package com.xfdream.music.service;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.xfdream.music.entity.DownLoadInfo;
import com.xfdream.music.entity.Song;

public class DownLoadManager {
	// ����״̬
	public static final int STATE_DOWNLOADING = 0;// ������
	public static final int STATE_PAUSE = 1;// ��ͣ
	public static final int STATE_WAIT = 2;// �ȴ�
	public static final int STATE_DELETE = 3;// ɾ��
	public static final int STATE_FAILED = 4;//����ʧ��
	public static final int STATE_PAUSEING = 5;// ������ͣ
	public static final int STATE_CONNECTION = 6;//������
	public static final int STATE_ERROR = 7;//����
	
	//��������-�㲥��������
	public static final String BROADCASTRECEVIER_ACTON="com.xfdream.music.download.brocast";
	
	public static final int FLAG_CHANGED=0;//����ǰ̨
	public static final int FLAG_COMPLETED=1;//�������
	public static final int FLAG_FAILED=2;//ʧ��
	public static final int FLAG_WAIT=3;//�ȴ�����
	public static final int FLAG_TIMEOUT=4;//���س�ʱ
	public static final int FLAG_ERROR=5;//��������
	public static final int FLAG_COMMON=6;//ɾ��
	
	//DownLoadService action
	public static final String SERVICE_ACTION="com.xfdream.music.service.download";
	
	//MediaPlayerService onStart flag
	public static final int SERVICE_DOWNLOAD_STOP=0;//ֹͣ����
	
	private DownLoadService mDownLoadService;
	private ContextWrapper mContextWrapper;
	
	public DownLoadManager(ContextWrapper cw) {
		mContextWrapper = cw;
	}
	
	/**
	 * ����ĳ����������
	 * */
	public void start(String url){
		if(mDownLoadService!=null){
			mDownLoadService.start(url);
		}
	}
	
	/**
	 * ���ĳ����������
	 * */
	public void add(Song song){
		if(mDownLoadService!=null){
			mDownLoadService.add(song);
		}
	}
	
	/**
	 * ɾ��ĳ����������
	 * */
	public void delete(String url){
		if(mDownLoadService!=null){
			mDownLoadService.delete(url);
		}
	}
	
	/**
	 * ��ͣĳ����������
	 * */
	public void pause(String url){
		if(mDownLoadService!=null){
			mDownLoadService.pause(url);
		}
	}
	
	/**
	 * ��ȡ��������
	 * */
	public List<DownLoadInfo> getDownLoadData(){
		if(mDownLoadService!=null){
			return mDownLoadService.getDownLoadData();
		}
		return null;
	}

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mDownLoadService = ((DownLoadService.DownLoadBinder) service)
					.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			mDownLoadService = null;
		}
	};
	
	/**
	 * ֹͣ����
	 * */
	public void stop(){
		if(mDownLoadService != null){
			mDownLoadService.stop();
        }
	}
	
	/**
	 * ��ʼ���񲢰󶨷���
	 * */
	public void startAndBindService(){ 
        mContextWrapper.startService(new Intent(SERVICE_ACTION)); 
        mContextWrapper.bindService(new Intent(SERVICE_ACTION), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

	/**
	 * ȡ����
	 * */
	public void unbindService(){
        if(mDownLoadService != null){
            mContextWrapper.unbindService(mServiceConnection);
        }
    }
}