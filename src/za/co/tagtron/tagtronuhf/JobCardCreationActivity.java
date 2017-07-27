package za.co.tagtron.tagtronuhf;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import za.co.tagtron.tagtronuhf.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class JobCardCreationActivity extends UhfScannerActivity {
	private static final String LOG_TAG = "JobCardCreationActivity";
	public static final String EXTRA_TAG_ID = "tag_id";
	private static final String COMMA_SEP = ",";
	private static final int MAX_TAGS = 4;
	private static final int DEFAULT_TAG_COUNT = 2;
	
	Button buttonDone;
	EditText editJobDate, editShaftNo, editNotes;
	Spinner spinnerName;
	long mDateStamp;
	
	RepairData repairData;
	TagArrayAdapter adapter;
	List<Tag> tags;
	
	ArrayAdapter<String> userListAdapter;
	List<String> userList = new ArrayList<String>();
	
	int minimumTags;
	
	public static void launch(Context context, String tagId) {
		Intent intentTo = new Intent();
		intentTo.setClass(context, JobCardCreationActivity.class);
		intentTo.putExtra(EXTRA_TAG_ID, tagId);
		context.startActivity(intentTo);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_job_card_creation);
		
		editJobDate = (EditText) findViewById(R.id.inputJobDate);
		spinnerName = (Spinner) findViewById(R.id.spinnerName);
		editShaftNo = (EditText) findViewById(R.id.inputShaftNo);
		editNotes = (EditText) findViewById(R.id.inputNotes);
		buttonDone = (Button) findViewById(R.id.buttonDone);
		
		buttonDone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				createJobCard();
			}
		});
		
		SharedPreferences settings = getSharedPreferences(SettingsHelper.PREF_NAME, MODE_PRIVATE);
		minimumTags = settings.getInt(SettingsHelper.MIN_TAGS, DEFAULT_TAG_COUNT);
		
		Calendar cal = Calendar.getInstance();
		setDate(cal);

		userListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, userList);
		userListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerName.setAdapter(userListAdapter);
		spinnerName.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// If last item selected (add new user)
				if (position == userList.size() - 1) {
					showAddUserDialog();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		updateUserList();
		
		tags = new ArrayList<Tag>();
		
		adapter = new TagArrayAdapter(this, tags);
		ListView listView = (ListView) findViewById(R.id.tagListView);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Tag tag = tags.get(position);
				removeTag(tag);
			}
		});
		
		String tagId = getIntent().getStringExtra(EXTRA_TAG_ID);
		if (!tagId.isEmpty()) {
			addTag(tagId);
			Log.i(LOG_TAG, "Job card creator launched for tag - " + tagId);
		}
	}
	
	public void setDate(Calendar date) {
		mDateStamp = date.getTimeInMillis();
		editJobDate.setText(DateFormat.format("yyyy-MM-dd", date));
	}

	private void addTag(String tagId) {
		if (tags.size() > MAX_TAGS - 1) {
			Toast.makeText(this, "Job already has " + MAX_TAGS + " tags associated!", Toast.LENGTH_SHORT).show();
			return;
		}
		
		Tag tag = new Tag(tagId);
		if (!tags.contains(tag)) {
			tags.add(tag);
			adapter.notifyDataSetChanged();
		}
	}
	
	private void removeTag(Tag tag) {
		if (tags.contains(tag)) {
			tags.remove(tag);
			adapter.notifyDataSetChanged();
		}
	}
	
	private void createJobCard() {
		String name = spinnerName.getSelectedItem().toString().trim();
		if (name.isEmpty() || name.equals(getResources().getString(R.string.add_new_user))) {
			Toast.makeText(this, "Please enter the technician's name", Toast.LENGTH_SHORT).show();
			return;
		}
		
		String shaftNo = editShaftNo.getText().toString().trim();
		if (shaftNo.isEmpty()) {
			Toast.makeText(this, "Please enter the shaft number", Toast.LENGTH_SHORT).show();
		}
		String notes = editNotes.getText().toString().trim();
		
		if (tags.size() < minimumTags) {
			Toast.makeText(this, "You must associate at least " + minimumTags + " tags with this job. Hold the trigger to scan more tags.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		RepairData rd = new RepairData("", name, mDateStamp, shaftNo, notes);
		for (Tag tag : tags) {
			rd.addTag(tag);
		}
		new WriteTask().execute(rd);
	}
	
	private boolean writeToDb(final RepairData rd) {
		Log.i(LOG_TAG, "Writing data to db: " + rd.getJobId());
        if (!rd.getJobId().isEmpty()) {
        	long result = getDbHelper().insertRepairData(rd, false);
        	return result != -1;
        } else {
        	Log.e(LOG_TAG, "No job ID - could not write to db!");
        	return false;
        }
    }
	
	private void showAddUserDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.add_new_user));
		builder.setMessage(getResources().getString(R.string.enter_techs_name));

		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
		builder.setView(input);

		// Set up the buttons
		builder.setPositiveButton("Add", new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        String name = input.getText().toString().trim();
		        if (!name.isEmpty()) {
		        	addUserToUserList(name);
		        	updateUserList();
		        	spinnerName.setSelection(0);
		        }
		    }
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.cancel();
		        spinnerName.setSelection(0);
		    }
		});
		builder.show();
	}
	
	private void updateUserList() {
		SharedPreferences settings = getSharedPreferences(SettingsHelper.PREF_NAME, MODE_PRIVATE);
		String users = settings.getString(SettingsHelper.USER_LIST, "");
		String[] userArr = users.split(COMMA_SEP);
		
		userList.clear();
		for (String s : userArr) {
			if (!s.isEmpty()) {
				userList.add(s);
			}
		}
		userList.add(getResources().getString(R.string.add_new_user));
		userListAdapter.notifyDataSetChanged();
	}
	
	private void addUserToUserList(String user) {
		SharedPreferences settings = getSharedPreferences(SettingsHelper.PREF_NAME, MODE_PRIVATE);
		String users = settings.getString(SettingsHelper.USER_LIST, "");
		if (users.isEmpty()) {
			users = user;
		}
		else {
			users = user.concat(COMMA_SEP).concat(users);
		}
		Editor editor = settings.edit();
		editor.putString(SettingsHelper.USER_LIST, users);
		editor.commit();
	}
	
	private class WriteTask extends AsyncTask<RepairData, Void, Boolean> {
    	private ProgressDialog progDialog;
    	
    	@Override
    	protected void onPreExecute() {
    		progDialog = ProgressDialog.show(JobCardCreationActivity.this, "Please wait", "Writing to database...");
    	}
    	
		@Override
		protected Boolean doInBackground(RepairData... params) {
			RepairData rd = params[0];
			if (rd != null) {
				boolean result = writeToDb(rd);
				new RemoteDbHelper().write(rd);
				// TODO: find a way to tell if the above succeeded, so we can flag this entry as synced 
				return result;
			}
			return false;
		}
    	
		@Override
		protected void onPostExecute(Boolean result) {
			progDialog.cancel();
			
			if (result) {
				Log.i(LOG_TAG, "Write success");
				Toast.makeText(JobCardCreationActivity.this, "Write success", Toast.LENGTH_SHORT).show();
				SoundUtils.playSuccessBeep();
				finish();
			}
			else {
				Log.e(LOG_TAG, "Write error!");
				Toast.makeText(JobCardCreationActivity.this, "Write error!", Toast.LENGTH_SHORT).show();
				SoundUtils.playErrorBeep();
			}
		}
    }

	@Override
	protected void handleTag(String epc, String rssi) {
		addTag(epc);
	}

	@Override
	protected void updateTags() { /* Do nothing */ }

	@Override
	protected void handleRepairData(RepairData t) { /* Do nothing */ }
	
	@Override
	protected void handleTagDataNotFound(String tagId) { /* Do nothing */ }
	
	@Override
	protected void handleNoConnectionAvailable() { /* Do nothing */ }
}