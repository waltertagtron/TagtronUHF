package za.co.tagtron.tagtronuhf;

import za.co.tagtron.tagtronuhf.R;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class JobCardViewerActivity extends ReaderActivity {
	private static final String LOG_TAG = "JobCardViewerActivity";
	
	public enum JobCardViewerActivityMode { EMPTY, NEW, EXISTING };
	
	public static final String EXTRA_REPAIR_DATA = "RepairData";
	
	private JobCardView mJobCard;
	private Button mBtnUpdateInspectionDate;
	private RepairData mRepairData;
		
	public static void launch(Context context, RepairData data) {
		Intent intentTo = new Intent();
		intentTo.setClass(context, JobCardViewerActivity.class);
		intentTo.putExtra(JobCardViewerActivity.EXTRA_REPAIR_DATA, data);
		context.startActivity(intentTo);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_job_card_viewer);
		mJobCard = (JobCardView) findViewById(R.id.job_card);
		mBtnUpdateInspectionDate = (Button) findViewById(R.id.button_update_inspection_date);
		
		mBtnUpdateInspectionDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				inspectItem();
			}
		});
		
		mRepairData = (RepairData) getIntent().getSerializableExtra(EXTRA_REPAIR_DATA);
		mJobCard.setJobData(mRepairData);
		Log.i(LOG_TAG, "Viewing data for tag - " + mRepairData.getJobId());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// This ends up reading twice on initial launch
		new ReadDbTask().execute(mRepairData.getTagId(0));
	}
	
	private void inspectItem() {
		InspectionActivity.launch(this, mRepairData);
	}
	
	private class ReadDbTask extends AsyncTask<String, Void, RepairData> {
    	@Override
		protected RepairData doInBackground(String... params) {
			String tagId = params[0];
			if (!tagId.isEmpty()) {
				RepairData foo = getDbHelper().getTagEntry(tagId);
				return foo;
			}
			return null;
		}
    	
		@Override
		protected void onPostExecute(RepairData result) {
			if (result != null) {
				mRepairData = result;
				mJobCard.setJobData(mRepairData);
			}
		}
    }
}
