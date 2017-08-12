package vexatos.conventional.command.area;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import vexatos.conventional.Conventional;
import vexatos.conventional.command.SubCommand;
import vexatos.conventional.reference.Config.Area;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Vexatos
 */
public class CommandAreaRemove extends SubCommand {

	public CommandAreaRemove() {
		super("remove");
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/cv area remove <name> - Removes the area with the specified name if it exists.";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length < 1) {
			throw new CommandException("first argument needs to be the name of the area.");
		}
		if(Objects.equals(args[0], Conventional.config.ALL.name)) {
			throw new CommandException("you can't remove this area!");
		}

		final List<Area> toRemove = new ArrayList<>();
		for(Area area : Conventional.config.areas) {
			if(Objects.equals(area.name, args[0])) {
				toRemove.add(area);
				break;
			}
		}
		if(!toRemove.isEmpty()) {
			for(Area area : toRemove) {
				sender.sendMessage(new TextComponentString(
					String.format(Locale.ENGLISH, "Removed area '%s' at [%s, %s, %s -> %s, %s, %d].", args[0],
						MathHelper.floor(area.pos.minX), MathHelper.floor(area.pos.minY), MathHelper.floor(area.pos.minZ),
						MathHelper.floor(area.pos.maxX), MathHelper.floor(area.pos.maxY), MathHelper.floor(area.pos.maxZ))
				));
			}
			Conventional.config.areas.removeAll(toRemove);
			Conventional.config.save();
		} else {
			throw new CommandException("area with the specified name does not exist.");
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		return getListOfStringsMatchingLastWord(args,
			Conventional.config.areas.stream().map(a -> a.name).collect(Collectors.toList()));
	}
}
