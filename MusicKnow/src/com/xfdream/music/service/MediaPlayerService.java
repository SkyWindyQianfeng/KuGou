package com.xfdream.music.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.xfdream.music.R;
import com.xfdream.music.activity.ListMainActivity;
import com.xfdream.music.dao.SongDao;
import com.xfdream.music.data.SystemSetting;
import com.xfdream.music.entity.Song;
import com.xfdream.music.util.Common;
import com.xfdream.music.util.XmlUtil;
import com.xfdream.music.widget.MusicKnowWidget;

public class MediaPlayerService extends Service {
	private final IBinder mBinder = new MediaPlayerBinder();
	private static final int NOTIFICATIONID = 0;
	// ���Ŷ���
	private static final int ACTION_NEXT = 1;// ��һ�ײ���
	private static final int ACTION_PREVIOUS = 2;// ��һ�ײ���
	private static final int ACTION_AUTO = 0;// �Զ�ִ����һ��

	private static final int LATELY_COUNT = 15;// ������ŵı�������

	private MediaPlayer mPlayer;
	private List<Song> list;// ���Ÿ����б�
	private Song song;// ��ǰ���ŵĸ���
	private int playerFlag;// Flag
	private int playerState;// ����״̬
	private int playerMode;// ����ģʽ
	private SongDao songDao;
	private String parameter;// ��ѯ����
	private Notification mNotification;
	private PendingIntent mPendingIntent;
	private NotificationManager mNotificationManager;
	private boolean isRun = true;// ���Ƹ����̵߳�
	private int currentDuration = 0;// �Ѿ�����ʱ��
	private String latelyStr;// ������Ÿ���ƴ�ӵ��ַ���
	private boolean isFirst = false;// �Ƿ��������󣬵�һ�β���
	private List<Integer> randomIds;
	private ExecutorService mExecutorService =null;//�̳߳�
	final Semaphore mSemaphore = new Semaphore(1);
	private boolean isDeleteStop=false;
	private boolean isPrepare=false;
	
	private String isStartup;
	
	//widget
	private RemoteViews remoteViews;
	private AppWidgetManager appWidgetManager;
	private ComponentName componentName;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mPlayer = new MediaPlayer();
		list = new ArrayList<Song>();
		randomIds = new ArrayList<Integer>();
		mExecutorService = Executors.newCachedThreadPool();
		songDao = new SongDao(this);
		isFirst = true;
		
		//widget
		appWidgetManager=AppWidgetManager.getInstance(this);
		componentName=new ComponentName(this, MusicKnowWidget.class);
		remoteViews=new RemoteViews(getPackageName(), R.layout.musicknow_widget);
		//��һ�׶���
		PendingIntent nextPendingIntent=PendingIntent.getService(this, 1, new Intent(MediaPlayerManager.SERVICE_ACTION)
		.putExtra("flag", MediaPlayerManager.SERVICE_MUSIC_NEXT)
		, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.btn_widget_next, nextPendingIntent);
		//����/��ͣ����
		PendingIntent playerPendingIntent=PendingIntent.getService(this, 2, new Intent(MediaPlayerManager.SERVICE_ACTION)
		.putExtra("flag", MediaPlayerManager.SERVICE_MUSIC_PLAYERORPAUSE)
		, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.btn_widget_player, playerPendingIntent);
		remoteViews.setOnClickPendingIntent(R.id.btn_widget_pause, playerPendingIntent);
		//��һ��
		PendingIntent prevPendingIntent=PendingIntent.getService(this, 3, new Intent(MediaPlayerManager.SERVICE_ACTION)
		.putExtra("flag", MediaPlayerManager.SERVICE_MUSIC_PREV)
		, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.btn_widget_prev, prevPendingIntent);
		
		init();
		
		mPlayer.setOnCompletionListener(completionListener);
		mPlayer.setOnBufferingUpdateListener(bufferingUpdateListener);
		mPlayer.setOnErrorListener(errorListener);

		// ����wake lock��֤��CPUά�ֻ���״̬
		mPlayer.setWakeMode(getApplicationContext(),
				PowerManager.PARTIAL_WAKE_LOCK);

