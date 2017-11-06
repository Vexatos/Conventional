package vexatos.conventional.integration.chiselsandbits;

import mod.chiselsandbits.api.EventBlockBitModification;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static vexatos.conventional.Conventional.config;
import static vexatos.conventional.Conventional.isAdventureMode;

/**
 * @author Vexatos
 */
public class ChiselsBitsHandler {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onChisel(EventBlockBitModification event) {
		if(isAdventureMode(event.getPlayer())) {
			if(event.isPlacing()) {
				if(!config.mayRightclick(event.getPlayer(), event.getWorld(), event.getPos())
					|| !config.mayRightclick(event.getPlayer(), event.getItemUsed())) {
					event.setCanceled(true);
				}
			} else {
				if(!config.mayBreak(event.getPlayer(), event.getWorld(), event.getPos())) {
					event.setCanceled(true);
				}
			}
		}
	}
}
