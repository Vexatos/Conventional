package vexatos.conventional;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import org.apache.logging.log4j.Logger;
import vexatos.conventional.command.CommandAddBlock;
import vexatos.conventional.command.CommandAddItem;
import vexatos.conventional.command.CommandList;
import vexatos.conventional.command.CommandReload;
import vexatos.conventional.command.CommandRemoveBlock;
import vexatos.conventional.command.CommandRemoveItem;
import vexatos.conventional.command.ConventionalCommand;
import vexatos.conventional.reference.Config;
import vexatos.conventional.reference.Mods;

@Mod(modid = Mods.Conventional, name = Mods.Conventional, version = "@VERSION@", acceptableRemoteVersions = "*")
public class Conventional {

	@Instance
	public static Conventional instance;

	public static Config config;
	public static Logger log;

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		log = e.getModLog();
		MinecraftForge.EVENT_BUS.register(this);
		config = new Config(new Configuration(e.getSuggestedConfigurationFile()));
		config.reload();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {
		config.save();
	}

	@EventHandler
	public void onServerStart(FMLServerAboutToStartEvent e) {
		config.reload();
	}

	@EventHandler
	public void onServerStarting(FMLServerStartingEvent e) {
		ConventionalCommand cmd = new ConventionalCommand();
		cmd.addCommand(new CommandReload());
		cmd.addCommand(new CommandList());
		ConventionalCommand addCmd = new ConventionalCommand("add");
		addCmd.addCommand(new CommandAddBlock());
		addCmd.addCommand(new CommandAddItem());
		cmd.addCommand(addCmd);
		ConventionalCommand rmvCmd = new ConventionalCommand("remove");
		rmvCmd.addCommand(new CommandRemoveBlock());
		rmvCmd.addCommand(new CommandRemoveItem());
		cmd.addCommand(rmvCmd);
		e.registerServerCommand(cmd);
	}

	@EventHandler
	public void onServerStopping(FMLServerStoppingEvent e) {
		config.save();
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPlace(PlaceEvent event) {
		if(isAdventureMode(event.player) && !config.mayRightclick(event.itemInHand)) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onBreakSpeed(BreakSpeed event) {
		if(isAdventureMode(event.entityPlayer) && !config.mayLeftclick(event.block, event.metadata)) {
			//event.setCanceled(true);
			event.newSpeed = Float.MIN_VALUE;
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onBreak(BreakEvent event) {
		if(isAdventureMode(event.getPlayer()) && !config.mayLeftclick(event.block, event.blockMetadata)) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onInteract(PlayerInteractEvent event) {
		if(event.isCanceled()) {
			return;
		}
		if(isAdventureMode(event.entityPlayer)) {
			if(event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
				/*if(!config.mayLeftclick(event.world, event.x, event.y, event.z)) {
					event.setCanceled(true);
				}*/
			} else if(event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
				final boolean
					validBlock = config.mayRightclick(event.world, event.x, event.y, event.z),
					validItem = config.mayRightclick(event.entityPlayer.getHeldItem());
				if(validBlock && validItem) {
					// Just return.
				} else if(!validBlock && !validItem) {
					event.setCanceled(true);
				} else if(validBlock) {
					event.useBlock = Result.ALLOW;
					event.useItem = Result.DENY;
				} else {
					event.useBlock = Result.DENY;
					event.useItem = Result.ALLOW;
				}
			} else if(!config.mayRightclick(event.entityPlayer.getHeldItem())) {
				event.setCanceled(true);
			}
		}
	}

	// checks for serverside and adventure mode
	private boolean isAdventureMode(EntityPlayer player) {
		/*if(player.worldObj.isRemote) {
			return isAdventureMode_Client(player);
		}*/
		return !player.worldObj.isRemote && ((EntityPlayerMP) player).theItemInWorldManager.getGameType().isAdventure();
		//return !player.worldObj.isRemote && ((EntityPlayerMP) player).theItemInWorldManager.getGameType().isSurvivalOrAdventure() && !player.canCommandSenderUseCommand(2, "cv");
	}

	/*private boolean isAdventureMode_Client(EntityPlayer player) {
		return Minecraft.getMinecraft().playerController.isNotCreative() && !player.canCommandSenderUseCommand(2, "cv");
	}*/

}
