package io.github.tehstoneman.betterstorage.common.tileentity;

import io.github.tehstoneman.betterstorage.BetterStorage;
import io.github.tehstoneman.betterstorage.common.inventory.ConnectedStackHandler;
import io.github.tehstoneman.betterstorage.config.BetterStorageConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.IChestLid;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public abstract class TileEntityConnectable extends TileEntityContainer implements IChestLid// , ITickable
{
	public ConnectedStackHandler				connectedInventory;
	private final LazyOptional< IItemHandler >	connectedHandler	= LazyOptional.of( () -> connectedInventory );

	public TileEntityConnectable( TileEntityType< ? > tileEntityTypeIn )
	{
		super( tileEntityTypeIn );
	}

	/*
	 * @Override
	 * public <T> LazyOptional< T > getCapability( Capability< T > capability, EnumFacing facing )
	 * {
	 * if( isMain() )
	 * if( isConnected() && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
	 * {
	 * connectedInventory = new ConnectedStackHandler( inventory, getConnectedTileEntity().inventory );
	 * return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty( capability, connectedHandler );
	 * }
	 * else
	 * return super.getCapability( capability, facing );
	 * return getMainTileEntity().getCapability( capability, facing );
	 * }
	 */

	/** Returns position of the connected TileEntity */
	public abstract BlockPos getConnected();

	/** Returns if this container is connected to another one. */
	public abstract boolean isConnected();

	/** Returns if this container is the main container, or not connected to another container. */
	public abstract boolean isMain();

	/** Returns the main container. */
	public TileEntityConnectable getMainTileEntity()
	{
		if( isMain() )
			return this;
		final TileEntityConnectable connectable = getConnectedTileEntity();
		if( connectable != null )
			return connectable;
		if( BetterStorageConfig.GENERAL.enableWarningMessages.get() )
			BetterStorage.LOGGER.warn( "getConnectedTileEntity() returned null in getMainTileEntity(). " + "Location: {},{},{}", pos.getX(),
					pos.getY(), pos.getZ() );
		return this;
	}

	/** Returns the connected container. */
	public TileEntityConnectable getConnectedTileEntity()
	{
		if( getWorld() == null || !isConnected() )
			return null;
		final TileEntity tileEntity = getWorld().getTileEntity( getConnected() );
		return tileEntity instanceof TileEntityConnectable ? (TileEntityConnectable)tileEntity : null;
	}

	/*
	 * ===================
	 * TileEntityContainer
	 * ===================
	 */

	/**
	 * Returns the unlocalized name of the container. <br>
	 * "Large" will be appended if the container is connected to another one.
	 */
	protected abstract String getConnectableName();

	/*
	 * @Override
	 * public ITextComponent getName()
	 * {
	 * return customName != null ? customName : new TextComponentTranslation( getConnectableName().concat( isConnected() ? "_large" : "" ) );
	 * }
	 */

	@Override
	protected boolean doesSyncPlayers()
	{
		return true;
	}

	@Override
	public int getRows()
	{
		if( isConnected() )
			return super.getRows() * 2;
		return super.getRows();
	}

	@Override
	public int getComparatorSignalStrength()
	{
		if( getWorld().isRemote )
			return 0;

		if( !isConnected() )
			return super.getComparatorSignalStrength();

		int i = 0;
		float f = 0.0F;

		for( int j = 0; j < inventory.getSlots(); ++j )
		{
			final ItemStack itemstack = inventory.getStackInSlot( j );

			if( itemstack != null )
			{
				f += (float)itemstack.getCount() / itemstack.getMaxStackSize();
				++i;
			}
		}

		final ItemStackHandler otherInventory = getConnectedTileEntity().inventory;

		for( int j = 0; j < otherInventory.getSlots(); ++j )
		{
			final ItemStack itemstack = otherInventory.getStackInSlot( j );

			if( itemstack != null )
			{
				f += (float)itemstack.getCount() / itemstack.getMaxStackSize();
				++i;
			}
		}

		f = f / ( inventory.getSlots() + otherInventory.getSlots() );
		return MathHelper.floor( f * 14.0F ) + ( i > 0 ? 1 : 0 );
	}

	// Update entity

	/*
	 * @Override
	 * public void update()
	 * {
	 * super.update();
	 *
	 * double x = pos.getX() + 0.5;
	 * final double y = pos.getY() + 0.5;
	 * double z = pos.getZ() + 0.5;
	 *
	 * if( isConnected() )
	 * {
	 * if( !isMain() )
	 * return;
	 * final TileEntityConnectable connectable = getConnectedTileEntity();
	 * if( connectable != null )
	 * {
	 * x = ( x + connectable.pos.getX() + 0.5 ) / 2;
	 * z = ( z + connectable.pos.getZ() + 0.5 ) / 2;
	 * lidAngle = Math.max( lidAngle, connectable.lidAngle );
	 * }
	 * }
	 *
	 * final float pitch = getWorld().rand.nextFloat() * 0.1F + 0.9F;
	 *
	 * // Play sound when opening
	 * if( lidAngle > 0.0F && prevLidAngle == 0.0F )
	 * getWorld().playSound( (EntityPlayer)null, x, y, z, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, pitch );
	 * // Play sound when closing
	 * if( lidAngle < 0.5F && prevLidAngle >= 0.5F )
	 * getWorld().playSound( (EntityPlayer)null, x, y, z, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, pitch );
	 * }
	 */

	/*
	 * @Override
	 * public ContainerBetterStorage getContainer( EntityPlayer player )
	 * {
	 * return new ContainerBetterStorage( getMainTileEntity(), player );
	 * }
	 */

	/*
	 * =========
	 * IChestLid
	 * =========
	 */

	protected abstract void playSound( SoundEvent soundIn );

	@OnlyIn( Dist.CLIENT )
	@Override
	public float getLidAngle( float partialTicks )
	{
		return prevLidAngle + ( lidAngle - prevLidAngle ) * partialTicks;
	}
}
