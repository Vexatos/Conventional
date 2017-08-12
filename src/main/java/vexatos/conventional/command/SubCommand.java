package vexatos.conventional.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Vexatos
 */
public abstract class SubCommand extends CommandBase {

	public final String name;

	public SubCommand(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public final String getUsage(ICommandSender sender) {
		return "/cv help for more information";
	}

	public abstract String getCommandUsage(ICommandSender sender);

	@Override
	public abstract void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException;

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		return super.getTabCompletions(server, sender, args, pos);
	}
}
