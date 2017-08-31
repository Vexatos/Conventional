package vexatos.conventional.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import vexatos.conventional.Conventional;
import vexatos.conventional.reference.Config;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Vexatos
 */
public class CommandAllow extends SubCommandWithArea {

	public CommandAllow(Supplier<Config.Area> area) {
		super("allow", area);
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/cv allow <id> - grants players the specified permission.";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(!(sender instanceof EntityPlayerMP)) {
			throw new CommandException("cannot process unless called from a player on the server side");
		}
		if(args.length < 1) {
			throw new CommandException("third argument needs to be a permission ID.");
		}
		Config.StringList list = area.get().permissions;
		String id = args[0];
		if(list.contains(id)) {
			throw new CommandException("entity is already in the whitelist.");
		}
		list.add(id);
		sender.sendMessage(new TextComponentString(String.format("Permission '%s' added!", id)));
		Conventional.config.save();
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		return super.getTabCompletions(server, sender, args, pos);
	}
}
