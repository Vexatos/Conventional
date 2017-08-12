package vexatos.conventional.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import vexatos.conventional.Conventional;
import vexatos.conventional.reference.Config;
import vexatos.conventional.util.RayTracer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Vexatos
 */
public class CommandRemoveEntity extends SubCommandWithArea {

	public CommandRemoveEntity(Supplier<Config.Area> area) {
		super("entity", area);
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/cv remove entity <left|right> - removes the entity class you are currently looking at.";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(!(sender instanceof EntityPlayerMP)) {
			throw new CommandException("cannot process unless called from a player on the server side");
		}
		if(args.length < 1 || (!args[0].equalsIgnoreCase("right") && !args[0].equalsIgnoreCase("left"))) {
			throw new CommandException("third argument needs to be 'left' or 'right'.");
		}
		Config.EntityList list = args[0].equalsIgnoreCase("right") ? area.get().entitiesAllowRightclick : area.get().entitiesAllowLeftclick;
		EntityPlayerMP player = (EntityPlayerMP) sender;
		RayTracer.instance().fire(player, 10);
		RayTraceResult result = RayTracer.instance().getTarget();
		if(result.typeOfHit != RayTraceResult.Type.ENTITY || result.entityHit == null) {
			throw new CommandException("the player is not looking at any entity");
		}
		Entity entity = result.entityHit;
		if(!entity.isDead) {
			String name = entity.getClass().getCanonicalName();
			if(!list.contains(name)) {
				throw new CommandException("entity is not in the whitelist.");
			}
			list.remove(name);
			sender.sendMessage(new TextComponentString(String.format("Entity '%s' removed!", name)));
			Conventional.config.save();
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		if(args.length <= 1) {
			return getListOfStringsMatchingLastWord(args, "left", "right");
		}
		return super.getTabCompletions(server, sender, args, pos);
	}
}
