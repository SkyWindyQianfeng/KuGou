package com.xfdream.music.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.xfdream.music.dao.AlbumDao;
import com.xfdream.music.dao.ArtistDao;
import com.xfdream.music.dao.SongDao;
import com.xfdream.music.entity.Album;
import com.xfdream.music.entity.Artist;
import com.xfdream.music.entity.ScanData;
import com.xfdream.music.entity.Song;

public class MusicManager {

	private Context context;

	public MusicManager(Context context) {
		this.context = context;
	}

	/**
	 * ��ѯ����ý�������Ŀ¼
	 * */
	public List<ScanData> searchByDirectory() {
		List<ScanData> list = new ArrayList<ScanData>();
		StringBuffer sb = new StringBuffer();
		String[] projection = { MediaStore.Audio.Media.DISPLAY_NAME,
				MediaStore.Audio.Media.DATA };
		Cursor cr = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null,
				null, MediaStore.Audio.Media.DISPLAY_NAME);
		String displayName = null;
		String data = null;
		while (cr.moveToNext()) {
			displayName = cr.getString(cr
					.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
			data = cr.getString(cr.getColumnIndex(MediaStore.Audio.Media.DATA));
			data = data.replace(displayName, "").toLowerCase();
			if (!sb.toString().contains(data)) {
				;
				list.add(new ScanData(data, true));
				sb.append(data);
			}
		}
		cr.close();
		return list;
	}

	/**
	 * ��ѯ����������ý����в����ڵ����и���
	 * */
	private HashMap<String, Song> searchBySong(String filePaths) {
		HashMap<String, Song> map = new HashMap<String, Song>();
		String[] projection = new String[] { MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.DISPLAY_NAME,
				MediaStore.Audio.Media.DURATION,
				MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.SIZE };
		Cursor cr = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null,
				null, MediaStore.Audio.Media._ID);
		Song song = null;
		while (cr.moveToNext()) {
			String filePath = cr.getString(
					cr.getColumnIndex(MediaStore.Audio.Media.DATA))
					.toLowerCase();
			if (!filePaths.contains(filePath)) {
				song = new Song();
				String album = cr.getString(cr
						.getColumnIndex(MediaStore.Audio.Media.ALBUM));
				String artist = cr.getString(cr
						.getColumnIndex(MediaStore.Audio.Media.ARTIST));
				// δ֪ר��
				if (TextUtils.isEmpty(album)
						|| album.toLowerCase().equals("<unknown>")) {
					song.setAlbum(new Album(-1, "δ֪ר��", ""));
				} else {
					song.setAlbum(new Album(-1, album.trim(), ""));
				}
				// δ֪����
				if (TextUtils.isEmpty(artist)
						|| artist.toLowerCase().equals("<unknown>")) {
					song.setArtist(new Artist(-1, "δ֪����", ""));
				} else {
					song.setArtist(new Artist(-1, artist.trim(), ""));
				}
				song.setDisplayName(cr.getString(cr
						.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
				song.setDownFinish(false);
				song.setDurationTime(cr.getInt(cr
						.getColumnIndex(MediaStore.Audio.Media.DURATION)));
				song.setFilePath(filePath);
				song.setLike(false);
				song.setLyricPath(null);
				song.setMimeType(cr.getString(cr
						.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)));
				song.setName(cr.getString(cr
						.getColumnIndex(MediaStore.Audio.Media.TITLE)));
				song.setNet(false);
				song.setNetUrl(null);
				song.setPlayerList("$1$");// ��ʾĬ���б�
				song.setSize(cr.getInt(cr
						.getColumnIndex(MediaStore.Audio.Media.SIZE)));
				map.put(filePath, song);
			}
		}
		return map;
	}

	/**
	 * ɨ�����
	 * */
	public void scanMusic(String rs, Handler handler) {
		List<String> list = new ArrayList<String>();
		ScanMusicFilenameFilter filenameFilter = new ScanMusicFilenameFilter();
		String[] filePaths = rs.split("\\$*\\$");// �ָ����ļ�·��
		for (int i = 0; i < filePaths.length; i++) {
			if (!filePaths[i].trim().equals("")) {
				File[] fs = new File(filePaths[i]).listFiles(filenameFilter);
				if (fs != null) {
					for (int j = 0; j < fs.length; j++) {
						list.add(fs[j].getPath().toLowerCase());
					}
				}
			}
		}

		SongDao songDao = new SongDao(context);
		AlbumDao albumDao = new AlbumDao(context);
		ArtistDao artistDao = new ArtistDao(context);

		// ��ѯ�����е����и�����Ϣ
		String song_filePaths = songDao.getFilePathALL().toLowerCase();
		// ��ѯ����������ý����в����ڵ����и���
		HashMap<String, Song> map = searchBySong(song_filePaths);

		int count = 0;
		for (int i = 0, len = list.size(); i < len; i++) {
			String fp = list.get(i);
			// �жϸ����Ƿ��ڱ�����
			if (!song_filePaths.contains("$" + fp + "$")) {

				// ��ʾɨ����Ϣ
				Message msg = handler.obtainMessage();
				Bundle data = new Bundle();
				data.putString("rs", fp);
				msg.what = 0;
				msg.setData(data);
				msg.sendToTarget();

				Song song = map.get(fp);
				if (song != null) {
					// ����ר��
					Album album = song.getAlbum();
					int albumId = albumDao.isExist(album.getName());
					if (albumId == -1) {
						albumId = (int) albumDao.add(album);
					}
					album.setId(albumId);
					song.setAlbum(album);

					// �������
					Artist artist = song.getArtist();
					int artistId = artistDao.isExist(artist.getName());
					if (artistId == -1) {
						artistId = (int) artistDao.add(artist);
					}
					artist.setId(artistId);
					song.setArtist(artist);
				} else {
					song = new Song();
					
					int unalbum_id = albumDao.isExist("δ֪ר��");
					if (unalbum_id == -1) {
						unalbum_id = (int) albumDao.add(new Album(0, "δ֪ר��",
								""));
					}
					song.setAlbum(new Album(unalbum_id, "", ""));
					
					int unartist_id = artistDao.isExist("δ֪����");
					if (unartist_id == -1) {
						unartist_id = (int) artistDao.add(new Artist(0, "δ֪����",
								""));
					}
					song.setArtist(new Artist(unartist_id, "", ""));
					song.setDisplayName(Common.clearDirectory(fp));
					song.setDownFinish(false);
					song.setDurationTime(-1);
					song.setFilePath(fp);
					song.setLike(false);
					song.setLyricPath(null);
					song.setMimeType("");
					song.setName("");
					song.setNet(false);
					song.setNetUrl(null);
					song.setPlayerList("$1$");// ��ʾĬ���б�
					song.setSize(-1);
				}
				if (songDao.add(song) > 0)
					count++;
			}
		}
		// ��ʾɨ����Ϣ
		Message msg = handler.obtainMessage();
		Bundle data = new Bundle();
		data.putString("rs", "ɨ����ϣ���" + count + "�ף�");
		msg.what = 1;
		msg.setData(data);
		msg.sendToTarget();
	}
}
