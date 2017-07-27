package za.co.tagtron.tagtronuhf;

import com.rscja.deviceapi.RFIDWithUHF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class UHFApplication extends Application {
	private static final String LOG_TAG = "UHFApp";
	
	private RFIDWithUHF reader;
	private TagDbHelper dbHelper;
	private boolean initStarted = false;
	private boolean initComplete = false;
	
	public RFIDWithUHF getReader(Context context) {
		if (reader == null) {
			initComplete = false;
			try {
				reader = RFIDWithUHF.getInstance();
			} catch (Exception ex) {

				Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
				return null;
			}
		}
			
		if (initComplete) {
			return reader;
		}
		else {
			initReader(context);
		}
		
		// Returning reader here even if it is still being initialised. The init dialog
		// will display until init is complete.
		return reader;
	}
	
	private void initReader(Context context) {
		// Don't init reader twice!
		if (initStarted) {
			Log.i(LOG_TAG, "Reader init in progress...");
			return;
		}
		
		Log.i(LOG_TAG, "Initialising reader...");
		try {
			reader = RFIDWithUHF.getInstance();
		} catch (Exception ex) {
			Log.e(LOG_TAG, "Exception while initialising reader! " + ex.getMessage());
			Toast.makeText(UHFApplication.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}

		if (reader != null) {
			new InitTask(context).execute();
		}
	}

//	private void destroyReader() {
//		Log.i(LOG_TAG, "Freeing reader resources...");
//		if (reader != null) {
//			reader.free();
//		}
//	}
	
	private void initDb() {
		Log.i(LOG_TAG, "Initialising DB");
		dbHelper = new TagDbHelper(this);
		
		try {
			dbHelper.getWritableDatabase();
		}
		catch (SQLiteException e) {
			Log.e(LOG_TAG, e.getMessage());
		}
	}
	
	public TagDbHelper getDbHelper() {
		return dbHelper;
	}
	
	public void closeDb() {
		if (dbHelper != null) {
			dbHelper.close();
		}
	}
	
	public class InitTask extends AsyncTask<String, Integer, Boolean> {
		ProgressDialog mypDialog;
		Context context;

		public InitTask(Context context) {
	        super();
	        this.context = context;
	    }
		
		@Override
		protected Boolean doInBackground(String... params) {
			initDb();
			return reader.init();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			mypDialog.cancel();
			if (!result) {
				Log.e(LOG_TAG, "Reader init failed!");
				Toast.makeText(UHFApplication.this, "Init failed!", Toast.LENGTH_SHORT).show();
				initComplete = false;
			} else {
				initComplete = true;
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mypDialog = new ProgressDialog(context);
			mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mypDialog.setMessage("Init...");
			mypDialog.setCanceledOnTouchOutside(false);
			mypDialog.show();
		}
	}
}
