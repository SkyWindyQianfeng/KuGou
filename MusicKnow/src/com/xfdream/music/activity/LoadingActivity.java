package com.xfdream.music.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.xfdream.music.R;
import com.xfdream.music.data.SystemSetting;
import com.xfdream.music.util.QuickTimer;
import com.xfdream.music.util.QuickTimer.OnTimeListener;

public class LoadingActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// �ޱ���
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// ȫ��
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// ����Ļ
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		super.onCreate(savedInstanceState);

		// ���ؽ���ͼƬ
		ImageView view = new ImageView(this);
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		view.setLayoutParams(params);
		view.setScaleType(ScaleType.FIT_XY);
		view.setImageResource(R.drawable.loading_img);
		setContentView(view);

		// ִ����ת
		new QuickTimer().start(new OnTimeListener() {
			public void onTimer() {
				new SystemSetting(LoadingActivity.this, true).setValue(
						SystemSetting.KEY_ISSTARTUP, "false");
				Intent it = new Intent(LoadingActivity.this,
						ListMainActivity.class);
				startActivity(it);
				finish();
			}
		}, 1000);
	}
}
