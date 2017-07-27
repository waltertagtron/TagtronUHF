package za.co.tagtron.tagtronuhf;

import java.util.Calendar;

public class Utils {

	public static boolean isToday(long timestamp) {
		Calendar now = Calendar.getInstance();
		Calendar other = Calendar.getInstance();
		other.setTimeInMillis(timestamp);
		
		if (now.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)) {
			return true;
		}
		return false;
	}
}
