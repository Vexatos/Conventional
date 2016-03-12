package vexatos.conventional.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

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
	public abstract void processCommand(ICommandSender sender, String[] args) throws CommandException;

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		return super.addTabCompletionOptions(sender, args, pos);
	}
}
