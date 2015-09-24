package com.Utils;

import java.util.ArrayList;
import java.util.Map;

public class TextUtils {
	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	public static boolean isValidEmail(final String hex) {
		return hex.matches(EMAIL_PATTERN);
	}

	public static int parseNullSafeInteger(String numberString) {
		int number = 0;
		try {
			Integer.parseInt(numberString);
		} catch (NumberFormatException e) {
		} catch (Exception e) {
		}
		return number;
	}

	public static <T> boolean isNull(T t) {
		return t == null;
	}

	public static boolean isNullOrEmpty(String s) {
		return (s == null) || (s.length() == 0) || (s.equalsIgnoreCase("null"));
	}

	public static <T> boolean isNullOrEmpty(ArrayList<T> a) {
		return (a == null) || (a.size() == 0);
	}

	public static <T, Y> boolean isNullOrEmpty(Map<T, Y> m) {
		return (m == null) || (m.size() == 0);
	}

	public static String convert4BytesString(String thString) {
		String fbString = "";
		for (int i = 0; i < thString.length(); ++i) {
			char c = thString.charAt(i);
			// If there's a char left, we chan check if the current and the next char
			// form a surrogate pair
			if (i < thString.length() - 1 && Character.isSurrogatePair(c, thString.charAt(i + 1))) {
				// if so, the codepoint must be stored on a 32bit int as char is only 16bit
				int codePoint = thString.codePointAt(i);
				// show the code point and the char
				fbString += String.format("%6d:%s", codePoint, new String(new int[] { codePoint }, 0, 1));
				++i;
			}
			// else this can only be a "normal" char
			else {
				fbString += String.format("%6d:%s", (int) c, c);
			}
		}

		return fbString;
	}
}
