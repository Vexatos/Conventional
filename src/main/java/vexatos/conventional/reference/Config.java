package vexatos.conventional.reference;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import vexatos.conventional.Conventional;
import vexatos.conventional.network.Packet;
import vexatos.conventional.network.PacketType;
import vexatos.conventional.util.RegistryUtil;
import vexatos.conventional.util.storage.AreaStorage;
import vexatos.conventional.util.storage.ConfigStorage;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Vexatos
 */
public class Config {

	//private Configuration config;
	public final Area ALL = new Area("all") {
		@Override
		public boolean isInArea(@Nullable Entity entity) {
			return true;
		}

		@Override
		public boolean isInArea(World world, BlockPos pos) {
			return true;
		}
	};
	private File dataFile;
	private static final Gson gson = new Gson();

	public Config(/*Configuration configuration*/) {
		//config = configuration;
		areas.add(ALL);
		dataFile = new File(Conventional.configDir, "Conventional.json");
	}

	public void load(ConfigStorage config) {
		ArrayList<AreaStorage> areas = config.areas;
		for(AreaStorage a : areas) {
			Area area = a.dim != null && a.pos != null ?
				new Area(a.name, a.dim, new BlockPos(a.pos.minX, a.pos.minY, a.pos.minZ), new BlockPos(a.pos.maxX, a.pos.maxY, a.pos.maxZ)) :
				new Area(a.name);
			area.fillBlockList(a.whitelists.blocks.allowAny.toArray(new String[a.whitelists.blocks.allowAny.size()]), area.blocksAllowAny, area.blocksAllowLeftclick, area.blocksAllowRightclick, area.blocksAllowBreak);
			area.fillBlockList(a.whitelists.blocks.allowLeftclick.toArray(new String[a.whitelists.blocks.allowLeftclick.size()]), area.blocksAllowLeftclick);
			area.fillBlockList(a.whitelists.blocks.allowBreak.toArray(new String[a.whitelists.blocks.allowBreak.size()]), area.blocksAllowBreak);
			area.fillBlockList(a.whitelists.blocks.allowRightclick.toArray(new String[a.whitelists.blocks.allowRightclick.size()]), area.blocksAllowRightclick);

			area.fillItemList(a.whitelists.items.allowRightclick.toArray(new String[a.whitelists.items.allowRightclick.size()]), area.itemsAllowRightclick);

			area.fillEntityList(a.whitelists.entities.allowLeftclick.toArray(new String[a.whitelists.entities.allowLeftclick.size()]), area.entitiesAllowLeftclick);
			area.fillEntityList(a.whitelists.entities.allowRightclick.toArray(new String[a.whitelists.entities.allowRightclick.size()]), area.entitiesAllowRightclick);

			area.fillStringList(a.permissions.toArray(new String[a.permissions.size()]), area.permissions);
			this.areas.add(area);
		}
	}

	private boolean loadFromFile() {
		boolean success;
		try(JsonReader r = new JsonReader(new FileReader(dataFile))) {
			ConfigStorage config = gson.fromJson(r, new ConfigStorage.Token().getType());
			load(config);
			success = true;
		} catch(FileNotFoundException e) {
			Conventional.log.error("Error loading config file", e);
			success = false;
		} catch(Exception e1) {
			success = false;
		}
		return success;
	}

	private boolean loadFromString(String s) {
		boolean success;
		try {
			ConfigStorage config = gson.fromJson(s, new ConfigStorage.Token().getType());
			load(config);
			success = true;
		} catch(Exception e) {
			Conventional.log.error("Error loading config file from server", e);
			success = false;
		}
		return success;
	}

	private boolean reload(Supplier<Boolean> f) {
		areas.clear();
		ALL.blocksAllowAny.clear();
		ALL.blocksAllowLeftclick.clear();
		ALL.blocksAllowBreak.clear();
		ALL.blocksAllowRightclick.clear();
		ALL.itemsAllowRightclick.clear();
		ALL.entitiesAllowRightclick.clear();
		ALL.entitiesAllowLeftclick.clear();
		ALL.permissions.clear();
		boolean success = f.get();
		for(Iterator<Area> i = areas.iterator(); i.hasNext(); ) {
			Area area = i.next();
			if(Objects.equals(area.name, ALL.name)) {
				ALL.blocksAllowAny.addAll(area.blocksAllowAny);
				ALL.blocksAllowLeftclick.addAll(area.blocksAllowLeftclick);
				ALL.blocksAllowBreak.addAll(area.blocksAllowBreak);
				ALL.blocksAllowRightclick.addAll(area.blocksAllowRightclick);
				ALL.itemsAllowRightclick.addAll(area.itemsAllowRightclick);
				ALL.entitiesAllowRightclick.addAll(area.entitiesAllowRightclick);
				ALL.entitiesAllowLeftclick.addAll(area.entitiesAllowLeftclick);
				ALL.permissions.addAll(area.permissions);
				i.remove();
			}
		}
		areas.add(0, ALL);
		return success;
	}