		// ֪ͨ����ʼ��
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				ListMainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
		mNotification = new Notification();
		mNotification.icon = R.drawable.icon;
		mNotification.flags = Notification.FLAG_ONGOING_EVENT;//��ʾ�ڡ����ڽ����С�
	}
	
	/**
	 * ��ʼ����Ϣ
	 * */
	private void init() {
		// ��ȡ������Ϣ
		SystemSetting setting = new SystemSetting(this, false);
		isStartup=setting.getValue(SystemSetting.KEY_ISSTARTUP);
		String t_playerFlag = setting.getValue(SystemSetting.KEY_PLAYER_FLAG);
		parameter = setting.getValue(SystemSetting.KEY_PLAYER_PARAMETER);
		String t_playerId = setting.getValue(SystemSetting.KEY_PLAYER_ID);
		String t_currentDuration = setting
				.getValue(SystemSetting.KEY_PLAYER_CURRENTDURATION);
		String t_playerMode = setting.getValue(SystemSetting.KEY_PLAYER_MODE);
		latelyStr = setting.getValue(SystemSetting.KEY_PLAYER_LATELY);
		if (TextUtils.isEmpty(t_playerFlag)) {
			playerFlag = MediaPlayerManager.PLAYERFLAG_ALL;
		} else {
			playerFlag = Integer.valueOf(t_playerFlag);
		}
		resetPlayerList();

		playerState = MediaPlayerManager.STATE_PAUSE;
		if (TextUtils.isEmpty(t_playerId) || t_playerId.equals("-1")) {
			if (list.size() != 0) {
				song = list.get(0);
			} else {
				playerState = MediaPlayerManager.STATE_NULL;
			}
		} else {
			// ��������б���������
			if (playerFlag == MediaPlayerManager.PLAYERFLAG_WEB) {
				for (Song s : list) {
					if (s.getId() == Integer.valueOf(t_playerId)) {
						song = s;
						break;
					}
				}
				// ��������Ҳ����ˣ��Ͳ���ȫ������
				if (song == null) {
					playerFlag = MediaPlayerManager.PLAYERFLAG_ALL;
					resetPlayerList();
					if (list.size() != 0) {
						song = list.get(0);
					} else {
						playerState = MediaPlayerManager.STATE_NULL;
					}
				}
			} else {
				song = songDao.searchById(Integer.valueOf(t_playerId));
			}
		}
		if (!TextUtils.isEmpty(t_currentDuration)) {
			currentDuration = Integer.valueOf(t_currentDuration);
		}
		if (TextUtils.isEmpty(t_playerMode)) {
			playerMode = MediaPlayerManager.MODE_CIRCLELIST;
		} else {
			playerMode = Integer.valueOf(t_playerMode);
			if (playerMode == MediaPlayerManager.MODE_CIRCLEONE) {
				mPlayer.setLooping(true);
			}
			if(playerMode==MediaPlayerManager.MODE_RANDOM){
				if(song!=null)
					randomIds.add(song.getId());
			}
		}
		updateWidget(true,currentDuration,getPlayerDuration());
	}

	/**
	 * ��ʼ��������Ϣ-ɨ��֮��
	 * */
	public void initScanner_SongInfo() {
		if (playerState != MediaPlayerManager.STATE_NULL) {
			return;
		}
		resetPlayerList();
		playerState = MediaPlayerManager.STATE_PAUSE;
		if (list.size() != 0) {
			song = list.get(0);
		} else {
			playerState = MediaPlayerManager.STATE_NULL;
		}
		Intent it = new Intent(MediaPlayerManager.BROADCASTRECEVIER_ACTON);
		it.putExtra("flag", MediaPlayerManager.FLAG_INIT);
		it.putExtra("currentPosition", currentDuration);
		it.putExtra("duration", getPlayerDuration());
		it.putExtra("title", getTitle());
		it.putExtra("albumPic", getAlbumPic());
		sendBroadcast(it);
		
		updateWidget(true,currentDuration,getPlayerDuration());
	}

	/**
	 * ��ʼ��������Ϣ-���Ž������ʱ
	 * */
	public void initPlayerMain_SongInfo() {
		Intent it = new Intent(MediaPlayerManager.BROADCASTRECEVIER_ACTON);
		it.putExtra("flag", MediaPlayerManager.FLAG_INIT);
		it.putExtra("playerState", playerState);
		it.putExtra("currentPosition", currentDuration);
		it.putExtra("duration", getPlayerDuration());
		it.putExtra("title", getTitle());
		it.putExtra("albumPic", getAlbumPic());
		it.putExtra("playerMode", playerMode);
		sendBroadcast(it);
	}

	// �������ʱ
	private OnCompletionListener completionListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			isRun = false;
			playerState = MediaPlayerManager.STATE_PAUSE;
			doPlayer(ACTION_AUTO,true);
			sendBroadcast(new Intent(MediaPlayerManager.BROADCASTRECEVIER_ACTON)
					.putExtra("flag", MediaPlayerManager.FLAG_LIST));
			updateWidget(true,currentDuration,getPlayerDuration());
		}
	};

	// ����ʱ
	private OnBufferingUpdateListener bufferingUpdateListener = new OnBufferingUpdateListener() {
		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			sendBroadcast(new Intent(MediaPlayerManager.BROADCASTRECEVIER_ACTON)
					.putExtra("flag", MediaPlayerManager.FLAG_BUFFERING)
					.putExtra("percent", percent));
		}
	};

	// ���ŷ�������ʱ
	private OnErrorListener errorListener = new OnErrorListener() {

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			doPlayer(ACTION_AUTO, true);
			return true;
		}

	};

	//onStartCommand
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if(intent.getAction()!=null&&intent.getAction().equals(MediaPlayerManager.SERVICE_ACTION)){
			int flag=intent.getIntExtra("flag", -1);
			if(flag==MediaPlayerManager.SERVICE_RESET_PLAYLIST){
				resetPlayerList();
			}else if(flag==MediaPlayerManager.SERVICE_MUSIC_PAUSE){
				if(playerState==MediaPlayerManager.STATE_PLAYER){
					pauseOrPlayer();
					sendBroadcast(new Intent(MediaPlayerManager.BROADCASTRECEVIER_ACTON).putExtra("flag", MediaPlayerManager.FLAG_LIST));
				}
			}else if(flag==MediaPlayerManager.SERVICE_MUSIC_STOP){
				//ֹͣ����
				stop();
			}else if(flag==MediaPlayerManager.SERVICE_MUSIC_PLAYERORPAUSE){
				
				if(isStartup==null||isStartup.equals("true")){
					isStartup="false";
					new SystemSetting(this, true).setValue(SystemSetting.KEY_ISSTARTUP, "false");
				}
				if(playerState==MediaPlayerManager.STATE_NULL){
					return;
				}
				//˳���б��Ž���
				if(playerState==MediaPlayerManager.STATE_OVER){
					return;
				}
				pauseOrPlayer();
				if(playerState==MediaPlayerManager.STATE_PLAYER||playerState==MediaPlayerManager.STATE_PREPARE){//����
					remoteViews.setViewVisibility(R.id.btn_widget_pause, View.VISIBLE);
					remoteViews.setViewVisibility(R.id.btn_widget_player, View.GONE);
				}else{
					remoteViews.setViewVisibility(R.id.btn_widget_pause, View.GONE);
					remoteViews.setViewVisibility(R.id.btn_widget_player, View.VISIBLE);
				}
				appWidgetManager.updateAppWidget(componentName,remoteViews);
			}else if(flag==MediaPlayerManager.SERVICE_MUSIC_NEXT){
				
				if(isStartup==null||isStartup.equals("true")){
					isStartup="false";
					new SystemSetting(this, true).setValue(SystemSetting.KEY_ISSTARTUP, "false");
				}
				
				remoteViews.setViewVisibility(R.id.btn_widget_pause, View.VISIBLE);
				remoteViews.setViewVisibility(R.id.btn_widget_player, View.GONE);
				appWidgetManager.updateAppWidget(componentName,remoteViews);
				nextPlayer();
			}else if(flag==MediaPlayerManager.SERVICE_MUSIC_PREV){
				
				if(isStartup==null||isStartup.equals("true")){
					isStartup="false";
					new SystemSetting(this, true).setValue(SystemSetting.KEY_ISSTARTUP, "false");
				}
				
				remoteViews.setViewVisibility(R.id.btn_widget_pause, View.VISIBLE);
				remoteViews.setViewVisibility(R.id.btn_widget_player, View.GONE);
				appWidgetManager.updateAppWidget(componentName,remoteViews);
				previousPlayer();
			}else if(flag==MediaPlayerManager.SERVICE_MUSIC_INIT){
				updateWidget(true,currentDuration,getPlayerDuration());
				if(isStartup==null||isStartup.equals("true")){
					stopSelf();
				}
			}
		}
	}

	/**
	 * ����Widget,isInit �Ƿ��ǳ�ʼ��������isState �Ƿ��Ǹ���״̬
	 * */
	private void updateWidget(boolean isInit,int currentDuration,int durationTime){
		if(isInit){
			//ר��ͼƬ
			String albumPic=getAlbumPic();
			if(TextUtils.isEmpty(albumPic)){
				remoteViews.setImageViewResource(R.id.iv_widget_album, R.drawable.min_default_album);
			}else{
				Bitmap bitmap=BitmapFactory.decodeFile(albumPic);
				//�ж�SDͼƬ�Ƿ����
				if(bitmap!=null){
					remoteViews.setImageViewBitmap(R.id.iv_widget_album, bitmap);
				}else{
					remoteViews.setImageViewResource(R.id.iv_widget_album, R.drawable.min_default_album);
				}
			}
			//��������
			remoteViews.setTextViewText(R.id.tv_widget_title, getTitle());
		}
		if(playerState==MediaPlayerManager.STATE_PLAYER||playerState==MediaPlayerManager.STATE_PREPARE){//����
			remoteViews.setViewVisibility(R.id.btn_widget_pause, View.VISIBLE);
			remoteViews.setViewVisibility(R.id.btn_widget_player, View.GONE);
		}else{
			remoteViews.setViewVisibility(R.id.btn_widget_pause, View.GONE);
			remoteViews.setViewVisibility(R.id.btn_widget_player, View.VISIBLE);
		}
		if(currentDuration==0){
			durationTime=100;
		}
		//��ʾ����
		remoteViews.setProgressBar(R.id.pb_widget_player, durationTime, currentDuration, false);
		appWidgetManager.updateAppWidget(componentName,remoteViews);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * ֹͣ����ʱ
	 * */
	public void stop() {
		// ��������[0:����Id 1:�Ѿ�����ʱ�� 2:����ģʽ3:�����б�Flag4:�����б��ѯ���� 5:������ŵ�]
		SystemSetting setting = new SystemSetting(this, true);
		String[] playerInfos = new String[6];
		playerInfos[0] = String.valueOf(getSongId());
		if (playerFlag == MediaPlayerManager.PLAYERFLAG_WEB) {
			currentDuration = 0;
		}
		playerInfos[1] = String.valueOf(currentDuration);
		playerInfos[2] = String.valueOf(playerMode);
		playerInfos[3] = String.valueOf(playerFlag);
		playerInfos[4] = parameter;
		playerInfos[5] = latelyStr;
		setting.setPlayerInfo(playerInfos);
		setting.setValue(SystemSetting.KEY_ISSTARTUP, "true");
		mNotificationManager.cancel(NOTIFICATIONID);
		playerState = MediaPlayerManager.STATE_STOP;
		isRun = false;

		if(mPlayer!=null){
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
			}
			mPlayer.release();
		}
		mPlayer = null;
		mExecutorService.shutdown();
		
		//ֹͣ����
		stopSelf();
	}

	/**
	 * �б������ʱ
	 * */
	private void playerOver() {
		playerState = MediaPlayerManager.STATE_OVER;
		song = null;
		currentDuration = 0;
		Intent it = new Intent(MediaPlayerManager.BROADCASTRECEVIER_ACTON);
		it.putExtra("flag", MediaPlayerManager.FLAG_INIT);
		it.putExtra("currentPosition", 0);
		it.putExtra("duration", 0);
		it.putExtra("title", getTitle());
		it.putExtra("albumPic", getAlbumPic());
		sendBroadcast(it);
		
		updateWidget(true,0,0);
	}

	// ׼��
	private void prepare(String path) {
		try {
			mPlayer.setDataSource(path);
			mPlayer.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ������������б�
	 * */
	private void addLately() {
		if (!TextUtils.isEmpty(latelyStr)) {
			String[] ss = latelyStr.split(",");
			boolean t_flag = false;
			String new_lately = "";
			String t_new_lately = "";
			int len = ss.length;
			for (int i = 0; i < len; i++) {
				if (!ss[i].equals(song.getId() + "")) {
					new_lately += ss[i] + ",";
					if (i < ss.length - 1)
						t_new_lately += ss[i] + ",";
				} else {
					t_flag = true;
				}
			}
			if (t_flag)
				len--;
			if (len < LATELY_COUNT) {
				latelyStr = song.getId() + "," + new_lately;
			} else {
				latelyStr = song.getId() + "," + t_new_lately;
			}
		} else {
			latelyStr = song.getId() + ",";
		}
	}

	private class MediaPlayerRunnable implements Runnable {
		@Override
		public void run() {
			try {
				mSemaphore.acquire();
				if(song==null){
					mSemaphore.release();
					return;
				}
				// ��������б����汾�ظ���������������ģ�
				if (playerFlag != MediaPlayerManager.PLAYERFLAG_WEB) {
					addLately();
				}
				mPlayer.reset();
				if (playerFlag == MediaPlayerManager.PLAYERFLAG_WEB) {
					prepare(song.getNetUrl());
				} else {
					prepare(song.getFilePath());
				}
				// �Ƿ��������󣬵�һ�β���
				if (isFirst) {
					mPlayer.seekTo(currentDuration);
				}
				isFirst = false;
				mPlayer.start();
				isRun = true;
				isDeleteStop=false;
				isPrepare=false;
				playerState = MediaPlayerManager.STATE_PLAYER;
				while (isRun) {
					if (playerState == MediaPlayerManager.STATE_PLAYER) {
						currentDuration = mPlayer.getCurrentPosition();
						sendBroadcast(new Intent(
								MediaPlayerManager.BROADCASTRECEVIER_ACTON)
								.putExtra("flag",
										MediaPlayerManager.FLAG_CHANGED)
								.putExtra("currentPosition", currentDuration)
								.putExtra("duration", mPlayer.getDuration()));
						updateWidget(false,currentDuration,mPlayer.getDuration());
						Thread.sleep(1000);
					}
				}
				if (mPlayer != null
						&& playerState != MediaPlayerManager.STATE_OVER
						&& playerState != MediaPlayerManager.STATE_STOP) {
					sendBroadcast(new Intent(
							MediaPlayerManager.BROADCASTRECEVIER_ACTON)
							.putExtra("flag", MediaPlayerManager.FLAG_CHANGED)
							.putExtra("currentPosition",
									mPlayer.getCurrentPosition())
							.putExtra("duration", mPlayer.getDuration()));
					updateWidget(false,currentDuration,mPlayer.getDuration());
				}
				if(isPrepare){
					sendBroadcast(new Intent(
							MediaPlayerManager.BROADCASTRECEVIER_ACTON)
							.putExtra("flag",
									MediaPlayerManager.FLAG_CHANGED)
							.putExtra("currentPosition", 0)
							.putExtra("duration", getPlayerDuration()));
					updateWidget(false,0,getPlayerDuration());
				}
				updateWidget(false,currentDuration,getPlayerDuration());
				currentDuration=0;
				if(isDeleteStop){
					playerOver();
				}
				mSemaphore.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ����
	 * */
	private void doPlayer(int action,boolean isPlayer) {
		switch (playerMode) {
		case MediaPlayerManager.MODE_CIRCLELIST:// ˳�򲥷�
			if (list.size() == 1) {
				// ������Ϻ󣬾�û����
				if (action != ACTION_AUTO) {
					if(isPlayer){
						player();
					}else{
						showPrepare();
					}
				} else {
					playerOver();
				}
			} else {
				int index = -1;
				for (int i = 0, len = list.size(); i < len; i++) {
					// ���б��в��Ҳ��Ÿ�����λ��
					if (list.get(i).getId() == song.getId()) {
						index = i;
						break;
					}
				}
				if (index != -1) {
					if (action == ACTION_AUTO || action == ACTION_NEXT) {
						// ����һ��
						if (index < (list.size() - 1)) {
							song = list.get(index + 1);
							if(isPlayer){
								player();
							}else{
								showPrepare();
							}
						} else {
							if (action == ACTION_AUTO) {
								playerOver();
							} else {
								song = list.get(0);
								if(isPlayer){
									player();
								}else{
									showPrepare();
								}
							}
						}
					} else {// ��һ��
						if (index > 0) {
							song = list.get(index - 1);
							if(isPlayer){
								player();
							}else{
								showPrepare();
							}
						} else {
							if (action == ACTION_AUTO) {
								playerOver();
							} else {
								song = list.get(list.size() - 1);
								if(isPlayer){
									player();
								}else{
									showPrepare();
								}
							}
						}
					}
				}
			}
			break;
		case MediaPlayerManager.MODE_RANDOM:// �������
			if (list.size() == 1) {
				if(isPlayer){
					player();
				}else{
					showPrepare();
				}
			} else if (list.size() == 2) {
				if (song.getId() == list.get(0).getId()) {// ��ǰ���Ÿ�������λ
					song = list.get(1);
				} else {
					song = list.get(0);
				}
				if(isPlayer){
					player();
				}else{
					showPrepare();
				}
			} else {
				if (action == ACTION_AUTO || action == ACTION_NEXT) {
					// ��һ�����ʱ��ֹ�ظ��ø���
					int random_index = -1;
					while (random_index == -1) {
						random_index = new Random().nextInt(list.size());
						if (song.getId() != list.get(random_index).getId()) {
							break;
						}
						random_index = -1;
					}
					song = list.get(random_index);
					randomIds.add(song.getId());
				} else {
					if (randomIds.size() > 1) {
						int len=randomIds.size();
						int j=0;
						Song s=null;
						//������һ�׸������Ҳ���������һ��...����û���ҵ����Ǿ��������һ��
						for (int i=len-1;i>0;i--) {
							s=searchPrevRandomSong(randomIds.get(i - 1));
							if(s!=null){
								j=i-1;
								break;
							}
						}
						if(s==null){
							randomIds.clear();
							song=null;
							//�������һ��
							if(list.size()>0){
								int random_index = new Random().nextInt(list.size());
								song=list.get(random_index);
								randomIds.add(song.getId());
							}
						}else{
							//�Ƴ���һ�����к��������
							for (int i = len-1; i >j; i--) {
								randomIds.remove(i);
							}
							song=s;
						}
					}
				}
				if(isPlayer){
					player();
				}else{
					showPrepare();
				}
			}
			break;
		case MediaPlayerManager.MODE_SEQUENCE:// �б�ѭ��
			if (list.size() == 1) {
				if(isPlayer){
					player();
				}else{
					showPrepare();
				}
			} else {
				int index = -1;
				for (int i = 0, len = list.size(); i < len; i++) {
					// ���б��в��Ҳ��Ÿ�����λ��
					if (list.get(i).getId() == song.getId()) {
						index = i;
						break;
					}
				}
				if (index != -1) {
					if (action == ACTION_AUTO || action == ACTION_NEXT) {
						// ����һ��
						if (index < (list.size() - 1)) {
							song = list.get(index + 1);
						} else {
							song = list.get(0);
						}
						if(isPlayer){
							player();
						}else{
							showPrepare();
						}
					} else {// ��һ��
						if (index > 0) {
							song = list.get(index - 1);
						} else {
							song = list.get(list.size() - 1);
						}
						if(isPlayer){
							player();
						}else{
							showPrepare();
						}
					}
				}
			}
			break;
		}
	}

	/**
	 * ������һ��������Ÿ����Ƿ����
	 * */
	private Song searchPrevRandomSong(int prevrandomid){
		for (int i = 0,len=list.size(); i < len; i++) {
			Song s=list.get(i);
			if(s.getId()==prevrandomid){
				return s;
			}
		}
		return null;
	}
	
	/**
	 * ָ��λ�ò���
	 * */
	public void seekTo(int msec) {
		mPlayer.seekTo(msec);
	}

	/**
	 * ���ò���ģʽ
	 * */
	public void setPlayerMode(int playerMode) {
		this.playerMode = playerMode;
		// ����ѭ��
		if (playerMode == MediaPlayerManager.MODE_CIRCLEONE) {
			mPlayer.setLooping(true);
		} else {
			mPlayer.setLooping(false);
			if (playerMode == MediaPlayerManager.MODE_RANDOM) {
				randomIds.clear();
				if (song != null) {
					randomIds.add(song.getId());
				}
			}
		}
	}

	/**
	 * ��ȡ��ǰ���Ÿ�����Id
	 * */
	public int getSongId() {
		if (song == null)
			return -1;
		return song.getId();
	}

	/**
	 * ��ȡ��ǰ���ŵ�Flag
	 * */
	public int getPlayerFlag() {
		return playerFlag;
	}

	/**
	 * ��ȡ��ǰ����״̬
	 * */
	public int getPlayerState() {
		return playerState;
	}

	/**
	 * ��ȡ��ǰ���Ÿ�������
	 * */
	public String getTitle() {
		if (song == null) {
			return "����һ��ͨ������������";
		}
		// �жϱ����Ƿ���ڣ�����������ʾ�ļ���
		if (TextUtils.isEmpty(song.getName())) {
			return Common.clearSuffix(song.getDisplayName());
		}
		return song.getArtist().getName() + "-" + song.getName();
	}

	/**
	 * ��ȡ��ǰ���Ÿ����Ľ���
	 * */
	public int getPlayerProgress() {
		return currentDuration;
	}

	/**
	 * ��ȡ��ǰ���Ÿ�����ʱ��
	 * */
	public int getPlayerDuration() {
		if (song == null) {
			return 0;
		}
		int durationTime=song.getDurationTime();
		//�ж�ɨ���ļ��Ƿ��Ѿ���ȡ�˸���ʱ��
		if(durationTime==-1){
			song.setDurationTime(getSongDuratonTime(song.getId(),song.getDurationTime()));
		}
		return durationTime;
	}

	private int getSongDuratonTime(int id,int durationTime){
		int rs=durationTime;
		MediaPlayer player=MediaPlayer.create(this, Uri.parse(song.getFilePath()));
		try {
			player.prepare();
			rs=player.getDuration();
		} catch (IllegalStateException e) {
		} catch (IOException e) {
		}finally{
			player.release();
			player=null;
		}
		if(rs!=-1){
			songDao.updateByDuration(id, rs);
		}
		return rs;
	}
	
	/**
	 * ��ȡר��ͼƬ
	 * */
	public String getAlbumPic() {
		if (song == null) {
			return null;
		}
		return song.getAlbum().getPicPath();
	}

	/**
	 * ��ȡ������ŵ�
	 * */
	public String getLatelyStr() {
		if (TextUtils.isEmpty(latelyStr)) {
			return null;
		}
		return latelyStr.substring(0, latelyStr.length() - 1);
	}

	/**
	 * ��ȡ��ǰ����ģʽ
	 * */
	public int getPlayerMode() {
		return playerMode;
	}

	/**
	 * ������һ��
	 * */
	public void nextPlayer() {
		doPlayer(ACTION_NEXT,true);
	}

	/**
	 * ������һ��
	 * */
	public void previousPlayer() {
		doPlayer(ACTION_PREVIOUS,true);
	}

	/**
	 * ����/��ͣ
	 * */
	public void pauseOrPlayer() {
		if (mPlayer.isPlaying()) {
			mPlayer.pause();
			currentDuration = mPlayer.getCurrentPosition();
			playerState = MediaPlayerManager.STATE_PAUSE;
		} else {
			// �Ƿ��������󣬵�һ�β���
			if (isFirst) {
				if (song != null) {
					player(song.getId(), playerFlag, parameter);
				} else {
					currentDuration = 0;
				}
			} else {
				if(isPrepare){
					player();
				}else{
					mPlayer.start();
				}
			}
			playerState = MediaPlayerManager.STATE_PLAYER;
		}
	}

	/**
	 * ����
	 * */
	private void player() {
		isRun = false;

		if (playerFlag != MediaPlayerManager.PLAYERFLAG_WEB) {
			// ׼��״̬
			playerState = MediaPlayerManager.STATE_PREPARE;
		} else {// ��������-����״̬
			playerState = MediaPlayerManager.STATE_BUFFER;
		}
		if (mPlayer.isPlaying()) {
			mPlayer.stop();
		}

		if(song!=null){
			// ֪ͨ������Ϣ
			mNotification.tickerText = getTitle();
			String name = song.getName();
			// �жϱ����Ƿ���ڣ�����������ʾ�ļ���
			if (TextUtils.isEmpty(name)) {
				name = Common.clearSuffix(song.getDisplayName());
			}
			mNotification.setLatestEventInfo(this, name,
					song.getArtist().getName(), mPendingIntent);
			mNotification.when = System.currentTimeMillis() + 1;
			mNotificationManager.notify(NOTIFICATIONID, mNotification);
			
			showPrepare();
		}
		mExecutorService.execute(new MediaPlayerRunnable());
	}

	/**
	 * ����ָ����������
	 * */
	public void player(int id, int playerFlag, String parameter) {
		if (this.playerFlag != playerFlag) {
			this.playerFlag = playerFlag;
			this.parameter = parameter;
			resetPlayerList();
		}
		this.parameter = parameter;
		this.playerFlag = playerFlag;
		if (playerFlag != MediaPlayerManager.PLAYERFLAG_WEB) {
			if (song != null) {
				// ������û�е����ǰ����ѡ��
				if (song.getId() != id) {
					isFirst = false;
				}
			}
			song = songDao.searchById(id);
			playerState = MediaPlayerManager.STATE_PLAYER;
		} else {
			for (Song s : list) {
				if (s.getId() == id) {
					song = s;
					isFirst = false;
					break;
				}
			}
		}
		if (playerMode == MediaPlayerManager.MODE_RANDOM) {
			randomIds.clear();
			randomIds.add(song.getId());
		}
		player();
	}

	/**
	 * ��ʾ����׼����Ϣ
	 * */
	private  void showPrepare(){
		sendBroadcast(new Intent(MediaPlayerManager.BROADCASTRECEVIER_ACTON)
		.putExtra("flag", MediaPlayerManager.FLAG_PREPARE)
		.putExtra("title",getTitle())
		.putExtra("currentPosition", isFirst ? currentDuration : 0)
		.putExtra("duration", getPlayerDuration())
		.putExtra("albumPic", song.getAlbum().getPicPath()));

		updateWidget(true,isFirst ? currentDuration : 0,getPlayerDuration());
	}
	
	/**
	 * ɾ������ʱ
	 * */
	public void delete(int songId){
		isFirst=false;
		//ɾ��'�����б�'���Ͳ���ȫ������
		if(songId==-1){
			isPrepare=true;
			isRun=false;
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
			}
			playerFlag=MediaPlayerManager.PLAYERFLAG_ALL;
			playerState=MediaPlayerManager.STATE_PAUSE;
			parameter="";
			resetPlayerList();
			song=list.size()>0?list.get(0):null;
			currentDuration=0;
			showPrepare();
			return;
		}
		//����ģʽ�£�ɾ����ǰ����
		if(playerMode==MediaPlayerManager.MODE_CIRCLEONE){
			isDeleteStop=true;
			isRun=false;
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
			}
		}else{
			//ֻ��һ�׸���
			if(list.size()<=1){
				isDeleteStop=true;
				isRun=false;
				if (mPlayer.isPlaying()) {
					mPlayer.stop();
				}
			}else{
				//��һ��
				if(playerState==MediaPlayerManager.STATE_PAUSE){
					isPrepare=true;
					isRun = false;
					if (mPlayer.isPlaying()) {
						mPlayer.stop();
					}
					doPlayer(ACTION_NEXT, false);
				}else{
					nextPlayer();
				}
				//���б���ɾ��
				for(int i=0,len=list.size();i<len;i++){
					if(songId==list.get(i).getId()){
						list.remove(i);
						break;
					}
				}
			}
		}
	}
	
	/**
	 * ��ȡ��ǰ��ѯ�б�����
	 * */
	public String getParameter(){
		return parameter;
	}
	
	/**
	 * �������
	 * */
	public void randomPlayer(int flag,String p){
		if(flag==playerFlag&&p.equals(parameter==null?"":parameter)){
			playerFlag=flag;
			parameter=p;
		}else{
			playerFlag=flag;
			parameter=p;
			resetPlayerList();
		}
		playerMode=MediaPlayerManager.MODE_RANDOM;
		randomIds.clear();
		if(list.size()>0){
			int random_index = new Random().nextInt(list.size());
			song=list.get(random_index);
			randomIds.add(song.getId());
			currentDuration=0;
			player();
		}else{
			isRun = false;
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
			}
			song=null;
			currentDuration=0;
			playerOver();
		}
	}
	
	/**
	 * ���ò��Ÿ����б�
	 * */
	public void resetPlayerList() {
		switch (playerFlag) {
		case MediaPlayerManager.PLAYERFLAG_ALL:
			list = songDao.searchAll();
			break;
		case MediaPlayerManager.PLAYERFLAG_ALBUM:
			list = songDao.searchAlbum(parameter);
			break;
		case MediaPlayerManager.PLAYERFLAG_ARTIST:
			list = songDao.searchArtist(parameter);
			break;
		case MediaPlayerManager.PLAYERFLAG_DOWNLOAD:
			list = songDao.searchDownLoad();
			break;
		case MediaPlayerManager.PLAYERFLAG_FOLDER:
			list = songDao.searchDirectory(parameter);
			break;
		case MediaPlayerManager.PLAYERFLAG_LATELY:
			list = songDao.searchLately(getLatelyStr());
			break;
		case MediaPlayerManager.PLAYERFLAG_LIKE:
			list = songDao.searchIsLike();
			break;
		case MediaPlayerManager.PLAYERFLAG_PLAYERLIST:
			list = songDao.searchPlayerList(parameter);
			break;
		case MediaPlayerManager.PLAYERFLAG_WEB:
			list = XmlUtil.parseWebSongList(this);
			break;
		default:
			break;
		}
	}

	public class MediaPlayerBinder extends Binder {
		public MediaPlayerService getService() {
			return MediaPlayerService.this;
		}
	}

	public IBinder getBinder() {
		return mBinder;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return getBinder();
	}

}
