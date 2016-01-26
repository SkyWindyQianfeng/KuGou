package com.xfdream.music.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * �ļ���������
 * */
public class ScanMusicFilenameFilter implements FilenameFilter {
	private static final String suffixs=".MP3.WMA.AAC.M4A";
	public boolean accept(File dir, String filename) {
		if(suffixs.contains("."+Common.getSuffix(filename))){
			return true;
		}
		return false;
	}

}
