package za.co.tagtron.tagtronuhf;

import android.media.AudioManager;
import android.media.ToneGenerator;

public class SoundUtils {

	public static void playSuccessBeep() {
		ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
		toneGen1.startTone(ToneGenerator.TONE_PROP_ACK, 200);
	}
	
	public static void playErrorBeep() {
		ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
		toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 500);
	}
}
