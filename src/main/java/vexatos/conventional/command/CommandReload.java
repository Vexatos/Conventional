package vexatos.conventional.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import vexatos.conventional.Conventional;

/**
 * @author Vexatos
 */
public class CommandReload extends SubCommand {

	public CommandReload() {
		super("reload");
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/cv reload - reloads the whitelists.";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		Conventional.config.reloadFromFile();
		Conventional.config.save();
		sender.sendMessage(new TextComponentString("Whitelists reloaded!"));
	}
}
