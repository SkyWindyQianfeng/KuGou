package com.xfdream.music.entity;

import java.util.List;

/**
 * �߳�������Ϣ
 * */
public class DownLoadInfo {

	private int id;// id
	private List<ThreadInfo> threadInfos;// ���߳���Ϣ
	private String url;// ����·��
	private int fileSize;// �ļ���С
	private String name;//��������
	private String artist;//����
	private String album;//ר��
	private String displayName;//�ļ���
	private String mimeType;//mime
	private int durationTime;//����ʱ��
	private int completeSize;//�����ؽ���
	private String filePath;//�����ļ���·��
	
	private int state;// ����״̬
	private int threadCount;//����ʱ��̵߳�����
	
	public DownLoadInfo() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<ThreadInfo> getThreadInfos() {
		return threadInfos;
	}

	public void setThreadInfos(List<ThreadInfo> threadInfos) {
		this.threadInfos = threadInfos;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public int getDurationTime() {
		return durationTime;
	}

	public void setDurationTime(int durationTime) {
		this.durationTime = durationTime;
	}

	public int getCompleteSize() {
		return completeSize;
	}

	public void setCompleteSize(int completeSize) {
		this.completeSize = completeSize;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}
}
