package vexatos.conventional.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

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
	public abstract void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException;

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
		return super.getTabCompletionOptions(server, sender, args, pos);
	}
}
