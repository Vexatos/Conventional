package vexatos.conventional.util;

import java.util.Arrays;

/**
 * @author Vexatos
 */
public class StringUtil {

	public static String[] dropArgs(String[] args, int count) {
		return Arrays.copyOfRange(args, count, args.length);
	}
}
