package com.xfdream.music.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.xfdream.music.entity.Album;
import com.xfdream.music.entity.Artist;
import com.xfdream.music.entity.Song;
import com.xfdream.music.util.Common;

/**
 * ����DAO
 * */
public class SongDao {
	private DBHpler dbHpler;

	public SongDao(Context context) {
		dbHpler = new DBHpler(context);
	}

	/**
	 * ��ѯ����Ŀ¼
	 * */
	public List<String[]> searchByDirectory() {
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		List<String[]> list = new ArrayList<String[]>();
		StringBuffer sb = new StringBuffer();
		Cursor cr = db.rawQuery("SELECT " + DBData.SONG_FILEPATH + ","
				+ DBData.SONG_ID + " FROM " + DBData.SONG_TABLENAME
				+ " ORDER BY " + DBData.SONG_FILEPATH + " DESC", null);
		while (cr.moveToNext()) {
			String filepath = Common.clearFileName(
					cr.getString(cr.getColumnIndex(DBData.SONG_FILEPATH)))
					.toLowerCase();
			if (!sb.toString().contains("$" + filepath + "$")) {
				sb.append("$").append(filepath).append("$");
				String[] s = new String[3];
				s[0] = filepath;
				s[1] = filepath;
				s[2] = "";
				list.add(s);
			}
		}
		cr.close();
		db.close();
		return list;
	}

