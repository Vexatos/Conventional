package vexatos.conventional.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import vexatos.conventional.Conventional;
import vexatos.conventional.reference.Config.Area;
import vexatos.conventional.util.StringUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Vexatos
 */
public class CommandArea extends ConventionalCommand {

	public CommandArea() {
		super("area");
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/cv area <name> - Executes commands on the area with the specified name.\n" + super.getCommandUsage(sender);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length < 1) {
			throw new WrongUsageException(getUsage(sender));
		}
		if(args[0].equalsIgnoreCase("help")) {
			String[] usage = getCommandUsage(sender).split("\n");
			for(String s : usage) {
				sender.sendMessage(new TextComponentString(" - " + s));
			}
			return;
		}
		for(SubCommand cmd : commands) {
			if(cmd.getName().equalsIgnoreCase(args[0])) {
				cmd.execute(server, sender, StringUtil.dropArgs(args, 1));
				return;
			}
		}
		for(Area area : Conventional.config.areas) {
			if(Objects.equals(area.name, args[0])) {
				ConventionalCommand cmd = new ConventionalCommand(args[0]);
				for(Function<Area, SubCommand> areaCommand : Conventional.areaCommands) {
					cmd.addCommand(areaCommand.apply(area));
				}
				cmd.execute(server, sender, StringUtil.dropArgs(args, 1));
				return;
			}
		}
		throw new WrongUsageException(getUsage(sender));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		List<String> options = super.getTabCompletions(server, sender, args, pos);
		if(args.length <= 1) {
			options.addAll(getListOfStringsMatchingLastWord(args,
				Conventional.config.areas.stream().map(a -> a.name).collect(Collectors.toList())));
		}
		return options;
	}
}
