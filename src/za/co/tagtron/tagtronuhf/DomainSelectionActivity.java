package za.co.tagtron.tagtronuhf;

import za.co.tagtron.tagtronuhf.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class DomainSelectionActivity extends Activity {

	public static void launch(Context context) {
		Intent intentTo = new Intent();
		intentTo.setClass(context, DomainSelectionActivity.class);
		context.startActivity(intentTo);
	}

	private EditText inputDomainName;
	private Button buttonDone;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_domain_selection);
		
		inputDomainName = (EditText) findViewById(R.id.editText_domain_name);
		buttonDone = (Button) findViewById(R.id.button_done);
		buttonDone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String domainName = inputDomainName.getText().toString().trim();
				
				SharedPreferences settings = getSharedPreferences(SettingsHelper.PREF_NAME, MODE_PRIVATE);
				Editor editor = settings.edit();
				editor.putString(SettingsHelper.DOMAIN_NAME, domainName);
				editor.commit();
				
				finish();
			}
		});
	}
	
	// TODO: query the server for domains
}
