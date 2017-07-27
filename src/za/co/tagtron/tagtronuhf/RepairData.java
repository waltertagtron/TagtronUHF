package za.co.tagtron.tagtronuhf;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.text.format.DateFormat;
import android.util.Log;

public class RepairData implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final String LOG_TAG = "RepairData";
	
	private String jobId = "";
	private long repairTimestamp, lastInspectionTimestamp; 
	private String technicianName = "";
	private String shaftNo = "";
	private String notes = "";
	private String lastInspectionStatus = "";
	private List<Tag> tags;
	
	public RepairData(String jobId, String technicianName, long repairTimestamp,
			String shaftNo,	String notes) {
		this(jobId, technicianName, repairTimestamp, shaftNo, notes, 0L, "");
	}
	
	public RepairData(String jobId, String technicianName, long repairTimestamp, 
			String shaftNo, String notes, long lastInspectionTimestamp,
			String lastInspectionStatus) {
		this(jobId, technicianName, repairTimestamp, shaftNo, notes, 
				lastInspectionTimestamp, lastInspectionStatus, "");
	}
	
	public RepairData(String jobId, String technicianName, long repairTimestamp,
			String shaftNo, String notes, long lastInspectionTimestamp,
			String lastInspectionStatus, String... tagIds) {
		
		if (repairTimestamp == 0) {
			repairTimestamp = System.currentTimeMillis();
		}
		
		if (jobId == null || jobId.isEmpty()) {
			jobId = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date(repairTimestamp)).toString();
			Log.i(LOG_TAG, "New job id - " + jobId);
		}

		this.jobId = jobId;
		this.repairTimestamp = repairTimestamp;
		this.technicianName = technicianName;
		this.shaftNo = shaftNo;
		this.notes = notes;
		this.lastInspectionTimestamp = lastInspectionTimestamp;
		this.lastInspectionStatus = lastInspectionStatus;
		
		for (String tagId : tagIds) {
			if (!tagId.isEmpty()) {
				addTag(new Tag(tagId));
			}
		}
	}

	public void addTag(Tag tag) {
		if (tags == null) {
			tags = new ArrayList<Tag>();
		}
		if (!tags.contains(tag)) {
			tags.add(tag);
		}
	}
	
	public void addTag(String tagId) {
		addTag(new Tag(tagId));
	}
	
	public String getJobId() {
		return jobId;
	}
	
	public String getTechnicianName() {
		return technicianName;
	}
	
	public String getShaftNo() {
		return shaftNo;
	}
	
	public String getNotes() {
		return notes;
	}
	
	public String getRepairDateStr() {
		if (repairTimestamp == 0) {
			return "-";
		}
		return DateFormat.format("yyyy-MM-dd ", new Date(repairTimestamp)).toString();
	}
	
	public String getLastInspectionDateStr() {
		if (lastInspectionTimestamp == 0) {
			return "-";
		}
		return DateFormat.format("yyyy-MM-dd", new Date(lastInspectionTimestamp)).toString();
	}
	
	public void setLastInspectionDate(long timestamp) {
		this.lastInspectionTimestamp = timestamp;
	}
	
	public String getStatus() {
		if (lastInspectionStatus.isEmpty()) {
			return "New";
		}
		return lastInspectionStatus;
	}
	
	public void setStatus(String status) {
		this.lastInspectionStatus = status;
	}
	
	public void setShaftNo(String shaftNo) {
		this.shaftNo = shaftNo;
	}
	
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public long getCreationTimestamp() {
		return repairTimestamp;
	}
	
	public long getLastInspectionTimestamp() {
		return lastInspectionTimestamp;
	}
	
	public String getTagId(int pos) {
		if (tags.size() > pos) {
			return tags.get(pos).getTagId();
		}
		return "";
	}
	
	public List<Tag> getAllTags() {
		return tags;
	}

	@Override
	public String toString() {
		return getRepairDateStr() + " - " + getTechnicianName();
	}
}
