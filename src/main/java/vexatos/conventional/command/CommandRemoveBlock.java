package vexatos.conventional.command;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.tuple.Pair;
import vexatos.conventional.Conventional;
import vexatos.conventional.reference.Config;
import vexatos.conventional.util.RayTracer;
import vexatos.conventional.util.RegistryUtil;

import java.util.List;

/**
 * @author Vexatos
 */
public class CommandRemoveBlock extends SubCommand {

	public CommandRemoveBlock() {
		super("block");
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(!(sender instanceof EntityPlayerMP)) {
			throw new WrongUsageException("cannot process unless called from a player on the server side");
		}
		if(args.length < 1 || (!args[0].equalsIgnoreCase("right") && !args[0].equalsIgnoreCase("left") && !args[0].equalsIgnoreCase("break"))) {
			throw new WrongUsageException("third argument needs to be 'left' or 'right' or 'break'.");
		}
		Config.BlockList list = args[0].equalsIgnoreCase("right") ?
			Conventional.config.blocksAllowRightclick : args[0].equalsIgnoreCase("left") ?
			Conventional.config.blocksAllowLeftclick : Conventional.config.blocksAllowBreak;
		EntityPlayerMP player = (EntityPlayerMP) sender;
		RayTracer.instance().fire(player, 10);
		RayTraceResult result = RayTracer.instance().getTarget();
		if(result.typeOfHit != RayTraceResult.Type.BLOCK) {
			throw new WrongUsageException("the player is not looking at any block");
		}
		IBlockState state = player.worldObj.getBlockState(result.getBlockPos());
		Block block = state.getBlock();
		if(!block.isAir(state, player.worldObj, result.getBlockPos())) {
			final String uid = RegistryUtil.getRegistryName(block);
			if(uid == null) {
				throw new WrongUsageException("unable to find identifier for block: " + block.getUnlocalizedName());
			}
			Pair<Block, Integer> pair = Pair.of(block, (args.length >= 2 && args[1].equalsIgnoreCase("ignore")) ? -1 : block.getMetaFromState(state));
			if(!list.contains(pair)) {
				throw new WrongUsageException("Block is not in the whitelist.");
			}
			list.remove(pair);
			sender.addChatMessage(new TextComponentString(String.format("Block '%s' removed!", uid)));
			Conventional.config.save();
		}
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/cv remove block <left|right|break> [ignore] - removes the block you are currently looking at. 'ignore' makes it search for an entry that ignores metadata.";
	}

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
		if(args.length <= 1) {
			return CommandBase.getListOfStringsMatchingLastWord(args, "left", "right", "break");
		} else if(args.length == 2) {
			return CommandBase.getListOfStringsMatchingLastWord(args, "ignore");
		}
		return super.getTabCompletionOptions(server, sender, args, pos);
	}
}