	/**
	 * ���
	 * */
	public long add(Song song) {
		SQLiteDatabase db = dbHpler.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBData.SONG_DISPLAYNAME, song.getDisplayName());
		values.put(DBData.SONG_FILEPATH, song.getFilePath());
		values.put(DBData.SONG_LYRICPATH, song.getLyricPath());
		values.put(DBData.SONG_MIMETYPE, song.getMimeType());
		values.put(DBData.SONG_NAME, song.getName());
		values.put(DBData.SONG_ALBUMID, song.getAlbum().getId());
		values.put(DBData.SONG_NETURL, song.getNetUrl());
		values.put(DBData.SONG_DURATIONTIME, song.getDurationTime());
		values.put(DBData.SONG_SIZE, song.getSize());
		values.put(DBData.SONG_ARTISTID, song.getArtist().getId());
		values.put(DBData.SONG_PLAYERLIST, song.getPlayerList());
		values.put(DBData.SONG_ISDOWNFINISH, song.isDownFinish());
		values.put(DBData.SONG_ISLIKE, song.isLike());
		values.put(DBData.SONG_ISNET, song.isNet());
		long rs = db.insert(DBData.SONG_TABLENAME, DBData.SONG_NAME, values);
		db.close();
		return rs;
	}

	/**
	 * ��ȡ��¼����
	 * */
	public int getCount() {
		int count = 0;
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		Cursor cr = db.rawQuery(
				"SELECT COUNT(*) FROM " + DBData.SONG_TABLENAME, null);
		if (cr.moveToNext()) {
			count = cr.getInt(0);
		}
		cr.close();
		db.close();
		return count;
	}

	/**
	 * ĳ�׸����Ӳ����б���ɾ��
	 * */
	public int deleteByPlayerList(int id, int pid) {
		int rs = 0;
		SQLiteDatabase db = dbHpler.getWritableDatabase();
		Cursor cr = db.rawQuery("SELECT " + DBData.SONG_PLAYERLIST + " FROM "
				+ DBData.SONG_TABLENAME + " WHERE " + DBData.SONG_ID + "=?",
				new String[] { String.valueOf(id) });
		String temp_pl = null;
		if (cr.moveToNext()) {
			temp_pl = cr.getString(0);
		}
		cr.close();
		if (temp_pl != null) {
			ContentValues values = new ContentValues();
			values.put(DBData.SONG_PLAYERLIST,
					temp_pl.replaceAll("$" + pid + "$", ""));
			rs = db.update(DBData.SONG_TABLENAME, values,
					DBData.SONG_ID + "=?", new String[] { String.valueOf(id) });
		}
		db.close();
		return rs;
	}

	/**
	 * ɾ��
	 * */
	public int delete(Integer... ids) {
		SQLiteDatabase db = dbHpler.getWritableDatabase();
		StringBuilder sb = new StringBuilder();
		String[] idstr = new String[ids.length];
		for (int i = 0; i < ids.length; i++) {
			sb.append("?,");
			idstr[i] = String.valueOf(ids[i]);
			Cursor cr = db.query(DBData.SONG_TABLENAME, new String[] {
					DBData.SONG_ARTISTID, DBData.SONG_ALBUMID }, DBData.SONG_ID
					+ "=?", new String[] { idstr[i] }, null, null, null);
			if (cr.moveToNext()) {
				final int artistid = cr.getInt(0);
				final int albumid = cr.getInt(1);
				// ɾ������
				Cursor artist_cr = db.rawQuery("SELECT COUNT(*) FROM "
						+ DBData.SONG_TABLENAME + " WHERE "
						+ DBData.SONG_ARTISTID + "=?",
						new String[] { String.valueOf(artistid) });
				if (artist_cr.getCount() == 1) {
					db.delete(DBData.ARTIST_TABLENAME, DBData.ARTIST_ID + "=?",
							new String[] { String.valueOf(artistid) });
				}
				// ɾ��ר��
				Cursor album_cr = db.rawQuery("SELECT COUNT(*) FROM "
						+ DBData.SONG_TABLENAME + " WHERE "
						+ DBData.SONG_ALBUMID + "=?",
						new String[] { String.valueOf(albumid) });
				if (album_cr.getCount() == 1) {
					db.delete(DBData.ALBUM_TABLENAME, DBData.ALBUM_ID + "=?",
							new String[] { String.valueOf(albumid) });
				}
			}
			cr.close();
		}
		sb.deleteCharAt(sb.length() - 1);
		int rs = db.delete(DBData.SONG_TABLENAME,
				DBData.SONG_ID + " in(" + sb.toString() + ")", idstr);
		db.close();
		return rs;
	}

	/**
	 * ��ѯȫ������
	 * */
	public List<Song> searchAll() {
		return commonSearch("", null);
	}

	/**
	 * ͨ�ò�ѯ
	 * */
	private List<Song> commonSearch(String whereString, String[] params) {
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		List<Song> list = new ArrayList<Song>();
		Song song = null;
		Cursor cr = db.rawQuery("SELECT A." + DBData.SONG_ID + ", A."
				+ DBData.SONG_DISPLAYNAME + ",B." + DBData.ARTIST_NAME
				+ " AS Bname" + ",C." + DBData.ALBUM_NAME + " AS Cname" + ",A."
				+ DBData.SONG_NAME + " AS Aname" + ",A." + DBData.SONG_ALBUMID
				+ ",A." + DBData.SONG_ARTISTID + ",C." + DBData.ALBUM_PICPATH
				+ " AS Cpicpath" + ",B." + DBData.ARTIST_PICPATH
				+ " AS Bpicpath" + ",A." + DBData.SONG_FILEPATH + ",A."
				+ DBData.SONG_DURATIONTIME + " FROM " + DBData.SONG_TABLENAME
				+ " AS A INNER JOIN " + DBData.ARTIST_TABLENAME
				+ " AS B ON  A." + DBData.SONG_ARTISTID + "=B."
				+ DBData.ARTIST_ID + " INNER JOIN " + DBData.ALBUM_TABLENAME
				+ " AS C ON A." + DBData.SONG_ALBUMID + "=C." + DBData.ALBUM_ID
				+ " " + whereString + " ORDER BY " + DBData.SONG_DISPLAYNAME
				+ " DESC", params);

		while (cr.moveToNext()) {
			song = new Song();
			song.setId(cr.getInt(cr.getColumnIndex(DBData.SONG_ID)));
			song.setDisplayName(cr.getString(cr
					.getColumnIndex(DBData.SONG_DISPLAYNAME)));
			song.setArtist(new Artist(cr.getInt(cr
					.getColumnIndex(DBData.SONG_ARTISTID)), cr.getString(cr
					.getColumnIndex("Bname")), cr.getString(cr
					.getColumnIndex("Bpicpath"))));
			song.setAlbum(new Album(cr.getInt(cr
					.getColumnIndex(DBData.SONG_ALBUMID)), cr.getString(cr
					.getColumnIndex("Cname")), cr.getString(cr
					.getColumnIndex("Cpicpath"))));
			song.setName(cr.getString(cr.getColumnIndex("Aname")));
			song.setFilePath(cr.getString(cr
					.getColumnIndex(DBData.SONG_FILEPATH)));
			song.setDurationTime(cr.getInt(cr
					.getColumnIndex(DBData.SONG_DURATIONTIME)));
			list.add(song);
		}
		cr.close();
		db.close();
		return list;
	}

	/**
	 * ����ȫ������[0:id,1:�ļ���,2:����,3:�ļ�·��,4:ϲ��]
	 * */
	public List<String[]> searchByAll() {
		SQLiteDatabase db = dbHpler.getWritableDatabase();
		List<String[]> list = new ArrayList<String[]>();
		Cursor cr = db.rawQuery("SELECT A." + DBData.SONG_ISLIKE + ",A."
				+ DBData.SONG_ID + ",A." + DBData.SONG_DISPLAYNAME + ",B."
				+ DBData.ARTIST_NAME + ",A." + DBData.SONG_FILEPATH + " FROM "
				+ DBData.SONG_TABLENAME + " AS A INNER JOIN "
				+ DBData.ARTIST_TABLENAME + " AS B WHERE A."
				+ DBData.SONG_ARTISTID + "=B." + DBData.ARTIST_ID
				+ " ORDER BY " + DBData.SONG_DISPLAYNAME + " DESC", null);
		while (cr.moveToNext()) {
			String[] s = new String[5];
			s[0] = String.valueOf(cr.getInt(cr.getColumnIndex(DBData.SONG_ID)));
			s[1] = Common.clearSuffix(cr.getString(cr
					.getColumnIndex(DBData.SONG_DISPLAYNAME)));
			s[2] = cr.getString(cr.getColumnIndex(DBData.ARTIST_NAME));
			s[3] = cr.getString(cr.getColumnIndex(DBData.SONG_FILEPATH));
			s[4] = String.valueOf(cr.getInt(cr
					.getColumnIndex(DBData.SONG_ISLIKE)));
			list.add(s);
		}
		cr.close();
		db.close();
		return list;
	}

	/**
	 * ���ݸ��ֲ�ѯ[0:id,1:�ļ���,2:ר��,3:�ļ�·��,4:ϲ��]
	 * */
	public List<String[]> searchByArtist(String artistId) {
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		List<String[]> list = new ArrayList<String[]>();
		Cursor cr = db.rawQuery("SELECT  A." + DBData.SONG_ISLIKE + ",A."
				+ DBData.SONG_ID + ",A." + DBData.SONG_DISPLAYNAME + ",B."
				+ DBData.ALBUM_NAME + ",A." + DBData.SONG_FILEPATH + " FROM "
				+ DBData.SONG_TABLENAME + " AS A INNER JOIN "
				+ DBData.ALBUM_TABLENAME + " AS B WHERE A."
				+ DBData.SONG_ALBUMID + "=B." + DBData.ALBUM_ID + " AND A."
				+ DBData.SONG_ARTISTID + "=? ORDER BY "
				+ DBData.SONG_DISPLAYNAME + " DESC", new String[] { artistId });
		while (cr.moveToNext()) {
			String[] s = new String[5];
			s[0] = String.valueOf(cr.getInt(cr.getColumnIndex(DBData.SONG_ID)));
			s[1] = Common.clearSuffix(cr.getString(cr
					.getColumnIndex(DBData.SONG_DISPLAYNAME)));
			s[2] = cr.getString(cr.getColumnIndex(DBData.ALBUM_NAME));
			s[3] = cr.getString(cr.getColumnIndex(DBData.SONG_FILEPATH));
			s[4] = String.valueOf(cr.getInt(cr
					.getColumnIndex(DBData.SONG_ISLIKE)));
			list.add(s);
		}
		cr.close();
		db.close();
		return list;
	}

	/**
	 * ���ݸ��ֲ�ѯ
	 * */
	public List<Song> searchArtist(String artistId) {
		return commonSearch(" WHERE A." + DBData.SONG_ARTISTID + "=? ",
				new String[] { artistId });
	}

	/**
	 * ����ר����ѯ[0:id,1:�ļ���,2:����,3:�ļ�·��,4:ϲ��]
	 * */
	public List<String[]> searchByAlbum(String albumId) {
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		List<String[]> list = new ArrayList<String[]>();
		Cursor cr = db.rawQuery("SELECT  A." + DBData.SONG_ISLIKE + ",A."
				+ DBData.SONG_ID + ",A." + DBData.SONG_DISPLAYNAME + ",B."
				+ DBData.ARTIST_NAME + ",A." + DBData.SONG_FILEPATH + " FROM "
				+ DBData.SONG_TABLENAME + " AS A INNER JOIN "
				+ DBData.ARTIST_TABLENAME + " AS B WHERE A."
				+ DBData.SONG_ARTISTID + "=B." + DBData.ARTIST_ID + " AND A."
				+ DBData.SONG_ALBUMID + "=? ORDER BY "
				+ DBData.SONG_DISPLAYNAME + " DESC", new String[] { albumId });
		while (cr.moveToNext()) {
			String[] s = new String[5];
			s[0] = String.valueOf(cr.getInt(cr.getColumnIndex(DBData.SONG_ID)));
			s[1] = Common.clearSuffix(cr.getString(cr
					.getColumnIndex(DBData.SONG_DISPLAYNAME)));
			s[2] = cr.getString(cr.getColumnIndex(DBData.ARTIST_NAME));
			s[3] = cr.getString(cr.getColumnIndex(DBData.SONG_FILEPATH));
			s[4] = String.valueOf(cr.getInt(cr
					.getColumnIndex(DBData.SONG_ISLIKE)));
			list.add(s);
		}
		cr.close();
		db.close();
		return list;
	}

	/**
	 * ����ר����ѯ
	 * */
	public List<Song> searchAlbum(String albumId) {
		return commonSearch(" WHERE A." + DBData.SONG_ALBUMID + "=?",
				new String[] { albumId });
	}

	/**
	 * �����ļ��в�ѯ[0:id,1:�ļ���,2:����,3:�ļ�·��,4:ϲ��]
	 * */
	public List<String[]> searchByDirectory(String filePath) {
		filePath = filePath.toLowerCase();
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		List<String[]> list = new ArrayList<String[]>();
		Cursor cr = db.rawQuery("SELECT  A." + DBData.SONG_ISLIKE + ",A."
				+ DBData.SONG_ID + ",A." + DBData.SONG_DISPLAYNAME + ",B."
				+ DBData.ARTIST_NAME + ",A." + DBData.SONG_FILEPATH + " FROM "
				+ DBData.SONG_TABLENAME + " AS A INNER JOIN "
				+ DBData.ARTIST_TABLENAME + " AS B WHERE A."
				+ DBData.SONG_ARTISTID + "=B." + DBData.ARTIST_ID
				+ " ORDER BY " + DBData.SONG_DISPLAYNAME + " DESC", null);
		while (cr.moveToNext()) {
			String filePaths = cr.getString(cr
					.getColumnIndex(DBData.SONG_FILEPATH));
			if (Common.clearFileName(filePaths).toLowerCase().equals(filePath)) {
				String[] s = new String[5];
				s[0] = String.valueOf(cr.getInt(cr
						.getColumnIndex(DBData.SONG_ID)));
				s[1] = Common.clearSuffix(cr.getString(cr
						.getColumnIndex(DBData.SONG_DISPLAYNAME)));
				s[2] = cr.getString(cr.getColumnIndex(DBData.ARTIST_NAME));
				s[3] = filePaths;
				s[4] = String.valueOf(cr.getInt(cr
						.getColumnIndex(DBData.SONG_ISLIKE)));
				list.add(s);
			}
		}
		cr.close();
		db.close();
		return list;
	}

	/**
	 * �����ļ��в�ѯ
	 * */
	public List<Song> searchDirectory(String filePath) {
		filePath = filePath.toLowerCase();
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		List<Song> list = new ArrayList<Song>();
		Song song = null;
		Cursor cr = db.rawQuery("SELECT A." + DBData.SONG_ID + ", A."
				+ DBData.SONG_DISPLAYNAME + ",B." + DBData.ARTIST_NAME
				+ " AS Bname" + ",C." + DBData.ALBUM_NAME + " AS Cname" + ",A."
				+ DBData.SONG_NAME + " AS Aname" + ",A." + DBData.SONG_ALBUMID
				+ ",A." + DBData.SONG_ARTISTID + ",C." + DBData.ALBUM_PICPATH
				+ " AS Cpicpath" + ",B." + DBData.ARTIST_PICPATH
				+ " AS Bpicpath" + ",A." + DBData.SONG_FILEPATH + ",A."
				+ DBData.SONG_DURATIONTIME + " FROM " + DBData.SONG_TABLENAME
				+ " AS A INNER JOIN " + DBData.ARTIST_TABLENAME
				+ " AS B ON  A." + DBData.SONG_ARTISTID + "=B."
				+ DBData.ARTIST_ID + " INNER JOIN " + DBData.ALBUM_TABLENAME
				+ " AS C ON A." + DBData.SONG_ALBUMID + "=C." + DBData.ALBUM_ID
				+ " ORDER BY " + DBData.SONG_DISPLAYNAME + " DESC", null);

		while (cr.moveToNext()) {
			String filePaths = cr.getString(cr
					.getColumnIndex(DBData.SONG_FILEPATH));
			if (Common.clearFileName(filePaths).toLowerCase().equals(filePath)) {
				song = new Song();
				song.setId(cr.getInt(cr.getColumnIndex(DBData.SONG_ID)));
				song.setDisplayName(cr.getString(cr
						.getColumnIndex(DBData.SONG_DISPLAYNAME)));
				song.setArtist(new Artist(cr.getInt(cr
						.getColumnIndex(DBData.SONG_ARTISTID)), cr.getString(cr
						.getColumnIndex("Bname")), cr.getString(cr
						.getColumnIndex("Bpicpath"))));
				song.setAlbum(new Album(cr.getInt(cr
						.getColumnIndex(DBData.SONG_ALBUMID)), cr.getString(cr
						.getColumnIndex("Cname")), cr.getString(cr
						.getColumnIndex("Cpicpath"))));
				song.setName(cr.getString(cr.getColumnIndex("Aname")));
				song.setFilePath(filePaths);
				song.setDurationTime(cr.getInt(cr
						.getColumnIndex(DBData.SONG_DURATIONTIME)));
				list.add(song);
			}
		}
		cr.close();
		db.close();
		return list;
	}

	/**
	 * �����������ѯ[0:id,1:�ļ���,2:����,3:�ļ�·��,4:ϲ��]
	 * */
	public List<String[]> searchByIsLike() {
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		List<String[]> list = new ArrayList<String[]>();
		Cursor cr = db.rawQuery("SELECT  A." + DBData.SONG_ISLIKE + ",A."
				+ DBData.SONG_ID + ",A." + DBData.SONG_DISPLAYNAME + ",B."
				+ DBData.ARTIST_NAME + ",A." + DBData.SONG_FILEPATH + " FROM "
				+ DBData.SONG_TABLENAME + " AS A INNER JOIN "
				+ DBData.ARTIST_TABLENAME + " AS B WHERE A."
				+ DBData.SONG_ARTISTID + "=B." + DBData.ARTIST_ID + " AND A."
				+ DBData.SONG_ISLIKE + "=1 ORDER BY " + DBData.SONG_DISPLAYNAME
				+ " DESC", null);
		while (cr.moveToNext()) {
			String[] s = new String[5];
			s[0] = String.valueOf(cr.getInt(cr.getColumnIndex(DBData.SONG_ID)));
			s[1] = Common.clearSuffix(cr.getString(cr
					.getColumnIndex(DBData.SONG_DISPLAYNAME)));
			s[2] = cr.getString(cr.getColumnIndex(DBData.ARTIST_NAME));
			s[3] = cr.getString(cr.getColumnIndex(DBData.SONG_FILEPATH));
			s[4] = String.valueOf(cr.getInt(cr
					.getColumnIndex(DBData.SONG_ISLIKE)));
			list.add(s);
		}
		cr.close();
		db.close();
		return list;
	}

	/**
	 * �����������ѯ[0:id,1:�ļ���,2:����,3:�ļ�·��,4:ϲ��]
	 * */
	public List<Song> searchIsLike() {
		return commonSearch(" WHERE A." + DBData.SONG_ISLIKE + "=1", null);
	}

	/**
	 * ����������Ų�ѯ
	 * */
	public List<String[]> searchByLately(String latelyStr) {
		if (TextUtils.isEmpty(latelyStr)) {
			return new ArrayList<String[]>();
		}
		StringBuffer sb = new StringBuffer();
		String[] ss = latelyStr.split(",");
		Cursor cr =null;
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		List<String[]> list = new ArrayList<String[]>();
		for (String param : ss) {
			sb.append("SELECT  A.").append(DBData.SONG_ISLIKE).append(",A.")
					.append(DBData.SONG_ID).append(",A.")
					.append(DBData.SONG_DISPLAYNAME + ",B.")
					.append(DBData.ARTIST_NAME).append(",A.")
					.append(DBData.SONG_FILEPATH).append(" FROM ")
					.append(DBData.SONG_TABLENAME).append(" AS A INNER JOIN ")
					.append(DBData.ARTIST_TABLENAME).append(" AS B ON A.")
					.append(DBData.SONG_ARTISTID).append("=B.")
					.append(DBData.ARTIST_ID).append("  WHERE A.")
					.append(DBData.SONG_ID).append("=").append(param).append(";");
			cr= db.rawQuery(sb.toString(), null);
			if (cr.moveToNext()) {
				String[] s = new String[5];
				s[0] = String.valueOf(cr.getInt(cr.getColumnIndex(DBData.SONG_ID)));
				s[1] = Common.clearSuffix(cr.getString(cr
						.getColumnIndex(DBData.SONG_DISPLAYNAME)));
				s[2] = cr.getString(cr.getColumnIndex(DBData.ARTIST_NAME));
				s[3] = cr.getString(cr.getColumnIndex(DBData.SONG_FILEPATH));
				s[4] = String.valueOf(cr.getInt(cr
						.getColumnIndex(DBData.SONG_ISLIKE)));
				list.add(s);
			}
			sb.setLength(0);
		}
		cr.close();
		db.close();
		return list;
	}

	/**
	 * ����������Ų�ѯ
	 * */
	public List<Song> searchLately(String latelyStr) {
		if (TextUtils.isEmpty(latelyStr)) {
			return new ArrayList<Song>();
		}
		StringBuffer sb = new StringBuffer();
		String[] ss = latelyStr.split(",");
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		List<Song> list = new ArrayList<Song>();
		Song song = null;
		Cursor cr=null;
		for (String s : ss) {
			sb.append("SELECT A.").append(DBData.SONG_ID).append(", A.")
					.append(DBData.SONG_DISPLAYNAME).append(",B.")
					.append(DBData.ARTIST_NAME).append(" AS Bname")
					.append(",C.").append(DBData.ALBUM_NAME)
					.append(" AS Cname").append(",A.").append(DBData.SONG_NAME)
					.append(" AS Aname").append(",A.")
					.append(DBData.SONG_ALBUMID).append(",A.")
					.append(DBData.SONG_ARTISTID).append(",C.")
					.append(DBData.ALBUM_PICPATH).append(" AS Cpicpath")
					.append(",B.").append(DBData.ARTIST_PICPATH)
					.append(" AS Bpicpath").append(",A.")
					.append(DBData.SONG_FILEPATH).append(",A.")
					.append(DBData.SONG_DURATIONTIME).append(" FROM ")
					.append(DBData.SONG_TABLENAME).append(" AS A INNER JOIN ")
					.append(DBData.ARTIST_TABLENAME).append(" AS B ON  A.")
					.append(DBData.SONG_ARTISTID).append("=B.")
					.append(DBData.ARTIST_ID).append(" INNER JOIN ")
					.append(DBData.ALBUM_TABLENAME).append(" AS C ON A.")
					.append(DBData.SONG_ALBUMID).append("=C.")
					.append(DBData.ALBUM_ID).append(" WHERE A.")
					.append(DBData.SONG_ID).append("=").append(s).append(";");
			cr = db.rawQuery(sb.toString(), null);
			if (cr.moveToNext()) {
				song = new Song();
				song.setId(cr.getInt(cr.getColumnIndex(DBData.SONG_ID)));
				song.setDisplayName(cr.getString(cr
						.getColumnIndex(DBData.SONG_DISPLAYNAME)));
				song.setArtist(new Artist(cr.getInt(cr
						.getColumnIndex(DBData.SONG_ARTISTID)), cr.getString(cr
						.getColumnIndex("Bname")), cr.getString(cr
						.getColumnIndex("Bpicpath"))));
				song.setAlbum(new Album(cr.getInt(cr
						.getColumnIndex(DBData.SONG_ALBUMID)), cr.getString(cr
						.getColumnIndex("Cname")), cr.getString(cr
						.getColumnIndex("Cpicpath"))));
				song.setName(cr.getString(cr.getColumnIndex("Aname")));
				song.setFilePath(cr.getString(cr
						.getColumnIndex(DBData.SONG_FILEPATH)));
				song.setDurationTime(cr.getInt(cr
						.getColumnIndex(DBData.SONG_DURATIONTIME)));
				list.add(song);
			}
			sb.setLength(0);
		}
		cr.close();
		db.close();
		return list;
	}

	/**
	 * ���ݲ����б��ѯ[0:id,1:�ļ���,2:����,3:�ļ�·��,4:ϲ��]
	 * */
	public List<String[]> searchByPlayerList(String playListId) {
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		List<String[]> list = new ArrayList<String[]>();
		Cursor cr = db.rawQuery("SELECT  A." + DBData.SONG_ISLIKE + ",A."
				+ DBData.SONG_PLAYERLIST + ",A." + DBData.SONG_ID + ",A."
				+ DBData.SONG_DISPLAYNAME + ",B." + DBData.ARTIST_NAME + ",A."
				+ DBData.SONG_FILEPATH + " FROM " + DBData.SONG_TABLENAME
				+ " AS A INNER JOIN " + DBData.ARTIST_TABLENAME
				+ " AS B WHERE A." + DBData.SONG_ARTISTID + "=B."
				+ DBData.ARTIST_ID + " ORDER BY " + DBData.SONG_DISPLAYNAME
				+ " DESC", null);
		while (cr.moveToNext()) {
			String playerList = cr.getString(cr
					.getColumnIndex(DBData.SONG_PLAYERLIST));
			if (playerList.contains(playListId)) {
				String[] s = new String[5];
				s[0] = String.valueOf(cr.getInt(cr
						.getColumnIndex(DBData.SONG_ID)));
				s[1] = Common.clearSuffix(cr.getString(cr
						.getColumnIndex(DBData.SONG_DISPLAYNAME)));
				s[2] = cr.getString(cr.getColumnIndex(DBData.ARTIST_NAME));
				s[3] = cr.getString(cr.getColumnIndex(DBData.SONG_FILEPATH));
				s[4] = String.valueOf(cr.getInt(cr
						.getColumnIndex(DBData.SONG_ISLIKE)));
				list.add(s);
			}
		}
		cr.close();
		db.close();
		return list;
	}

	/**
	 * ���ݲ����б��ѯ
	 * */
	public List<Song> searchPlayerList(String playListId) {
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		List<Song> list = new ArrayList<Song>();
		Song song = null;
		Cursor cr = db.rawQuery("SELECT A." + DBData.SONG_ID + ", A."
				+ DBData.SONG_DISPLAYNAME + ",B." + DBData.ARTIST_NAME
				+ " AS Bname" + ",C." + DBData.ALBUM_NAME + " AS Cname" + ",A."
				+ DBData.SONG_NAME + " AS Aname" + ",A." + DBData.SONG_ALBUMID
				+ ",A." + DBData.SONG_ARTISTID + ",A." + DBData.SONG_PLAYERLIST
				+ ",C." + DBData.ALBUM_PICPATH + " AS Cpicpath" + ",B."
				+ DBData.ARTIST_PICPATH + " AS Bpicpath" + ",A."
				+ DBData.SONG_FILEPATH + ",A." + DBData.SONG_DURATIONTIME
				+ " FROM " + DBData.SONG_TABLENAME + " AS A INNER JOIN "
				+ DBData.ARTIST_TABLENAME + " AS B ON  A."
				+ DBData.SONG_ARTISTID + "=B." + DBData.ARTIST_ID
				+ " INNER JOIN " + DBData.ALBUM_TABLENAME + " AS C ON A."
				+ DBData.SONG_ALBUMID + "=C." + DBData.ALBUM_ID + " ORDER BY "
				+ DBData.SONG_DISPLAYNAME + " DESC", null);

		while (cr.moveToNext()) {
			String playerList = cr.getString(cr
					.getColumnIndex(DBData.SONG_PLAYERLIST));
			if (playerList.contains(playListId)) {
				song = new Song();
				song.setId(cr.getInt(cr.getColumnIndex(DBData.SONG_ID)));
				song.setDisplayName(cr.getString(cr
						.getColumnIndex(DBData.SONG_DISPLAYNAME)));
				song.setArtist(new Artist(cr.getInt(cr
						.getColumnIndex(DBData.SONG_ARTISTID)), cr.getString(cr
						.getColumnIndex("Bname")), cr.getString(cr
						.getColumnIndex("Bpicpath"))));
				song.setAlbum(new Album(cr.getInt(cr
						.getColumnIndex(DBData.SONG_ALBUMID)), cr.getString(cr
						.getColumnIndex("Cname")), cr.getString(cr
						.getColumnIndex("Cpicpath"))));
				song.setName(cr.getString(cr.getColumnIndex("Aname")));
				song.setFilePath(cr.getString(cr
						.getColumnIndex(DBData.SONG_FILEPATH)));
				song.setDurationTime(cr.getInt(cr
						.getColumnIndex(DBData.SONG_DURATIONTIME)));
				list.add(song);
			}
		}
		cr.close();
		db.close();
		return list;
	}

	/**
	 * �������и���·����'$[string]$'�ָ�
	 * */
	public String getFilePathALL() {
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		StringBuffer sb = new StringBuffer();
		Cursor cr = db.rawQuery("SELECT " + DBData.SONG_FILEPATH + " FROM "
				+ DBData.SONG_TABLENAME + " ORDER BY " + DBData.SONG_ID
				+ " DESC", null);
		while (cr.moveToNext()) {
			sb.append("$")
					.append(cr.getString(cr
							.getColumnIndex(DBData.SONG_FILEPATH))).append("$");
		}
		cr.close();
		db.close();
		return sb.toString();
	}

	private List<Song> addList(Cursor cr) {
		List<Song> list = new ArrayList<Song>();
		Song song = null;
		while (cr.moveToNext()) {
			song = new Song();
			song.setId(cr.getInt(cr.getColumnIndex(DBData.SONG_ID)));
			song.setAlbum(new Album(cr.getInt(cr
					.getColumnIndex(DBData.SONG_ALBUMID)), null, null));
			song.setArtist(new Artist(cr.getInt(cr
					.getColumnIndex(DBData.SONG_ARTISTID)), null, null));
			song.setDisplayName(cr.getString(cr
					.getColumnIndex(DBData.SONG_DISPLAYNAME)));
			song.setDownFinish(cr.getInt(cr
					.getColumnIndex(DBData.SONG_ISDOWNFINISH)) == 1 ? true
					: false);
			song.setDurationTime(cr.getInt(cr
					.getColumnIndex(DBData.SONG_DURATIONTIME)));
			song.setFilePath(cr.getString(cr
					.getColumnIndex(DBData.SONG_FILEPATH)));
			song.setLike(cr.getInt(cr.getColumnIndex(DBData.SONG_ISLIKE)) == 1 ? true
					: false);
			song.setLyricPath(cr.getString(cr
					.getColumnIndex(DBData.SONG_LYRICPATH)));
			song.setMimeType(cr.getString(cr
					.getColumnIndex(DBData.SONG_MIMETYPE)));
			song.setName(cr.getString(cr.getColumnIndex(DBData.SONG_NAME)));
			song.setNet(cr.getInt(cr.getColumnIndex(DBData.SONG_ISNET)) == 1 ? true
					: false);
			song.setNetUrl(cr.getString(cr.getColumnIndex(DBData.SONG_NETURL)));
			song.setPlayerList(cr.getString(cr
					.getColumnIndex(DBData.SONG_PLAYERLIST)));
			song.setSize(cr.getInt(cr.getColumnIndex(DBData.SONG_SIZE)));
			list.add(song);
		}
		return list;
	}

	/**
	 * ��ҳ��ѯ
	 * */
	public List<Song> searchByPage(int pageindex, int pagesize) {
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		List<Song> list;
		Cursor cr = db.rawQuery("SELECT * FROM " + DBData.SONG_TABLENAME
				+ " LIMIT ?,? ORDER BY " + DBData.SONG_NAME + " DESC",
				new String[] { String.valueOf((pageindex - 1) * pagesize),
						String.valueOf(pagesize) });
		list = addList(cr);
		cr.close();
		db.close();
		return list;
	}

	/**
	 * �����Ƿ������
	 * */
	public void updateByLike(int id, int like) {
		SQLiteDatabase db = dbHpler.getWritableDatabase();
		db.execSQL("UPDATE " + DBData.SONG_TABLENAME + " SET "
				+ DBData.SONG_ISLIKE + "=" + like + " WHERE " + DBData.SONG_ID
				+ "=" + id);
		db.close();
	}

	/**
	 * ���²����б�
	 * */
	public void updateByPlayerList(int id, int pid) {
		SQLiteDatabase db = dbHpler.getWritableDatabase();
		Cursor cr = db.rawQuery("SELECT " + DBData.SONG_PLAYERLIST + " FROM "
				+ DBData.SONG_TABLENAME + " WHERE " + DBData.SONG_ID + "=?",
				new String[] { String.valueOf(id) });
		String temp_pl = null;
		if (cr.moveToNext()) {
			temp_pl = cr.getString(0);
		}
		cr.close();
		if (!temp_pl.contains("$" + pid + "$")) {
			db.execSQL("UPDATE " + DBData.SONG_TABLENAME + " SET "
					+ DBData.SONG_PLAYERLIST + "=? WHERE " + DBData.SONG_ID
					+ "=? ", new Object[] { temp_pl + "$" + pid + "$", id });
		}
		db.close();
	}

	/**
	 * ����id��ѯ������Ϣ
	 * */
	public Song searchById(int id) {
		Song song = null;
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		Cursor cr = db.rawQuery("SELECT  A." + DBData.SONG_DISPLAYNAME + ",B."
				+ DBData.ARTIST_NAME + " AS Bname" + ",C." + DBData.ALBUM_NAME
				+ " AS Cname" + ",A." + DBData.SONG_NAME + " AS Aname" + ",A."
				+ DBData.SONG_ALBUMID + ",A." + DBData.SONG_ARTISTID + ",C."
				+ DBData.ALBUM_PICPATH + " AS Cpicpath" + ",B."
				+ DBData.ARTIST_PICPATH + " AS Bpicpath" + ",A."
				+ DBData.SONG_FILEPATH + ",A." + DBData.SONG_DURATIONTIME
				+ ",A." + DBData.SONG_SIZE + " FROM " + DBData.SONG_TABLENAME
				+ " AS A INNER JOIN " + DBData.ARTIST_TABLENAME
				+ " AS B ON  A." + DBData.SONG_ARTISTID + "=B."
				+ DBData.ARTIST_ID + " INNER JOIN " + DBData.ALBUM_TABLENAME
				+ " AS C ON A." + DBData.SONG_ALBUMID + "=C." + DBData.ALBUM_ID
				+ " WHERE A." + DBData.SONG_ID + "=?",
				new String[] { String.valueOf(id) });
		if (cr.moveToNext()) {
			song = new Song();
			song.setId(id);
			song.setDisplayName(cr.getString(cr
					.getColumnIndex(DBData.SONG_DISPLAYNAME)));
			song.setArtist(new Artist(cr.getInt(cr
					.getColumnIndex(DBData.SONG_ARTISTID)), cr.getString(cr
					.getColumnIndex("Bname")), cr.getString(cr
					.getColumnIndex("Bpicpath"))));
			song.setAlbum(new Album(cr.getInt(cr
					.getColumnIndex(DBData.SONG_ALBUMID)), cr.getString(cr
					.getColumnIndex("Cname")), cr.getString(cr
					.getColumnIndex("Cpicpath"))));
			song.setName(cr.getString(cr.getColumnIndex("Aname")));
			song.setFilePath(cr.getString(cr
					.getColumnIndex(DBData.SONG_FILEPATH)));
			song.setDurationTime(cr.getInt(cr
					.getColumnIndex(DBData.SONG_DURATIONTIME)));
			song.setSize(cr.getInt(cr.getColumnIndex(DBData.SONG_SIZE)));
		}
		cr.close();
		db.close();
		return song;
	}

	/**
	 * �����ļ���С
	 * */
	public void updateBySize(int id, int size) {
		SQLiteDatabase db = dbHpler.getWritableDatabase();
		db.execSQL("UPDATE " + DBData.SONG_TABLENAME + " SET "
				+ DBData.SONG_SIZE + "=" + size + " WHERE "
				+ DBData.SONG_ID + "=" + id);
	}
	
	/**
	 * ���²���ʱ��
	 * */
	public void updateByDuration(int id, int duration) {
		SQLiteDatabase db = dbHpler.getWritableDatabase();
		db.execSQL("UPDATE " + DBData.SONG_TABLENAME + " SET "
				+ DBData.SONG_DURATIONTIME + "=" + duration + "  WHERE "
				+ DBData.SONG_ID + "=" + id);
	}
	
	/**
	 * ��ѯ������صĸ���
	 * */
	public List<Song> searchByDownLoad() {
		List<Song> list = new ArrayList<Song>();
		Song song = null;
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		Cursor cr = db.rawQuery("SELECT A." + DBData.SONG_ID + ",A."
				+ DBData.SONG_FILEPATH + ",A." + DBData.SONG_NAME
				+ " AS Aname,B." + DBData.ARTIST_NAME + " AS Bname FROM "
				+ DBData.SONG_TABLENAME + " AS A INNER JOIN "
				+ DBData.ARTIST_TABLENAME + " AS B ON A."
				+ DBData.SONG_ARTISTID + "=B." + DBData.ARTIST_ID + " WHERE A."
				+ DBData.SONG_ISDOWNFINISH + "=1", null);
		while (cr.moveToNext()) {
			song = new Song();
			song.setId(cr.getInt(cr.getColumnIndex(DBData.SONG_ID)));
			song.setName(cr.getString(cr.getColumnIndex("Aname")));
			song.setArtist(new Artist(0, cr.getString(cr
					.getColumnIndex("Bname")), null));
			song.setFilePath(cr.getString(cr
					.getColumnIndex(DBData.SONG_FILEPATH)));
			list.add(song);
		}
		cr.close();
		db.close();
		return list;
	}
	
	/**
	 * ��ѯ������صĸ���
	 * */
	public List<Song> searchDownLoad() {
		return commonSearch(" WHERE A." + DBData.SONG_ISDOWNFINISH + "=1", null);
	}

	/**
	 * �����������״̬�� idΪ-1��ʾȫ��
	 * */
	public int updateByDownLoadState(int id) {
		SQLiteDatabase db = dbHpler.getWritableDatabase();
		int rs = 0;
		ContentValues values = new ContentValues();
		values.put(DBData.SONG_ISDOWNFINISH, false);
		if (id == -1) {
			rs = db.update(DBData.SONG_TABLENAME, values, null, null);
		} else {
			rs = db.update(DBData.SONG_TABLENAME, values,
					DBData.SONG_ID + "=?", new String[] { String.valueOf(id) });
		}
		db.close();
		return rs;
	}

	/**
	 * �ж����������Ƿ����
	 * */
	public boolean isExist(String url) {
		int rs = 0;
		SQLiteDatabase db = dbHpler.getReadableDatabase();
		Cursor cr = db.rawQuery("SELECT COUNT(*) FROM " + DBData.SONG_TABLENAME
				+ " WHERE " + DBData.SONG_NETURL + "=?", new String[] { url });
		while (cr.moveToNext()) {
			rs = cr.getInt(0);
		}
		cr.close();
		db.close();
		return rs > 0;
	}
}
