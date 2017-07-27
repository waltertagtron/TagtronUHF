package za.co.tagtron.tagtronuhf;

import java.util.ArrayList;
import java.util.List;

import za.co.tagtron.tagtronuhf.TagRecordContract.TagRecord;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TagDbHelper extends SQLiteOpenHelper {
	public static final String LOG_TAG = "DbHelper";
    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "TagRecords.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String LONG_TYPE = " INTEGER";
    private static final String BOOLEAN_TYPE = " BOOLEAN";
    private static final String NOT_NULL = " NOT NULL";
    private static final String COMMA_SEP = ",";
    private static final String UNIQUE = " UNIQUE";
    private static final String SQL_CREATE_ENTRIES_TABLE =
        "CREATE TABLE " + TagRecord.TABLE_NAME_TAGS + " (" +
        TagRecord._ID + " INTEGER PRIMARY KEY," +
        TagRecord.COLUMN_NAME_JOB_ID + TEXT_TYPE + NOT_NULL + UNIQUE + COMMA_SEP +
        TagRecord.COLUMN_NAME_JOB_DATE + LONG_TYPE + NOT_NULL + COMMA_SEP +
        TagRecord.COLUMN_NAME_LAST_UPDATE_DATE + LONG_TYPE + COMMA_SEP +
        TagRecord.COLUMN_NAME_STATUS + TEXT_TYPE + COMMA_SEP +
        TagRecord.COLUMN_NAME_TECH_NAME + TEXT_TYPE + COMMA_SEP +
        TagRecord.COLUMN_NAME_TAG_UII_0 + TEXT_TYPE + NOT_NULL + COMMA_SEP +
        TagRecord.COLUMN_NAME_TAG_UII_1 + TEXT_TYPE + COMMA_SEP +
        TagRecord.COLUMN_NAME_TAG_UII_2 + TEXT_TYPE + COMMA_SEP +
        TagRecord.COLUMN_NAME_TAG_UII_3 + TEXT_TYPE + COMMA_SEP +
        TagRecord.COLUMN_NAME_SYNCED + BOOLEAN_TYPE + NOT_NULL +
        " )";

    private static final String SQL_CREATE_INSPECTIONS_TABLE =
        "CREATE TABLE " + TagRecord.TABLE_NAME_INSPECTIONS + " (" +
        TagRecord._ID + " INTEGER PRIMARY KEY," +
        TagRecord.COLUMN_NAME_JOB_ID + TEXT_TYPE + COMMA_SEP +
        TagRecord.COLUMN_NAME_INSPECTION_DATE + LONG_TYPE + COMMA_SEP +
        TagRecord.COLUMN_NAME_INSPECTOR_NAME + TEXT_TYPE + COMMA_SEP +
        TagRecord.COLUMN_NAME_INSPECTION_STATUS + TEXT_TYPE + COMMA_SEP +
        TagRecord.COLUMN_NAME_GPS_COORDINATES + TEXT_TYPE + COMMA_SEP +
        TagRecord.COLUMN_NAME_SYNCED + BOOLEAN_TYPE + NOT_NULL +
        " )";
    
    private static final String SQL_DROP_TABLE_TAGS = "DROP TABLE IF EXISTS " + TagRecord.TABLE_NAME_TAGS;
    private static final String SQL_DROP_TABLE_INSPECTIONS = "DROP TABLE IF EXISTS " + TagRecord.TABLE_NAME_INSPECTIONS;
    
    public TagDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public void onCreate(SQLiteDatabase db) {
    	Log.i(LOG_TAG, "Creating DB table...");
        db.execSQL(SQL_CREATE_ENTRIES_TABLE);
        db.execSQL(SQL_CREATE_INSPECTIONS_TABLE);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
    	Log.i(LOG_TAG, "Upgrading DB to version " + newVersion);
        db.execSQL(SQL_DROP_TABLE_TAGS);
        db.execSQL(SQL_DROP_TABLE_INSPECTIONS);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //onUpgrade(db, oldVersion, newVersion);
    }
    
    public void deleteDb() {
    	getWritableDatabase().execSQL(SQL_DROP_TABLE_TAGS);
    	getWritableDatabase().execSQL(SQL_DROP_TABLE_INSPECTIONS);
    }
    
    public long insertRepairData(RepairData record, boolean isSynced) {
    	SQLiteDatabase db;
    	try {
    		db = getWritableDatabase();
    	}
    	catch (SQLiteException e) {
    		Log.e(LOG_TAG, "Could not open database - " + e.getMessage());
    		return -1;
    	}
    	
    	ContentValues values = new ContentValues();
    	values.put(TagRecord.COLUMN_NAME_JOB_ID, record.getJobId());
    	values.put(TagRecord.COLUMN_NAME_JOB_DATE, record.getCreationTimestamp());
    	values.put(TagRecord.COLUMN_NAME_LAST_UPDATE_DATE, record.getLastInspectionTimestamp());
    	values.put(TagRecord.COLUMN_NAME_STATUS, record.getStatus());
    	values.put(TagRecord.COLUMN_NAME_TECH_NAME, record.getTechnicianName());
    	values.put(TagRecord.COLUMN_NAME_SYNCED, isSynced ? 1 : 0);
    	values.put(TagRecord.COLUMN_NAME_TAG_UII_0, record.getTagId(0));
    	values.put(TagRecord.COLUMN_NAME_TAG_UII_1, record.getTagId(1));
    	values.put(TagRecord.COLUMN_NAME_TAG_UII_2, record.getTagId(2));
    	values.put(TagRecord.COLUMN_NAME_TAG_UII_3, record.getTagId(3));

    	try {
    		long newRowId = db.insertWithOnConflict(TagRecord.TABLE_NAME_TAGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    		return newRowId;
    	}
    	finally {
    		db.close();
    	}
    }
    
    public boolean updateInspectionDate(String jobId, long timestamp, String status) {
    	SQLiteDatabase db;
    	try {
    		db = getWritableDatabase();
    	}
    	catch (SQLiteException e) {
    		Log.e(LOG_TAG, "Could not open database - " + e.getMessage());
    		return false;
    	}
    	
    	ContentValues values = new ContentValues();
    	values.put(TagRecord.COLUMN_NAME_LAST_UPDATE_DATE, timestamp);
    	values.put(TagRecord.COLUMN_NAME_STATUS, status);
    	
    	String selection = TagRecord.COLUMN_NAME_JOB_ID + "=?";
    	String[] selectionArgs = { jobId };
    	
    	try {
    		int count = db.update(TagRecord.TABLE_NAME_TAGS, values, selection, selectionArgs);
    		return count > 0;
    	}
    	finally {
    		db.close();
    	}
    }
    
    public RepairData getTagEntry(String tagId) {
    	SQLiteDatabase db;
    	try {
    		db = getReadableDatabase();
    	}
    	catch (SQLiteException e) {
    		Log.e(LOG_TAG, "Could not open database - " + e.getMessage());
    		return null;
    	}
    	
    	String[] columns = { TagRecord.COLUMN_NAME_JOB_ID,
    						 TagRecord.COLUMN_NAME_TECH_NAME,
    						 TagRecord.COLUMN_NAME_JOB_DATE,
    						 TagRecord.COLUMN_NAME_LAST_UPDATE_DATE,
    						 TagRecord.COLUMN_NAME_STATUS,
    						 TagRecord.COLUMN_NAME_TAG_UII_0,
    						 TagRecord.COLUMN_NAME_TAG_UII_1,
    						 TagRecord.COLUMN_NAME_TAG_UII_2,
    						 TagRecord.COLUMN_NAME_TAG_UII_3 };
    	String selection = TagRecord.COLUMN_NAME_TAG_UII_0 + "=? OR " +
    					   TagRecord.COLUMN_NAME_TAG_UII_1 + "=? OR " +
    					   TagRecord.COLUMN_NAME_TAG_UII_2 + "=? OR " +
    					   TagRecord.COLUMN_NAME_TAG_UII_3 + "=?";
    			
    	String[] selectionArgs = { tagId, tagId, tagId, tagId };
    	
    	try {
    		Cursor cursor = db.query(TagRecord.TABLE_NAME_TAGS, columns, selection, selectionArgs, null, null, null);
	    	if (cursor.moveToFirst()) {
	    		int jobIdIndex = cursor.getColumnIndex(TagRecord.COLUMN_NAME_JOB_ID);
	    		int nameIndex = cursor.getColumnIndex(TagRecord.COLUMN_NAME_TECH_NAME);
	    		int jobDateIndex = cursor.getColumnIndex(TagRecord.COLUMN_NAME_JOB_DATE);
	    		int updateIndex = cursor.getColumnIndex(TagRecord.COLUMN_NAME_LAST_UPDATE_DATE);
	    		int statusIndex = cursor.getColumnIndex(TagRecord.COLUMN_NAME_STATUS);
	    		int tagId0Index = cursor.getColumnIndex(TagRecord.COLUMN_NAME_TAG_UII_0);
	    		int tagId1Index = cursor.getColumnIndex(TagRecord.COLUMN_NAME_TAG_UII_1);
	    		int tagId2Index = cursor.getColumnIndex(TagRecord.COLUMN_NAME_TAG_UII_2);
	    		int tagId3Index = cursor.getColumnIndex(TagRecord.COLUMN_NAME_TAG_UII_3);
	    		
	    		String jobId = cursor.getString(jobIdIndex);
	    		String name = cursor.getString(nameIndex);
	    		long jobTimestamp = cursor.getLong(jobDateIndex);
	    		long updateTimestamp = cursor.getLong(updateIndex);
	    		String status = cursor.getString(statusIndex);
	    		String tagId0 = cursor.getString(tagId0Index);
	    		String tagId1 = cursor.getString(tagId1Index);
	    		String tagId2 = cursor.getString(tagId2Index);
	    		String tagId3 = cursor.getString(tagId3Index);
	    		
	    		RepairData rd = new RepairData(jobId, name, jobTimestamp, updateTimestamp, status);
	    		if (!tagId0.isEmpty()) { rd.addTag(tagId0); }
	    		if (!tagId1.isEmpty()) { rd.addTag(tagId1); }
	    		if (!tagId2.isEmpty()) { rd.addTag(tagId2); }
	    		if (!tagId3.isEmpty()) { rd.addTag(tagId3); }
	    		return rd;
	    	}
    	}
    	finally {
    		db.close();
    	}
    	return null;
    }
    
    public List<RepairData> getUnsyncedRepairEntries() {
    	SQLiteDatabase db;
    	try {
    		db = getReadableDatabase();
    	}
    	catch (SQLiteException e) {
    		Log.e(LOG_TAG, "Could not open database - " + e.getMessage());
    		return null;
    	}
    	
    	String selection = TagRecord.COLUMN_NAME_SYNCED + "=?";
    	String[] selectionArgs = { "0" };
    
    	List<RepairData> items = new ArrayList<RepairData>();
    	try {
	    	Cursor cursor = db.query(TagRecord.TABLE_NAME_TAGS, null, selection, selectionArgs, null, null, null);
	    	if (cursor.moveToFirst()) {
	    		int jobIdIndex = cursor.getColumnIndex(TagRecord.COLUMN_NAME_JOB_ID);
	    		int nameIndex = cursor.getColumnIndex(TagRecord.COLUMN_NAME_TECH_NAME);
	    		int jobDateIndex = cursor.getColumnIndex(TagRecord.COLUMN_NAME_JOB_DATE);
	    		int updateIndex = cursor.getColumnIndex(TagRecord.COLUMN_NAME_LAST_UPDATE_DATE);
	    		int statusIndex = cursor.getColumnIndex(TagRecord.COLUMN_NAME_STATUS);
	    		int tagId0Index = cursor.getColumnIndex(TagRecord.COLUMN_NAME_TAG_UII_0);
	    		int tagId1Index = cursor.getColumnIndex(TagRecord.COLUMN_NAME_TAG_UII_1);
	    		int tagId2Index = cursor.getColumnIndex(TagRecord.COLUMN_NAME_TAG_UII_2);
	    		int tagId3Index = cursor.getColumnIndex(TagRecord.COLUMN_NAME_TAG_UII_3);
	    		
	    		String jobId = cursor.getString(jobIdIndex);
	    		String name = cursor.getString(nameIndex);
	    		long jobTimestamp = cursor.getLong(jobDateIndex);
	    		long updateTimestamp = cursor.getLong(updateIndex);
	    		String status = cursor.getString(statusIndex);
	    		String tagId0 = cursor.getString(tagId0Index);
	    		String tagId1 = cursor.getString(tagId1Index);
	    		String tagId2 = cursor.getString(tagId2Index);
	    		String tagId3 = cursor.getString(tagId3Index);
	    		
	    		RepairData rd = new RepairData(jobId, name, jobTimestamp, updateTimestamp, status);
	    		if (!tagId0.isEmpty()) { rd.addTag(tagId0); }
	    		if (!tagId1.isEmpty()) { rd.addTag(tagId1); }
	    		if (!tagId2.isEmpty()) { rd.addTag(tagId2); }
	    		if (!tagId3.isEmpty()) { rd.addTag(tagId3); }
	    		items.add(rd);
	    	}
    	}
    	finally {
    		db.close();
    	}
    	return items;
    }
    
    public boolean markTagAsSynced(String jobId) {
    	SQLiteDatabase db;
    	try {
    		db = getReadableDatabase();
    	}
    	catch (SQLiteException e) {
    		Log.e(LOG_TAG, "Could not open database - " + e.getMessage());
    		return false;
    	}
    	
    	ContentValues values = new ContentValues();
    	values.put(TagRecord.COLUMN_NAME_SYNCED, 1);
    	String selection = TagRecord.COLUMN_NAME_JOB_ID + "=?";
    	String[] selectionArgs = { jobId };
    	
    	try {
    		int count = db.update(TagRecord.TABLE_NAME_TAGS, values, selection, selectionArgs);
    		return count > 0;
    	}
    	finally {
    		db.close();
    	}
    }

    /******** Inspections ********/
    
    public long insertInspectionEntry(InspectionData record) {
    	SQLiteDatabase db;
    	try {
    		db = getWritableDatabase();
    	}
    	catch (SQLiteException e) {
    		Log.e(LOG_TAG, "Could not open database - " + e.getMessage());
    		return -1;
    	}
    	
    	ContentValues values = new ContentValues();
    	values.put(TagRecord.COLUMN_NAME_JOB_ID, record.getJobId());
    	values.put(TagRecord.COLUMN_NAME_INSPECTION_DATE, record.getTimestamp());
    	values.put(TagRecord.COLUMN_NAME_INSPECTOR_NAME, record.getInspectorName());
    	values.put(TagRecord.COLUMN_NAME_INSPECTION_STATUS, record.getStatus());
    	values.put(TagRecord.COLUMN_NAME_GPS_COORDINATES, record.getGpsCoords());
    	values.put(TagRecord.COLUMN_NAME_SYNCED, 0);

    	try {
    		long newRowId = db.insertWithOnConflict(TagRecord.TABLE_NAME_INSPECTIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    		return newRowId;
    	}
    	finally {
    		db.close();
    	}
    }

    public List<InspectionData> getUnsyncedInspectionEntries() {
    	SQLiteDatabase db;
    	try {
    		db = getReadableDatabase();
    	}
    	catch (SQLiteException e) {
    		Log.e(LOG_TAG, "Could not open database - " + e.getMessage());
    		return null;
    	}
    	
    	String selection = TagRecord.COLUMN_NAME_SYNCED + "=?";
    	String[] selectionArgs = { "0" };
    
    	List<InspectionData> items = new ArrayList<InspectionData>();
    	try {
	    	Cursor cursor = db.query(TagRecord.TABLE_NAME_INSPECTIONS, null, selection, selectionArgs, null, null, null);
	    	if (cursor.moveToFirst()) {
	    		int jobIdIndex = cursor.getColumnIndex(TagRecord.COLUMN_NAME_JOB_ID);
	    		int dateIndex = cursor.getColumnIndex(TagRecord.COLUMN_NAME_INSPECTION_DATE);
	    		int nameIndex = cursor.getColumnIndex(TagRecord.COLUMN_NAME_INSPECTOR_NAME);
	    		int statusIndex = cursor.getColumnIndex(TagRecord.COLUMN_NAME_INSPECTION_STATUS);
	    		int gpsIndex = cursor.getColumnIndex(TagRecord.COLUMN_NAME_GPS_COORDINATES);
	    		String jobId = cursor.getString(jobIdIndex);
	    		long timestamp = cursor.getLong(dateIndex);
	    		String name = cursor.getString(nameIndex);
	    		String status = cursor.getString(statusIndex);
	    		String gps = cursor.getString(gpsIndex);

	    		InspectionData id = new InspectionData(jobId, name, gps, status, timestamp);
	    		items.add(id);
	    	}
    	}
    	finally {
    		db.close();
    	}
    	return items;
    }
    
    public boolean markAllInspectionsAsSynced() {
    	SQLiteDatabase db;
    	try {
    		db = getReadableDatabase();
    	}
    	catch (SQLiteException e) {
    		Log.e(LOG_TAG, "Could not open database - " + e.getMessage());
    		return false;
    	}
    	
    	ContentValues values = new ContentValues();
    	values.put(TagRecord.COLUMN_NAME_SYNCED, 1);
    	
    	try {
    		int count = db.update(TagRecord.TABLE_NAME_INSPECTIONS, values, null, null);
    		return count > 0;
    	}
    	finally {
    		db.close();
    	}
    }
}