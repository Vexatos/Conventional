package vexatos.conventional.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import java.util.List;

/**
 * @author Vexatos
 */
public abstract class SubCommand extends CommandBase {

	protected String name;

	public SubCommand(String name) {
		this.name = name;
	}

	@Override
	public String getCommandName() {
		return this.name;
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public final String getCommandUsage(ICommandSender sender) {
		return "/cv help for more information";
	}

	public abstract String getUsage(ICommandSender sender);

	@Override
	public abstract void processCommand(ICommandSender sender, String[] args);

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		return super.addTabCompletionOptions(sender, args);
	}
}
