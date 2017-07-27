package za.co.tagtron.tagtronuhf;

import java.text.SimpleDateFormat;
import java.util.Date;

public class InspectionData {
	
	private String jobId, name, gpsCoords, status;
	private long timestamp = 0;
	
	/** uii, name, company, gpsCoords, status, timestamp **/
	public InspectionData(String jobId, String name, String gpsCoords, String status, long timestamp) {
		this.jobId = jobId;
		this.name = name;
		this.gpsCoords = gpsCoords;
		this.status = status;
		this.timestamp = timestamp;
	}
	
	/** Unique id for this inspection based on timestamp **/
	public String getInspectionId() {
		return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date(timestamp)).toString();
	}
	
	public String getJobId() {
		return jobId;
	}
	
	public String getInspectorName() {
		return name;
	}
	
	public String getGpsCoords() {
		return gpsCoords;
	}
	
	public String getStatus() {
		return status;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public String getInspectionDateStr() {
		if (timestamp == 0) {
			return "-";
		}
		return new SimpleDateFormat("yyyy-MM-dd").format(new Date(timestamp)).toString();
	}
	
	public String getInspectionDateTimeStr() {
		if (timestamp == 0) {
			return "-";
		}
		return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(timestamp)).toString();
	}
}
