package vexatos.conventional.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import vexatos.conventional.util.StringUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vexatos
 */
public class ConventionalCommand extends SubCommand {

	public List<SubCommand> commands = new ArrayList<SubCommand>();

	public ConventionalCommand(String name) {
		super(name);
	}

	public void addCommand(SubCommand cmd) {
		commands.add(cmd);
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		String text = "";
		for(SubCommand cmd : commands) {
			text += (cmd.getCommandUsage(sender) + "\n");
		}
		return text.replaceAll("\\n$", "");
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
		throw new WrongUsageException(getUsage(sender));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		if(args.length <= 1) {
			List<String> words = new ArrayList<String>();
			for(SubCommand cmd : commands) {
				words.add(cmd.getName());
			}
			return getListOfStringsMatchingLastWord(args, words.toArray(new String[words.size()]));
		} else {
			String cmdname = args[0];
			for(SubCommand cmd : commands) {
				if(cmd.getName().equalsIgnoreCase(cmdname)) {
					return cmd.getTabCompletions(server, sender, StringUtil.dropArgs(args, 1), pos);
				}
			}
		}
		return super.getTabCompletions(server, sender, args, pos);
	}
}
