package vexatos.conventional.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.network.INetHandler;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import vexatos.conventional.Conventional;
import vexatos.conventional.network.MessageHandlerBase;
import vexatos.conventional.network.Packet;

import javax.annotation.Nullable;
import java.io.File;

public class ClientProxy extends CommonProxy {

	@Override
	public boolean isClient() {
		return true;
	}

	@Override
	public File getMinecraftDirectory() {
		return Minecraft.getMinecraft().mcDataDir;
	}

	@Override
	public World getWorld(int dimensionId) {
		if(getCurrentClientDimension() != dimensionId) {
			return null;
		} else {
			return Minecraft.getMinecraft().world;
		}
	}

	@Override
	public int getCurrentClientDimension() {
		return Minecraft.getMinecraft().world.provider.getDimension();
	}

	@Override
	public void handlePacket(@Nullable MessageHandlerBase client, @Nullable MessageHandlerBase server, Packet packet, INetHandler handler) {
		try {
			switch(FMLCommonHandler.instance().getEffectiveSide()) {
				case CLIENT:
					if(client != null) {
						client.onMessage(packet, handler, Minecraft.getMinecraft().player);
					}
					break;
				case SERVER:
					super.handlePacket(client, server, packet, handler);
					break;
			}
		} catch(Exception e) {
			Conventional.log.warn("Caught a network exception! Is someone sending malformed packets?");
			e.printStackTrace();
		}
	}
}
