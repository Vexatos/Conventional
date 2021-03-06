package vexatos.conventional.command;

import com.google.common.base.Joiner;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import vexatos.conventional.reference.Config;
import vexatos.conventional.reference.Config.Area;
import vexatos.conventional.reference.Config.BlockList;
import vexatos.conventional.reference.Config.EntityList;
import vexatos.conventional.reference.Config.ItemList;
import vexatos.conventional.reference.Config.StringList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Vexatos
 */
public class CommandList extends SubCommandWithArea {

	private static final Joiner joiner = Joiner.on(", ");

	public CommandList(Supplier<Area> area) {
		super("list", area);
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/cv list block <left|right|break> - returns all entries in the specified list\n"
			+ "/cv list item - same, just for the item list.\n"
			+ "/cv list entity <left|right> - same, just for the entity lists.";
	}

	private static final List<String> VALID_COMMANDS = Arrays.asList("block", "item", "entity", "permissions");

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length < 1 || !VALID_COMMANDS.contains(args[0])) {
			throw new CommandException("second argument needs to be 'block' or 'item' or 'entity' or 'permissions'.");
		}
		String[] uids;
		if(args[0].equalsIgnoreCase("block")) {
			if(args.length < 2 || (!args[1].equalsIgnoreCase("right") && !args[1].equalsIgnoreCase("left") && !args[1].equalsIgnoreCase("break"))) {
				throw new CommandException("third argument needs to be 'left' or 'right' or 'break'.");
			}
			BlockList list = args[1].equalsIgnoreCase("right") ?
				area.get().blocksAllowRightclick : args[1].equalsIgnoreCase("left") ?
				area.get().blocksAllowLeftclick : area.get().blocksAllowBreak;
			uids = area.get().getUIDs(list);
		} else if(args[0].equalsIgnoreCase("entity")) {
			if(args.length < 2 || (!args[1].equalsIgnoreCase("right") && !args[1].equalsIgnoreCase("left"))) {
				throw new CommandException("third argument needs to be 'left' or 'right'.");
			}
			EntityList list = args[1].equalsIgnoreCase("right") ?
				area.get().entitiesAllowRightclick :
				area.get().entitiesAllowLeftclick;
			uids = area.get().getUIDs(list);
		} else if(args[0].equalsIgnoreCase("permissions")) {
			StringList list = area.get().permissions;
			uids = list.toArray(new String[list.size()]);
		} else {
			ItemList list = area.get().itemsAllowRightclick;
			uids = area.get().getUIDs(list);
		}
		sender.sendMessage(new TextComponentString("Entries in the list:"));
		Arrays.sort(uids, String.CASE_INSENSITIVE_ORDER);
		ArrayList<String> line = new ArrayList<String>(5);
		for(String uid : uids) {
			line.add(uid);
			if(line.size() >= 5) {
				sender.sendMessage(new TextComponentString(joiner.join(line)));
				line.clear();
			}
		}
		if(!line.isEmpty()) {
			sender.sendMessage(new TextComponentString(joiner.join(line)));
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		if(args.length <= 1) {
			return getListOfStringsMatchingLastWord(args, "block", "item", "entity", "permissions");
		} else if(args.length == 2) {
			if("block".equalsIgnoreCase(args[0])) {
				return getListOfStringsMatchingLastWord(args, "left", "right", "break");
			} else if("entity".equalsIgnoreCase(args[0])) {
				return getListOfStringsMatchingLastWord(args, "left", "right");
			}
		}
		return super.getTabCompletions(server, sender, args, pos);
	}
}
