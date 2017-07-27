package za.co.tagtron.tagtronuhf;

import java.util.List;

import za.co.tagtron.tagtronuhf.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends UhfScannerActivity {
	public static final String LOG_TAG = "TagtronUHF_Main";

	public enum ScanMode {
		SCAN, READ, WRITE
	};

	Button m_btnConfig, m_btnScan;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		m_btnConfig = (Button) findViewById(R.id.button_config);
		m_btnScan = (Button) findViewById(R.id.button_scan);

		m_btnScan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				JobCardCreationActivity.launch(MainActivity.this, "");
			}
		});
		
		m_btnConfig.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ConfigActivity.launch(MainActivity.this);
			}
		});
		
		((UHFApplication) getApplicationContext()).getReader(this);
		
		stopScanningAfterFirstResult = true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (checkDomainExists()) {
			if (NetworkUtils.isNetworkAvailable(this)) {
				TagDbHelper tdh = getDbHelper();
				// The db helper may not have been created yet!
				if (tdh != null) {
					RemoteDbHelper rdh = new RemoteDbHelper();
					List<RepairData> unsyncedEntries = tdh.getUnsyncedRepairEntries();
					List<InspectionData> unsyncedInspections = tdh.getUnsyncedInspectionEntries();
					
					rdh.write(unsyncedEntries.toArray(new RepairData[0]));
					rdh.update(unsyncedInspections.toArray(new InspectionData[0]));

				}
			}
		}
	}

	private boolean checkDomainExists() {
		SharedPreferences settings = getSharedPreferences(SettingsHelper.PREF_NAME, MODE_PRIVATE);
		String domainName = settings.getString(SettingsHelper.DOMAIN_NAME, "");
		if (domainName.isEmpty()) {
			DomainSelectionActivity.launch(this);
			
			return false;
		}
		else {
			RemoteDbHelper.setDomainName(domainName);
		}
		return true;
	}

	@Override
	protected void handleTag(String epc, String rssi) {
		readTagContentsFromDb(epc, true);
	}

	@Override
	protected void updateTags() {
		// Do nothing
	}

	@Override
	protected void handleRepairData(final RepairData rd) {
		if (rd != null && !rd.getJobId().isEmpty()) {
			JobCardViewerActivity.launch(this, rd);
		}
		else if (rd != null) {
		    AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    builder.setTitle("New job card?");
		    builder.setMessage("This tag has no associated data. Would you like to create a new job card?");
		    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					JobCardCreationActivity.launch(MainActivity.this, rd.getTagId(0));
				}
		    });
		    builder.setNegativeButton("Cancel", null);
		    builder.show();
		}
	}
	
	@Override
	protected void handleTagDataNotFound(final String tagId) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("New job card?");
	    builder.setMessage("This tag has no associated data. Would you like to create a new job card?");
	    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				JobCardCreationActivity.launch(MainActivity.this, tagId);
			}
	    });
	    builder.setNegativeButton("Cancel", null);
	    builder.show();
	}
	
	@Override
	protected void handleNoConnectionAvailable() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("No connection");
	    builder.setMessage("Could not connect to remote server. Please check WiFi is on, or a SIM card is installed with data connection.");
	    builder.setPositiveButton("OK", null);
	    builder.show();
	}
}
