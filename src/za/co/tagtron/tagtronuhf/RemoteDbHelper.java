package za.co.tagtron.tagtronuhf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpledb.*;
import com.amazonaws.services.simpledb.model.*;

public class RemoteDbHelper {
	private static final String LOG_TAG = "RemoteDB";
	private static final String ACCESS_KEY_ID = "AKIAJHAWTL36GAELBFIQ";
	private static final String SECRET_KEY = "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";
	
	private static final String ATTR_JOB_ID = "JobId";
    private static final String ATTR_NAME = "Name";
    private static final String ATTR_JOB_DATE = "JobDate";
    private static final String ATTR_LAST_INSPECTION_DATE = "LastInspectionDate";
    private static final String ATTR_LAST_INSPECTION_STATUS = "LastInspectionStatus";
    private static final String ATTR_TAG_ID = "TagId";
    private static final String ATTR_INSPECTION_DATE = "InspectionDate";
    private static final String	ATTR_INSPECTOR_NAME = "InspectorName";
    private static final String ATTR_GPS_COORDS = "GPSCoords";
    private static final String ATTR_INSPECTION_STATUS = "InspectionStatus";
    private static final String STATUS_NEW = "New";

    private static String dbDomain;
	private static String dbInspectionsDomain;

	public static void setDomainName(String domainName) {
		dbDomain = domainName;
		dbInspectionsDomain = dbDomain + "Inspections";
	}
	
    public void write(RepairData... rd) {
    	if (rd.length > 0) {
    		new BatchWriteTask().execute(rd);
    	}
    }
    
    public void update(InspectionData... id) {
    	if (id.length > 0) {
    		new UpdateTask().execute(id);
    	}
    }
    
//    public void sync(TagDbHelper helper) {
//    	new SyncTask().execute(helper);
//    }
    
	private static void writeToRemoteDb(RepairData... data) {
		AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_KEY );
		AmazonSimpleDBClient sdbClient = new AmazonSimpleDBClient(credentials);
		sdbClient.setRegion(Region.getRegion(Regions.EU_WEST_1));

