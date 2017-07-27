package za.co.tagtron.tagtronuhf;

import com.rscja.deviceapi.RFIDWithUHF;

import android.app.Activity;

public abstract class ReaderActivity extends Activity {
	protected RFIDWithUHF getReader() {
		return ((UHFApplication) getApplicationContext()).getReader(this);
	}
		
	protected TagDbHelper getDbHelper() {
		return ((UHFApplication) getApplicationContext()).getDbHelper();
	}
}
