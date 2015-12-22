package irdeto.software;

import java.io.File;
import java.io.FilenameFilter;

public class MyFilenameFilter implements FilenameFilter {

	@Override
	public boolean accept(File dir, String filename) {
		// TODO Auto-generated method stub

		if (filename.compareTo("irdeto.mp3") == 0)
			return false;

		if (filename.toUpperCase().contains(".WAV"))
			return true;

		if (filename.toUpperCase().endsWith(".MP3"))
			return true;
		if (filename.toUpperCase().endsWith(".JPG"))
			return true;
		if (filename.toUpperCase().endsWith(".PNG"))
			return true;
		if (filename.toUpperCase().endsWith(".BMP"))
			return true;
		return false;
	}
}
