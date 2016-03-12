package vexatos.conventional.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import vexatos.conventional.Conventional;
import vexatos.conventional.reference.Config;
import vexatos.conventional.util.RayTracer;

import java.util.List;

/**
 * @author Vexatos
 */
public class CommandRemoveEntity extends SubCommand {

	public CommandRemoveEntity() {
		super("entity");
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/cv remove entity <left|right> - removes the entity class you are currently looking at.";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if(!(sender instanceof EntityPlayerMP)) {
			throw new WrongUsageException("cannot process unless called from a player on the server side");
		}
		if(args.length < 1 || (!args[0].equalsIgnoreCase("right") && !args[0].equalsIgnoreCase("left"))) {
			throw new WrongUsageException("third argument needs to be 'left' or 'right'.");
		}
		Config.EntityList list = args[0].equalsIgnoreCase("right") ? Conventional.config.entitiesAllowRightclick : Conventional.config.entitiesAllowLeftclick;
		EntityPlayerMP player = (EntityPlayerMP) sender;
		RayTracer.instance().fire(player, 10);
		MovingObjectPosition mop = RayTracer.instance().getTarget();
		if(mop.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY || mop.entityHit == null) {
			throw new WrongUsageException("the player is not looking at any entity");
		}
		Entity entity = mop.entityHit;
		if(!entity.isDead) {
			String name = entity.getClass().getCanonicalName();
			if(!list.contains(name)) {
				throw new WrongUsageException("entity is not in the whitelist.");
			}
			list.remove(name);
			sender.addChatMessage(new ChatComponentText(String.format("Entity '%s' removed!", name)));
			Conventional.config.save();
		}
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		if(args.length <= 1) {
			return CommandBase.getListOfStringsMatchingLastWord(args, "left", "right");
		}
		return super.addTabCompletionOptions(sender, args, pos);
	}
}
