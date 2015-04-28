package IC.Parser;

import java.util.Collection;

public class StringUtils {

	public static String joinStrings(Collection<?> parts) {
		return joinStrings(parts, ", ");
	}

	private static String joinStrings(Collection<?> parts, String delimeter) {
		String joined = "";
		int count = 0;
		for (Object part : parts) {
			if (count > 0)
				joined += ", ";
			joined += part;
			++count;
		}
		return joined;
	}

}
