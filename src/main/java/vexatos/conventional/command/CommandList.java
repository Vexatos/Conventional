package vexatos.conventional.command;

import com.google.common.base.Joiner;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;
import vexatos.conventional.Conventional;
import vexatos.conventional.reference.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Vexatos
 */
public class CommandList extends SubCommand {

	private static final Joiner joiner = Joiner.on(", ");

	public CommandList() {
		super("list");
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/cv list block <left|right> - returns all entries in the specified list\n"
			+ "/cv list item - Same, just for the item list.";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if(args.length < 1 || (!args[0].equalsIgnoreCase("block") && !args[0].equalsIgnoreCase("item"))) {
			throw new WrongUsageException("second argument needs to be 'block' or 'item'.");
		}
		String[] uids;
		if(args[0].equalsIgnoreCase("block")) {
			if(args.length < 2 || (!args[1].equalsIgnoreCase("right") && !args[1].equalsIgnoreCase("left"))) {
				throw new WrongUsageException("third argument needs to be 'left' or 'right'.");
			}
			Config.BlockList list = args[1].equalsIgnoreCase("right") ? Conventional.config.blocksAllowRightclick : Conventional.config.blocksAllowLeftclick;
			uids = Conventional.config.getUIDs(list);
		} else {
			Config.ItemList list = Conventional.config.itemsAllowRightclick;
			uids = Conventional.config.getUIDs(list);
		}
		sender.addChatMessage(new ChatComponentText("Entries in the list:"));
		Arrays.sort(uids, String.CASE_INSENSITIVE_ORDER);
		ArrayList<String> line = new ArrayList<String>(5);
		for(String uid : uids) {
			line.add(uid);
			if(line.size() >= 5) {
				sender.addChatMessage(new ChatComponentText(joiner.join(line)));
				line.clear();
			}
		}
		if(!line.isEmpty()) {
			sender.addChatMessage(new ChatComponentText(joiner.join(line)));
		}
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		if(args.length <= 1) {
			return CommandBase.getListOfStringsMatchingLastWord(args, "block", "item");
		} else if(args.length == 2 && "block".equalsIgnoreCase(args[0])) {
			return CommandBase.getListOfStringsMatchingLastWord(args, "left", "right");
		}
		return super.addTabCompletionOptions(sender, args);
	}
}
