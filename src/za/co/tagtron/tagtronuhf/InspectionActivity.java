package za.co.tagtron.tagtronuhf;

import java.util.Date;

import za.co.tagtron.tagtronuhf.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class InspectionActivity extends ReaderActivity {
	private static final String EXTRA_TAG_DATA = "data";
	
	private LocationManager locationManager;
	
	private RepairData repairData;
	private EditText editTextName, editTextDate;
	private Button buttonInspectionOk, buttonInspectionFailed;
	
	private String location = "-";
	private long timestamp;

	public static void launch(Context context, RepairData repairData) {
		Intent intentTo = new Intent();
		intentTo.setClass(context, InspectionActivity.class);
		intentTo.putExtra(EXTRA_TAG_DATA, repairData);
		context.startActivity(intentTo);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inspection);
		
		locationManager = (LocationManager)	getSystemService(Context.LOCATION_SERVICE);
		editTextName = (EditText) findViewById(R.id.editTextName);
		editTextDate = (EditText) findViewById(R.id.editTextDate);
		
		buttonInspectionOk = (Button) findViewById(R.id.buttonInspectionOk);
		buttonInspectionFailed = (Button) findViewById(R.id.buttonInspectionFailed);
		
		buttonInspectionOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				inspectionComplete(true);
				finish();
			}
		});
		
		buttonInspectionFailed.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showConfirmInspectionFailedDialog();
			}
		});
		
		repairData = (RepairData) getIntent().getSerializableExtra(EXTRA_TAG_DATA);
	}

	@Override
	public void onResume() {
		super.onResume();
		
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {		
			Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (loc == null) {
				location = "Unknown";
			}
			else {
				double latitude = loc.getLatitude();
				double longitude = loc.getLongitude();
				
				location = String.valueOf(latitude) + " " + String.valueOf(longitude);
			}
		}
		
		Date d = new Date();
		timestamp = d.getTime();
		String dateStr = DateFormat.format("yyyy-MM-dd hh:mm", d).toString();
		editTextDate.setText(dateStr);
	}
	
	private void showConfirmInspectionFailedDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Confirm inspection failed?");
		builder.setMessage("Once confirmed, all tags for this job should be scrapped immediately.");
		builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	inspectionComplete(false);
		    	finish();
		    }
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.cancel();
		    }
		});
		builder.show();
	}
	
	private void inspectionComplete(boolean ok) {
		String status = ok ? "OK" : "Failed";
		String name = editTextName.getText().toString().trim();
		String jobId = repairData.getJobId();
		
		InspectionData id = new InspectionData(jobId, name, location, status, timestamp);
		
		getDbHelper().insertInspectionEntry(id);
		getDbHelper().updateInspectionDate(jobId, timestamp, status);
		
		if (NetworkUtils.isNetworkAvailable(this)) {
			RemoteDbHelper rdbh = new RemoteDbHelper();
			rdbh.update(id);
		}
	}
}
