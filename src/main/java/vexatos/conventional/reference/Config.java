package vexatos.conventional.reference;

import com.google.common.base.Strings;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.tuple.Pair;
import vexatos.conventional.Conventional;

import java.util.ArrayList;

/**
 * @author Vexatos
 */
public class Config {

	private Configuration config;
	//private List<Pair<Block, Integer>> blocksAllowAny = new ArrayList<Pair<Block, Integer>>();
	public BlockList blocksAllowLeftclick = new BlockList();
	public BlockList blocksAllowRightclick = new BlockList();
	//private List<Pair<Item, Integer>> itemsAllowAny = new ArrayList<Pair<Item, Integer>>();
	public ItemList itemsAllowRightclick = new ItemList();

	public Config(Configuration configuration) {
		config = configuration;
	}

	public void reload() {
		blocksAllowLeftclick.clear();
		blocksAllowRightclick.clear();
		itemsAllowRightclick.clear();
		config.load();
		fillBlockList(
			config.getStringList("allowAnything", "blocks", new String[0], "Allow any interaction with these blocks."),
			blocksAllowLeftclick, blocksAllowRightclick
		);
		fillBlockList(
			config.getStringList("allowLeftclick", "blocks", new String[0], "Allow left clicking these blocks."),
			blocksAllowLeftclick
		);
		fillBlockList(
			config.getStringList("allowRightclick", "blocks", new String[0], "Allow right clicking these blocks."), blocksAllowRightclick
		);
		fillItemList(
			config.getStringList("allowRightclick", "items", new String[0], "Allow right clicking these items."),
			itemsAllowRightclick
		);
	}

	private String[] getUIDs(BlockList... lists) {
		ArrayList<String> uids = new ArrayList<String>();
		for(BlockList list : lists) {
			for(Pair<Block, Integer> pair : list) {
				GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(pair.getKey());
				if(uid != null) {
					String name = uid.toString();
					String s = name + (pair.getValue() != -1 ? ("@" + pair.getValue()) : "");
					if(uids.contains(s) || uids.contains(name)) {
						continue;
					}
					uids.add(s);
				}
			}
		}
		return uids.toArray(new String[uids.size()]);
	}

	private String[] getUIDs(ItemList... lists) {
		ArrayList<String> uids = new ArrayList<String>();
		for(ItemList list : lists) {
			for(Pair<Item, Integer> pair : list) {
				GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(pair.getKey());
				if(uid != null) {
					uids.add(uid.toString() + (pair.getValue() != -1 ? ("@" + pair.getValue()) : ""));
				}
			}
		}
		return uids.toArray(new String[uids.size()]);
	}

	private void fillBlockList(String[] blockList, BlockList... toFill) {
		for(String data : blockList) {
			Entry entry = getEntry(data);
			Block block = GameRegistry.findBlock(entry.modid, entry.name);
			if(block != null) {
				Pair<Block, Integer> pair = Pair.of(block, entry.meta);
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
			Item item = GameRegistry.findItem(entry.modid, entry.name);
			if(item != null) {
				Pair<Item, Integer> pair = Pair.of(item, entry.meta);
				for(ItemList list : toFill) {
					list.add(pair);
				}
			} else {
				Conventional.log.warn("Found an item added to a whitelist that does not exist: " + data);
			}
		}
	}

	private Entry getEntry(String data) {
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
		return new Entry(name, modid, meta);
	}

	public void save() {
		config.get("blocks", "allowLeftclick", new String[0]).setValues(getUIDs(blocksAllowLeftclick));
		config.get("blocks", "allowRightclick", new String[0]).setValues(getUIDs(blocksAllowRightclick));
		config.get("items", "allowRightclick", new String[0]).setValues(getUIDs(itemsAllowRightclick));
		config.save();
	}

	public boolean mayLeftclick(Block block, int meta) {
		if(block == null) {
			return true;
		}
		//final Pair<Block, Integer> toTest = new Pair<Block, Integer>(block, meta);
		for(Pair<Block, Integer> pair : blocksAllowLeftclick) {
			if(pair.getKey().equals(block) && (pair.getValue() == -1 || pair.getValue() == meta)) {
				return true;
			}
		}
		return false;
	}

	public boolean mayLeftclick(World world, int x, int y, int z) {
		return mayLeftclick(world.getBlock(x, y, z), world.getBlockMetadata(x, y, z));
	}

	public boolean mayRightclick(Block block, int meta) {
		if(block == null) {
			return true;
		}
		//final Pair<Block, Integer> toTest = new Pair<Block, Integer>(block, meta);
		for(Pair<Block, Integer> pair : blocksAllowRightclick) {
			if(pair.getKey().equals(block) && (pair.getValue() == -1 || pair.getValue() == meta)) {
				return true;
			}
		}
		return false;
	}

	public boolean mayRightclick(ItemStack stack) {
		if(stack == null || stack.getItem() == null) {
			return true;
		}
		for(Pair<Item, Integer> pair : itemsAllowRightclick) {
			if(pair.getKey().equals(stack.getItem()) && (pair.getValue() == -1 || pair.getValue() == stack.getItemDamage())) {
				return true;
			}
		}
		return false;
	}

	public boolean mayRightclick(World world, int x, int y, int z) {
		return mayRightclick(world.getBlock(x, y, z), world.getBlockMetadata(x, y, z));
	}

	private static class Entry {

		public final String name;
		public final String modid;
		public final int meta;

		private Entry(String name, String modid, int meta) {
			this.name = name;
			this.modid = modid;
			this.meta = meta;
		}
	}

	public class BlockList extends ArrayList<Pair<Block, Integer>> {

	}

	public class ItemList extends ArrayList<Pair<Item, Integer>> {

	}
}
