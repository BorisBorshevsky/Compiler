package IC.Parser;

import java.util.Collection;

public class JoinStringsHelper {

	public static String joinStrings(Collection<?> parts) {
		return joinStrings(parts, ", ");
	}

	public static String joinStrings(Collection<?> parts, String delimeter) {
		String joined = "";
		int count = 0;
		for (Object part : parts) {
			if (count > 0)
				joined += delimeter;
			joined += part;
			++count;
		}
		return joined;
	}

}
