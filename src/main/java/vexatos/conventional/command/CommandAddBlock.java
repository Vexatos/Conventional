package vexatos.conventional.command;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.tuple.Pair;
import vexatos.conventional.Conventional;
import vexatos.conventional.reference.Config;
import vexatos.conventional.reference.Config.ItemData;
import vexatos.conventional.util.RayTracer;
import vexatos.conventional.util.RegistryUtil;
import vexatos.conventional.util.StringUtil;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Vexatos
 */
public class CommandAddBlock extends SubCommandWithArea {

	public CommandAddBlock(Supplier<Config.Area> area) {
		super("block", area);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(!(sender instanceof EntityPlayerMP)) {
			throw new CommandException("cannot process unless called from a player on the server side");
		}
		if(args.length < 1 || (!args[0].equalsIgnoreCase("right") && !args[0].equalsIgnoreCase("left") && !args[0].equalsIgnoreCase("break"))) {
			throw new CommandException("third argument needs to be 'left' or 'right' or 'break'.");
		}
		Config.BlockList list = args[0].equalsIgnoreCase("right") ?
			area.get().blocksAllowRightclick : args[0].equalsIgnoreCase("left") ?
			area.get().blocksAllowLeftclick : area.get().blocksAllowBreak;
		EntityPlayerMP player = (EntityPlayerMP) sender;
		RayTracer.instance().fire(player, 10);
		RayTraceResult result = RayTracer.instance().getTarget();
		if(result.typeOfHit != RayTraceResult.Type.BLOCK) {
			throw new CommandException("the player is not looking at any block");
		}
		IBlockState state = player.world.getBlockState(result.getBlockPos());
		Block block = state.getBlock();
		if(!block.isAir(state, player.world, result.getBlockPos())) {
			final String uid = RegistryUtil.getRegistryName(block);
			if(uid == null) {
				throw new CommandException("unable to find identifier for block: " + block.getUnlocalizedName());
			}
			List<String> modifiers = Arrays.stream(StringUtil.dropArgs(args, 1)).map(s -> s.toLowerCase(Locale.ENGLISH)).collect(Collectors.toList());
			int meta = modifiers.contains("ignore") ? -1 : block.getMetaFromState(state);
			if(modifiers.contains("sneak") && modifiers.contains("nosneak")) {
				throw new CommandException("cannot specify 'sneak' and 'nosneak' at the same time.");
			}
			Boolean sneak = modifiers.contains("sneak") ? Boolean.TRUE : modifiers.contains("nosneak") ? Boolean.FALSE : null;
			Pair<Block, ItemData> pair = Pair.of(block, new ItemData(meta, sneak));
			if(list.contains(pair) || list.contains(Pair.of(block, new ItemData(-1, sneak)))) {
				throw new CommandException("block is already in the whitelist.");
			}
			list.add(pair);
			sender.sendMessage(new TextComponentString(String.format("Block '%s' added!", uid)));
			Conventional.config.save();
		}
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/cv add block <left|right|break> [ignore] [sneak/nosneak] - adds the block you are currently looking at. 'ignore' makes it ignore metadata. 'sneak' and 'nosneak' only allow it while the player is or is not sneaking.";
	}

	@Nullable
	static List<String> tabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		if(args.length <= 1) {
			return getListOfStringsMatchingLastWord(args, "left", "right", "break");
		} else if(args.length == 2) {
			return getListOfStringsMatchingLastWord(args, "ignore", "sneak", "nosneak");
		} else if(args.length == 3) {
			if(args[1].equalsIgnoreCase("ignore")) {
				return getListOfStringsMatchingLastWord(args, "sneak", "nosneak");
			} else if(args[1].equalsIgnoreCase("sneak") || args[1].equalsIgnoreCase("nosneak")) {
				return getListOfStringsMatchingLastWord(args, "ignore");
			}
		}
		return null;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		return Optional.ofNullable(tabCompletions(server, sender, args, pos)).orElseGet(() -> super.getTabCompletions(server, sender, args, pos));
	}
}
