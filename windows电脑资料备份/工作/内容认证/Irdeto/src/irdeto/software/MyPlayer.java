package irdeto.software;

import java.util.Vector;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class MyPlayer {
	private AudioTrack aAudioTrack01 = null;
	private float midVol = 0;
	public byte[] data = null;
	protected int m_out_buf_size;
	private boolean stop = false;

	// iChannel = 0 means left channel test, iChannel = 1 means right channel
	// test.
	public void playSound(Vector<byte[]> audiodata, int iChannel) {

		if (stop)
			return;
		// If now is playing...
		if (aAudioTrack01 != null) {
			aAudioTrack01.release();
			aAudioTrack01 = null;
		}
		// Get the AudioTrack minimum buffer size

		int iMinBufSize = AudioTrack.getMinBufferSize(22050,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		if (iMinBufSize == AudioTrack.ERROR_BAD_VALUE
				|| iMinBufSize == AudioTrack.ERROR) {
			return;
		}
		// Constructor a AudioTrack object
		try {
			aAudioTrack01 = new AudioTrack(AudioManager.STREAM_MUSIC, 22050,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT, 4096,
					AudioTrack.MODE_STREAM);

		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
		}
		// Write data to buffer
		try {
			aAudioTrack01.play();
			for (int i = 0; i < audiodata.size(); i++) {
				if (Thread.currentThread().isAlive())
					aAudioTrack01.write(audiodata.elementAt(i), 0,
							audiodata.elementAt(i).length);
			}

			// aAudioTrack01.write(data, 0, data.length);
			float lValue = 0;
			float rValue = 0;

			if (iChannel == 0) {
				lValue = 1.0f;
				rValue = 0.0f;
			} else if (iChannel == 1) {
				lValue = 0.0f;
				rValue = 1.0f;
			}

			// aAudioTrack01.play();
			// String result = "false";
			if (aAudioTrack01.setStereoVolume(lValue, rValue) == AudioTrack.SUCCESS) {

			}
			// Log.i("Auto", result);
			aAudioTrack01.stop();
			if (aAudioTrack01.setStereoVolume(midVol, midVol) == AudioTrack.SUCCESS) {
				// mTextView01.setText("Restore setStereoVolume successfully!");
			}
			aAudioTrack01.release();
			aAudioTrack01 = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// stop player
	public void stop_sound() {
		stop = true;
		if (aAudioTrack01 != null) {
			Operate_File.IsStop = true;
			aAudioTrack01.stop();
		}
	}

}
