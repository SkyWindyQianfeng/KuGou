package com.xfdream.music.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MediaPlayerManager {
	
	private MediaPlayerService mMediaPlayerService;
	private ContextWrapper mContextWrapper;
	
	//����ģʽ
	/**
	 * ˳�򲥷� 0
	 * */
	public static final int MODE_CIRCLELIST=0;
	/**
	 * ������� 1
	 * */
	public static final int MODE_RANDOM=1;
	/**
	 * ����ѭ�� 2
	 * */
	public static final int MODE_CIRCLEONE=2;
	/**
	 * �б�ѭ�� 3
	 * */
	public static final int MODE_SEQUENCE=3;
	
	//����״̬
	public static final int STATE_NULL=0;//����
	public static final int STATE_BUFFER=1;//����
	public static final int STATE_PAUSE=2;//��ͣ
	public static final int STATE_PLAYER=3;//����
	public static final int STATE_PREPARE=4;//׼��
	public static final int STATE_OVER=5;//���Ž���
	public static final int STATE_STOP=6;//ֹͣ
	
	//����Flag
	public static final int PLAYERFLAG_WEB=0;//����
	public static final int PLAYERFLAG_ALL=1;//ȫ��
	public static final int PLAYERFLAG_ARTIST=2;//����
	public static final int PLAYERFLAG_ALBUM=3;//ר��
	public static final int PLAYERFLAG_FOLDER=4;//�ļ���
	public static final int PLAYERFLAG_PLAYERLIST=5;//�����б�
	public static final int PLAYERFLAG_LIKE=6;//�����
	public static final int PLAYERFLAG_LATELY=7;//�������
	public static final int PLAYERFLAG_DOWNLOAD=9;//�������
	
	//���Ÿ���-�㲥��������
	public static final String BROADCASTRECEVIER_ACTON="com.xfdream.music.player.brocast";

	public static final int FLAG_CHANGED=0;//����ǰ̨
	public static final int FLAG_PREPARE=1;//׼��״̬
	public static final int FLAG_INIT=2;//��ʼ������
	public static final int FLAG_LIST=3;//�Զ�����ʱ������ǰ̨�б�״̬
	public static final int FLAG_BUFFERING=4;//��������-��������
	
	public static final int FLAG_AUTOSHUTDOWN=5;//��ʱ�ػ�
	
	//MediaPlayerService action
	public static final String SERVICE_ACTION="com.xfdream.music.service.meidaplayer";
		
	//MediaPlayerService onStart flag
	public static final int SERVICE_RESET_PLAYLIST=0;//���²����б�
	public static final int SERVICE_MUSIC_PAUSE=1;//��ͣ
	public static final int SERVICE_MUSIC_PLAYERORPAUSE=2;//����/��ͣ
	public static final int SERVICE_MUSIC_PREV=3;//��һ��
	public static final int SERVICE_MUSIC_NEXT=4;//��һ��
	public static final int SERVICE_MUSIC_STOP=5;//ֹͣ����
	public static final int SERVICE_MUSIC_INIT=6;//���widgetʱ����ʼ��widget��Ϣ
	
	private ServiceConnectionListener mConnectionListener;
	
	public MediaPlayerManager(ContextWrapper cw) {
		mContextWrapper = cw;
	}
	
	public interface ServiceConnectionListener{
    	public void onServiceConnected();
    	public void onServiceDisconnected();
	}
	
	public void setConnectionListener(ServiceConnectionListener listener){
    	mConnectionListener = listener;
    }
	
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mMediaPlayerService = ((MediaPlayerService.MediaPlayerBinder) service)
					.getService();
			if(mConnectionListener!=null){
				mConnectionListener.onServiceConnected();
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			mMediaPlayerService = null;
			if(mConnectionListener!=null){
				mConnectionListener.onServiceDisconnected();
			}
		}
	};
	
	/**
	 * ��ʼ��������Ϣ-���Ž������ʱ
	 * */
	public void initPlayerMain_SongInfo(){
		if(mMediaPlayerService!=null){
			mMediaPlayerService.initPlayerMain_SongInfo();
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
	 * ֹͣ����
	 * */
	public void stop(){
        if(mMediaPlayerService != null){
        	mMediaPlayerService.stop();
        }
    }
	
	/**
	 * ȡ����
	 * */
	public void unbindService(){
        if(mMediaPlayerService != null){
            mContextWrapper.unbindService(mServiceConnection);
        }
    }
	
	/**
	 * ���ò���ģʽ
	 * */
	public void setPlayerMode(int playerMode){
		if(mMediaPlayerService!=null){
			mMediaPlayerService.setPlayerMode(playerMode);
		}
	}
	
	/**
	 * ��ȡר��ͼƬ
	 * */
	public String getAlbumPic(){
		if(mMediaPlayerService!=null){
			mMediaPlayerService.getAlbumPic();
		}
		return null;
	}
	
	/**
	 * ��ȡ��ǰ���Ÿ�����Id
	 * */
	public int getSongId(){
		if(mMediaPlayerService!=null){
			return mMediaPlayerService.getSongId();
		}
		return -1;
	}
	
	/**
	 * ��ȡ��ǰ���ŵ�Flag
	 * */
	public int getPlayerFlag(){
		if(mMediaPlayerService!=null){
			return mMediaPlayerService.getPlayerFlag();
		}
		return -1;
	}
	
	/**
	 * ��ȡ��ǰ����״̬
	 * */
	public int getPlayerState(){
		if(mMediaPlayerService!=null){
			return mMediaPlayerService.getPlayerState();
		}
		return -1;
	}
	
	/**
	 * ָ��λ�ò���
	 * */
	public void seekTo(int msec){
		if(mMediaPlayerService!=null){
			mMediaPlayerService.seekTo(msec);
		}
	}
	
	/**
	 * ��ȡ��ǰ���Ÿ�������
	 * */
	public String getTitle(){
		if(mMediaPlayerService!=null){
			return mMediaPlayerService.getTitle();
		}
		return null;
	}
	
	/**
	 * ��ȡ��ǰ���Ÿ����Ľ���
	 * */
	public int getPlayerProgress(){
		if(mMediaPlayerService!=null){
			return mMediaPlayerService.getPlayerProgress();
		}
		return -1;
	}
	
	/**
	 * ��ȡ��ǰ���Ÿ�����ʱ��
	 * */
	public int getPlayerDuration(){
		if(mMediaPlayerService!=null){
			return mMediaPlayerService.getPlayerDuration();
		}
		return -1;
	}
	
	/**
	 * ������һ��
	 * */
	public void nextPlayer(){
		if(mMediaPlayerService!=null){
			mMediaPlayerService.nextPlayer();
		}
	}
	
	/**
	 * ������һ��
	 * */
	public void previousPlayer(){
		if(mMediaPlayerService!=null){
			mMediaPlayerService.previousPlayer();
		}
	}
	
	/**
	 * ����/��ͣ
	 * */
	public void pauseOrPlayer(){
		if(mMediaPlayerService!=null){
			mMediaPlayerService.pauseOrPlayer();
		}
	}

	/**
	 * ����ָ����������
	 * */
	public void player(int id,int playerFlag,String parameter){
		if(mMediaPlayerService!=null){
			mMediaPlayerService.player(id,playerFlag,parameter);
		}
	}
	
	/**
	 * ���ò��Ÿ����б�
	 * */
	public void resetPlayerList(){
		if(mMediaPlayerService!=null){
			mMediaPlayerService.resetPlayerList();
		}
	}
	
	/**
	 * ��ȡ��ǰ����ģʽ
	 * */
	public int getPlayerMode(){
		if(mMediaPlayerService!=null){
			return mMediaPlayerService.getPlayerMode();
		}
		return -1;
	}
	
	/**
	 * ��ʼ��������Ϣ-ɨ��֮��
	 * */
	public void initScanner_SongInfo(){
		if(mMediaPlayerService!=null){
			mMediaPlayerService.initScanner_SongInfo();
		}
	}
	
	/**
	/**
	 * ��ȡ��ǰ��ѯ�б�����
	 * */
	public String getParameter(){
		if(mMediaPlayerService!=null){
			return mMediaPlayerService.getParameter();
		}
		return null;
	}
	
	
	/**
	 * ��ȡ������ŵ�
	 * */
	public String getLatelyStr(){
		if(mMediaPlayerService!=null){
			return mMediaPlayerService.getLatelyStr();
		}
		return null;
	}
	
	/**
	 * �������
	 * */
	public void randomPlayer(int flag,String parameter){
		if(mMediaPlayerService!=null){
			mMediaPlayerService.randomPlayer(flag,parameter);
		}
	}
	/**
	 * ɾ������ʱ
	 * */
	public void delete(int songId){
		if(mMediaPlayerService!=null){
			mMediaPlayerService.delete(songId);
		}
	}
}
