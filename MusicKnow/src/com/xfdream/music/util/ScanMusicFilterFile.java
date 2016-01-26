package com.xfdream.music.util;

import java.io.File;
import java.io.FileFilter;

/**
 * ÎÄ¼ş¹ıÂËÆ÷
 * */
public class ScanMusicFilterFile implements FileFilter{

	public boolean accept(File pathname) {
		if(pathname.isDirectory()&&pathname.canRead()){
			return pathname.list().length>0;
		}
		return false;
	}
}
