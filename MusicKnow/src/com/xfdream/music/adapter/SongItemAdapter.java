package com.xfdream.music.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.xfdream.music.R;
import com.xfdream.music.service.MediaPlayerManager;

public class SongItemAdapter extends BaseAdapter {
	private Context context;
	private List<String[]> data;
	private int[] playerInfo=new int[2];//0:playerId,1:playerstate
	private ItemListener mItemListener;
	
	/**
	 * List<String[]> data��0:����id��1:�ϱ��⣬2���±��⣬3:�ļ�·����4���Ƿ����
	 * */
	public SongItemAdapter(Context context,List<String[]> data,int[] playerInfo){
		this.context=context;
		this.data=data;
		this.playerInfo=playerInfo;
	}
	
	public SongItemAdapter setItemListener(ItemListener mItemListener){
		this.mItemListener=mItemListener;
		return this;
	}
	
	public void setPlayerInfo(int[] playerInfo){
		this.playerInfo=playerInfo;
		notifyDataSetChanged();
	}
	
	public void setPlayerState(int playerState){
		this.playerInfo[1]=playerState;
		notifyDataSetChanged();
	}
	
	public int getCount() {
		return data.size();
	}

	public void deleteItem(int position){
		data.remove(position);
		notifyDataSetChanged();
	}
	
	public Object getItem(int position) {
		return data.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public int[] getPlayerId() {
		return playerInfo;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder=null;
		if(convertView==null){
			viewHolder=new ViewHolder();
			convertView=LayoutInflater.from(context).inflate(R.layout.song_list_item, null);
			viewHolder.tv_song_list_item_number=(TextView)convertView.findViewById(R.id.tv_song_list_item_number);
			viewHolder.tv_song_list_item_top=(TextView)convertView.findViewById(R.id.tv_song_list_item_top);
			viewHolder.tv_song_list_item_bottom=(TextView)convertView.findViewById(R.id.tv_song_list_item_bottom);
			viewHolder.ibtn_song_list_item_menu=(ImageButton)convertView.findViewById(R.id.ibtn_song_list_item_menu);
			viewHolder.ibtn_song_list_item_like=(ImageButton)convertView.findViewById(R.id.ibtn_song_list_item_like);
			
			convertView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder)convertView.getTag();
		}
		final String[] d=data.get(position);
		//�Ƿ��벥�Ÿ�����ͬһ��
		if(Integer.valueOf(d[0])==playerInfo[0]){
			viewHolder.tv_song_list_item_number.setText("");
			//��ͣ
			if(playerInfo[1]==MediaPlayerManager.STATE_PAUSE){
				viewHolder.tv_song_list_item_number.setBackgroundResource(R.drawable.music_list_item_pause);
			}else if(playerInfo[1]==MediaPlayerManager.STATE_PLAYER||playerInfo[1]==MediaPlayerManager.STATE_PREPARE){//����
				viewHolder.tv_song_list_item_number.setBackgroundResource(R.drawable.music_list_item_player);
			}else if(playerInfo[1]==MediaPlayerManager.STATE_OVER){
				viewHolder.tv_song_list_item_number.setText((position+1)+"");
				viewHolder.tv_song_list_item_number.setBackgroundResource(0);
			}
		}else{
			viewHolder.tv_song_list_item_number.setText((position+1)+"");
			viewHolder.tv_song_list_item_number.setBackgroundResource(0);
		}
		
		viewHolder.tv_song_list_item_top.setText(d[1]);
		viewHolder.tv_song_list_item_top.setTag(d[3]);//�ļ�·��
		viewHolder.tv_song_list_item_bottom.setText(d[2]);
		viewHolder.tv_song_list_item_bottom.setTag(d[0]);
		
		//�Ƿ����
		if(d[4].equals("1")){
			viewHolder.ibtn_song_list_item_like.setBackgroundResource(R.drawable.like);
		}else{
			viewHolder.ibtn_song_list_item_like.setBackgroundResource(R.drawable.dislike);
		}
		
		viewHolder.ibtn_song_list_item_like.setTag(d[4]);
		viewHolder.ibtn_song_list_item_like.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if(mItemListener!=null){
					mItemListener.onLikeClick(Integer.valueOf(d[0]), v, position);
				}
			}
		});
		
		viewHolder.ibtn_song_list_item_menu.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(mItemListener!=null){
					mItemListener.onMenuClick(Integer.valueOf(d[0]),d[1],d[3],position);
				}
			}
		});
		
		
		viewHolder.ibtn_song_list_item_like.setFocusable(false);
		viewHolder.ibtn_song_list_item_like.setFocusableInTouchMode(false);
		
		viewHolder.ibtn_song_list_item_menu.setFocusable(false);
		viewHolder.ibtn_song_list_item_menu.setFocusableInTouchMode(false);
		return convertView;
	}
 
	public class ViewHolder{
		public TextView tv_song_list_item_number;
		public TextView tv_song_list_item_top;
		public TextView tv_song_list_item_bottom;
		public ImageButton ibtn_song_list_item_menu;
		public ImageButton ibtn_song_list_item_like;
	}
	
	public interface ItemListener{
		void onLikeClick(int id,View view,int position);
		void onMenuClick(int id,String text,String path,int position);
	}
}
