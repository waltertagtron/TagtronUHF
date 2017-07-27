package za.co.tagtron.tagtronuhf;

import android.provider.BaseColumns;

public final class TagRecordContract {
    // To prevent someone from accidentally instantiating the contract class
    public TagRecordContract() {}

    public static abstract class TagRecord implements BaseColumns {
        public static final String TABLE_NAME_TAGS = "tag_entries";
        public static final String COLUMN_NAME_JOB_ID = "job_id";
        public static final String COLUMN_NAME_JOB_DATE = "job_date";
        public static final String COLUMN_NAME_LAST_UPDATE_DATE = "last_update_date";
        public static final String COLUMN_NAME_TECH_NAME = "technician_name";
        public static final String COLUMN_NAME_SHAFT_NO = "shaft_no";
        public static final String COLUMN_NAME_NOTES = "notes";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_SYNCED = "synced";
        public static final String COLUMN_NAME_TAG_UII_0 = "tag_uii_0";
        public static final String COLUMN_NAME_TAG_UII_1 = "tag_uii_1";
        public static final String COLUMN_NAME_TAG_UII_2 = "tag_uii_2";
        public static final String COLUMN_NAME_TAG_UII_3 = "tag_uii_3";
        
        
        public static final String TABLE_NAME_INSPECTIONS = "inspections";
        public static final String COLUMN_NAME_INSPECTOR_NAME = "inspector_name";
        public static final String COLUMN_NAME_INSPECTION_DATE = "inspection_date";
        public static final String COLUMN_NAME_INSPECTION_STATUS = "inspection_status";
        public static final String COLUMN_NAME_GPS_COORDINATES = "gps_coordinates";
    }
}