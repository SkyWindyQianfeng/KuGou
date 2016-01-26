package com.xfdream.music.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.xfdream.music.R;
import com.xfdream.music.adapter.DownLoadListAdapter;
import com.xfdream.music.adapter.DownLoadingListAdapter;
import com.xfdream.music.adapter.ListItemAdapter;
import com.xfdream.music.adapter.MenuAdapter;
import com.xfdream.music.adapter.SongItemAdapter;
import com.xfdream.music.adapter.SongItemWebAdapter;
import com.xfdream.music.custom.FlingGalleryView;
import com.xfdream.music.custom.FlingGalleryView.OnScrollToScreenListener;
import com.xfdream.music.custom.XfDialog;
import com.xfdream.music.custom.XfMenu;
import com.xfdream.music.dao.AlbumDao;
import com.xfdream.music.dao.ArtistDao;
import com.xfdream.music.dao.DownLoadInfoDao;
import com.xfdream.music.dao.PlayerListDao;
import com.xfdream.music.dao.SongDao;
import com.xfdream.music.data.SystemSetting;
import com.xfdream.music.entity.PlayerList;
import com.xfdream.music.entity.Song;
import com.xfdream.music.recevier.AutoShutdownRecevier;
import com.xfdream.music.service.DownLoadManager;
import com.xfdream.music.service.MediaPlayerManager;
import com.xfdream.music.service.MediaPlayerManager.ServiceConnectionListener;
import com.xfdream.music.util.Common;
import com.xfdream.music.util.XmlUtil;

public class ListMainActivity extends BaseActivity {
	//������ѡ���������
	private ViewGroup[] vg_list_tab_item = new ViewGroup[3];
	private FlingGalleryView fgv_list_main;

	//��ǰ��Ļ���±�
	private int screenIndex = 0;
	//������������
	private String[] list_item_items;
	//��������icon
	private int[] list_item_icons = new int[] { R.drawable.list_music_icon,
			R.drawable.list_web_icon, R.drawable.list_download_icon };
	
	//�����б�
	private ViewGroup list_main_music;
	//��������
	private ViewGroup list_main_web;
	//���ع���
	private ViewGroup list_main_download;

	//����Ļ���ݲ���
	private ViewGroup rl_list_main_content;
	//�л����ݲ���
	private ViewGroup rl_list_content;
	
	//�������ֺ����ع���Ķ���������
	private ImageButton ibtn_list_content_icon;//���ͼ��
	private ImageButton ibtn_list_content_do_icon;//�ұ�ͼ��
	private TextView tv_list_content_title;//����
	private ListView lv_list_change_content;//�滻ListView
	private Button btn_list_random_music2;//�������
	
	//���������������
	private Button btn_list_random_music_local;
	//���������������
	private Button btn_list_random_music_web;
	
	//�������ֲ����б�
	private ListView lv_list_web;
	
	//�ײ�������
	private ImageButton ibtn_player_albumart;//ר������
	private ImageButton ibtn_player_control;//����/��ͣ
	private TextView tv_player_title;//���Ÿ��� ����-����
	private ProgressBar pb_player_progress;//���Ž�����
	private TextView tv_player_currentPosition;//��ǰ���ŵĽ���
	private TextView tv_player_duration;//��������ʱ��
	
	/**
	 * Ĭ��ҳ��0
	 * 1.ȫ������ 2.���� 3.ר�� 4.�ļ��� 5.�����б� 6.����� 7.������� 8.�������� 9.�������
	 * 22.���ֶ��� 33.ר������ 44.�ļ��ж��� 55.�����б���� 
	 * */
	private int pageNumber=0;
	
	private SongDao songDao;
	private ArtistDao artistDao;
	private AlbumDao albumDao;
	private PlayerListDao playerListDao;
	private Toast toast;
	private LayoutParams params;
	private LayoutInflater inflater;
	private DownLoadInfoDao downLoadInfoDao;
	
	private DownLoadManager downLoadManager;
	private DownLoadBroadcastRecevier downLoadBroadcastRecevier;
	
	private MediaPlayerManager mediaPlayerManager;
	private MediaPlayerBroadcastReceiver mediaPlayerBroadcastReceiver;
	
