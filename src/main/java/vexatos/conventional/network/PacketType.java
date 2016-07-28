package vexatos.conventional.network;

import javax.annotation.Nullable;

public enum PacketType {
	CONFIG_SYNC;

	public static final PacketType[] VALUES = values();

	@Nullable
	public static PacketType of(int index) {
		return index >= 0 && index < VALUES.length ? VALUES[index] : null;
	}
}
