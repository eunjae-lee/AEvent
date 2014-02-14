package net.eunjae.android.aevent.util;

public class CompareUtil {
	public static boolean isSame(Object obj1, Object obj2) {
		if (obj1 == null && obj2 == null) {
			return true;
		} else if (obj1 != null && obj2 != null) {
			return obj1.equals(obj2);
		} else {
			return false;
		}
	}

	public static boolean isSame(Object[] obj1, Object[] obj2) {
		if (obj1 == null && obj2 == null) {
			return true;
		} else if (obj1 != null && obj2 != null) {
			if (obj1.length == obj2.length) {
				for (int i = 0; i < obj1.length; i++) {
					if (!obj1[i].equals(obj2[i])) {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public static boolean isDifferent(String str1, String str2) {
		return !isSame(str1, str2);
	}

	public static boolean isSame(String str1, String str2) {
		if (str1 == null && str2 == null) {
			return true;
		} else if (str1 != null && str2 != null) {
			return str1.equals(str2);
		} else {
			return false;
		}
	}
}

