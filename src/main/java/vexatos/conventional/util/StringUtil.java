package vexatos.conventional.util;

import java.util.ArrayList;

/**
 * @author Vexatos
 */
public class StringUtil {

	public static String[] dropArgs(String[] args, int count) {
		ArrayList<String> list = new ArrayList<String>();
		for(String s : args) {
			if(count > 0) {
				count--;
			} else {
				list.add(s);
			}
		}
		return list.toArray(new String[list.size()]);
	}
}
