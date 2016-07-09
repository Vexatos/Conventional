package vexatos.conventional.command.area;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import vexatos.conventional.Conventional;
import vexatos.conventional.command.CommandArea;
import vexatos.conventional.command.SubCommand;
import vexatos.conventional.reference.Config.Area;

import java.util.Locale;
import java.util.Objects;

/**
 * @author Vexatos
 */
public class CommandAreaCreate extends SubCommand {

	private final CommandArea parent;

	public CommandAreaCreate(CommandArea parent) {
		super("create");
		this.parent = parent;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/cv area create <name> - Creates an area between the currently selected positions with the specified name";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(!(sender instanceof EntityPlayerMP)) {
			throw new CommandException("cannot process unless called from a player on the server side");
		}
		if(args.length < 1) {
			throw new CommandException("first argument needs to be the name of the area.");
		}
		for(SubCommand cmd : parent.commands) {
			if(Objects.equals(cmd.name, args[0])) {
				throw new CommandException("invalid name.");
			}
		}
		for(Area area : Conventional.config.areas) {
			if(Objects.equals(area.name, args[0])) {
				throw new CommandException("area with the specified name already exists.");
			}
		}
		if(!Conventional.config.positions1.containsKey(sender.getName())) {
			throw new CommandException("position 1 not specified.");
		}
		if(!Conventional.config.positions2.containsKey(sender.getName())) {
			throw new CommandException("position 2 not specified.");
		}
		final BlockPos pos1 = Conventional.config.positions1.get(sender.getName());
		final BlockPos pos2 = Conventional.config.positions2.get(sender.getName());
		Conventional.config.areas.add(new Area(args[0], sender.getEntityWorld().provider.getDimension(), pos1, pos2)
		);
		sender.addChatMessage(new TextComponentString(
			String.format(Locale.ENGLISH, "Added area '%s' at [%s, %s, %s -> %s, %s, %s].", args[0],
				pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ())
		));
		Conventional.config.save();
	}
}
