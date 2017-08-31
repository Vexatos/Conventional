package vexatos.conventional;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import org.apache.logging.log4j.Logger;
import vexatos.conventional.command.CommandAddBlock;
import vexatos.conventional.command.CommandAddEntity;
import vexatos.conventional.command.CommandAddItem;
import vexatos.conventional.command.CommandAllow;
import vexatos.conventional.command.CommandArea;
import vexatos.conventional.command.CommandDeny;
import vexatos.conventional.command.CommandExclude;
import vexatos.conventional.command.CommandList;
import vexatos.conventional.command.CommandReload;
import vexatos.conventional.command.CommandRemoveBlock;
import vexatos.conventional.command.CommandRemoveEntity;
import vexatos.conventional.command.CommandRemoveItem;
import vexatos.conventional.command.ConventionalCommand;
import vexatos.conventional.command.SubCommand;
import vexatos.conventional.command.area.CommandAreaCreate;
import vexatos.conventional.command.area.CommandAreaRemove;
import vexatos.conventional.command.area.CommandPosition;
import vexatos.conventional.event.PermissionEvent;
import vexatos.conventional.integration.chiselsandbits.ChiselsBitsHandler;
import vexatos.conventional.network.NetworkHandlerClient;
import vexatos.conventional.network.PacketHandler;
import vexatos.conventional.proxy.CommonProxy;
import vexatos.conventional.reference.Config;
import vexatos.conventional.reference.Mods;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Mod(modid = Mods.Conventional, name = Mods.Conventional, version = "@VERSION@", dependencies = "after:" + Mods.ChiselsBits + "@[10.9,)")
public class Conventional {

	@Instance
	public static Conventional instance;

	public static Config config;
	public static Logger log;
	public static File configDir;

	public static List<Function<Config.Area, SubCommand>> areaCommands = new ArrayList<>();

	@SidedProxy(clientSide = "vexatos.conventional.proxy.ClientProxy", serverSide = "vexatos.conventional.proxy.CommonProxy")
	public static CommonProxy proxy;

	public static PacketHandler packet;

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		log = e.getModLog();
		configDir = e.getModConfigurationDirectory();
		MinecraftForge.EVENT_BUS.register(this);
		if(Mods.isLoaded(Mods.ChiselsBits)) {
			MinecraftForge.EVENT_BUS.register(new ChiselsBitsHandler());
		}
		//FMLCommonHandler.instance().bus().register(this);
		//config = new Config(new Configuration(e.getSuggestedConfigurationFile()));
		config = new Config();

