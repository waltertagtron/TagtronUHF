package za.co.tagtron.tagtronuhf;

import za.co.tagtron.tagtronuhf.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ConfigActivity extends ReaderActivity {
	private static final String LOG_TAG = "ConfigActivity";
	private static final int MIN_POWER = 5;
	private static final int DEFAULT_TAGS_PER_JOB = 2;

	private SeekBar mSeekPowerOutput;
	private Button mBtnSetPower;
	private int powerOutput;
	private int tagsPerJob;
	
	public static void launch(Context context) {
		Intent intentTo = new Intent();
		intentTo.setClass(context, ConfigActivity.class);
		context.startActivity(intentTo);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config);
		
		SharedPreferences settings = getSharedPreferences(SettingsHelper.PREF_NAME, MODE_PRIVATE);
		tagsPerJob = settings.getInt(SettingsHelper.MIN_TAGS, DEFAULT_TAGS_PER_JOB);
		
		Spinner spinner = (Spinner) findViewById(R.id.spinner_min_tags);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.array_min_tags, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(tagsPerJob - 1);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				tagsPerJob = position + 1;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		mSeekPowerOutput = (SeekBar) findViewById(R.id.seekbar_power);
		mSeekPowerOutput.setOnSeekBarChangeListener(
		    new OnSeekBarChangeListener()
		    {
		        @Override
		        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		        	powerOutput = progress + MIN_POWER;
		        }

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
		    }
		);
		
		mBtnSetPower = (Button) findViewById(R.id.button_set_power);
		mBtnSetPower.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getReader().setPower(powerOutput)) {
					Toast.makeText(ConfigActivity.this, "Power set to " + powerOutput, Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(ConfigActivity.this, "Failed to set power!", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		AsyncTask<Void, Void, Integer> readPowerTask = new AsyncTask<Void, Void, Integer>() {
			@Override
			protected Integer doInBackground(Void... params) {
				Log.i(LOG_TAG, "Getting power setting...");
				int power = getReader().getPower();
				return power;
			}
			
			@Override
			protected void onPostExecute(Integer value) {
				Log.i(LOG_TAG, "Power setting at: " + value);
				powerOutput = value;
				mSeekPowerOutput.setProgress(powerOutput - MIN_POWER);
			}
		};
		readPowerTask.execute();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		SharedPreferences settings = getSharedPreferences(SettingsHelper.PREF_NAME, MODE_PRIVATE);
		Editor editor = settings.edit();
		editor.putInt(SettingsHelper.MIN_TAGS, tagsPerJob);
		editor.commit();
	}
}
