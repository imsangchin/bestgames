package irdeto.software;

import java.util.Vector;

public class MyThread_Play implements Runnable {
	static int index = 0;
	static final Object lock = new Object();
	public static MyPlayer play = new MyPlayer();

	public void run() {
		// play the sound when the sound has received 8KB
		synchronized (lock) {
			byte[] temp;
			if (index < Operate_File.audio_data.size())
				temp = Operate_File.audio_data.elementAt(index);
			else {
				temp = null;
			}
			Vector<byte[]> content = new Vector<byte[]>(20);
			play = new MyPlayer();
			index++;
			while (temp != null) {
				content.add(temp);
				if (index % 21 == 0) {
					try {
						if (play != null)
							play.playSound(content, 1);
						// sound play over and read content form memory again
						if (index < Operate_File.audio_data.size())
							temp = Operate_File.audio_data.elementAt(index);
						else
							temp = null;
					} catch (Exception e) {
						e.printStackTrace();
					}
					// clear the content
					content.clear();
				}
				index++;
				if (index < Operate_File.audio_data.size())
					temp = Operate_File.audio_data.elementAt(index);
				else {
					if (play != null)
						play.playSound(content, 1);
					content.clear();
					if (index < Operate_File.audio_data.size())
						temp = Operate_File.audio_data.elementAt(index);
					else
						temp = null;
				}
			}
			content.clear();
		}
	}
}