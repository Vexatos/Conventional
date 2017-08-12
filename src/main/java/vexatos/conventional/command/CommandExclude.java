package vexatos.conventional.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import vexatos.conventional.Conventional;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Vexatos
 */
public class CommandExclude extends SubCommand {

	public CommandExclude() {
		super("exclude");
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/cv exclude [player] - temporarily excludes a player (or you) from being affected by cv. Does not persist across server launches.\n"
			+ "Run again to include a player once more.";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		final String name = args.length < 1 ? sender.getName() : args[0];
		Conventional.config.excludePlayer(name);
		sender.sendMessage(new TextComponentString(
			String.format("player '%s' has been %s.", name, Conventional.config.isExcluded(name) ? "excluded" : "included again")
		));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		if(args.length <= 1) {
			return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
		}
		return super.getTabCompletions(server, sender, args, pos);
	}
}
