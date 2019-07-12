package io.github.tehstoneman.betterstorage.proxy;

import io.github.tehstoneman.betterstorage.api.IProxy;
import io.github.tehstoneman.betterstorage.client.gui.GuiBetterStorage;
import io.github.tehstoneman.betterstorage.client.renderer.TileEntityLockerRenderer;
import io.github.tehstoneman.betterstorage.common.inventory.BetterStorageContainerTypes;
import io.github.tehstoneman.betterstorage.common.tileentity.TileEntityLocker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

//@SideOnly( Side.CLIENT )
public class ClientProxy implements IProxy
{
	// public static final Map< Class< ? extends TileEntity >, BetterStorageRenderingHandler > renderingHandlers = new HashMap<>();

	@Override
	public void setup( FMLCommonSetupEvent event )
	{
		// OBJLoader.INSTANCE.addDomain( ModInfo.modId );
		// ClientRegistry.bindTileEntitySpecialRenderer( TileEntityReinforcedChest.class, new TileEntityReinforcedChestRenderer() );
		ClientRegistry.bindTileEntitySpecialRenderer( TileEntityLocker.class, new TileEntityLockerRenderer() );

		ScreenManager.registerFactory( BetterStorageContainerTypes.LOCKER, GuiBetterStorage::new );
	}

	@Override
	public World getClientWorld()
	{
		return Minecraft.getInstance().world;
	}
	/*
	 * @Override
	 * public void initialize()
	 * {
	 * super.initialize();
	 *
	 * // new KeyBindingHandler();
	 *
	 * registerRenderers();
	 * BetterStorageColorHandler.registerColorHandlers();
	 * }
	 */

	/*@Override
	public void postInit()
	{
		super.postInit();

		//@formatter:off
		Minecraft.getInstance().getItemColors().registerItemColorHandler( new KeyColor(),
				BetterStorageItems.KEY,
				BetterStorageItems.LOCK );
		Minecraft.getInstance().getItemColors().registerItemColorHandler( new CardboardColor(),
				Item.getItemFromBlock( BetterStorageBlocks.CARDBOARD_BOX ),
				BetterStorageItems.CARDBOARD_AXE,
				BetterStorageItems.CARDBOARD_BOOTS,
				BetterStorageItems.CARDBOARD_CHESTPLATE,
				BetterStorageItems.CARDBOARD_HELMET,
				BetterStorageItems.CARDBOARD_HOE,
				BetterStorageItems.CARDBOARD_LEGGINGS,
				BetterStorageItems.CARDBOARD_PICKAXE,
				BetterStorageItems.CARDBOARD_SHOVEL,
				BetterStorageItems.CARDBOARD_SWORD );
		//@formatter:on
	}*/

	/*
	 * private void registerRenderers()
	 * {
	 * // RenderingRegistry.registerEntityRenderingHandler(EntityFrienderman.class, new RenderFrienderman());
	 * // RenderingRegistry.registerEntityRenderingHandler(EntityCluckington.class, new RenderChicken(new ModelCluckington(), 0.4F));
	 *
	 * // ClientRegistry.bindTileEntitySpecialRenderer( TileEntityReinforcedChest.class, new TileEntityReinforcedChestRenderer() );
	 * // ClientRegistry.bindTileEntitySpecialRenderer( TileEntityReinforcedLocker.class, new TileEntityLockerRenderer() );
	 * // ClientRegistry.bindTileEntitySpecialRenderer( TileEntityLockableDoor.class, new TileEntityLockableDoorRenderer() );
	 * // ClientRegistry.bindTileEntitySpecialRenderer( TileEntityPresent.class, new TileEntityPresentRenderer() );
	 * // Addon.registerRenderersAll();
	 * }
	 */
}
