package com.xfdream.music.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xfdream.music.entity.Artist;

/**
 * ����DAO
 * */
public class ArtistDao {
	private DBHpler dbHpler=null;
	
	public ArtistDao(Context context){
		dbHpler=new DBHpler(context);
	}
	
	/**
	 * ȫ����ѯ
	 * */
	public List<String[]> searchAll(){
		List<String[]> list=new ArrayList<String[]>();
		SQLiteDatabase db=dbHpler.getReadableDatabase();
		Cursor cr=db.rawQuery("SELECT * FROM "+DBData.ARTIST_TABLENAME+" ORDER BY "+DBData.ARTIST_NAME+" ASC",null);
		while(cr.moveToNext()){
			String[] s=new String[3];
			s[0]=String.valueOf(cr.getInt(cr.getColumnIndex(DBData.ARTIST_ID)));
			s[1]=cr.getString(cr.getColumnIndex(DBData.ARTIST_NAME));
			s[2]=cr.getString(cr.getColumnIndex(DBData.ARTIST_PICPATH));
			list.add(s);
		}
		cr.close();
		db.close();
		return list;
	}
	
	/**
	 * �жϸ����Ƿ���ڣ����ڷ���id
	 * */
	public int isExist(String name){
		int id=-1;
		SQLiteDatabase db=dbHpler.getReadableDatabase();
		Cursor cr=db.rawQuery("SELECT "+DBData.ARTIST_ID+" FROM "+DBData.ARTIST_TABLENAME+" WHERE "+DBData.ARTIST_NAME+"=?", new String[]{name});
		if(cr.moveToNext()){
			id=cr.getInt(0);
		}
		cr.close();
		db.close();
		return id;
	}
	
	/**
	 * ��ȡ��¼����
	 * */
	public int getCount(){
		int count=0;
		SQLiteDatabase db=dbHpler.getReadableDatabase();
		Cursor cr=db.rawQuery("SELECT COUNT(*) FROM "+DBData.ARTIST_TABLENAME, null);
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
	public long add(Artist artist){
		SQLiteDatabase db=dbHpler.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put(DBData.ARTIST_NAME, artist.getName());
		values.put(DBData.ARTIST_PICPATH, artist.getPicPath());
		long rs=db.insert(DBData.ARTIST_TABLENAME, DBData.ARTIST_NAME, values);
		db.close();
		return rs;
	}
	
	/**
	 * ɾ��
	 * */
	public int delete(int id){
		SQLiteDatabase db=dbHpler.getWritableDatabase();
		int rs=db.delete(DBData.ARTIST_TABLENAME, DBData.ARTIST_ID+"=?",new String[]{String.valueOf(id)});
		db.close();
		return rs;
	}
	
	/**
	 * ����
	 * */
	public int update(Artist artist){
		SQLiteDatabase db=dbHpler.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put(DBData.ARTIST_NAME, artist.getName());
		values.put(DBData.ARTIST_PICPATH, artist.getPicPath());
		int rs=db.update(DBData.ARTIST_TABLENAME, values, DBData.ARTIST_ID+"=?", new String[]{String.valueOf(artist.getId())});
		db.close();
		return rs;
	}
}