	public boolean reloadFromFile() {
		return reload(this::loadFromFile);
	}

	public boolean reloadFromString(final String s) {
		return reload(() -> this.loadFromString(s));
	}

	public void save() {
		save(true);
	}

	public void save(boolean overwrite) {
		if(dataFile.exists()) {
			if(overwrite) {
				File tmpFile = new File(dataFile.getAbsolutePath() + ".tmp");
				doSave(tmpFile);
				if(FileUtils.deleteQuietly(dataFile)) {
					tmpFile.renameTo(dataFile);
				}
			}
		} else {
			doSave(dataFile);
		}
		sendConfigToAll();
	}

	public void sendConfigToAll() {
		sendConfig(Conventional.packet::sendToAll);
	}

	public void sendConfigTo(final EntityPlayerMP player) {
		sendConfig(p -> Conventional.packet.sendTo(p, player));
	}

	public void sendConfig(final Consumer<Packet> f) {
		try {
			final ConfigStorage config = buildStorage();
			Packet p = Conventional.packet.create(PacketType.CONFIG_SYNC.ordinal());
			p.writeString(gson.toJson(config, new ConfigStorage.Token().getType()));
			Conventional.instance.schedule(() -> f.accept(p));
		} catch(Exception e) {
			Conventional.log.error("Error sending config file", e);
		}
	}

	private void doSave(File file) {
		try(JsonWriter w = new JsonWriter(new FileWriter(file, false))) {
			w.setIndent("  ");
			ConfigStorage config = buildStorage();
			gson.toJson(config, new ConfigStorage.Token().getType(), w);
		} catch(Exception e) {
			Conventional.log.error("Error saving config file", e);
		}
	}

	private ConfigStorage buildStorage() {
		ConfigStorage config = new ConfigStorage();
		ArrayList<AreaStorage> areas = config.areas;
		for(Area area : this.areas) {
			AreaStorage a = new AreaStorage();
			a.whitelists.blocks.allowAny.addAll(Arrays.asList(area.getUIDs(true, area.blocksAllowAny)));
			a.whitelists.blocks.allowLeftclick.addAll(Arrays.asList(area.getUIDs(area.blocksAllowLeftclick)));
			a.whitelists.blocks.allowBreak.addAll(Arrays.asList(area.getUIDs(area.blocksAllowBreak)));
			a.whitelists.blocks.allowRightclick.addAll(Arrays.asList(area.getUIDs(area.blocksAllowRightclick)));

			a.whitelists.items.allowRightclick.addAll(Arrays.asList(area.getUIDs(area.itemsAllowRightclick)));

			a.whitelists.entities.allowLeftclick.addAll(Arrays.asList(area.getUIDs(area.entitiesAllowLeftclick)));
			a.whitelists.entities.allowRightclick.addAll(Arrays.asList(area.getUIDs(area.entitiesAllowRightclick)));

			a.permissions.addAll(area.permissions);

			if(area.dim != null && area.pos != null) {
				a.dim = area.dim;
				a.pos = new AreaStorage.Position();
				a.pos.minX = MathHelper.floor(area.pos.minX);
				a.pos.minY = MathHelper.floor(area.pos.minY);
				a.pos.minZ = MathHelper.floor(area.pos.minZ);
				a.pos.maxX = MathHelper.floor(area.pos.maxX);
				a.pos.maxY = MathHelper.floor(area.pos.maxY);
				a.pos.maxZ = MathHelper.floor(area.pos.maxZ);
			}
			a.name = area.name;
			areas.add(a);
		}
		return config;
	}

