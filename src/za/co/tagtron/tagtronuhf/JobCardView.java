package za.co.tagtron.tagtronuhf;

import za.co.tagtron.tagtronuhf.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

class JobCardView extends RelativeLayout {
	
	private RepairData job;
	private TextView mTvName, mTvJobDate, mTvInspectionDate, mTvStatus;
	
	public JobCardView(Context context) {
		super(context);
		initializeViews(context);
	}

	public JobCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initializeViews(context);
	}

	public JobCardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initializeViews(context);
	}

	private void initializeViews(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.job_card_view, this);
	}
	
	@Override
	protected void onFinishInflate() {
	   super.onFinishInflate();
	}
	
	public void setJobData(RepairData job) {
		mTvName = (TextView) findViewById(R.id.jobCard_textName);
		mTvJobDate = (TextView) findViewById(R.id.jobCard_textDate);
		mTvInspectionDate = (TextView) findViewById(R.id.jobCard_textLastInspectionDate);
		mTvStatus = (TextView) findViewById(R.id.jobCard_textStatus);
		
		if (job == null) {
			mTvName.setText("");
			mTvJobDate.setText("-");
			mTvInspectionDate.setText("-");
			mTvStatus.setText("");
		}
		else {
			mTvName.setText(job.getTechnicianName());
			mTvJobDate.setText(job.getRepairDateStr());
			mTvInspectionDate.setText(job.getLastInspectionDateStr());
			mTvStatus.setText(job.getStatus());
		}
		this.job = job;
	}
	
	public RepairData getJobData() {
		return job;
	}
}
