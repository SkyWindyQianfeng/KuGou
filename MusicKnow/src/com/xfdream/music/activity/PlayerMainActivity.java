package com.xfdream.music.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.xfdream.music.R;
import com.xfdream.music.custom.FlingGalleryView;
import com.xfdream.music.custom.XfDialog;
import com.xfdream.music.custom.FlingGalleryView.OnCustomTouchListener;
import com.xfdream.music.service.MediaPlayerManager;
import com.xfdream.music.service.MediaPlayerManager.ServiceConnectionListener;
import com.xfdream.music.util.Common;
import com.xfdream.music.util.ImageUtil;

public class PlayerMainActivity extends BaseActivity {
	private RelativeLayout ll_player_voice;

	//���Ʋ���
	private ImageButton ibtn_player_voice;
	private ImageButton ibtn_player_list;
	private ImageButton ibtn_player_control_menu;
	private ImageButton ibtn_player_control_mode;
	private ImageButton ibtn_player_control_pre;
	private ImageButton ibtn_player_control_play;
	private ImageButton ibtn_player_control_next;
	
	//������Ϣ
	private TextView tv_player_playing_time;
	private TextView tv_player_playering_duration;
	private TextView tv_player_song_info;

	//��������
	private SeekBar sb_player_voice;
	//���ڲ��Ž���
	private SeekBar sb_player_playprogress;

	//ר��
	private ViewGroup player_main_album;
	private ImageView iv_player_ablum;
	private ImageView iv_player_ablum_reflection;

	private FlingGalleryView fgv_player_main;

	// ���������ʾ�����ض���
	private Animation showVoicePanelAnimation;
	private Animation hiddenVoicePanelAnimation;

	// ����ģʽ
	private String[] player_modeStr;

	//����ģʽ��Drawable Id
	private static final int[] MODE_DRAWABLE_ID=new int[]{
		R.drawable.player_btn_player_mode_circlelist,
		R.drawable.player_btn_player_mode_random,
		R.drawable.player_btn_player_mode_circleone,
		R.drawable.player_btn_player_mode_sequence
	};
	
	private Toast toastMsg;
	private MediaPlayerManager mediaPlayerManager;
	private MediaPlayerBroadcastReceiver mediaPlayerBroadcastReceiver;
	
	private AudioManager audioManager;// ��ȡϵͳ��Ƶ����

	private boolean isSeekDrag=false;//�����Ƿ����϶�
	
