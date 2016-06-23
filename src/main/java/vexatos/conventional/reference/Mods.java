package vexatos.conventional.reference;

import net.minecraftforge.fml.common.Loader;

/**
 * @author Vexatos
 */
public class Mods {

	// The mod itself
	public static final String
		Conventional = "Conventional";

	// Other mods
	public static final String
		ChiselsBits = "chiselsandbits";

	public static boolean isLoaded(String name) {
		return Loader.isModLoaded(name);
	}
}
