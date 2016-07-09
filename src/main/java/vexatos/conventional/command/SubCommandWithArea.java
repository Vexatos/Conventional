package vexatos.conventional.command;

import vexatos.conventional.reference.Config;

import java.util.function.Supplier;

/**
 * @author Vexatos
 */
public abstract class SubCommandWithArea extends SubCommand {

	protected final Supplier<Config.Area> area;

	public SubCommandWithArea(String name, Supplier<Config.Area> area) {
		super(name);
		this.area = area;
	}
}