	//�����ʾ����
	private ViewGroup player_main_lyric;
	private TextView tv_player_lyric_info;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_main);
		
		ll_player_voice = (RelativeLayout) this
				.findViewById(R.id.ll_player_voice);

		ibtn_player_voice = (ImageButton) this
				.findViewById(R.id.ibtn_player_voice);
		ibtn_player_list = (ImageButton) this
				.findViewById(R.id.ibtn_player_list);
		ibtn_player_control_menu = (ImageButton) this
				.findViewById(R.id.ibtn_player_control_menu);
		ibtn_player_control_mode = (ImageButton) this
				.findViewById(R.id.ibtn_player_control_mode);
		ibtn_player_control_pre = (ImageButton) this
				.findViewById(R.id.ibtn_player_control_pre);
		ibtn_player_control_play = (ImageButton) this
				.findViewById(R.id.ibtn_player_control_play);
		ibtn_player_control_next = (ImageButton) this
				.findViewById(R.id.ibtn_player_control_next);

		ibtn_player_voice.setOnClickListener(listener);
		ibtn_player_list.setOnClickListener(listener);
		ibtn_player_control_menu.setOnClickListener(listener);
		ibtn_player_control_mode.setOnClickListener(listener);
		ibtn_player_control_pre.setOnClickListener(listener);
		ibtn_player_control_play.setOnClickListener(listener);
		ibtn_player_control_next.setOnClickListener(listener);

		tv_player_playing_time = (TextView) this
				.findViewById(R.id.tv_player_playing_time);
		tv_player_playering_duration = (TextView) this
				.findViewById(R.id.tv_player_playering_duration);
		tv_player_song_info = (TextView) this
				.findViewById(R.id.tv_player_song_info);

		sb_player_voice = (SeekBar) this.findViewById(R.id.sb_player_voice);
		sb_player_playprogress = (SeekBar) this
				.findViewById(R.id.sb_player_playprogress);

		sb_player_voice.setOnSeekBarChangeListener(seekBarChangeListener);
		sb_player_playprogress
				.setOnSeekBarChangeListener(seekBarChangeListener);
		sb_player_playprogress.setMax(100);

		player_main_album = (ViewGroup) this
				.findViewById(R.id.player_main_album);
		iv_player_ablum = (ImageView) player_main_album
				.findViewById(R.id.iv_player_ablum);
		iv_player_ablum_reflection = (ImageView) player_main_album
				.findViewById(R.id.iv_player_ablum_reflection);
		setAlbum(R.drawable.default_album);
		
		fgv_player_main = (FlingGalleryView) this
				.findViewById(R.id.fgv_player_main);
		fgv_player_main.setOnCustomTouchListener(customTouchListener);
		
		showVoicePanelAnimation = AnimationUtils.loadAnimation(
				PlayerMainActivity.this, R.anim.push_up_in);
		hiddenVoicePanelAnimation = AnimationUtils.loadAnimation(
				PlayerMainActivity.this, R.anim.push_up_out);

		player_modeStr = getResources().getStringArray(R.array.player_mode);

		player_main_lyric=(ViewGroup)this.findViewById(R.id.player_main_lyric);
		tv_player_lyric_info=(TextView)player_main_lyric.findViewById(R.id.tv_player_lyric_info);
		tv_player_lyric_info.setText("��ʱ�޸��");
		
		//ע�Ქ����-�㲥������
		mediaPlayerBroadcastReceiver=new MediaPlayerBroadcastReceiver();
		registerReceiver(mediaPlayerBroadcastReceiver, new IntentFilter(MediaPlayerManager.BROADCASTRECEVIER_ACTON));
		//����������
		mediaPlayerManager=new MediaPlayerManager(this);
		mediaPlayerManager.setConnectionListener(mConnectionListener);
		mediaPlayerManager.startAndBindService();
		
		// ��ȡϵͳ��������
		audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		// ��ȡϵͳ���ֵ�ǰ����
		int currentVolume = audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		sb_player_voice.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		sb_player_voice.setProgress(currentVolume);
	}
	
	private ServiceConnectionListener mConnectionListener=new ServiceConnectionListener() {
		@Override
		public void onServiceDisconnected() {
		}
		@Override
		public void onServiceConnected() {			
			mediaPlayerManager.initPlayerMain_SongInfo();
		}
	};
	
	/**
	 * ������-�㲥������
	 * */
	private class MediaPlayerBroadcastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			int flag=intent.getIntExtra("flag", -1);
			if(flag==MediaPlayerManager.FLAG_CHANGED){
				if(!isSeekDrag){
					int currentPosition=intent.getIntExtra("currentPosition", 0);
					int duration=intent.getIntExtra("duration", 0);
					tv_player_playing_time.setText(Common.formatSecondTime(currentPosition));
					tv_player_playering_duration.setText(Common.formatSecondTime(duration));
					sb_player_playprogress.setProgress(currentPosition);
					sb_player_playprogress.setMax(duration);
				}
			}else if(flag==MediaPlayerManager.FLAG_PREPARE){
				String albumPic=intent.getStringExtra("albumPic");
				tv_player_song_info.setText(intent.getStringExtra("title"));
				if(TextUtils.isEmpty(albumPic)){
					setAlbum(R.drawable.default_album);
				}else{
					Bitmap bitmap=BitmapFactory.decodeFile(albumPic);
					//�ж�SDͼƬ�Ƿ����
					if(bitmap!=null){
						setAlbum(bitmap);
					}else{
						setAlbum(R.drawable.default_album);
					}
				}
				int duration=intent.getIntExtra("duration", 0);
				int currentPosition=intent.getIntExtra("currentPosition", 0);
				tv_player_playing_time.setText(Common.formatSecondTime(currentPosition));
				tv_player_playering_duration.setText(Common.formatSecondTime(duration));
				sb_player_playprogress.setMax(duration);
				sb_player_playprogress.setProgress(currentPosition);
				sb_player_playprogress.setSecondaryProgress(0);
			}else if(flag==MediaPlayerManager.FLAG_INIT){//��ʼ��������Ϣ
				int currentPosition=intent.getIntExtra("currentPosition", 0);
				int duration=intent.getIntExtra("duration", 0);
				int playerMode=intent.getIntExtra("playerMode", 0);
				int playerState=intent.getIntExtra("playerState", 0);
				
				if(playerState==MediaPlayerManager.STATE_PLAYER||playerState==MediaPlayerManager.STATE_PREPARE){//����
					ibtn_player_control_play.setBackgroundResource(R.drawable.player_btn_player_pause);
				}else{
					ibtn_player_control_play.setBackgroundResource(R.drawable.player_btn_player_play);
				}
				
				ibtn_player_control_mode
				.setBackgroundResource(MODE_DRAWABLE_ID[playerMode]);
				
				sb_player_playprogress.setMax(duration);
				sb_player_playprogress.setProgress(currentPosition);
				tv_player_playing_time.setText(Common.formatSecondTime(currentPosition));
				tv_player_playering_duration.setText(Common.formatSecondTime(duration));
				tv_player_song_info.setText(intent.getStringExtra("title"));
				String albumPic=intent.getStringExtra("albumPic");
				if(TextUtils.isEmpty(albumPic)){
					setAlbum(R.drawable.default_album);
				}else{
					Bitmap bitmap=BitmapFactory.decodeFile(albumPic);
					//�ж�SDͼƬ�Ƿ����
					if(bitmap!=null){
						setAlbum(bitmap);
					}else{
						setAlbum(R.drawable.default_album);
					}
				}
			}else if(flag==MediaPlayerManager.FLAG_BUFFERING){
				int percent=intent.getIntExtra("percent", 0);
				percent=(int)(sb_player_playprogress.getMax()/100f)*percent;
				sb_player_playprogress.setSecondaryProgress(percent);
			}else if(flag==MediaPlayerManager.FLAG_LIST){
				int state=mediaPlayerManager.getPlayerState();
				if(state==MediaPlayerManager.STATE_PLAYER||state==MediaPlayerManager.STATE_PREPARE){//����
					ibtn_player_control_play.setBackgroundResource(R.drawable.player_btn_player_pause);
				}else{
					ibtn_player_control_play.setBackgroundResource(R.drawable.player_btn_player_play);
				}
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		mediaPlayerManager.unbindService();
		unregisterReceiver(mediaPlayerBroadcastReceiver);
		super.onDestroy();
	}
	
	// ��¼FlingGalleryView �ϴ�Touch�¼���Position
	private float lastX = 0;
	private float lastY = 0;
	private OnCustomTouchListener customTouchListener = new OnCustomTouchListener() {

		public void operation(MotionEvent event) {
			final int action = event.getAction();
			if (action == MotionEvent.ACTION_DOWN) {
				lastX = event.getX();
				lastY = event.getY();
			} else if (action == MotionEvent.ACTION_UP) {
				if (lastX == event.getX() && lastY == event.getY()) {
					voicePanelAnimation();
				}
			}
		}

	};

	// ����ר������-Ĭ��ͼƬ
	private void setAlbum(int rid) {
		iv_player_ablum.setImageResource(rid);
		iv_player_ablum_reflection.setImageBitmap(ImageUtil
				.createReflectionBitmapForSingle(BitmapFactory.decodeResource(
						getResources(), rid)));
	}
	
	// ����ר������-SD·��
	private void setAlbum(Bitmap bitmap){
		iv_player_ablum.setImageBitmap(bitmap);
		iv_player_ablum_reflection.setImageBitmap(ImageUtil
				.createReflectionBitmapForSingle(bitmap));
	}
	
	// ���������ʾ������
	private void voicePanelAnimation() {
		if (ll_player_voice.getVisibility() == View.GONE) {
			ll_player_voice.startAnimation(showVoicePanelAnimation);
			ll_player_voice.setVisibility(View.VISIBLE);
		} else {
			ll_player_voice.startAnimation(hiddenVoicePanelAnimation);
			ll_player_voice.setVisibility(View.GONE);
		}
	}

	private OnClickListener listener = new OnClickListener() {

		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.ibtn_player_voice:
				voicePanelAnimation();
				break;
			case R.id.ibtn_player_list:
				finish();
				break;
			case R.id.ibtn_player_control_menu:
				new XfDialog.Builder(PlayerMainActivity.this).setTitle(tv_player_song_info.getText()).setMessage("��Ǹ����ʱû�п���").create().show();
				break;
			case R.id.ibtn_player_control_mode:
				int player_mode=mediaPlayerManager.getPlayerMode();
				if (player_mode ==MediaPlayerManager.MODE_SEQUENCE) {
					player_mode = MediaPlayerManager.MODE_CIRCLELIST;
				} else {
					player_mode++;
				}
				mediaPlayerManager.setPlayerMode(player_mode);
				ibtn_player_control_mode
						.setBackgroundResource(MODE_DRAWABLE_ID[player_mode]);
				toastMsg = Common.showMessage(toastMsg,
						PlayerMainActivity.this, player_modeStr[player_mode]);
				break;
			case R.id.ibtn_player_control_pre:
				ibtn_player_control_play.setBackgroundResource(R.drawable.player_btn_player_pause);
				mediaPlayerManager.previousPlayer();
				break;
			case R.id.ibtn_player_control_play:
				if(mediaPlayerManager.getPlayerState()==MediaPlayerManager.STATE_NULL){
					toastMsg=Common.showMessage(toastMsg, PlayerMainActivity.this, "������Ӹ���...");
					return;
				}
				//˳���б��Ž���
				if(mediaPlayerManager.getPlayerState()==MediaPlayerManager.STATE_OVER){
					toastMsg=Common.showMessage(toastMsg, PlayerMainActivity.this, "�����б��Ѿ���˳�򲥷���ϣ�");
					return;
				}
				mediaPlayerManager.pauseOrPlayer();
				final int state=mediaPlayerManager.getPlayerState();
				if(state==MediaPlayerManager.STATE_PLAYER||state==MediaPlayerManager.STATE_PREPARE){//����
					ibtn_player_control_play.setBackgroundResource(R.drawable.player_btn_player_pause);
				}else if(state==MediaPlayerManager.STATE_PAUSE){//��ͣ
					ibtn_player_control_play.setBackgroundResource(R.drawable.player_btn_player_play);
				}
				break;
			case R.id.ibtn_player_control_next:
				ibtn_player_control_play.setBackgroundResource(R.drawable.player_btn_player_pause);
				mediaPlayerManager.nextPlayer();
				break;
			default:
				break;
			}
		}
	};
	
	private OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {

		public void onStopTrackingTouch(SeekBar seekBar) {
			if (seekBar.getId() == R.id.sb_player_voice) {

			} else if (seekBar.getId() == R.id.sb_player_playprogress) {
				isSeekDrag=false;
				mediaPlayerManager.seekTo(seekBar.getProgress());
			}
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			if(seekBar.getId() == R.id.sb_player_playprogress) {
				 isSeekDrag=true;
				 tv_player_playing_time.setText(Common.formatSecondTime(seekBar.getProgress()));
			}
		}

		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (seekBar.getId() == R.id.sb_player_voice) {
				// ��������
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						progress, 0);
			}else if(seekBar.getId() == R.id.sb_player_playprogress) {
				if(isSeekDrag){
					tv_player_playing_time.setText(Common.formatSecondTime(progress));
				}
			}
		}
	};
}