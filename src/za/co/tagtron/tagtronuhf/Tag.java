package za.co.tagtron.tagtronuhf;

import java.io.Serializable;

import android.util.Log;

public class Tag implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String LOG_TAG = "Tag";
	
	private String tagId;
	private double rssi;	/* Range from -100 : -35 db */
	
	public Tag(String tagId) {
		this.tagId = tagId;
		this.rssi = -100;
	}
	
	public Tag(String tagId, String rssi) {
		this.tagId = tagId;
		
		try {
			this.rssi = Double.valueOf(rssi);
		}
		catch (NumberFormatException e) {
			this.rssi = -100.0;
			Log.e(LOG_TAG, "Error parsing tag RSSI!");
		}
	}
	
	public String getTagId() {
		return tagId;
	}
	
	public double getRssi() {
		return rssi;
	}
	
	public String toString() {
		return tagId + " @ " + rssi + "DB";
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Tag) {
			if (this.getTagId().equals(((Tag)obj).getTagId())) {
				return true;
			}
		}
		return false;
	}
}