		Log.i(LOG_TAG, "Writing " + data.length + " items to remote DB...");
		List<ReplaceableItem> items = new ArrayList<ReplaceableItem>();
		for (RepairData rd : data) {
			if (rd == null) {
				continue;
			}
			List<ReplaceableAttribute> attrs = new ArrayList<ReplaceableAttribute>();
			attrs.add(new ReplaceableAttribute(ATTR_NAME, rd.getTechnicianName(), Boolean.TRUE));
			attrs.add(new ReplaceableAttribute(ATTR_JOB_DATE, rd.getRepairDateStr(), Boolean.TRUE));
			attrs.add(new ReplaceableAttribute(ATTR_LAST_INSPECTION_DATE, rd.getLastInspectionDateStr(), Boolean.TRUE));
			attrs.add(new ReplaceableAttribute(ATTR_LAST_INSPECTION_STATUS, STATUS_NEW, Boolean.TRUE));
			for (Tag t : rd.getAllTags()) {
				attrs.add(new ReplaceableAttribute(ATTR_TAG_ID, t.getTagId(), Boolean.FALSE));
			}

			ReplaceableItem item = new ReplaceableItem().withName(rd.getJobId()).withAttributes(attrs);
			items.add(item);
		}				
		BatchPutAttributesRequest bpar = new BatchPutAttributesRequest(dbDomain, items);
		sdbClient.batchPutAttributes(bpar);
	}
	
	private static void updateRemoteItem(InspectionData... data) {
		AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_KEY );
		AmazonSimpleDBClient sdbClient = new AmazonSimpleDBClient(credentials);
		sdbClient.setRegion(Region.getRegion(Regions.EU_WEST_1));

		Log.i(LOG_TAG, "Updating " + data.length + " items in remote DB...");
		List<ReplaceableItem> items = new ArrayList<ReplaceableItem>();
		List<ReplaceableItem> inspectionItems = new ArrayList<ReplaceableItem>();
		for (InspectionData id : data) {
			List<ReplaceableAttribute> attrs = new ArrayList<ReplaceableAttribute>();
			attrs.add(new ReplaceableAttribute(ATTR_LAST_INSPECTION_DATE, id.getInspectionDateStr(), Boolean.TRUE));
			attrs.add(new ReplaceableAttribute(ATTR_LAST_INSPECTION_STATUS, id.getStatus(), Boolean.TRUE));
			ReplaceableItem item = new ReplaceableItem().withName(id.getJobId()).withAttributes(attrs);
			items.add(item);
			
			List<ReplaceableAttribute> inspectionAttrs = new ArrayList<ReplaceableAttribute>();
			inspectionAttrs.add(new ReplaceableAttribute(ATTR_JOB_ID, id.getJobId(), Boolean.FALSE));
			inspectionAttrs.add(new ReplaceableAttribute(ATTR_INSPECTION_DATE, id.getInspectionDateTimeStr(), Boolean.FALSE));
			inspectionAttrs.add(new ReplaceableAttribute(ATTR_INSPECTOR_NAME, id.getInspectorName(), Boolean.FALSE));
			inspectionAttrs.add(new ReplaceableAttribute(ATTR_GPS_COORDS, id.getGpsCoords(), Boolean.FALSE));
			inspectionAttrs.add(new ReplaceableAttribute(ATTR_INSPECTION_STATUS, id.getStatus(), Boolean.FALSE));
			ReplaceableItem inspectionItem = new ReplaceableItem().withName(id.getInspectionId()).withAttributes(inspectionAttrs);
			inspectionItems.add(inspectionItem);
		}
		BatchPutAttributesRequest bpar = new BatchPutAttributesRequest(dbDomain, items);
		sdbClient.batchPutAttributes(bpar);
		
		bpar = new BatchPutAttributesRequest(dbInspectionsDomain, inspectionItems);
		sdbClient.batchPutAttributes(bpar);
	}
	
	public static RepairData readFromRemoteDb(String tagId) throws RuntimeException {
		AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_KEY );
		AmazonSimpleDBClient sdbClient = new AmazonSimpleDBClient(credentials);
		sdbClient.setRegion(Region.getRegion(Regions.EU_WEST_1));
		
		String select = "SELECT * FROM " + dbDomain + " WHERE TagId = '" + tagId + "'";
		SelectRequest selReq = new SelectRequest().withSelectExpression(select);
		
		SelectResult result = sdbClient.select(selReq);
		List<Item> items = result.getItems();
		if (items.size() >= 1) {
			Item item = items.get(0);
			
			String jobId = item.getName();
			String name = "";
			long jobDate = 0;
			long inspectionDate = 0;
			String lastInspectionStatus = "";
			List<String> tagIds = new ArrayList<String>();
			
			List<Attribute> attrs = item.getAttributes();
			for (Attribute attr : attrs) {
				String attrName = attr.getName();
				if (attrName.equals(ATTR_NAME)) {
					name = attr.getValue();
				}
				else if (attrName.equals(ATTR_JOB_DATE)) {
					String jobDateStr = attr.getValue();
					if (jobDateStr.equals("-")) {
						jobDate = 0;
					}
					else {
						SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
						Date d;
						try {
							d = f.parse(jobDateStr);
						} catch (ParseException e) {
							e.printStackTrace();
							return null;
						}
						jobDate = d.getTime();
					}
				}
				else if (attrName.equals(ATTR_LAST_INSPECTION_DATE)) {
					String inspDateStr = attr.getValue();
					if (inspDateStr.equals("-")) {
						inspectionDate = 0;
					}
					else {
						SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
						Date d;
						try {
							d = f.parse(inspDateStr);
						} catch (ParseException e) {
							e.printStackTrace();
							return null;
						}
						inspectionDate = d.getTime();
					}
				}
				else if (attrName.equals(ATTR_LAST_INSPECTION_STATUS)) {
					lastInspectionStatus = attr.getValue();
				}
				else if (attrName.equals(ATTR_TAG_ID)) {
					tagIds.add(attr.getValue());
				}
			}
			return new RepairData(jobId, name, jobDate, inspectionDate, lastInspectionStatus, tagIds.toArray(new String[0]));
		}
		return null;
	}
	
//	private static boolean syncDb(TagDbHelper dbHelper) {
//		AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_KEY );
//		AmazonSimpleDBClient sdbClient = new AmazonSimpleDBClient(credentials);
//		sdbClient.setRegion(Region.getRegion(Regions.EU_WEST_1));
//
//		boolean updated = false;
//		List<RepairData> items = dbHelper.getUnsyncedRepairEntries();
//		if (items.size() > 0) {
//			writeToRemoteDb(items.toArray(new RepairData[0]));
//
//			for (RepairData item : items) {
//				dbHelper.markTagAsSynced(item.getJobId());
//			}
//			updated = true;
//		}
//		
//		List<InspectionData> inspections = dbHelper.getUnsyncedInspectionEntries();
//		if (inspections.size() > 0) {
//			updateRemoteItem(inspections.toArray(new InspectionData[inspections.size()]));
//			dbHelper.markAllInspectionsAsSynced();
//			updated = true;
//		}
//		return updated;
//		
//		//TODO: read items
//		// We don't really need to sync items to the local DB, as they are fetched as
//		// needed when the user scans a tag.
//	}
	
	private class BatchWriteTask extends AsyncTask<RepairData, Void, Boolean> {
        @Override
        protected Boolean doInBackground(RepairData... rd) {
            try {
            	RemoteDbHelper.writeToRemoteDb(rd);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Unable to write to remote DB!");
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
        	if (result) {
        		Log.i(LOG_TAG, "Batch write success.");
        	}
        }
    }
	
	private class UpdateTask extends AsyncTask<InspectionData, Void, Boolean> {
        @Override
        protected Boolean doInBackground(InspectionData... rd) {
            try {
            	RemoteDbHelper.updateRemoteItem(rd);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Unable to write to remote DB!");
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
        	if (result) {
        		Log.i(LOG_TAG, "Update success.");
        	}
        }
    }
}
