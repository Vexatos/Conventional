package vexatos.conventional.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import vexatos.conventional.Conventional;
import vexatos.conventional.reference.Config;
import vexatos.conventional.util.RayTracer;

import java.util.List;

/**
 * @author Vexatos
 */
public class CommandAddEntity extends SubCommand {

	public CommandAddEntity() {
		super("entity");
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/cv add entity <left|right> - adds the entity class you are currently looking at.";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(!(sender instanceof EntityPlayerMP)) {
			throw new WrongUsageException("cannot process unless called from a player on the server side");
		}
		if(args.length < 1 || (!args[0].equalsIgnoreCase("right") && !args[0].equalsIgnoreCase("left"))) {
			throw new WrongUsageException("third argument needs to be 'left' or 'right'.");
		}
		Config.EntityList list = args[0].equalsIgnoreCase("right") ? Conventional.config.entitiesAllowRightclick : Conventional.config.entitiesAllowLeftclick;
		EntityPlayerMP player = (EntityPlayerMP) sender;
		RayTracer.instance().fire(player, 10);
		RayTraceResult result = RayTracer.instance().getTarget();
		if(result.typeOfHit != RayTraceResult.Type.ENTITY || result.entityHit == null) {
			throw new WrongUsageException("the player is not looking at any entity");
		}
		Entity entity = result.entityHit;
		if(!entity.isDead) {
			String name = entity.getClass().getCanonicalName();
			if(list.contains(name)) {
				throw new WrongUsageException("entity is already in the whitelist.");
			}
			list.add(name);
			sender.addChatMessage(new TextComponentString(String.format("Entity '%s' added!", name)));
			Conventional.config.save();
		}
	}

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
		if(args.length <= 1) {
			return CommandBase.getListOfStringsMatchingLastWord(args, "left", "right");
		}
		return super.getTabCompletionOptions(server, sender, args, pos);
	}
}
