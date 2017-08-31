package vexatos.conventional.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * This event can be posted to the {@code MinecraftForge.EVENT_BUS}
 * to check whether a player has been granted permission by Conventional.
 * Permissions can be any string and are granted in the config file,
 * or using the {@code /cv allow} command.
 * <br/>
 * If this event is not canceled, the permission has been granted.
 * @author Vexatos
 */
@Cancelable
public class PermissionEvent extends Event {

	public final String id;
	public final EntityPlayer player;

	public PermissionEvent(final String id, final EntityPlayer player) {
		this.id = id;
		this.player = player;
	}
}
