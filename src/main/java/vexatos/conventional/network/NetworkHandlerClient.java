package vexatos.conventional.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import vexatos.conventional.Conventional;

import java.io.IOException;

public class NetworkHandlerClient extends MessageHandlerBase {

	@Override
	public void onMessage(Packet packet, INetHandler handler, EntityPlayer player, int command)
		throws IOException {
		final PacketType type = PacketType.of(command);
		if(type == null) {
			return;
		}
		switch(type) {
			case CONFIG_SYNC: {
				Conventional.config.reloadFromString(packet.readString());
			}
			break;
		}
	}
}