	private XfMenu xfMenu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_main);
		
		SystemSetting setting = new SystemSetting(this, false);
		String isStartup=setting.getValue(SystemSetting.KEY_ISSTARTUP);
		//���Loading����ҳ��
		if(isStartup==null||isStartup.equals("true")){
			startActivity(new Intent(this,LoadingActivity.class));
			this.finish();
		}else{
			checkScannerTip(setting);
		}
		
		songDao=new SongDao(this);
		artistDao=new ArtistDao(this);
		albumDao=new AlbumDao(this);
		playerListDao=new PlayerListDao(this);
		downLoadInfoDao=new DownLoadInfoDao(this);
		params=new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		inflater=(LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
		
		//������ѡ����� ʵ����
		vg_list_tab_item[0] = (ViewGroup) this
				.findViewById(R.id.list_tab_item_music);
		vg_list_tab_item[1] = (ViewGroup) this
				.findViewById(R.id.list_tab_item_web);
		vg_list_tab_item[2] = (ViewGroup) this
				.findViewById(R.id.list_tab_item_download);

		//����Ļ���ݲ���ѡ�� ʵ����
		list_main_music = (ViewGroup) this.findViewById(R.id.list_main_music);
		list_main_web = (ViewGroup) this.findViewById(R.id.list_main_web);
		list_main_download = (ViewGroup) this.findViewById(R.id.list_main_download);
		
		//�����������
		btn_list_random_music_local=(Button)list_main_music.findViewById(R.id.btn_list_random_music);
		btn_list_random_music_web=(Button)list_main_web.findViewById(R.id.btn_list_random_web);
		btn_list_random_music_local.setOnClickListener(btn_randomPlayerListener);
		btn_list_random_music_web.setOnClickListener(btn_randomPlayerListener);
		
		//����Ļ���ݲ��ֺ��л����ݲ��� ʵ����
		rl_list_main_content=(ViewGroup)this.findViewById(R.id.rl_list_main_content);
		rl_list_content=(ViewGroup)this.findViewById(R.id.rl_list_content);
		
		//�������ֺ����ع���Ķ���������-���� �������������
		ibtn_list_content_icon=(ImageButton)rl_list_content.findViewById(R.id.ibtn_list_content_icon);
		ibtn_list_content_do_icon=(ImageButton)rl_list_content.findViewById(R.id.ibtn_list_content_do_icon);
		tv_list_content_title=(TextView)rl_list_content.findViewById(R.id.tv_list_content_title);
		lv_list_change_content=(ListView)rl_list_content.findViewById(R.id.lv_list_change_content);
		ibtn_list_content_icon.setOnClickListener(imageButton_listener);
		ibtn_list_content_do_icon.setOnClickListener(imageButton_listener);
		lv_list_change_content.setOnItemClickListener(list_change_content_listener);
		lv_list_change_content.setOnItemLongClickListener(list_change_content_looglistener);
		btn_list_random_music2=(Button)rl_list_content.findViewById(R.id.btn_list_random_music2);
		btn_list_random_music2.setOnClickListener(btn_randomPlayerListener);
		
		//�ײ�������
		ibtn_player_albumart=(ImageButton)this.findViewById(R.id.ibtn_player_albumart);
		ibtn_player_control=(ImageButton)this.findViewById(R.id.ibtn_player_control);
		tv_player_title=(TextView)this.findViewById(R.id.tv_player_title);
		pb_player_progress=(ProgressBar)this.findViewById(R.id.pb_player_progress);
		tv_player_currentPosition=(TextView)this.findViewById(R.id.tv_player_currentPosition);
		tv_player_duration=(TextView)this.findViewById(R.id.tv_player_duration);
		
		ibtn_player_albumart.setOnClickListener(imageButton_listener);
		ibtn_player_control.setOnClickListener(imageButton_listener);
		
		//�л�����Ļ��������
		fgv_list_main = (FlingGalleryView) rl_list_main_content
				.findViewById(R.id.fgv_list_main);
		fgv_list_main.setDefaultScreen(screenIndex);
		fgv_list_main.setOnScrollToScreenListener(scrollToScreenListener);

		//����Դ�ļ��л�ȡ������ѡ�����
		list_item_items = getResources().getStringArray(R.array.list_tab_items);
		//��ʼ��������
		initTabItem();
		//��ʼ������������������
		initListMusicItem();
		//��ʼ����������
		lv_list_web=(ListView)list_main_web.findViewById(R.id.lv_list_web);
		lv_list_web.setAdapter(new SongItemWebAdapter(this, XmlUtil.parseWebSongList(this)).setItemListener(mItemListener));
		lv_list_web.setOnItemClickListener(webItemClickListener);
		
		//��ʼ�����ع���
		initDownLoad();
		
		//���ع���
		downLoadManager=new DownLoadManager(this);
		downLoadManager.startAndBindService();
		
		//����������
		mediaPlayerManager=new MediaPlayerManager(this);
		mediaPlayerManager.setConnectionListener(mConnectionListener);
		
		createMenu();
	}
	
	/**
	 * �����ʾɨ�������ʾ(��һ�ν����������)
	 * */
	private void checkScannerTip(SystemSetting setting){
		if(setting.getValue(SystemSetting.KEY_ISSCANNERTIP)==null){
			new XfDialog.Builder(this).setTitle("ɨ����ʾ").setMessage("�Ƿ�Ҫɨ�豾�ظ�����⣿").setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					dialog.dismiss();
					Intent it=new Intent(ListMainActivity.this,ScanMusicActivity.class);
					startActivityForResult(it, 1);
				}
			}).setNegativeButton("ȡ��", null).create().show();
			setting.setValue(SystemSetting.KEY_ISSCANNERTIP, "OK");
		}
	}
	
	/**
	 * �������
	 * */
	private OnClickListener btn_randomPlayerListener=new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(mediaPlayerManager.getPlayerFlag()==MediaPlayerManager.PLAYERFLAG_WEB){
				int[] playerInfo=new int[]{-1,-1};
				((SongItemWebAdapter)lv_list_web.getAdapter()).setPlayerInfo(playerInfo);
			}
			if(v.getId()==R.id.btn_list_random_music){
				mediaPlayerManager.randomPlayer(MediaPlayerManager.PLAYERFLAG_ALL, "");
			}else if(v.getId()==R.id.btn_list_random_web){
				if(!Common.getNetIsAvailable(ListMainActivity.this)){
					toast=Common.showMessage(toast, ListMainActivity.this, "��ǰ���粻����");
					return;
				}
				mediaPlayerManager.randomPlayer(MediaPlayerManager.PLAYERFLAG_WEB, "");
			}else if(v.getId()==R.id.btn_list_random_music2){
				if(Integer.valueOf(v.getTag().toString())==0){
					return;
				}
				if(pageNumber==1){
					mediaPlayerManager.randomPlayer(MediaPlayerManager.PLAYERFLAG_ALL, "");
				}else if(pageNumber==6){
					mediaPlayerManager.randomPlayer(MediaPlayerManager.PLAYERFLAG_LIKE, "");
				}else if(pageNumber==7){
					mediaPlayerManager.randomPlayer(MediaPlayerManager.PLAYERFLAG_LATELY, "");
				}else if(pageNumber==9){
					mediaPlayerManager.randomPlayer(MediaPlayerManager.PLAYERFLAG_DOWNLOAD, "");
				}else if(pageNumber==22){
					mediaPlayerManager.randomPlayer(MediaPlayerManager.PLAYERFLAG_ARTIST, condition);
				}else if(pageNumber==33){
					mediaPlayerManager.randomPlayer(MediaPlayerManager.PLAYERFLAG_ALBUM, condition);
				}else if(pageNumber==44){
					mediaPlayerManager.randomPlayer(MediaPlayerManager.PLAYERFLAG_FOLDER, condition);
				}else if(pageNumber==55){
					mediaPlayerManager.randomPlayer(MediaPlayerManager.PLAYERFLAG_PLAYERLIST, condition);
				}
			}
		}
	};
	
	//�������֣��������/��ͣ
	private OnItemClickListener webItemClickListener=new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if(!Common.getNetIsAvailable(ListMainActivity.this)){
				toast=Common.showMessage(toast, ListMainActivity.this, "��ǰ���粻����");
				return;
			}
			int songId=Integer.valueOf(((SongItemWebAdapter.ViewHolder)view.getTag()).tv_web_list_item_top.getTag().toString());
			if(songId==mediaPlayerManager.getSongId()){
				PlayerOrPause(view);
			}else {
				ibtn_player_control.setBackgroundResource(R.drawable.player_btn_mini_pause);
				mediaPlayerManager.player(songId,MediaPlayerManager.PLAYERFLAG_WEB, null);
				int[] playerInfo=new int[]{songId,mediaPlayerManager.getPlayerState()};
				((SongItemWebAdapter)lv_list_web.getAdapter()).setPlayerInfo(playerInfo);
			}
		}
	};

	private ServiceConnectionListener mConnectionListener=new ServiceConnectionListener() {
		@Override
		public void onServiceDisconnected() {
		}
		@Override
		public void onServiceConnected() {
			//ÿ�ν���activityʱ��Ҫ����������
			mediaPlayerManager.initPlayerMain_SongInfo();
			updateSongItemList();
		}
	};
	
	@Override
	protected void onStart() {
		super.onStart();
		//ע�Ქ����-�㲥������
		mediaPlayerBroadcastReceiver=new MediaPlayerBroadcastReceiver();
		registerReceiver(mediaPlayerBroadcastReceiver, new IntentFilter(MediaPlayerManager.BROADCASTRECEVIER_ACTON));
		//ע����������-�㲥������
		downLoadBroadcastRecevier=new DownLoadBroadcastRecevier();
		registerReceiver(downLoadBroadcastRecevier, new IntentFilter(DownLoadManager.BROADCASTRECEVIER_ACTON));
		mediaPlayerManager.startAndBindService();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(mediaPlayerBroadcastReceiver);
		unregisterReceiver(downLoadBroadcastRecevier);
		mediaPlayerManager.unbindService();
	}

	@Override
	protected void onDestroy() {
		downLoadManager.unbindService();
		super.onDestroy();
	}
	
	
	/**
	 * ������-�㲥������
	 * */
	private class MediaPlayerBroadcastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			int flag=intent.getIntExtra("flag", -1);
			if(flag==MediaPlayerManager.FLAG_CHANGED){
				int currentPosition=intent.getIntExtra("currentPosition", 0);
				int duration=intent.getIntExtra("duration", 0);
				tv_player_currentPosition.setText(Common.formatSecondTime(currentPosition));
				tv_player_duration.setText(Common.formatSecondTime(duration));
				pb_player_progress.setProgress(currentPosition);
				pb_player_progress.setMax(duration);
				
			}else if(flag==MediaPlayerManager.FLAG_PREPARE){
				String albumPic=intent.getStringExtra("albumPic");
				tv_player_title.setText(intent.getStringExtra("title"));
				if(TextUtils.isEmpty(albumPic)){
					ibtn_player_albumart.setImageResource(R.drawable.min_default_album);
				}else{
					Bitmap bitmap=BitmapFactory.decodeFile(albumPic);
					//�ж�SDͼƬ�Ƿ����
					if(bitmap!=null){
						ibtn_player_albumart.setImageBitmap(bitmap);
					}else{
						ibtn_player_albumart.setImageResource(R.drawable.min_default_album);
					}
				}
				int duration=intent.getIntExtra("duration", 0);
				int currentPosition=intent.getIntExtra("currentPosition", 0);
				tv_player_currentPosition.setText(Common.formatSecondTime(currentPosition));
				tv_player_duration.setText(Common.formatSecondTime(duration));
				pb_player_progress.setMax(duration);
				pb_player_progress.setProgress(currentPosition);
				pb_player_progress.setSecondaryProgress(0);
				
				//���²����б�״̬
				updateSongItemList();
			}else if(flag==MediaPlayerManager.FLAG_INIT){//��ʼ��������Ϣ
				int currentPosition=intent.getIntExtra("currentPosition", 0);
				int duration=intent.getIntExtra("duration", 0);
				pb_player_progress.setMax(duration);
				pb_player_progress.setProgress(currentPosition);
				tv_player_currentPosition.setText(Common.formatSecondTime(currentPosition));
				tv_player_duration.setText(Common.formatSecondTime(duration));
				tv_player_title.setText(intent.getStringExtra("title"));
				String albumPic=intent.getStringExtra("albumPic");
				if(TextUtils.isEmpty(albumPic)){
					ibtn_player_albumart.setImageResource(R.drawable.min_default_album);
				}else{
					Bitmap bitmap=BitmapFactory.decodeFile(albumPic);
					//�ж�SD��ͼƬ�Ƿ����
					if(bitmap!=null){
						ibtn_player_albumart.setImageBitmap(bitmap);
					}else{
						ibtn_player_albumart.setImageResource(R.drawable.min_default_album);
					}
				}
				int playerState=intent.getIntExtra("playerState", 0);
				if(playerState==MediaPlayerManager.STATE_PLAYER||playerState==MediaPlayerManager.STATE_PREPARE){//����
					ibtn_player_control.setBackgroundResource(R.drawable.player_btn_mini_pause);
				}else{
					ibtn_player_control.setBackgroundResource(R.drawable.player_btn_mini_player);
				}

				if(mediaPlayerManager.getPlayerState()==MediaPlayerManager.STATE_OVER){
					if(pageNumber==1||pageNumber==6||pageNumber==7||pageNumber==22||pageNumber==33||pageNumber==44||pageNumber==55){
						((SongItemAdapter)lv_list_change_content.getAdapter()).setPlayerState(mediaPlayerManager.getPlayerState());
					}
					if(pageNumber==9){
						((DownLoadListAdapter)lv_list_change_content.getAdapter()).setPlayerState(mediaPlayerManager.getPlayerState());
					}
				}
			}else if(flag==MediaPlayerManager.FLAG_LIST){
				//�Զ��и貥�ţ�����ǰ̨�����б�
				updateSongItemList();
			}else if(flag==MediaPlayerManager.FLAG_BUFFERING){
				int percent=intent.getIntExtra("percent", 0);
				percent=(int)(pb_player_progress.getMax()/100f)*percent;
				pb_player_progress.setSecondaryProgress(percent);
			}
		}
	}
	
	//�Զ��и貥�ţ�����ǰ̨�����б�
	private void updateSongItemList(){
		int[] playerInfo=new int[]{mediaPlayerManager.getSongId(),mediaPlayerManager.getPlayerState()};
		if(mediaPlayerManager.getPlayerFlag()==MediaPlayerManager.PLAYERFLAG_WEB){
			((SongItemWebAdapter)lv_list_web.getAdapter()).setPlayerInfo(playerInfo);
		}else{
			if(pageNumber==1||pageNumber==6||pageNumber==7||pageNumber==22||pageNumber==33||pageNumber==44||pageNumber==55){
				((SongItemAdapter)lv_list_change_content.getAdapter()).setPlayerInfo(playerInfo);
			}
			if(pageNumber==9){
				((DownLoadListAdapter)lv_list_change_content.getAdapter()).setPlayerInfo(playerInfo);
			}
		}
		int state=mediaPlayerManager.getPlayerState();
		if(state==MediaPlayerManager.STATE_PLAYER||state==MediaPlayerManager.STATE_PREPARE){//����
			ibtn_player_control.setBackgroundResource(R.drawable.player_btn_mini_pause);
		}else if(state==MediaPlayerManager.STATE_PAUSE){//��ͣ
			ibtn_player_control.setBackgroundResource(R.drawable.player_btn_mini_player);
		}
	}
	
	/**
	 * ��ʼ�����ع�����Ϣ
	 * */
	private void initDownLoad(){
		List<HashMap<String, Object>> data=Common.getListDownLoadData();
		SimpleAdapter download_adapter = new SimpleAdapter(this, data,
				R.layout.list_item, new String[] { "icon", "title", "icon2" },
				new int[] { R.id.iv_list_item_icon, R.id.tv_list_item_title,
						R.id.iv_list_item_icon2 });

		ListView lv_list_download = (ListView) list_main_download
				.findViewById(R.id.lv_list_download);
		lv_list_download.setAdapter(download_adapter);
		lv_list_download.setOnItemClickListener(list_download_listener);
	}
	
	//�����������¼�
	private SongItemWebAdapter.ItemListener mItemListener=new SongItemWebAdapter.ItemListener() {
		@Override
		public void onDownLoad(Song	song) {
			if(!Common.getNetIsAvailable(ListMainActivity.this)){
				toast=Common.showMessage(toast, ListMainActivity.this, "��ǰ���粻����");
				return;
			}
			if(!Common.isExistSdCard()){
				toast=Common.showMessage(toast, ListMainActivity.this, "���Ȳ���SD��");
				return;
			}
			//�ж��Ƿ��������б���
			if(downLoadInfoDao.isExist(song.getNetUrl())){
				toast=Common.showMessage(toast, ListMainActivity.this, "�˸����Ѿ��������б���");
				return;
			}
			//�ж��Ƿ��Ѿ����ع�
			if(songDao.isExist(song.getNetUrl())){
				toast=Common.showMessage(toast, ListMainActivity.this, "�˸����Ѿ������ع���");
				return;
			}
			//��ӵ������б���
			downLoadManager.add(song);
		}
		
	};
	
	//ImageButton click
	private OnClickListener imageButton_listener=new OnClickListener() {
		
		public void onClick(View v) {
			if(v.getId()==R.id.ibtn_list_content_icon){
				rl_list_content.setVisibility(View.GONE);
				rl_list_main_content.setVisibility(View.VISIBLE);
				pageNumber=0;
			}else if(v.getId()==R.id.ibtn_list_content_do_icon){
				if(pageNumber==5){//�����б�ʱ�������˵�
					doPlayList(0,0,null);
				}
			}else if(v.getId()==R.id.ibtn_player_control){
				PlayerOrPause(null);
			}else if(v.getId()==R.id.ibtn_player_albumart){
				startActivity(new Intent(ListMainActivity.this,PlayerMainActivity.class));
			}
		}
	};
	
	/**
	 * ���Ż���ͣ����
	 * */
	private void PlayerOrPause(View v){
		if(mediaPlayerManager.getPlayerState()==MediaPlayerManager.STATE_NULL){
			toast=Common.showMessage(toast, ListMainActivity.this, "������Ӹ���...");
			return;
		}
		if(v==null){
			//��ǰ�б��Ž���
			if(mediaPlayerManager.getPlayerState()==MediaPlayerManager.STATE_OVER){
				toast=Common.showMessage(toast, ListMainActivity.this, "��ǰ�б��Ѿ�������ϣ�");
				return;
			}
		}
		mediaPlayerManager.pauseOrPlayer();
		final int state=mediaPlayerManager.getPlayerState();
		int itemRsId=0;
		if(state==MediaPlayerManager.STATE_PLAYER||state==MediaPlayerManager.STATE_PREPARE){//����
			ibtn_player_control.setBackgroundResource(R.drawable.player_btn_mini_pause);
			itemRsId=R.drawable.music_list_item_player;
		}else if(state==MediaPlayerManager.STATE_PAUSE){//��ͣ
			ibtn_player_control.setBackgroundResource(R.drawable.player_btn_mini_player);
			itemRsId=R.drawable.music_list_item_pause;
		}
		if(mediaPlayerManager.getPlayerFlag()==MediaPlayerManager.PLAYERFLAG_WEB){
			if(v==null){
				((SongItemWebAdapter)lv_list_web.getAdapter()).setPlayerState(mediaPlayerManager.getPlayerState());
			}else{
				((SongItemWebAdapter.ViewHolder)v.getTag()).tv_web_list_item_number.setBackgroundResource(itemRsId);
			}
		}else{
			if(pageNumber==1||pageNumber==6||pageNumber==7||pageNumber==22||pageNumber==33||pageNumber==44||pageNumber==55){
				if(v==null){
					((SongItemAdapter)lv_list_change_content.getAdapter()).setPlayerState(mediaPlayerManager.getPlayerState());
				}else{
					((SongItemAdapter.ViewHolder)v.getTag()).tv_song_list_item_number.setBackgroundResource(itemRsId);
				}
			}
			if(pageNumber==9){
				if(v==null){
					((DownLoadListAdapter)lv_list_change_content.getAdapter()).setPlayerState(mediaPlayerManager.getPlayerState());
				}else{
					((DownLoadListAdapter.ViewHolder)v.getTag()).tv_download_list_item_number.setBackgroundResource(itemRsId);
				}
			}
		}
	}
	
	/**
	 * ��ӻ���²����б�
	 * */
	private void doPlayList(final int flag,final int id,String text){
		String actionmsg = null;
		final EditText et_newPlayList=new EditText(ListMainActivity.this);
		et_newPlayList.setLayoutParams(params);
		et_newPlayList.setTextSize(15);
		if(flag==0){//�½�
			actionmsg="����";
			et_newPlayList.setHint("�����벥���б�����");
		}else if(flag==1){//����
			actionmsg="����";
			et_newPlayList.setText(text);
			et_newPlayList.selectAll();
		}
		final String actionmsg2=actionmsg;

		new XfDialog.Builder(ListMainActivity.this).setTitle(actionmsg+"�����б�")
		.setView(et_newPlayList,5,10,5,10).setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				String text=et_newPlayList.getText().toString().trim();
				if(!TextUtils.isEmpty(text)){
					if(playerListDao.isExists(text)){
						toast=Common.showMessage(toast, ListMainActivity.this, "�������Ѿ����ڣ�");
					}else{
						PlayerList playerList=new PlayerList();
						playerList.setName(text);
						
						int rowId=-1;
						if(flag==0){//���������б�
							rowId=(int) playerListDao.add(playerList);
						}else if(flag==1){//���²����б�
							playerList.setId(id);
							rowId=playerListDao.update(playerList);
						}
						if(rowId>0){//�ж��Ƿ�ɹ�
							toast=Common.showMessage(toast, ListMainActivity.this, actionmsg2+"�ɹ���");
							lv_list_change_content.setAdapter(new ListItemAdapter(ListMainActivity.this, 
									playerListDao.searchAll(),R.drawable.local_custom));
							dialog.cancel();
							dialog.dismiss();
						}else{
							toast=Common.showMessage(toast, ListMainActivity.this, actionmsg2+"ʧ�ܣ�");
						}
					}
				}
			}
		}).setNegativeButton("ȡ��",null).create().show();
	}

	/**
	 * �����ײ��˵�
	 * */
	private void createMenu(){
		xfMenu=new XfMenu(ListMainActivity.this);
		
		List<int[]> data1=new ArrayList<int[]>();
		data1.add(new int[]{R.drawable.btn_menu_scanner,R.string.scan_title});
		data1.add(new int[]{R.drawable.btn_menu_skin,R.string.skinsetting_title});
		data1.add(new int[]{R.drawable.btn_menu_exit,R.string.exit_title});
		xfMenu.addItem("����", data1, new MenuAdapter.ItemListener() {
			@Override
			public void onClickListener(int position, View view) {
				xfMenu.cancel();
				if(position==0){
					Intent it=new Intent(ListMainActivity.this,ScanMusicActivity.class);
					startActivityForResult(it, 1);
				}else if(position==1){
					Intent it=new Intent(ListMainActivity.this,SkinSettingActivity.class);
					startActivityForResult(it,2);
				}else if(position==2){
					cancelAutoShutdown();
					mediaPlayerManager.stop();
					downLoadManager.stop();
					finish();
				}
			}
		});
		
		List<int[]> data2=new ArrayList<int[]>();
		data2.add(new int[]{R.drawable.btn_menu_sleep,R.string.sleep_title});
		
		SystemSetting setting = new SystemSetting(this, false);
		String brightness=setting.getValue(SystemSetting.KEY_BRIGHTNESS);
		if(brightness!=null&&brightness.equals("0")){//ҹ��ģʽ
			data2.add(new int[]{R.drawable.btn_menu_brightness,R.string.brightness_title});
		}else{
			data2.add(new int[]{R.drawable.btn_menu_darkness,R.string.darkness_title});
		}
		data2.add(new int[]{R.drawable.btn_menu_setting,R.string.systemsetting_title});
		xfMenu.addItem("����", data2, new MenuAdapter.ItemListener() {
			@Override
			public void onClickListener(int position, View view) {
				xfMenu.cancel();
				if(position==0){
					final SystemSetting setting=new SystemSetting(ListMainActivity.this, true);
					final String autotime=setting.getValue(SystemSetting.KEY_AUTO_SLEEP);
					
					final EditText et_alarmTime=new EditText(ListMainActivity.this);
					et_alarmTime.setKeyListener(new DigitsKeyListener());
					et_alarmTime.setHint("��λ������");

					if(!TextUtils.isEmpty(autotime)){
						et_alarmTime.setText(autotime);
					}
					et_alarmTime.setLayoutParams(params);
					et_alarmTime.setTextSize(12);
					new XfDialog.Builder(ListMainActivity.this).setTitle("���ö�ʱ�ر�ʱ��")
					.setView(et_alarmTime).setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							dialog.dismiss();
							AlarmManager alarmManager=(AlarmManager)getSystemService(ALARM_SERVICE);
							PendingIntent operation=PendingIntent.getBroadcast(ListMainActivity.this, 0, new Intent(ListMainActivity.this,AutoShutdownRecevier.class), PendingIntent.FLAG_UPDATE_CURRENT);
							String str=et_alarmTime.getText().toString();
							
							//ȡ����ʱ�ػ�
							if(!TextUtils.isEmpty(autotime)){
								alarmManager.cancel(operation);
							}
							//������ʱ�ػ�
							if(!TextUtils.isEmpty(str)){
								int autotime=Integer.valueOf(str);
								if(autotime!=0){
									setting.setValue(SystemSetting.KEY_AUTO_SLEEP,String.valueOf(autotime));
									alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+autotime*60*1000, operation);	
								}
								toast=Common.showMessage(toast, ListMainActivity.this, "����һ��ͨ����"+et_alarmTime.getText().toString()+"����֮���Զ��ر�");
							}else{
								setting.setValue(SystemSetting.KEY_AUTO_SLEEP,"");
							}
						}
					}).setNegativeButton("ȡ��", null).create().show();
				}else if(position==1){
					setBrightness(view);
				}else if(position==2){
					Intent it=new Intent(ListMainActivity.this,SystemSettingActivity.class);
					startActivity(it);
				}
			}
		});
		
		List<int[]> data3=new ArrayList<int[]>();
		data3.add(new int[]{R.drawable.btn_menu_about,R.string.about_title});
		xfMenu.addItem("����", data3, new MenuAdapter.ItemListener() {
			@Override
			public void onClickListener(int position, View view) {
				xfMenu.cancel();
				startActivity(new Intent(ListMainActivity.this,AboutActivity.class));
			}
		});
		
		xfMenu.create();
	}
	
	/**
	 * ȡ����ʱ�ر�
	 * */
	private void cancelAutoShutdown(){
		AlarmManager alarmManager=(AlarmManager)getSystemService(ALARM_SERVICE);
		PendingIntent operation=PendingIntent.getBroadcast(ListMainActivity.this, 0, new Intent(ListMainActivity.this,AutoShutdownRecevier.class), PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.cancel(operation);
		new SystemSetting(ListMainActivity.this, true).setValue(SystemSetting.KEY_AUTO_SLEEP,"");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==1&&resultCode==1){
			mediaPlayerManager.initScanner_SongInfo();
			updateListAdapterData();
		}else if(requestCode==2&&resultCode==2){
			SystemSetting setting = new SystemSetting(this, false);
			this.getWindow().setBackgroundDrawableResource(
					setting.getCurrentSkinResId());
		}
	}

	//��д���ؼ��¼�
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			if(pageNumber==0){
				int state=mediaPlayerManager.getPlayerState();
				if(state==MediaPlayerManager.STATE_NULL||state==MediaPlayerManager.STATE_OVER||state==MediaPlayerManager.STATE_PAUSE){
					cancelAutoShutdown();
					mediaPlayerManager.stop();
					downLoadManager.stop();
				}
				finish();
				return true;
			}
			return backPage();
		}else if(keyCode==KeyEvent.KEYCODE_MENU&&!xfMenu.isShowing()){
			xfMenu.showAtLocation(findViewById(R.id.rl_parent_cotent), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	/**
	 * ���ؼ��¼�
	 * */
	private boolean backPage(){
		if(pageNumber<10){
			rl_list_content.setVisibility(View.GONE);
			rl_list_main_content.setVisibility(View.VISIBLE);
			pageNumber=0;
			return true;
		}else{
			if(pageNumber==22){
				jumpPage(1, 2,null);
			}else if(pageNumber==33){
				jumpPage(1, 3,null);
			}else if(pageNumber==44){
				jumpPage(1, 4,null);
			}else if(pageNumber==55){
				jumpPage(1, 5,null);
			}
		}
		return false;
	}
	
	//��ʼ����������
	private void initListMusicItem() {
		List<HashMap<String, Object>> data=Common.getListMusicData();
		SimpleAdapter music_adapter = new SimpleAdapter(this, data,
				R.layout.list_item, new String[] { "icon", "title", "icon2" },
				new int[] { R.id.iv_list_item_icon, R.id.tv_list_item_title,
						R.id.iv_list_item_icon2 });

		ListView lv_list_music = (ListView) list_main_music
				.findViewById(R.id.lv_list_music);
		lv_list_music.setAdapter(music_adapter);
		lv_list_music.setOnItemClickListener(list_music_listener);
	}
	
	//�����б���ĵ���¼�
	private OnItemClickListener list_music_listener=new OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			jumpPage(1, position+1,null);
		}
	};
	
	//���ع����б���ĵ���¼�
	private OnItemClickListener list_download_listener=new OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			jumpPage(1, position+8,null);
		}
	};
		
	/**
	 * ��ǰ�����б�Ĳ����б�Ĳ�ѯ����
	 * */
	private String condition=null;
	
	/**
	 * ��תĳҳ���¼�
	 * */
	private void jumpPage(int classIndex,int flag,Object obj){
		int[] playerInfo=new int[]{mediaPlayerManager.getSongId(),mediaPlayerManager.getPlayerState()};
		if(classIndex==1){
			rl_list_main_content.setVisibility(View.GONE);
			rl_list_content.setVisibility(View.VISIBLE);
			ibtn_list_content_icon.setBackgroundResource(R.drawable.player_btn_list);
			btn_list_random_music2.setVisibility(View.GONE);
			ibtn_list_content_do_icon.setVisibility(View.GONE);

			if(flag==1){//ȫ������
				tv_list_content_title.setText("ȫ������");
				List<String[]> data=songDao.searchByAll();
				lv_list_change_content.setAdapter(new SongItemAdapter(ListMainActivity.this, data,playerInfo).setItemListener(songItemListener));
				btn_list_random_music2.setVisibility(View.VISIBLE);
				btn_list_random_music2.setText("(��"+data.size()+"��)�������");
				btn_list_random_music2.setTag(data.size());
			}else if(flag==2){//����
				tv_list_content_title.setText("����");
				lv_list_change_content.setAdapter(new ListItemAdapter(ListMainActivity.this, 
						artistDao.searchAll(),R.drawable.default_list_singer));
			}else if(flag==3){//ר��
				tv_list_content_title.setText("ר��");
				lv_list_change_content.setAdapter(new ListItemAdapter(ListMainActivity.this, 
						albumDao.searchAll(),R.drawable.default_list_album));
			}else if(flag==4){//�ļ���
				tv_list_content_title.setText("�ļ���");
				lv_list_change_content.setAdapter(new ListItemAdapter(ListMainActivity.this,
						songDao.searchByDirectory(), R.drawable.local_file));
			}else if(flag==5){//�����б�
				tv_list_content_title.setText("�����б�");
				lv_list_change_content.setAdapter(new ListItemAdapter(ListMainActivity.this, 
						playerListDao.searchAll(),R.drawable.local_custom));
				ibtn_list_content_do_icon.setVisibility(View.VISIBLE);
			}else if(flag==6){//�����
				tv_list_content_title.setText("�����");
				btn_list_random_music2.setVisibility(View.VISIBLE);
				List<String[]> data=songDao.searchByIsLike();
				lv_list_change_content.setAdapter(new SongItemAdapter(ListMainActivity.this, data,playerInfo).setItemListener(songItemListener));
				btn_list_random_music2.setVisibility(View.VISIBLE);
				btn_list_random_music2.setText("(��"+data.size()+"��)�������");
				btn_list_random_music2.setTag(data.size());
			}else if(flag==7){//�������
				tv_list_content_title.setText("�������");
				btn_list_random_music2.setVisibility(View.VISIBLE);
				List<String[]> data=songDao.searchByLately(mediaPlayerManager.getLatelyStr());
				lv_list_change_content.setAdapter(new SongItemAdapter(ListMainActivity.this, data,playerInfo).setItemListener(songItemListener));
				btn_list_random_music2.setVisibility(View.VISIBLE);
				btn_list_random_music2.setText("(��"+data.size()+"��)�������");
				btn_list_random_music2.setTag(data.size());
			}else if(flag==8){//��������
				tv_list_content_title.setText("��������");
				lv_list_change_content.setAdapter(new DownLoadingListAdapter(ListMainActivity.this, 
						downLoadManager.getDownLoadData()).setItemListener(downLoadingListItemListener));
			}else if(flag==9){//�������
				tv_list_content_title.setText("�������");
				List<Song> data=songDao.searchByDownLoad();
				lv_list_change_content.setAdapter(new DownLoadListAdapter(ListMainActivity.this, 
						data,playerInfo).setItemListener(downLoadListItemListener));
				btn_list_random_music2.setVisibility(View.VISIBLE);
				btn_list_random_music2.setText("(��"+data.size()+"��)�������");
				btn_list_random_music2.setTag(data.size());
			}
			pageNumber=flag;
		}else if(classIndex==2){
			btn_list_random_music2.setVisibility(View.VISIBLE);
			TextView textView=((ListItemAdapter.ViewHolder)obj).textView;
			condition=textView.getTag().toString().trim();
			tv_list_content_title.setText(textView.getText().toString());
			List<String[]> data=null;
			if(flag==22){//ĳ���ָ���
				data=songDao.searchByArtist(condition);
			}else if(flag==33){//ĳר������
				data=songDao.searchByAlbum(condition);
			}else if(flag==44){//ĳ�ļ��и���
				data=songDao.searchByDirectory(condition);
			}else if(flag==55){//ĳ�����б�ĸ���
				data=songDao.searchByPlayerList("$"+condition+"$");
			}
			lv_list_change_content.setAdapter(new SongItemAdapter(ListMainActivity.this, data,playerInfo).setItemListener(songItemListener));
			btn_list_random_music2.setText("(��"+data.size()+"��)�������");
			btn_list_random_music2.setTag(data.size());
			pageNumber=flag;
		}
	}
	
	/**
	 * ���±����б������չʾ��ɨ���
	 * */
	private void updateListAdapterData(){
		int[] playerInfo=new int[]{mediaPlayerManager.getSongId(),mediaPlayerManager.getPlayerState()};
		if(pageNumber==1){
			List<String[]> data=songDao.searchByAll();
			lv_list_change_content.setAdapter(new SongItemAdapter(ListMainActivity.this, data,playerInfo).setItemListener(songItemListener));
			btn_list_random_music2.setText("(��"+data.size()+"��)�������");
			btn_list_random_music2.setTag(data.size());
		}else if(pageNumber==22||pageNumber==33||pageNumber==44||pageNumber==55){
			List<String[]> data=null;
			if(pageNumber==22){
				data=songDao.searchByArtist(condition);
			}else if(pageNumber==33){
				data=songDao.searchByAlbum(condition);
			}else if(pageNumber==44){
				data=songDao.searchByDirectory(condition);
			}else if(pageNumber==55){
				data=songDao.searchByPlayerList("$"+condition+"$");
			}
			lv_list_change_content.setAdapter(new SongItemAdapter(ListMainActivity.this, data,playerInfo).setItemListener(songItemListener));
			btn_list_random_music2.setText("(��"+data.size()+"��)�������");
			btn_list_random_music2.setTag(data.size());
		}
	}
	
	/**
	 * ��������-�㲥������
	 * */
	private class DownLoadBroadcastRecevier extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			int flag=intent.getIntExtra("flag", -1);
			if(flag==DownLoadManager.FLAG_CHANGED){
				if(pageNumber==8){
					((DownLoadingListAdapter)lv_list_change_content.getAdapter()).setData(downLoadManager.getDownLoadData());
				}
			}else if(flag==DownLoadManager.FLAG_WAIT){
				toast=Common.showMessage(toast, ListMainActivity.this,"\""+intent.getStringExtra("displayname")+"\""+"��ӵ��������б���!");
			}else if(flag==DownLoadManager.FLAG_COMPLETED){
				if(pageNumber==8){
					((DownLoadingListAdapter)lv_list_change_content.getAdapter()).setData(downLoadManager.getDownLoadData());
				}
				toast=Common.showMessage(toast, ListMainActivity.this,"\""+intent.getStringExtra("displayname")+"\""+"�������!");
			}else if(flag==DownLoadManager.FLAG_FAILED){
				if(pageNumber==8){
					((DownLoadingListAdapter)lv_list_change_content.getAdapter()).setData(downLoadManager.getDownLoadData());
				}
				toast=Common.showMessage(toast, ListMainActivity.this,"\""+intent.getStringExtra("displayname")+"\""+"�������Ҳ����ļ�!");
			}else if(flag==DownLoadManager.FLAG_TIMEOUT){
				if(pageNumber==8){
					((DownLoadingListAdapter)lv_list_change_content.getAdapter()).setData(downLoadManager.getDownLoadData());
				}
				toast=Common.showMessage(toast, ListMainActivity.this,"\""+intent.getStringExtra("displayname")+"\""+"�����Ѿ���ʱ!");
			}else if(flag==DownLoadManager.FLAG_ERROR){
				if(pageNumber==8){
					((DownLoadingListAdapter)lv_list_change_content.getAdapter()).setData(downLoadManager.getDownLoadData());
				}
				toast=Common.showMessage(toast, ListMainActivity.this,"\""+intent.getStringExtra("displayname")+"\""+"��������!");
			}else if(flag==DownLoadManager.FLAG_COMMON){
				if(pageNumber==8){
					((DownLoadingListAdapter)lv_list_change_content.getAdapter()).setData(downLoadManager.getDownLoadData());
				}
			}
		}
		
	}
	
	/**
	 * ɾ�����������ò����б�
	 * */
	private void deleteForResetPlayerList(int songId,int flag,String parameter){
		final int state=mediaPlayerManager.getPlayerState();
		if(state==MediaPlayerManager.STATE_NULL||state==MediaPlayerManager.STATE_OVER){
			return;
		}
		if(mediaPlayerManager.getPlayerFlag()==MediaPlayerManager.PLAYERFLAG_WEB){
			return;
		}
		String t_parameter=mediaPlayerManager.getParameter();
		if(t_parameter==null) t_parameter="";
		if(flag==MediaPlayerManager.PLAYERFLAG_ALL||(mediaPlayerManager.getPlayerFlag()==flag&&parameter.equals(t_parameter))){
			//ɾ��'�����б�'���Ͳ���ȫ������
			if(songId==-1){
				mediaPlayerManager.delete(-1);
				return;
			}else{
				//����ǵ�ǰ���Ÿ�������Ҫ�л���һ��
				if(songId==mediaPlayerManager.getSongId()){
					mediaPlayerManager.delete(songId);
				}
			}
			mediaPlayerManager.resetPlayerList();
		}
	}
	
	/**
	 * ��������-��ʼ����/��ͣ���أ�ɾ��
	 * */
	private DownLoadingListAdapter.ItemListener downLoadingListItemListener=new DownLoadingListAdapter.ItemListener() {
		
		@Override
		public void onDelete(String url) {
			downLoadManager.delete(url);
		}

		@Override
		public void onPause(String url,int state) {
			if(state==DownLoadManager.STATE_PAUSE||state==DownLoadManager.STATE_ERROR||state==DownLoadManager.STATE_FAILED){
				downLoadManager.start(url);
			}else if(state==DownLoadManager.STATE_DOWNLOADING||state==DownLoadManager.STATE_CONNECTION||state==DownLoadManager.STATE_WAIT){
				downLoadManager.pause(url);
			}
		}
	};
	
	/**
	 * �������-ɾ��
	 * */
	private DownLoadListAdapter.ItemListener downLoadListItemListener=new DownLoadListAdapter.ItemListener(){
		@Override
		public void onDelete(int id, String path, int position) {
			createDeleteSongDialog(id, path, position,false);
		}
	};
	
	//�������ֺ����ع���Ķ���������-�滻��ListView ItemClick�¼�
	private OnItemClickListener list_change_content_listener=new OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if(pageNumber==2){//���������б�
				jumpPage(2, 22,view.getTag());
			}else if(pageNumber==3){//ר�������б�
				jumpPage(2, 33,view.getTag());
			}else if(pageNumber==4){//�ļ��������б�
				jumpPage(2, 44,view.getTag());
			}else if(pageNumber==5){//���������б�
				ibtn_list_content_do_icon.setVisibility(View.GONE);
				jumpPage(2, 55,view.getTag());
			}else if(pageNumber==8){//���������б�
				DownLoadingListAdapter.ViewHolder viewHolder=(DownLoadingListAdapter.ViewHolder)view.getTag();
				int state=Integer.valueOf(viewHolder.tv_download_list_item_top.getTag().toString());
				String url=viewHolder.tv_download_list_item_number.getTag().toString();
				downLoadingListItemListener.onPause(url, state);
			}else if(pageNumber==9){//��������б�
				if(mediaPlayerManager.getPlayerFlag()==MediaPlayerManager.PLAYERFLAG_WEB){
					int[] playerInfo=new int[]{-1,-1};
					((SongItemWebAdapter)lv_list_web.getAdapter()).setPlayerInfo(playerInfo);
				}
				int songId=Integer.valueOf(((DownLoadListAdapter.ViewHolder)view.getTag()).ibtn_download_list_item_menu.getTag().toString());
				if(songId==mediaPlayerManager.getSongId()){
					PlayerOrPause(view);
				}else {
					ibtn_player_control.setBackgroundResource(R.drawable.player_btn_mini_pause);
					mediaPlayerManager.player(songId,MediaPlayerManager.PLAYERFLAG_DOWNLOAD, null);
					int[] playerInfo=new int[]{songId,mediaPlayerManager.getPlayerState()};
					((DownLoadListAdapter)lv_list_change_content.getAdapter()).setPlayerInfo(playerInfo);
				}
			}else if(pageNumber==1){//ȫ�������б�
				playerMusicByItem(view,MediaPlayerManager.PLAYERFLAG_ALL,null);
			}else if(pageNumber==6){//������б�
				playerMusicByItem(view,MediaPlayerManager.PLAYERFLAG_LIKE,null);
			}else if(pageNumber==7){//��������б�
				playerMusicByItem(view,MediaPlayerManager.PLAYERFLAG_LATELY,null);			
			}else if(pageNumber==22){//ĳ���ָ����б�
				playerMusicByItem(view,MediaPlayerManager.PLAYERFLAG_ARTIST,condition);
			}else if(pageNumber==33){//ĳר�������б�
				playerMusicByItem(view,MediaPlayerManager.PLAYERFLAG_ALBUM,condition);
			}else if(pageNumber==44){//ĳ�ļ��и����б�
				playerMusicByItem(view,MediaPlayerManager.PLAYERFLAG_FOLDER,condition);
			}else if(pageNumber==55){//ĳ�����б�����б�
				playerMusicByItem(view,MediaPlayerManager.PLAYERFLAG_PLAYERLIST,condition);
			}
		}
	};
	
	private void playerMusicByItem(View view,int flag,String condition){
		if(mediaPlayerManager.getPlayerFlag()==MediaPlayerManager.PLAYERFLAG_WEB){
			int[] playerInfo=new int[]{-1,-1};
			((SongItemWebAdapter)lv_list_web.getAdapter()).setPlayerInfo(playerInfo);
		}
		int songId=Integer.valueOf(((SongItemAdapter.ViewHolder)view.getTag()).tv_song_list_item_bottom.getTag().toString());
		if(songId==mediaPlayerManager.getSongId()){
			PlayerOrPause(view);
		}else {
			ibtn_player_control.setBackgroundResource(R.drawable.player_btn_mini_pause);
			mediaPlayerManager.player(songId,flag, condition);
			int[] playerInfo=new int[]{songId,mediaPlayerManager.getPlayerState()};
			((SongItemAdapter)lv_list_change_content.getAdapter()).setPlayerInfo(playerInfo);
		}
	}
	
	//�������ֺ����ع���Ķ���������-�滻��ListView ItemLoogClick�¼�
	private OnItemLongClickListener list_change_content_looglistener=new OnItemLongClickListener() {
		
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			if(pageNumber==5){//�����б�ʱ�������˵�
				if(position!=0){
					doPlayListLoogItemDialog(view);
					return true;
				}
			}else{
				if(!(pageNumber==2||pageNumber==3||pageNumber==4||pageNumber==5||pageNumber==8||pageNumber==9)){
					final SongItemAdapter.ViewHolder viewHolder=(SongItemAdapter.ViewHolder)view.getTag();
					final String path=viewHolder.tv_song_list_item_top.getTag().toString();//����·��
					final int sid=Integer.parseInt(viewHolder.tv_song_list_item_bottom.getTag().toString());//����id
					final String text=viewHolder.tv_song_list_item_top.getText().toString();
					
					doListSongLoogItemDialog(sid,text,path,position);
				}
			}
			return false;
		}
		
	};
	
	//�����б����¼�
	private SongItemAdapter.ItemListener songItemListener=new SongItemAdapter.ItemListener() {
		@Override
		public void onLikeClick(int id, View view, int position) {
			//�ų�����������б�
			if(pageNumber==6){
				songDao.updateByLike(id, 0);
				//���¸����б�
				((SongItemAdapter)lv_list_change_content.getAdapter()).deleteItem(position);
				btn_list_random_music2.setText("(��"+lv_list_change_content.getCount()+"��)�������");
				btn_list_random_music2.setTag(lv_list_change_content.getCount());
				deleteForResetPlayerList(id,MediaPlayerManager.PLAYERFLAG_LIKE,"");
				return;
			}
			if(view.getTag().equals("1")){
				view.setTag("0");
				view.setBackgroundResource(R.drawable.dislike);
				songDao.updateByLike(id, 0);
			}else{
				view.setTag("1");
				view.setBackgroundResource(R.drawable.like);
				songDao.updateByLike(id, 1);
			}
		}
		@Override
		public void onMenuClick(int id, String text, String path, int position) {
			doListSongLoogItemDialog(id,text,path,position);
		}
	};
	
	/**
	 * ���������б�˵��Ի���
	 * */
	private void doListSongLoogItemDialog(final int sid,String text,final String path,final int parentposition){
		String delete_title="�Ƴ�����";
		if(pageNumber==9){
			delete_title="�������";
		}
		
		String[] menustring=new String[]{"��ӵ��б�","��Ϊ����",delete_title,"�鿴����"};
		ListView menulist=new ListView(ListMainActivity.this);
		menulist.setCacheColorHint(Color.TRANSPARENT);
		menulist.setDividerHeight(1);
		menulist.setAdapter(new ArrayAdapter<String>(ListMainActivity.this, R.layout.dialog_menu_item, R.id.text1, menustring));
		menulist.setLayoutParams(new LayoutParams(Common.getScreen(ListMainActivity.this)[0]/2, LayoutParams.WRAP_CONTENT));
		
		final XfDialog xfdialog=new XfDialog.Builder(ListMainActivity.this).setTitle(text).setView(menulist).create();
		xfdialog.show();
		
		menulist.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				xfdialog.cancel();
				xfdialog.dismiss();
				if(position==0){//��ӵ��б�
					createPlayerListDialog(sid);
				}else if(position==1){//��Ϊ����
					createRingDialog(path);
				}else if(position==2){//�Ƴ�����
					createDeleteSongDialog(sid,path,parentposition,true);
				}else if(position==3){//�鿴����
					createSongDetailDialog(sid);
				}
			}
		});
	}
	
	/**
	 * ������ϸ�Ի���
	 * */
	private void createSongDetailDialog(int id){
		Song song=songDao.searchById(id);
		File file=new File(song.getFilePath());
		//����������
		if(!file.exists()){
			toast=Common.showMessage(toast, ListMainActivity.this, "�����Ѿ������ڣ���ɾ��������");
			return;
		}
		if(song.getSize()==-1){
			song.setSize((int)file.length());
			songDao.updateBySize(id, song.getSize());
		}
		//��ʾ��ʱɨ��ʱ������ý����в����ڵĸ���
		int duration=song.getDurationTime();
		if(duration==-1){
			//��ȡ����ʱ��
			MediaPlayer t_MediaPlayer=new MediaPlayer();	
			try {
				t_MediaPlayer.setDataSource(song.getFilePath());
				t_MediaPlayer.prepare();
				duration=t_MediaPlayer.getDuration();
			} catch (IllegalArgumentException e) {
			} catch (IllegalStateException e) {
			} catch (IOException e) {
			}finally{
				t_MediaPlayer.release();
				t_MediaPlayer=null;
			}
			if(duration!=-1){
				song.setDurationTime(duration);
				//�������ݿ�
				songDao.updateByDuration(id,duration);
			}
		}
		
		View view=inflater.inflate(R.layout.song_detail, null);
		view.setLayoutParams(new LayoutParams(Common.getScreen(ListMainActivity.this)[0]/2, LayoutParams.WRAP_CONTENT));
		
		((TextView)view.findViewById(R.id.tv_song_title)).setText(song.getName());
		((TextView)view.findViewById(R.id.tv_song_album)).setText(song.getAlbum().getName());
		((TextView)view.findViewById(R.id.tv_song_artist)).setText(song.getArtist().getName());
		((TextView)view.findViewById(R.id.tv_song_duration)).setText(Common.formatSecondTime(duration));
		((TextView)view.findViewById(R.id.tv_song_filepath)).setText(song.getFilePath());
		((TextView)view.findViewById(R.id.tv_song_format)).setText(Common.getSuffix(song.getDisplayName()));
		((TextView)view.findViewById(R.id.tv_song_size)).setText(Common.formatByteToMB(song.getSize())+"MB");
		
		new XfDialog.Builder(ListMainActivity.this).setTitle("������ϸ��Ϣ").setNeutralButton("ȷ��", null).setView(view).create().show();
	}
	
	/**
	 * ��ӵ��б�Ի���
	 * */
	private void createPlayerListDialog(final int id){
		List<String[]> pList=playerListDao.searchAll();
		
		RadioGroup rg_pl=new RadioGroup(ListMainActivity.this);
		rg_pl.setLayoutParams(params);
		final List<RadioButton> rbtns=new ArrayList<RadioButton>();
		
		for (int i = 0; i < pList.size();i++) {
			String[] str_temp=pList.get(i);
			RadioButton rbtn_temp=new RadioButton(ListMainActivity.this);
			rbtn_temp.setText(str_temp[1]);
			rbtn_temp.setTag(str_temp[0]);
			rg_pl.addView(rbtn_temp, params);
			rbtns.add(rbtn_temp);
		}
		
		new XfDialog.Builder(ListMainActivity.this).setTitle("�����б�").setView(rg_pl).setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				dialog.dismiss();
				int selectedIndex=-1;
				for (int i = 0; i < rbtns.size(); i++) {
					if(rbtns.get(i).isChecked()){
						selectedIndex=i;
						break;
					}
				}
				if(selectedIndex!=-1){
					songDao.updateByPlayerList(id,Integer.valueOf(rbtns.get(selectedIndex).getTag().toString()));
					toast=Common.showMessage(toast, ListMainActivity.this, "��ӳɹ�");
				}else{
					toast=Common.showMessage(toast, ListMainActivity.this, "��ѡ��Ҫ��ӵ��Ĳ����б�");
				}
			}
		}).setNegativeButton("ȡ��", null).create().show();
	}
	
	/**
	 * �Ƴ������Ի���:flag�Ƿ��Ǳ��ظ����б�ɾ��
	 * */
	private void createDeleteSongDialog(final int sid,final String filepath,final int position,final boolean flag){
		String t_title="�Ƴ�����";
		if(pageNumber==9){
			t_title="�������";
		}
		final String title=t_title;
		final CheckBox cb_deletesong=new CheckBox(ListMainActivity.this);
		cb_deletesong.setLayoutParams(params);
		cb_deletesong.setText("ͬʱɾ�������ļ�");

		XfDialog.Builder builder=new XfDialog.Builder(ListMainActivity.this).setView(cb_deletesong)
		.setTitle(title).setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(cb_deletesong.isChecked()){
					Common.deleteFile(ListMainActivity.this, filepath);
				}
				int rs=0;
				//�Ӳ����б����Ƴ�
				if(!cb_deletesong.isChecked()&&pageNumber==55){
					rs=songDao.deleteByPlayerList(sid, Integer.valueOf(condition));
				}else{
					//û��ѡ�в������������ɾ��
					if(!cb_deletesong.isChecked()&&!flag){
						rs=songDao.updateByDownLoadState(sid);
					}else{
						rs=songDao.delete(sid);
					}
				}
				if(rs>0){
					toast=Common.showMessage(toast, ListMainActivity.this, title+"�ɹ�");
					dialog.cancel();
					dialog.dismiss();
					
					//���¸����б�
					if(flag){
						((SongItemAdapter)lv_list_change_content.getAdapter()).deleteItem(position);
					}else{
						((DownLoadListAdapter)lv_list_change_content.getAdapter()).deleteItem(position);
					}
					if(pageNumber==1){
						deleteForResetPlayerList(sid,MediaPlayerManager.PLAYERFLAG_ALL,"");
					}else if(pageNumber==6){
						deleteForResetPlayerList(sid,MediaPlayerManager.PLAYERFLAG_LIKE,"");
					}else if(pageNumber==7){
						deleteForResetPlayerList(sid,MediaPlayerManager.PLAYERFLAG_LATELY,"");
					}else if(pageNumber==9){
						deleteForResetPlayerList(sid,MediaPlayerManager.PLAYERFLAG_DOWNLOAD,"");
					}else if(pageNumber==22){
						deleteForResetPlayerList(sid,MediaPlayerManager.PLAYERFLAG_ARTIST,condition);
					}else if(pageNumber==33){
						deleteForResetPlayerList(sid,MediaPlayerManager.PLAYERFLAG_ALBUM,condition);
					}else if(pageNumber==44){
						deleteForResetPlayerList(sid,MediaPlayerManager.PLAYERFLAG_FOLDER,condition);
					}else if(pageNumber==55){
						deleteForResetPlayerList(sid,MediaPlayerManager.PLAYERFLAG_PLAYERLIST,condition);
					}
					btn_list_random_music2.setText("(��"+lv_list_change_content.getCount()+"��)�������");
					btn_list_random_music2.setTag(lv_list_change_content.getCount());
				}else{
					toast=Common.showMessage(toast, ListMainActivity.this, title+"ʧ��");
				}
			}
		}).setNegativeButton("ȡ��", null);

		builder.create().show();
	}
	
	/**
	 * ���������Ի���
	 * */
	private void createRingDialog(final String filepath){
		RadioGroup rg_ring=new RadioGroup(ListMainActivity.this);
		rg_ring.setLayoutParams(params);
		final RadioButton rbtn_ringtones=new RadioButton(ListMainActivity.this);
		rbtn_ringtones.setText("��������");
		rg_ring.addView(rbtn_ringtones, params);
		final RadioButton rbtn_alarms=new RadioButton(ListMainActivity.this);
		rbtn_alarms.setText("��������");
		rg_ring.addView(rbtn_alarms, params);
		final RadioButton rbtn_notifications=new RadioButton(ListMainActivity.this);
		rbtn_notifications.setText("֪ͨ����");
		rg_ring.addView(rbtn_notifications, params);
		final RadioButton rbtn_all=new RadioButton(ListMainActivity.this);
		rbtn_all.setText("ȫ������");
		rg_ring.addView(rbtn_all, params);
		
		new XfDialog.Builder(ListMainActivity.this).setTitle("��������").setView(rg_ring).setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				ContentValues cv=new ContentValues();
				int type=-1;
				if(rbtn_ringtones.isChecked()){
					type=RingtoneManager.TYPE_RINGTONE;
					cv.put(MediaStore.Audio.Media.IS_RINGTONE, true);
				}else if(rbtn_alarms.isChecked()){
					type=RingtoneManager.TYPE_ALARM;
					cv.put(MediaStore.Audio.Media.IS_ALARM, true);
				}else if(rbtn_notifications.isChecked()){
					type=RingtoneManager.TYPE_NOTIFICATION;
					cv.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
				}else if(rbtn_all.isChecked()){
					type=RingtoneManager.TYPE_ALL;
					cv.put(MediaStore.Audio.Media.IS_RINGTONE, true);
					cv.put(MediaStore.Audio.Media.IS_ALARM, true);
					cv.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
				}
				if(type==-1){
					toast=Common.showMessage(toast, ListMainActivity.this, "��ѡ����������");
				}else{
					Uri uri = MediaStore.Audio.Media.getContentUriForPath(filepath);
					Uri ringtoneUri =null;
					Cursor cursor = ListMainActivity.this.getContentResolver().query(uri, null, MediaStore.MediaColumns.DATA + "=?", new String[] { filepath },null);
					//��ѯý����д��ڵ�
					if (cursor.getCount() > 0&&cursor.moveToFirst()) {
						String _id = cursor.getString(0);
						  //����ý���
						  getContentResolver().update(uri, cv, MediaStore.MediaColumns.DATA + "=?",new String[] { filepath }); 
						  ringtoneUri= Uri.withAppendedPath(uri, _id);
					}else{//�����ھ����
						 cv.put(MediaStore.MediaColumns.DATA,filepath);
						 ringtoneUri= ListMainActivity.this.getContentResolver().insert(uri, cv);
					}
					try {
						RingtoneManager.setActualDefaultRingtoneUri(ListMainActivity.this, type, ringtoneUri);
						toast=Common.showMessage(toast, ListMainActivity.this, "�������óɹ�");
					} catch (Exception e) {
						toast=Common.showMessage(toast, ListMainActivity.this, "��������ʧ��");
					}
					dialog.cancel();
					dialog.dismiss();
				}
			}
		}).setNegativeButton("ȡ��", null).show();
	}
	
	/**
	 * ���������б�˵��Ի���
	 * */
	private void doPlayListLoogItemDialog(View view){
		final TextView textView=((ListItemAdapter.ViewHolder)view.getTag()).textView;
		final String text=textView.getText().toString();//�����б�����
		final int plid=Integer.parseInt(textView.getTag().toString());//�����б�id
		
		String[] menustring=new String[]{"������","ɾ��"};
		ListView menulist=new ListView(ListMainActivity.this);
		menulist.setCacheColorHint(Color.TRANSPARENT);
		menulist.setDividerHeight(1);
		menulist.setAdapter(new ArrayAdapter<String>(ListMainActivity.this, R.layout.dialog_menu_item, R.id.text1, menustring));
		menulist.setLayoutParams(new LayoutParams(Common.getScreen(ListMainActivity.this)[0]/2, LayoutParams.WRAP_CONTENT));
		
		final XfDialog xfdialog=new XfDialog.Builder(ListMainActivity.this).setTitle(text).setView(menulist).create();
		xfdialog.show();
		
		menulist.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(position==0){//������
					xfdialog.cancel();
					xfdialog.dismiss();
					doPlayList(1, plid,text);
				}else if(position==1){//ɾ��
					xfdialog.cancel();
					xfdialog.dismiss();
					new XfDialog.Builder(ListMainActivity.this).setTitle("ɾ����ʾ").setMessage("�Ƿ�Ҫɾ����������б�")
					.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if(playerListDao.delete(plid)>0){
								toast=Common.showMessage(toast, ListMainActivity.this, "ɾ���ɹ���");
								lv_list_change_content.setAdapter(new ListItemAdapter(ListMainActivity.this, 
										playerListDao.searchAll(),R.drawable.local_custom));
								
								//�������ڲ����б�
								deleteForResetPlayerList(-1, MediaPlayerManager.PLAYERFLAG_PLAYERLIST, String.valueOf(plid));
							}else{
								toast=Common.showMessage(toast, ListMainActivity.this, "ɾ��ʧ�ܣ�");
							}
							dialog.cancel();
							dialog.dismiss();
						}
					}).setNegativeButton("ȡ��",null).create().show();
				}
			}
			
		});
		
	}
	
	/**
	 * ��ʼ��������
	 * */
	private void initTabItem() {
		for (int i = 0; i < vg_list_tab_item.length; i++) {
			vg_list_tab_item[i].setOnClickListener(tabClickListener);
			if (screenIndex == i) {
				vg_list_tab_item[0]
						.setBackgroundResource(R.drawable.list_top_press);
			}
			((ImageView) vg_list_tab_item[i]
					.findViewById(R.id.iv_list_item_icon))
					.setImageResource(list_item_icons[i]);
			((TextView) vg_list_tab_item[i]
					.findViewById(R.id.tv_list_item_text))
					.setText(list_item_items[i]);
		}
	}

	//����Ļ���һ����¼�
	private OnScrollToScreenListener scrollToScreenListener = new OnScrollToScreenListener() {

		public void operation(int currentScreen, int screenCount) {
			vg_list_tab_item[screenIndex].setBackgroundResource(0);
			screenIndex = currentScreen;
			vg_list_tab_item[screenIndex]
					.setBackgroundResource(R.drawable.list_top_press);
		}
	};

	//������ѡ��л��¼�
	private OnClickListener tabClickListener = new OnClickListener() {

		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.list_tab_item_music:
				if (screenIndex == 0) {
					return;
				}
				vg_list_tab_item[screenIndex].setBackgroundResource(0);
				screenIndex = 0;
				break;
			case R.id.list_tab_item_web:
				if (screenIndex == 1) {
					return;
				}
				vg_list_tab_item[screenIndex].setBackgroundResource(0);
				screenIndex = 1;
				break;
			case R.id.list_tab_item_download:
				if (screenIndex == 2) {
					return;
				}
				vg_list_tab_item[screenIndex].setBackgroundResource(0);
				screenIndex = 2;
				break;
			default:
				break;
			}
			vg_list_tab_item[screenIndex]
					.setBackgroundResource(R.drawable.list_top_press);
			fgv_list_main.setToScreen(screenIndex, true);
		}
	};
}
