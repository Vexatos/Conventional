package vexatos.conventional.util.storage;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * @author Vexatos
 */
public class ConfigStorage {

	public ArrayList<AreaStorage> areas = new ArrayList<>();

	public static final class Token extends TypeToken<ConfigStorage> {

	}
}
