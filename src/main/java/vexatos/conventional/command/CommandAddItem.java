package vexatos.conventional.command;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import org.apache.commons.lang3.tuple.Pair;
import vexatos.conventional.Conventional;
import vexatos.conventional.reference.Config;

import java.util.List;

/**
 * @author Vexatos
 */
public class CommandAddItem extends SubCommand {

	public CommandAddItem() {
		super("item");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if(!(sender instanceof EntityPlayerMP)) {
			throw new WrongUsageException("cannot process unless called from a player on the server side");
		}
		Config.ItemList list = Conventional.config.itemsAllowRightclick;
		EntityPlayerMP player = (EntityPlayerMP) sender;
		ItemStack stack = player.getHeldItem();
		if(stack != null && stack.getItem() != null) {
			GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(stack.getItem());
			if(uid == null) {
				throw new WrongUsageException("unable to find identifier for item: " + stack.getUnlocalizedName());
			}
			Pair<Item, Integer> pair = Pair.of(stack.getItem(), args.length >= 1 && args[0].equalsIgnoreCase("ignore") ? -1 : stack.getItemDamage());
			if(list.contains(pair) || list.contains(Pair.of(stack.getItem(), -1))) {
				throw new WrongUsageException("item is already in the whitelist.");
			}
			list.add(pair);
			sender.addChatMessage(new ChatComponentText(String.format("item '%s' added!", uid.toString())));
			Conventional.config.save();
		}
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/cv add item [ignore] - adds the item currently in your hand. 'ignore' makes it ignore metadata.";
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		if(args.length <= 1) {
			return CommandBase.getListOfStringsMatchingLastWord(args, "ignore");
		}
		return super.addTabCompletionOptions(sender, args);
	}
}
