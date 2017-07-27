package za.co.tagtron.tagtronuhf;

import java.math.BigInteger;

public class StringUtils {

	public static String convertUtf8ToHexStr(String arg) {
	    return String.format("%040x", new BigInteger(1, arg.getBytes())).substring(0, 40);
	}
	
	public static String convertHexStrToUtf8(String arg) {
		return new String(new BigInteger(arg, 16).toByteArray());
	}
}
