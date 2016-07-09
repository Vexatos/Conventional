package vexatos.conventional.command.area;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import vexatos.conventional.Conventional;
import vexatos.conventional.command.SubCommand;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * @author Vexatos
 */
public class CommandPosition extends SubCommand {

	public CommandPosition() {
		super("pos");
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/cv pos <1|2|get> - sets a corner of the selection area or returns the currently specified ones.";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(!(sender instanceof EntityPlayerMP)) {
			throw new CommandException("cannot process unless called from a player on the server side");
		}
		if(args.length < 1) {
			throw new CommandException("first argument needs to be '1' or '2' or 'get'.");
		}
		if(Objects.equals(args[0], "get")) {
			final BlockPos pos1 = Conventional.config.positions1.get(sender.getName());
			final BlockPos pos2 = Conventional.config.positions2.get(sender.getName());
			final String s1 = pos1 != null ? String.format(Locale.ENGLISH, "%s, %s, %s",
				pos1.getX(), pos1.getY(), pos1.getZ()) : "???";
			final String s2 = pos2 != null ? String.format(Locale.ENGLISH, "%s, %s, %s",
				pos2.getX(), pos2.getY(), pos2.getZ()) : "???";
			sender.addChatMessage(new TextComponentString(
				String.format(Locale.ENGLISH, "current positions: [%s -> %s].", s1, s2)
			));
		}
		try {
			int i = Integer.parseInt(args[0]);
			if(i != 1 && i != 2) {
				throw new CommandException("first argument needs to be '1' or '2' or 'get'.");
			}
			final BlockPos pos = sender.getPosition();
			switch(i) {
				case 1: {
					Conventional.config.positions1.put(sender.getName(), pos);
				}
				break;
				case 2: {
					Conventional.config.positions2.put(sender.getName(), pos);
				}
				break;
			}
			sender.addChatMessage(new TextComponentString(
				String.format(Locale.ENGLISH, "Set position %s to %s, %s, %s.", i, pos.getX(), pos.getY(), pos.getZ())
			));
		} catch(NumberFormatException e) {
			throw new CommandException("first argument needs to be '1' or '2' or 'get'.");
		}
	}

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		if(args.length <= 1) {
			return CommandBase.getListOfStringsMatchingLastWord(args, "1", "2", "get");
		}
		return super.getTabCompletionOptions(server, sender, args, pos);
	}
}
