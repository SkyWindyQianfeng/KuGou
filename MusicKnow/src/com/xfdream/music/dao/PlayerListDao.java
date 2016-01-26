package com.xfdream.music.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xfdream.music.entity.PlayerList;

public class PlayerListDao {
	
	private DBHpler dbHpler;
	
	public PlayerListDao(Context context){
		dbHpler=new DBHpler(context);
	}
	
	/**
	 * ȫ����ѯ[0:id,1:name,2:""]
	 * */
	public List<String[]> searchAll(){
		List<String[]> list=new ArrayList<String[]>();
		SQLiteDatabase db=dbHpler.getReadableDatabase();
		Cursor cr=db.rawQuery("SELECT * FROM "+DBData.PLAYERLIST_TABLENAME+" ORDER BY "+DBData.PLAYERLIST_DATE+" DESC",null);
		while(cr.moveToNext()){
			String[] s=new String[3];
			s[0]=String.valueOf(cr.getInt(cr.getColumnIndex(DBData.PLAYERLIST_ID)));
			s[1]=cr.getString(cr.getColumnIndex(DBData.PLAYERLIST_NAME));
			s[2]="";
			list.add(s);
		}
		cr.close();
		db.close();
		return list;
	}
	
	/**
	 * �ж��Ƿ����ƴ���
	 * */
	public boolean isExists(String name){
		int count=0;
		SQLiteDatabase db=dbHpler.getReadableDatabase();
		Cursor cr=db.rawQuery("SELECT COUNT(*) FROM "+DBData.PLAYERLIST_TABLENAME+" WHERE "+DBData.PLAYERLIST_NAME+"=?", new String[]{name});
		if(cr.moveToNext()){
			count=cr.getInt(0);
		}
		cr.close();
		db.close();
		return count>0;
	}
	
	/**
	 * ��ȡ��¼����
	 * */
	public int getCount(){
		int count=0;
		SQLiteDatabase db=dbHpler.getReadableDatabase();
		Cursor cr=db.rawQuery("SELECT COUNT(*) FROM "+DBData.PLAYERLIST_TABLENAME, null);
		if(cr.moveToNext()){
			count=cr.getInt(0);
		}
		cr.close();
		db.close();
		return count;
	}
	
	/**
	 * ���
	 * */
	public long add(PlayerList playerList){
		SQLiteDatabase db=dbHpler.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put(DBData.PLAYERLIST_NAME, playerList.getName());
		values.put(DBData.PLAYERLIST_DATE, playerList.getDate());
		long rs=db.insert(DBData.PLAYERLIST_TABLENAME, DBData.PLAYERLIST_NAME, values);
		db.close();
		return rs;
	}
	
	/**
	 * ɾ��
	 * */
	public int delete(int id){
		SQLiteDatabase db=dbHpler.getWritableDatabase();
		//����Song���е��������
		db.execSQL(" UPDATE "+DBData.SONG_TABLENAME+" SET "
		+DBData.SONG_PLAYERLIST+"=replace("+DBData.SONG_PLAYERLIST+",?,'')", new String[]{"$"+id+"$"});
		
		int rs=db.delete(DBData.PLAYERLIST_TABLENAME, DBData.PLAYERLIST_ID+"=?",new String[]{String.valueOf(id)});
		db.close();
		return rs;
	}
	
	/**
	 * ����
	 * */
	public int update(PlayerList playerList){
		SQLiteDatabase db=dbHpler.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put(DBData.PLAYERLIST_NAME, playerList.getName());
		values.put(DBData.PLAYERLIST_DATE, playerList.getDate());
		int rs=db.update(DBData.PLAYERLIST_TABLENAME, values, DBData.PLAYERLIST_ID+"=?", new String[]{String.valueOf(playerList.getId())});
		db.close();
		return rs;
	}
}
