package vexatos.conventional.reference;

import cpw.mods.fml.common.Loader;

/**
 * @author Vexatos
 */
public class Mods {

	//The mod itself
	public static final String
		Conventional = "Conventional";

	public static boolean isLoaded(String name) {
		return Loader.isModLoaded(name);
	}
}