		areaCommands.add(a -> new CommandList(() -> a));
		areaCommands.add(a -> {
			ConventionalCommand addCmd = new ConventionalCommand("add");
			addCmd.addCommand(new CommandAddBlock(() -> a));
			addCmd.addCommand(new CommandAddItem(() -> a));
			addCmd.addCommand(new CommandAddEntity(() -> a));
			return addCmd;
		});
		areaCommands.add(a -> {
			ConventionalCommand rmvCmd = new ConventionalCommand("remove");
			rmvCmd.addCommand(new CommandRemoveBlock(() -> a));
			rmvCmd.addCommand(new CommandRemoveItem(() -> a));
			rmvCmd.addCommand(new CommandRemoveEntity(() -> a));
			return rmvCmd;
		});
		areaCommands.add(a -> new CommandAllow(() -> a));
		areaCommands.add(a -> new CommandDeny(() -> a));
	}

	@EventHandler
	public void init(FMLInitializationEvent e) {
		packet = new PacketHandler(Mods.Conventional, new NetworkHandlerClient(), null);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {
		config.save(false); // generate config file if it doesn't exist yet.
	}

	@EventHandler
	public void onServerStart(FMLServerAboutToStartEvent e) {
		config.reloadFromFile();
	}

	@EventHandler
	public void onServerStarting(FMLServerStartingEvent e) {
		ConventionalCommand cmd = new ConventionalCommand("cv");
		cmd.addCommand(new CommandReload());
		cmd.addCommand(new CommandList(() -> config.ALL));
		cmd.addCommand(new CommandAllow(() -> config.ALL));
		cmd.addCommand(new CommandDeny(() -> config.ALL));
		cmd.addCommand(new CommandExclude());
		ConventionalCommand addCmd = new ConventionalCommand("add");
		addCmd.addCommand(new CommandAddBlock(() -> config.ALL));
		addCmd.addCommand(new CommandAddItem(() -> config.ALL));
		addCmd.addCommand(new CommandAddEntity(() -> config.ALL));
		cmd.addCommand(addCmd);
		CommandArea areaCmd = new CommandArea();
		areaCmd.addCommand(new CommandAreaCreate(areaCmd));
		areaCmd.addCommand(new CommandAreaRemove());
		cmd.addCommand(areaCmd);
		cmd.addCommand(new CommandPosition());
		ConventionalCommand rmvCmd = new ConventionalCommand("remove");
		rmvCmd.addCommand(new CommandRemoveBlock(() -> config.ALL));
		rmvCmd.addCommand(new CommandRemoveItem(() -> config.ALL));
		rmvCmd.addCommand(new CommandRemoveEntity(() -> config.ALL));
		cmd.addCommand(rmvCmd);
		e.registerServerCommand(cmd);
	}

	@EventHandler
	public void onServerStopping(FMLServerStoppingEvent e) {
		// NO-OP
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlace(PlaceEvent event) {
		if(isAdventureMode(event.getPlayer()) && !config.mayRightclick(event.getPlayer(), event.getPlayer().getHeldItem(event.getHand()))) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onBreakSpeed(BreakSpeed event) {
		if(isAdventureMode(event.getEntityPlayer()) && !config.mayBreak(event.getEntityPlayer().world, event.getPos())) {
			//event.setCanceled(true);
			event.setNewSpeed(Float.MIN_VALUE);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onBreak(BreakEvent event) {
		if(isAdventureMode(event.getPlayer()) && !config.mayBreak(event.getWorld(), event.getPos())) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onInteract(PlayerInteractEvent event) {
		if(event.isCanceled()) {
			return;
		}
		if(isAdventureMode(event.getEntityPlayer())) {
			if(event instanceof PlayerInteractEvent.LeftClickBlock) {
				if(!config.mayLeftclick(event.getWorld(), event.getPos())) {
					event.setCanceled(true);
				}
			} else if(event instanceof PlayerInteractEvent.RightClickBlock) {
				final PlayerInteractEvent.RightClickBlock rcevent = (PlayerInteractEvent.RightClickBlock) event;
				final boolean
					validBlock = config.mayRightclick(rcevent.getWorld(), rcevent.getPos()),
					validItem = config.mayRightclick(event.getEntityPlayer(), rcevent.getItemStack());
				if(validBlock && validItem) {
					// Just return.
				} else if(!validBlock && !validItem) {
					rcevent.setCanceled(true);
				} else if(validBlock) {
					rcevent.setUseBlock(Result.ALLOW);
					rcevent.setUseItem(Result.DENY);
				} else {
					rcevent.setUseBlock(Result.DENY);
					rcevent.setUseItem(Result.ALLOW);
				}
			} else if(event instanceof PlayerInteractEvent.EntityInteract) {
				if(isAdventureMode(event.getEntityPlayer()) && !config.mayRightclick(((PlayerInteractEvent.EntityInteract) event).getTarget())) {
					event.setCanceled(true);
				}
			} else if(event instanceof PlayerInteractEvent.EntityInteractSpecific) {
				if(isAdventureMode(event.getEntityPlayer()) && !config.mayRightclick(((PlayerInteractEvent.EntityInteractSpecific) event).getTarget())) {
					event.setCanceled(true);
				}
			} else if(event.isCancelable() && !config.mayRightclick(event.getEntityPlayer(), event.getItemStack())) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityLeftclick(AttackEntityEvent event) {
		if(isAdventureMode(event.getEntityPlayer()) && !config.mayLeftclick(event.getTarget())) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onPermissionEvent(PermissionEvent event) {
		if(isAdventureMode(event.player) && !config.hasPermission(event.id, event.player)) {
			event.setCanceled(true);
		}
	}

	// checks for serverside and adventure mode
	public static boolean isAdventureMode(EntityPlayer player) {
		/*if(player.world.isRemote) {
			return isAdventureMode_Client(player);
		}*/
		//return !player.world.isRemote && ((EntityPlayerMP) player).theItemInWorldManager.getGameType().isAdventure();
		return !(player instanceof FakePlayer)
			&& !player.isCreative()
			&& !config.isExcluded(player.getName())
			/* && !player.canCommandSenderUseCommand(2, "cv")*/;
	}

	/*private boolean isAdventureMode_Client(EntityPlayer player) {
		return Minecraft.getMinecraft().playerController.isNotCreative() && !player.canCommandSenderUseCommand(2, "cv");
	}*/
	/*@SubscribeEvent
	public void logEvent(Event e) {
		String s = e.toString();
		if(s.contains("client") || e.toString().contains("Render") || s.contains("Update") || s.contains("Tick") || s.contains("Chunk") || s.contains("EntityConstructing")
			|| e instanceof InputEvent || s.contains("Spawn")) {
			return;
		}
		if(s.contains("Attack") || s.contains("Interact"))
			log.info(Thread.currentThread().getName() + ": " + e.toString());
	}*/

	private final Set<Runnable> pending = new HashSet<>();

	public void schedule(Runnable r) {
		synchronized(pending) {
			pending.add(r);
		}
	}

	@SubscribeEvent
	public void onTick(ServerTickEvent e) {
		if(e.phase == TickEvent.Phase.START) {
			final Runnable[] pending;
			synchronized(this.pending) {
				pending = this.pending.isEmpty() ? null : this.pending.toArray(new Runnable[0]);
				this.pending.clear();
			}
			if(pending != null) {
				for(Runnable r : pending) {
					try {
						r.run();
					} catch(Throwable t) {
						Conventional.log.warn("Error in scheduled tick action.", t);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onLogin(PlayerLoggedInEvent e) {
		if(e.player instanceof EntityPlayerMP) {
			config.sendConfigTo((EntityPlayerMP) e.player);
		}
	}
}
