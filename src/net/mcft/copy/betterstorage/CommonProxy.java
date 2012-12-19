package net.mcft.copy.betterstorage;

import net.mcft.copy.betterstorage.blocks.TileEntityCrate;
import net.mcft.copy.betterstorage.blocks.TileEntityReinforcedChest;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy {
	public void init() {
		registerTileEntites();
		preloadTextures();
	}
	
	public void registerTileEntites() {
		GameRegistry.registerTileEntity(TileEntityReinforcedChest.class, "container.reinforcedChest");
		GameRegistry.registerTileEntity(TileEntityCrate.class, "container.crate");
	}
	
	public void preloadTextures() {  }
	
}