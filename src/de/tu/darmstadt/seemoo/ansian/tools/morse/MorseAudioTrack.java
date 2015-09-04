package de.tu.darmstadt.seemoo.ansian.tools.morse;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;

public class MorseAudioTrack extends AudioTrack {

	private short[] sound;

	public MorseAudioTrack(int sampleRateInHz, short[] sound) throws IllegalArgumentException {
		super(AudioManager.STREAM_MUSIC, sampleRateInHz, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
				AudioRecord.getMinBufferSize(sampleRateInHz, AudioFormat.CHANNEL_OUT_STEREO,
						AudioFormat.ENCODING_PCM_16BIT),
				AudioTrack.MODE_STREAM);
		this.sound = sound;
	}

	public void playSound() throws IllegalStateException {
		play();
		write(sound, 0, sound.length);
		stop();
	}

}
