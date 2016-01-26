package com.xfdream.music.entity;


public class Sentence {

	private long fromTime;// ��ʼ������
	private long toTime;// ����������
	private String content;// �˾�����

	public Sentence(String content, long fromTime, long toTime) {
		this.content = content;
		this.fromTime = fromTime;
		this.toTime = toTime;
	}

	public Sentence(String content, long fromTime) {
		this(content, fromTime, 0);
	}

	public Sentence(String content) {
		this(content, 0, 0);
	}

	public long getFromTime() {
		return fromTime;
	}

	public void setFromTime(long fromTime) {
		this.fromTime = fromTime;
	}

	public long getToTime() {
		return toTime;
	}

	public void setToTime(long toTime) {
		this.toTime = toTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	// ���ĳ��ʱ���Ƿ������ĳ���м�
	public boolean isInTime(long time) {
		return time >= fromTime && time <= toTime;
	}

	// �õ�������ӵ�ʱ�䳤��,����Ϊ��λ
	public long getDuring() {
		return toTime - fromTime;
	}
}