	public boolean mayLeftclick(EntityPlayer player, World world, BlockPos pos) {
		for(Area area : areas) {
			if(area.mayLeftclick(player, world, pos)) {
				return true;
			}
		}
		for(EnumFacing dir : EnumFacing.VALUES) {
			TileEntity tile = world.getTileEntity(pos.offset(dir));
			if(tile instanceof TileEntitySign) {
				for(ITextComponent s : ((TileEntitySign) tile).signText) {
					if("[public left]".equalsIgnoreCase(s.getUnformattedText())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean mayLeftclick(EntityPlayer player, Entity entity) {
		for(Area area : areas) {
			if(area.mayLeftclick(player, entity)) {
				return true;
			}
		}
		return false;
	}

	public boolean mayBreak(EntityPlayer player, World world, BlockPos pos) {
		for(Area area : areas) {
			if(area.mayBreak(player, world, pos)) {
				return true;
			}
		}
		for(EnumFacing dir : EnumFacing.VALUES) {
			TileEntity tile = world.getTileEntity(pos.offset(dir));
			if(tile instanceof TileEntitySign) {
				for(ITextComponent s : ((TileEntitySign) tile).signText) {
					if("[public break]".equalsIgnoreCase(s.getUnformattedText())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean mayRightclick(Entity user, @Nullable ItemStack stack) {
		for(Area area : areas) {
			if(area.mayRightclick(user, stack)) {
				return true;
			}
		}
		return false;
	}

	public boolean mayRightclick(EntityPlayer player, World world, BlockPos pos) {
		for(Area area : areas) {
			if(area.mayRightclick(player, world, pos)) {
				return true;
			}
		}
		for(EnumFacing dir : EnumFacing.VALUES) {
			TileEntity tile = world.getTileEntity(pos.offset(dir));
			if(tile instanceof TileEntitySign) {
				for(ITextComponent s : ((TileEntitySign) tile).signText) {
					if("[public right]".equalsIgnoreCase(s.getUnformattedText())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean mayRightclick(EntityPlayer player, Entity entity) {
		for(Area area : areas) {
			if(area.mayRightclick(player, entity)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasPermission(String id, EntityPlayer player) {
		for(Area area : areas) {
			if(area.hasPermission(id, player)) {
				return true;
			}
		}
		return false;
	}

	private static boolean matchSneak(boolean isSneaking, @Nullable Boolean shouldSneak) {
		return shouldSneak == null || isSneaking == shouldSneak;
	}

	private static class Entry {

		public final String name;
		public final String modid;
		public final int meta;
		public final Boolean sneak;

		private Entry(String name, String modid, int meta, @Nullable Boolean sneak) {
			this.name = name;
			this.modid = modid;
			this.meta = meta;
			this.sneak = sneak;
		}

		public ItemData itemData() {
			return new ItemData(this.meta, this.sneak);
		}
	}

	public static class ItemData {

		public final int metadata;
		public final Boolean sneak;

		public ItemData(int metadata, @Nullable Boolean sneak) {
			this.metadata = metadata;
			this.sneak = sneak;
		}

		@Override
		public int hashCode() {
			return Objects.hash(metadata, sneak);
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ItemData) {
				ItemData data = (ItemData) obj;
				return metadata == data.metadata && Objects.equals(sneak, data.sneak);
			}
			return false;
		}
	}

	public static class BlockList extends ArrayList<Pair<Block, ItemData>> {

	}

	public static class ItemList extends ArrayList<Pair<Item, ItemData>> {

	}

	public static class StringList extends ArrayList<String> {

	}

	public static class EntityList extends ArrayList<Pair<String, Boolean>> {

	}

	private final List<String> excludedPlayers = new ArrayList<String>();

	public boolean isExcluded(String name) {
		return excludedPlayers.contains(name);
	}

	public void excludePlayer(String name) {
		if(isExcluded(name)) {
			excludedPlayers.remove(name);
		} else {
			excludedPlayers.add(name);
		}
	}

	// Position management

	public final HashMap<String, BlockPos> positions1 = new HashMap<String, BlockPos>();
	public final HashMap<String, BlockPos> positions2 = new HashMap<String, BlockPos>();

	public final AreaList areas = new AreaList();

	public static class AreaList extends ArrayList<Area> {

	}

	public static class Area {

		public final BlockList blocksAllowAny = new BlockList();
		public final BlockList blocksAllowLeftclick = new BlockList();
		public final BlockList blocksAllowBreak = new BlockList();
		public final BlockList blocksAllowRightclick = new BlockList();
		//private List<Pair<Item, Integer>> itemsAllowAny = new ArrayList<Pair<Item, Integer>>();
		public final ItemList itemsAllowRightclick = new ItemList();

		public final EntityList entitiesAllowRightclick = new EntityList();
		public final EntityList entitiesAllowLeftclick = new EntityList();

		public final StringList permissions = new StringList();

		public Integer dim;
		public AxisAlignedBB pos;
		public String name;

		public Area(String name, int dim, BlockPos pos1, BlockPos pos2) {
			this.dim = dim;
			this.pos = new AxisAlignedBB(pos1, pos2);
			this.name = name;
		}

		public Area(String name) {
			this.name = name;
			this.dim = null;
			this.pos = null;
		}

		private static boolean isInside(AxisAlignedBB bb, double xCoord, double yCoord, double zCoord) {
			return (xCoord >= bb.minX && xCoord <= bb.maxX) && ((yCoord >= bb.minY && yCoord <= bb.maxY) && (zCoord >= bb.minZ && zCoord <= bb.maxZ));
		}

		private static boolean isInside(AxisAlignedBB bb, BlockPos pos) {
			return isInside(bb, pos.getX(), pos.getY(), pos.getZ());
		}

		public boolean isInArea(@Nullable Entity entity) {
			return entity != null && entity.world != null && entity.world.provider != null && entity.world.provider.getDimension() == dim
				&& isInside(this.pos, entity.posX, entity.posY, entity.posZ);
		}

		public boolean isInArea(World world, BlockPos pos) {
			return world.provider != null && world.provider.getDimension() == dim && isInside(this.pos, pos);
		}

		public String[] getUIDs(BlockList... lists) {
			return getUIDs(false, lists);
		}

		public String[] getUIDs(boolean isAllowAny, BlockList... lists) {
			ArrayList<String> uids = new ArrayList<String>();
			for(BlockList list : lists) {
				for(Pair<Block, ItemData> pair : list) {
					if(!isAllowAny && blocksAllowAny.contains(pair)) {
						continue;
					}
					final String uid = RegistryUtil.getRegistryName(pair.getKey());
					if(uid != null) {
						String s = uid + (pair.getValue().metadata != -1 ? ("@" + pair.getValue().metadata) : "");
						if(pair.getValue().sneak != null) {
							s += "!" + (pair.getValue().sneak ? "s" : "n");
						}
						if(uids.contains(s) || uids.contains(uid)) {
							continue;
						}
						uids.add(s);
					}
				}
			}
			return uids.toArray(new String[uids.size()]);
		}

		public String[] getUIDs(ItemList... lists) {
			ArrayList<String> uids = new ArrayList<String>();
			for(ItemList list : lists) {
				for(Pair<Item, ItemData> pair : list) {
					final String uid = RegistryUtil.getRegistryName(pair.getKey());
					if(uid != null) {
						String s = uid + (pair.getValue().metadata != -1 ? ("@" + pair.getValue().metadata) : "");
						if(pair.getValue().sneak != null) {
							s += "!" + (pair.getValue().sneak ? "s" : "n");
						}
						uids.add(s);
					}
				}
			}
			return uids.toArray(new String[uids.size()]);
		}

		public String[] getUIDs(EntityList... lists) {
			ArrayList<String> uids = new ArrayList<String>();
			for(EntityList list : lists) {
				for(Pair<String, Boolean> pair : list) {
					String s = pair.getKey();
					if(pair.getValue() != null) {
						s += "!" + (pair.getValue() ? "s" : "n");
					}
					uids.add(s);
				}
			}
			return uids.toArray(new String[uids.size()]);
		}

		private void fillBlockList(String[] blockList, BlockList... toFill) {
			for(String data : blockList) {
				Entry entry = getEntry(data);
				Block block = Block.REGISTRY.getObject(new ResourceLocation(entry.modid, entry.name));
				if(block != null) {
					Pair<Block, ItemData> pair = Pair.of(block, entry.itemData());
					for(BlockList list : toFill) {
						list.add(pair);
					}
				} else {
					Conventional.log.warn("Found a block added to a whitelist that does not exist: " + data);
				}
			}
		}

		private void fillItemList(String[] itemList, ItemList... toFill) {
			for(String data : itemList) {
				Entry entry = getEntry(data);
				Item item = Item.REGISTRY.getObject(new ResourceLocation(entry.modid, entry.name));
				if(item != null) {
					Pair<Item, ItemData> pair = Pair.of(item, entry.itemData());
					for(ItemList list : toFill) {
						list.add(pair);
					}
				} else {
					Conventional.log.warn("Found an item added to a whitelist that does not exist: " + data);
				}
			}
		}

		private void fillEntityList(String[] entityList, EntityList... toFill) {
			for(String data : entityList) {
				Entry entry = getEntry(data); // Hijacking the block and item parser because it works for now.
				Pair<String, Boolean> pair = Pair.of(entry.name, entry.sneak);
				for(EntityList list : toFill) {
					list.add(pair);
				}
			}
		}

		private void fillStringList(String[] entityList, StringList... toFill) {
			for(StringList list : toFill) {
				Collections.addAll(list, entityList);
			}
		}

		private Entry getEntry(String data) {
			final int boolSplitIndex = data.lastIndexOf("!");
			final Boolean sneak;
			if(boolSplitIndex > 0) {
				String modifiers = data.substring(boolSplitIndex);
				data = data.substring(0, boolSplitIndex);
				sneak = modifiers.contains("s") ? Boolean.TRUE : modifiers.contains("n") ? Boolean.FALSE : null;
			} else {
				sneak = null;
			}

			final int splitIndex = data.lastIndexOf('@');
			String name, optMeta, modid;
			if(splitIndex > 0) {
				name = data.substring(0, splitIndex);
				optMeta = data.substring(splitIndex);
			} else {
				name = data;
				optMeta = "";
			}
			final int modSplitIndex = data.indexOf(':');
			if(modSplitIndex > 0) {
				modid = name.substring(0, modSplitIndex);
				name = name.substring(modSplitIndex + 1);
			} else {
				modid = "minecraft";
			}

			final int meta = (Strings.isNullOrEmpty(optMeta)) ? -1 : Integer.parseInt(optMeta.substring(1));
			return new Entry(name, modid, meta, sneak);
		}

		private boolean mayLeftclick(EntityPlayer player, @Nullable IBlockState state) {
			if(state == null) {
				return true;
			}
			//final Pair<Block, Integer> toTest = new Pair<Block, Integer>(block, meta);
			final Block block = state.getBlock();
			for(Pair<Block, ItemData> pair : blocksAllowLeftclick) {
				if(pair.getKey().equals(block) && (pair.getValue().metadata == -1 || pair.getValue().metadata == block.getMetaFromState(state))) {
					return matchSneak(player.isSneaking(), pair.getValue().sneak);
				}
			}
			return false;
		}

		public boolean mayLeftclick(EntityPlayer player, World world, BlockPos pos) {
			return isInArea(world, pos) && mayLeftclick(player, world.getBlockState(pos));
		}

		public boolean mayLeftclick(EntityPlayer player, Entity entity) {
			if(!isInArea(entity)) {
				return false;
			}
			for(Pair<String, Boolean> pair : entitiesAllowLeftclick) {
				if(pair.getKey().equals(entity.getClass().getCanonicalName())) {
					return matchSneak(player.isSneaking(), pair.getValue());
				}
			}
			return false;
		}

		private boolean mayBreak(EntityPlayer player, @Nullable IBlockState state) {
			if(state == null) {
				return true;
			}
			//final Pair<Block, Integer> toTest = new Pair<Block, Integer>(block, meta);
			final Block block = state.getBlock();
			for(Pair<Block, ItemData> pair : blocksAllowBreak) {
				if(pair.getKey().equals(block) && (pair.getValue().metadata == -1 || pair.getValue().metadata == block.getMetaFromState(state))) {
					return matchSneak(player.isSneaking(), pair.getValue().sneak);
				}
			}
			return false;
		}

		public boolean mayBreak(EntityPlayer player, World world, BlockPos pos) {
			return isInArea(world, pos) && mayBreak(player, world.getBlockState(pos));
		}

		private boolean mayRightclick(EntityPlayer player, @Nullable IBlockState state) {
			if(state == null) {
				return true;
			}
			//final Pair<Block, Integer> toTest = new Pair<Block, Integer>(block, meta);
			final Block block = state.getBlock();
			for(Pair<Block, ItemData> pair : blocksAllowRightclick) {
				if(pair.getKey().equals(block) && (pair.getValue().metadata == -1 || pair.getValue().metadata == block.getMetaFromState(state))) {
					return matchSneak(player.isSneaking(), pair.getValue().sneak);
				}
			}
			return false;
		}

		public boolean mayRightclick(Entity user, @Nullable ItemStack stack) {
			if(!isInArea(user)) {
				return false;
			}
			if(stack == null) {
				return false;
			}
			for(Pair<Item, ItemData> pair : itemsAllowRightclick) {
				if(pair.getKey().equals(stack.getItem()) && (pair.getValue().metadata == -1 || pair.getValue().metadata == stack.getItemDamage())) {
					return matchSneak(user.isSneaking(), pair.getValue().sneak);
				}
			}
			return false;
		}

		public boolean mayRightclick(EntityPlayer player, World world, BlockPos pos) {
			return isInArea(world, pos) && mayRightclick(player, world.getBlockState(pos));
		}

		public boolean mayRightclick(EntityPlayer player, Entity entity) {
			if(!isInArea(entity)) {
				return false;
			}
			for(Pair<String, Boolean> pair : entitiesAllowRightclick) {
				if(pair.getKey().equals(entity.getClass().getCanonicalName())) {
					return matchSneak(player.isSneaking(), pair.getValue());
				}
			}
			return false;
		}

		public boolean hasPermission(String id, EntityPlayer player) {
			return isInArea(player) && permissions.contains(id);
		}
	}
}
