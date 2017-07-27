package za.co.tagtron.tagtronuhf;

import java.util.HashMap;
import java.util.Map;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

/**
 * Abstract superclass which contains methods for scanning for tags.
 * Handles trigger button clicks and all RF aspects.
 * @author Dev
 *
 */
public abstract class UhfScannerActivity extends ReaderActivity {
	private static final String LOG_TAG = "UhfScannerActivity";
	private static final int SCAN_DELAY = 50;
	private static final Long RECUR_DELAY = Long.valueOf(10000);
	
	public static final String DEFAULT_PASS = "000000";
	public static final int DEFAULT_OFFSET = 0;
	
	private Handler handler;
	private boolean isScanning = false;
	private boolean newTagFound = false;
	protected boolean stopScanningAfterFirstResult = false;
	private Map<String, Long> tagIds = new HashMap<String, Long>();
	
	/**
	 * The reader has found a tag - handle it.
	 * @param epc
	 * @param rssi
	 */
	protected abstract void handleTag(final String epc, final String rssi);
	
	/**
	 * Scanning is complete and one or more tags were found - the activity should update it's views.
	 */
	protected abstract void updateTags();

	/**
	 * Tag contents were read from the DB - either local or remote.
	 * @param t
	 */
	protected abstract void handleRepairData(final RepairData t);
	
	/**
	 * Handle a tag for which no data was found in the DB - local nor remote
	 */
	protected abstract void handleTagDataNotFound(final String tagId);
	
	/**
	 * Handle the case where there is no connection available
	 */
	protected abstract void handleNoConnectionAvailable();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String result = msg.obj + "";
				String[] strs = result.split("@");
				handleTag(strs[0],strs[1]);
			}
		};
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		stopScanning();
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 139) {	// Trigger press
            if (event.getRepeatCount() == 0) {
            	startScanning();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
	
	@Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 139) {	// Trigger release
            if (event.getRepeatCount() == 0) {
            	stopScanning();
            }
            return true;
        }
        
        // For some reason back key is not being handled properly by super :(
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	finish();
        	return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }
	
	private void startScanning() {
		Log.i(LOG_TAG, "Starting scanning...");
		newTagFound = false;
		if (getReader().startInventoryTag((byte) 0, (byte) 0)) {
			isScanning = true;
			new TagInventoryThread(SCAN_DELAY).start();
		} else {
			getReader().stopInventory();
			Log.e(LOG_TAG, "Error starting scan!");
			Toast.makeText(this, "Error starting scan!", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void stopScanning() {
		if (isScanning) {
			Log.i(LOG_TAG, "Stopping scanning...");
			isScanning = false;
			getReader().stopInventory();
			
			if (newTagFound) {
				newTagFound = false;
				updateTags();
			}
		}
	}
	
	class TagInventoryThread extends Thread {
		private int mDelay = 80;

		public TagInventoryThread(int delay) {
			mDelay = delay;
		}

		public void run() {
			String[] res = null;

			while (isScanning) {
				res = getReader().readTagFromBuffer();

				if (res != null) {
					Long currentMillis = System.currentTimeMillis();
					String epc = res[1];
					String rssi = res[2];
					if (tagIds.containsKey(epc)) {
						Long millis = tagIds.get(epc);
						// If it's been less than 10 seconds since we've scanned this tag, ignore it and continue
						if (millis + RECUR_DELAY > currentMillis) {
							continue;
						}
					}
					
					newTagFound = true;
					tagIds.put(epc, currentMillis);
					Message msg = handler.obtainMessage();
					msg.obj = epc + "@"	+ rssi;
					handler.sendMessage(msg);
					SoundUtils.playSuccessBeep();
					
					if (stopScanningAfterFirstResult) {
						stopScanning();
					}
				}
				try {
					sleep(mDelay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Asynchronously get the contents of the tag from the DB. Results are returned
	 * via the tagContents method.
	 * @param epc
	 * @param fetchRemote - if true, will attempt to fetch from the remote database
	 * 						when not found locally.
	 */
	protected void readTagContentsFromDb(final String epc, final boolean fetchRemote) {
		AsyncTask<Void, Integer, RepairData> readTagDataTask = new AsyncTask<Void, Integer, RepairData>() {
			ProgressDialog progressDialog;
			
			@Override
			protected void onPreExecute() {
				super.onPreExecute();

				progressDialog = new ProgressDialog(UhfScannerActivity.this);
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setMessage("Tag found. Fetching data...");
				progressDialog.setCanceledOnTouchOutside(false);
				progressDialog.show();
			}
			
			@Override
			protected RepairData doInBackground(Void... params) {
				Log.i(LOG_TAG, "Reading tag data for " + epc);
				boolean updateLocalDb = false;
						
				// So we're just reading from the local DB here... not from the tag itself
				RepairData rd = getDbHelper().getTagEntry(epc);
				if (rd == null && fetchRemote) {
					if (NetworkUtils.isNetworkAvailable(UhfScannerActivity.this)) {
						// If there's nothing in local DB, try remote
						Log.i(LOG_TAG, "Fetching record from remote DB - " + epc);
						publishProgress(1);

						try {
							rd = RemoteDbHelper.readFromRemoteDb(epc);
						}
						catch (RuntimeException ex) {
							Log.e(LOG_TAG, "Runtime exception when reading from remote db!");
							ex.printStackTrace();
						}
						
						if (rd != null) {
							updateLocalDb = true;
						}
					}
				}
					
				Tag t = new Tag(epc, "");
				if (rd != null) {
					rd.addTag(t);;

					//We don't have a local copy of this, so put it in the DB
					if (updateLocalDb) {
						Log.i(LOG_TAG, "Record found! Inserting into local DB.");
						getDbHelper().insertRepairData(rd, true);
					}
				}
				else {
					Log.e(LOG_TAG, "Couldn't find tag data - " + epc);
				}
				return rd;
			}
			
			@Override
			protected void onProgressUpdate(Integer... values) {
				if (values[0] == 1) {
					progressDialog.setMessage("Tag found. Fetching data from remote server...");
				}
			}
			
			@Override
			protected void onPostExecute(RepairData rd) {
				progressDialog.cancel();
				if (rd != null) {
					handleRepairData(rd);
				}
				else {
					if (NetworkUtils.isNetworkAvailable(UhfScannerActivity.this)) {
						handleTagDataNotFound(epc);
					}
					else {
						handleNoConnectionAvailable();
					}
				}
			}
		};
		readTagDataTask.execute();
	}
}
