package vexatos.conventional.util.storage;

import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * @author Vexatos
 */
public class AreaStorage {

	public String name;
	@Nullable
	public Integer dim;
	@Nullable
	public Position pos;

	public Whitelists whitelists = new Whitelists();
	public ArrayList<String> permissions = new ArrayList<>();

	public static class Position {

		public int minX, minY, minZ;
		public int maxX, maxY, maxZ;
	}

	public static class Whitelists {

		public Whitelists.Blocks blocks = new Whitelists.Blocks();
		public Whitelists.Items items = new Whitelists.Items();
		public Whitelists.Entities entities = new Whitelists.Entities();

		public static class Blocks {

			public ArrayList<String> allowAny = new ArrayList<>();
			public ArrayList<String> allowLeftclick = new ArrayList<>();
			public ArrayList<String> allowBreak = new ArrayList<>();
			public ArrayList<String> allowRightclick = new ArrayList<>();
		}

		public static class Items {

			public ArrayList<String> allowRightclick = new ArrayList<>();
		}

		public static class Entities {

			public ArrayList<String> allowRightclick = new ArrayList<>();
			public ArrayList<String> allowLeftclick = new ArrayList<>();
		}
	}
}
