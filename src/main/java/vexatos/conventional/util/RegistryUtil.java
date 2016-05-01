package vexatos.conventional.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

/**
 * @author Vexatos
 */
public class RegistryUtil {

	public static String getRegistryName(Block block) {
		final ResourceLocation registryName = block.getRegistryName();
		return registryName != null ? registryName.toString() : null;
	}

	public static String getRegistryName(Item item) {
		final ResourceLocation registryName = item.getRegistryName();
		return registryName != null ? registryName.toString() : null;
	}
}
