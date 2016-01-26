package com.xfdream.music.activity;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;

import com.xfdream.music.service.MediaPlayerManager;

public class WidgetInitActivity extends Activity {
	private int appWidgetId=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setResult(RESULT_CANCELED);

		//��ȡappwidgetid
		Bundle bundle=getIntent().getExtras();
		if(bundle!=null){
			appWidgetId=bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		//�ж��Ƿ���Ч
		if(appWidgetId==AppWidgetManager.INVALID_APPWIDGET_ID){
			finish();
		}
		
		setResult(RESULT_OK,new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId));
		
		//��ʼ����Ϣ
		startService(new Intent(MediaPlayerManager.SERVICE_ACTION).putExtra("flag", MediaPlayerManager.SERVICE_MUSIC_INIT));
		finish();
	}

}
