package com.xfdream.music.entity;


/**
 * ���߳�����-���̵߳���Ϣ
 * */
public class ThreadInfo {
	
	private int id;//id
	private int downLoadInfoId;//DOWNLOADINFO id
	private int startPosition;//��ʼ���صĴ�С
	private int endPosition;//�������صĴ�С
	private int completeSize;//�����صĴ�С
	
	public ThreadInfo(){
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDownLoadInfoId() {
		return downLoadInfoId;
	}

	public void setDownLoadInfoId(int downLoadInfoId) {
		this.downLoadInfoId = downLoadInfoId;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}

	public int getEndPosition() {
		return endPosition;
	}

	public void setEndPosition(int endPosition) {
		this.endPosition = endPosition;
	}

	public int getCompleteSize() {
		return completeSize;
	}

	public void setCompleteSize(int completeSize) {
		this.completeSize = completeSize;
	}
	
}
