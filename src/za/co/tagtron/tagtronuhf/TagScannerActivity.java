package za.co.tagtron.tagtronuhf;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class TagScannerActivity extends UhfScannerActivity {
	private static final String LOG_TAG = "TagScannerActivity";
	
	TagArrayAdapter adapter;
	List<Tag> tags;
	
	Button clearBtn;
	
	public static void launch(Context context) {
		Intent intentTo = new Intent();
		intentTo.setClass(context, TagScannerActivity.class);
		context.startActivity(intentTo);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_tag_scanner);
//		
//		tags = new ArrayList<Tag>();
//		
//		adapter = new TagArrayAdapter(this, tags);
//		ListView listView = (ListView) findViewById(R.id.list_view);
//		listView.setAdapter(adapter);
//		listView.setOnItemClickListener(new OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				Tag tag = tags.get(position);
//				RepairData data;
//				if (tag.getContents() == null) {
//					data = new RepairData("", "", 0, tag.getTagId());
//				}
//				else {
//					data = (RepairData) tag.getContents();
//				}
//				JobCardViewerActivity.launch(TagScannerActivity.this, data);
//			}
//		});
//		
//		clearBtn = (Button) findViewById(R.id.button_clear);
//		clearBtn.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				clearTags();				
//			}
//		});
//		
		Toast.makeText(this, "Hold trigger to scan for tags", Toast.LENGTH_SHORT).show();
	}
	
	private void clearTags() {
		tags.clear();
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void handleTag(String epc, String rssi) {
		Tag newTag = new Tag(epc, rssi);
		
		if (!tags.contains(newTag)) {
			Log.i(LOG_TAG, "Tag found - " + epc);
			tags.add(newTag);
			adapter.notifyDataSetChanged();
		}
		else {
			Log.i(LOG_TAG, "Duplicate tag - " + epc);
			int pos = tags.indexOf(newTag);
			Tag existing = tags.get(pos);
//			if (existing.getContents() == null) {
//				Log.i(LOG_TAG, "Duplicate has no contents!");
//			}
		}
	}
	
	@Override
	protected void updateTags() {
		// Tags have been added - try read the data from DB...
		
//		for (Tag t : tags) {
//			if (t.getContents() == null) {
//				readTagContentsFromDb(t.getTagId(), true);
//			}
//		}			
	}

	@Override
	protected void handleRepairData(RepairData t) {
		//TODO: fix this
//		if (t != null) {
//			tags.add(t);
//			adapter.notifyDataSetChanged();
//		}
	}
	
	@Override
	protected void handleTagDataNotFound(String tagId) {}

	@Override
	protected void handleNoConnectionAvailable() { /* Do nothing */ }
}
