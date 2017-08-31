package vexatos.conventional.event;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.ArrayList;
import java.util.List;

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

	public static class Registry {

		private static final List<String> IDs = new ArrayList<>();

		/**
		 * Register a string ID to use. This is entirely optional
		 * and only used for Tab completion in the commands.
		 * @param id The permission ID to register
		 * @return <tt>true</tt> if the ID has not already been registered yet.
		 */
		public static boolean register(final String id) {
			return !IDs.contains(id) && IDs.add(id);
		}

		public static ImmutableList<String> getIDs() {
			return ImmutableList.copyOf(IDs);
		}
	}
}
