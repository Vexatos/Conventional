package vexatos.conventional.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.tuple.Pair;
import vexatos.conventional.Conventional;
import vexatos.conventional.reference.Config;
import vexatos.conventional.reference.Config.EntityList;
import vexatos.conventional.util.RayTracer;
import vexatos.conventional.util.StringUtil;

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
public class CommandAddEntity extends SubCommandWithArea {

	public CommandAddEntity(Supplier<Config.Area> area) {
		super("entity", area);
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/cv add entity <left|right> [sneak/nosneak] - adds the entity class you are currently looking at. 'sneak' and 'nosneak' only allow it while the player is or is not sneaking.";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(!(sender instanceof EntityPlayerMP)) {
			throw new CommandException("cannot process unless called from a player on the server side");
		}
		if(args.length < 1 || (!args[0].equalsIgnoreCase("right") && !args[0].equalsIgnoreCase("left"))) {
			throw new CommandException("third argument needs to be 'left' or 'right'.");
		}
		EntityList list = args[0].equalsIgnoreCase("right") ? area.get().entitiesAllowRightclick : area.get().entitiesAllowLeftclick;
		EntityPlayerMP player = (EntityPlayerMP) sender;
		RayTracer.instance().fire(player, 10);
		RayTraceResult result = RayTracer.instance().getTarget();
		if(result.typeOfHit != RayTraceResult.Type.ENTITY || result.entityHit == null) {
			throw new CommandException("the player is not looking at any entity");
		}
		Entity entity = result.entityHit;
		if(!entity.isDead) {
			String name = entity.getClass().getCanonicalName();
			List<String> modifiers = Arrays.stream(StringUtil.dropArgs(args, 1)).map(s -> s.toLowerCase(Locale.ENGLISH)).collect(Collectors.toList());
			if(modifiers.contains("sneak") && modifiers.contains("nosneak")) {
				throw new CommandException("cannot specify 'sneak' and 'nosneak' at the same time.");
			}
			Boolean sneak = modifiers.contains("sneak") ? Boolean.TRUE : modifiers.contains("nosneak") ? Boolean.FALSE : null;
			Pair<String, Boolean> pair = Pair.of(name, sneak);
			if(list.contains(pair)) {
				throw new CommandException("entity is already in the whitelist.");
			}
			list.add(pair);
			sender.sendMessage(new TextComponentString(String.format("Entity '%s' added!", name)));
			Conventional.config.save();
		}
	}

	@Nullable
	static List<String> tabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		if(args.length <= 1) {
			return getListOfStringsMatchingLastWord(args, "left", "right");
		} else if(args.length == 2) {
			return getListOfStringsMatchingLastWord(args, "sneak", "nosneak");
		}
		return null;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		return Optional.ofNullable(tabCompletions(server, sender, args, pos)).orElseGet(() -> super.getTabCompletions(server, sender, args, pos));
	}
}
