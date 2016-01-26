package com.xfdream.music.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.xfdream.music.R;
import com.xfdream.music.adapter.ScanListAdapter;
import com.xfdream.music.entity.ScanData;
import com.xfdream.music.util.Common;
import com.xfdream.music.util.ScanMusicFilterFile;

public class ScanDirectoryActivity extends SettingActivity {
	private ListView lv_scan_music_list;
	private File[] files;
	private List<ScanData> data;
	private ScanMusicFilterFile myFilterFile;//�����ļ�
	private File currrentFile;//��ǰ�ļ���·��
	private ScanListAdapter adapter;
	private String rootFilePath;//�����ļ���·��
	private String rs="";//���ؽ��

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan_music_adddirectory);
		
		resultCode=ScanMusicActivity.SCAN_MUSIC_CANCEL;
		setBackButton();
		setTopTitle(getResources().getString(R.string.scan_directory_title));
		
		//���SD��
		if(!Common.isExistSdCard()){
			Toast.makeText(this, "���Ȳ���SD��", Toast.LENGTH_SHORT).show();
			setResult(RESULT_CANCELED);
			this.finish();
		}
		
		lv_scan_music_list=(ListView)this.findViewById(R.id.lv_scan_music_list);
		
		((Button)this.findViewById(R.id.btn_scan_add)).setOnClickListener(listener);
		((Button)this.findViewById(R.id.btn_scan_back)).setOnClickListener(listener);
		((Button)this.findViewById(R.id.btn_scan_directory_goup)).setOnClickListener(listener);

		//��ȡĬ������Ŀ¼��ѡȡ���
		rs=getIntent().getStringExtra("rs");
		
		//��ȡmnt��Ŀ¼
		myFilterFile=new ScanMusicFilterFile();
		lv_scan_music_list=(ListView)this.findViewById(R.id.lv_scan_music_list);
		currrentFile=Environment.getExternalStorageDirectory().getParentFile();//��ȡsd��·���ĸ�Ŀ¼�ļ�
		rootFilePath=currrentFile.getPath().toLowerCase();//���ø�Ŀ¼��·��
		data=new ArrayList<ScanData>();
		getFilePath(currrentFile);//����Ŀ¼
		adapter=new ScanListAdapter(this,data);
		adapter.setCheckFilePath(rs);
		lv_scan_music_list.setAdapter(adapter);
		lv_scan_music_list.setOnItemClickListener(itemClickListener);
	}

	private OnClickListener listener=new OnClickListener() {
		
		public void onClick(View v) {
			
			switch (v.getId()) {
			case R.id.btn_scan_add:
				//���ɨ��Ŀ¼
				Intent addit=new Intent(ScanDirectoryActivity.this,ScanMusicActivity.class);
				addit.putExtra("rs", adapter.getCheckFilePath());
				setResult(ScanMusicActivity.SCAN_MUSIC_OK, addit);
				finish();
				break;
			case R.id.btn_scan_directory_goup:
				//�ж��Ƿ��Ǹ���Ŀ¼
				if(!currrentFile.getPath().toLowerCase().equals(rootFilePath)){
					currrentFile=currrentFile.getParentFile();
					getFilePath(currrentFile);
					adapter.notifyDataSetChanged();
				}
				break;
			case R.id.btn_scan_back:
				setResult(-1);
				finish();
				break;
			default:
				break;
			}
		}
	};
	
	private OnItemClickListener itemClickListener=new OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			//������һ��Ŀ¼
			currrentFile=files[position];
			rs=adapter.getCheckFilePath();
			getFilePath(currrentFile);
			adapter.notifyDataSetChanged();
		}
		
	};
	
	private void getFilePath(File parent){
		data.clear();
		files=parent.listFiles(myFilterFile);
		for(File file:files){
			String fp=file.getPath().toLowerCase();
			//�ж��Ƿ���ѡ�е�
			ScanData d=new ScanData(fp+"/",false);
			if(rs.contains("$"+fp+"/$")){
				d.setChecked(true);
			}
			data.add(d);
		}
	}
}
