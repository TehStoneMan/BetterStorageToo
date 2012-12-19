package net.mcft.copy.betterstorage;

import java.io.File;

import net.minecraftforge.common.Configuration;

public class Config {
	
	// The following IDs are also used as default values.
	
	public static int crateId = 2830;
	// Defines the beginning of a ~20 id range.
	public static int chestBaseId = 2835;
	
	public static int keyId = 28540;
	public static int lockId = 28545;
	
	/** When set to true, will make all chests the vanilla chest size. */
	public static boolean normalSizedChests = false;
	
	private static Configuration config;
	
	public static void load(File file) {
		config = new Configuration(file);
		config.load();
		
		crateId     = config.getBlock("crate", crateId).getInt();
		chestBaseId = config.getBlock("chestBaseId", chestBaseId).getInt();
		
		keyId  = config.getItem("key", keyId).getInt();
		lockId = config.getItem("lock", lockId).getInt();
		
		normalSizedChests = config.get(Configuration.CATEGORY_GENERAL, "normalSizedChests", normalSizedChests,
		                               "When set to true, will make all chests the vanilla chest size.").getBoolean(normalSizedChests);
	}
	
	public static void save() {
		config.save();
	}
	
}