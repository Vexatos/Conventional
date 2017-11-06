package vexatos.conventional.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.tuple.Pair;
import vexatos.conventional.Conventional;
import vexatos.conventional.reference.Config;
import vexatos.conventional.util.RegistryUtil;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Vexatos
 */
public class CommandRemoveItem extends SubCommandWithArea {

	public CommandRemoveItem(Supplier<Config.Area> area) {
		super("item", area);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(!(sender instanceof EntityPlayerMP)) {
			throw new CommandException("cannot process unless called from a player on the server side");
		}
		Config.ItemList list = area.get().itemsAllowRightclick;
		EntityPlayerMP player = (EntityPlayerMP) sender;
		ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
		if(stack != null && stack.getItem() != null) {
			final String uid = RegistryUtil.getRegistryName(stack.getItem());
			if(uid == null) {
				throw new CommandException("unable to find identifier for item: " + stack.getUnlocalizedName());
			}
			List<String> modifiers = Arrays.stream(args).map(s -> s.toLowerCase(Locale.ENGLISH)).collect(Collectors.toList());
			int meta = modifiers.contains("ignore") ? -1 : stack.getItemDamage();
			if(modifiers.contains("sneak") && modifiers.contains("nosneak")) {
				throw new CommandException("cannot specify 'sneak' and 'nosneak' at the same time.");
			}
			Boolean sneak = modifiers.contains("sneak") ? Boolean.TRUE : modifiers.contains("nosneak") ? Boolean.FALSE : null;
			Pair<Item, Config.ItemData> pair = Pair.of(stack.getItem(), new Config.ItemData(meta, sneak));
			if(!list.contains(pair)) {
				throw new CommandException("item is not in the whitelist.");
			}
			list.remove(pair);
			sender.sendMessage(new TextComponentString(String.format("Item '%s' removed!", uid)));
			Conventional.config.save();
		}
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/cv remove item [ignore] [sneak/nosneak] - Removes the item currently in your hand. 'ignore' makes it search for an entry that ignores metadata.";
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		return Optional.ofNullable(CommandAddItem.tabCompletions(server, sender, args, pos)).orElseGet(() -> super.getTabCompletions(server, sender, args, pos));
	}
}
