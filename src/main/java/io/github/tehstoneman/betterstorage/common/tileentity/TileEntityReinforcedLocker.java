package io.github.tehstoneman.betterstorage.common.tileentity;

import io.github.tehstoneman.betterstorage.BetterStorage;
import io.github.tehstoneman.betterstorage.ModInfo;
import io.github.tehstoneman.betterstorage.api.IHasConfig;
import io.github.tehstoneman.betterstorage.api.IHexKeyConfig;
import io.github.tehstoneman.betterstorage.api.lock.IKey;
import io.github.tehstoneman.betterstorage.api.lock.IKeyLockable;
import io.github.tehstoneman.betterstorage.common.capabilities.CapabilityConfig;
import io.github.tehstoneman.betterstorage.common.enchantment.EnchantmentBetterStorage;
import io.github.tehstoneman.betterstorage.common.inventory.ConfigContainer;
import io.github.tehstoneman.betterstorage.common.inventory.ContainerReinforcedLocker;
import io.github.tehstoneman.betterstorage.common.item.BetterStorageItems;
import io.github.tehstoneman.betterstorage.common.world.storage.HexKeyConfig;
import io.github.tehstoneman.betterstorage.config.BetterStorageConfig;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityReinforcedLocker extends TileEntityLocker implements IKeyLockable, IHasConfig
{
	private boolean								powered;
	private ItemStack							lock			= ItemStack.EMPTY.copy();
	public HexKeyConfig							config;
	private final LazyOptional< IHexKeyConfig >	configHandler	= LazyOptional.of( () -> config );

	public TileEntityReinforcedLocker()
	{
		super( BetterStorageTileEntityTypes.REINFORCED_LOCKER.get() );
		config = new HexKeyConfig()
		{
			@Override
			protected void onContentsChanged( int slot )
			{
				TileEntityReinforcedLocker.this.markDirty();
			}
		};
	}

	@Override
	public <T> LazyOptional< T > getCapability( Capability< T > capability, Direction facing )
	{
		if( capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && isLocked() && facing != null )
			return LazyOptional.empty();
		if( capability == CapabilityConfig.CONFIG_CAPABILITY && !isLocked() )
			return CapabilityConfig.CONFIG_CAPABILITY.orEmpty( capability, configHandler );
		return super.getCapability( capability, facing );
	}

	@Override
	public int getColumns()
	{
		return BetterStorageConfig.COMMON.reinforcedColumns.get();
	}

	@Override
	protected String getConnectableName()
	{
		return ModInfo.CONTAINER_REINFORCED_LOCKER_NAME;
	}

	@Override
	public void dropInventoryItems()
	{
		super.dropInventoryItems();
		if( !config.isEmpty() )
			if( isConnected() && isMain() )
				( (TileEntityReinforcedLocker)getConnectedTileEntity() ).setConfig( config );
			else
				for( int i = 0; i < config.getSlots(); i++ )
				{
					final ItemStack stack = config.getStackInSlot( i );
					if( !stack.isEmpty() )
						InventoryHelper.spawnItemStack( getWorld(), pos.getX(), pos.getY(), pos.getZ(), stack );
				}
	}

	@Override
	public Container createMenu( int windowID, PlayerInventory playerInventory, PlayerEntity player )
	{
		if( isMain() )
		{
			if( player.getHeldItemMainhand().getItem() == BetterStorageItems.HEX_KEY.get() )
				return new ConfigContainer( windowID, playerInventory, world, pos );
			return new ContainerReinforcedLocker( windowID, playerInventory, world, pos );
		}
		else
			return getMainTileEntity().createMenu( windowID, playerInventory, player );
	}

	@Override
	public void tick()
	{
		final int x = pos.getX();
		final int y = pos.getY();
		final int z = pos.getZ();
		++ticksSinceSync;
		if( !world.isRemote && numPlayersUsing != 0 && ( ticksSinceSync + x + y + z ) % 200 == 0 )
		{
			numPlayersUsing = 0;
			final float f = 5.0F;

			for( final PlayerEntity entityplayer : world.getEntitiesWithinAABB( PlayerEntity.class,
					new AxisAlignedBB( x - 5.0F, y - 5.0F, z - 5.0F, x + 1 + 5.0F, y + 1 + 5.0F, z + 1 + 5.0F ) ) )
				if( entityplayer.openContainer instanceof ContainerReinforcedLocker )
					++numPlayersUsing;
		}

		prevLidAngle = lidAngle;
		final float f1 = 0.1F;
		if( numPlayersUsing > 0 && lidAngle == 0.0F )
			playSound( SoundEvents.BLOCK_CHEST_OPEN );

		if( numPlayersUsing == 0 && lidAngle > 0.0F || numPlayersUsing > 0 && lidAngle < 1.0F )
		{
			final float f2 = lidAngle;
			if( numPlayersUsing > 0 )
				lidAngle += 0.1F;
			else
				lidAngle -= 0.1F;

			if( lidAngle > 1.0F )
				lidAngle = 1.0F;

			final float f3 = 0.5F;
			if( lidAngle < 0.5F && f2 >= 0.5F )
				playSound( SoundEvents.BLOCK_CHEST_CLOSE );

			if( lidAngle < 0.0F )
				lidAngle = 0.0F;
		}
	}

	/*
	 * ============
	 * IKeyLockable
	 * ============
	 */

	@Override
	public ItemStack getLock()
	{
		if( isMain() )
			return lock;
		else
			return ( (IKeyLockable)getMainTileEntity() ).getLock();
	}

	@Override
	public void setLock( ItemStack lock )
	{
		if( isMain() )
		{
			if( lock.isEmpty() || isLockValid( lock ) )
			{
				this.lock = lock;
				getWorld().notifyBlockUpdate( pos, getBlockState(), getBlockState(), 3 );
				setPowered( EnchantmentHelper.getEnchantmentLevel( EnchantmentBetterStorage.TRIGGER.get(), lock ) > 0 );
				markDirty();
			}
		}
		else
			( (IKeyLockable)getMainTileEntity() ).setLock( lock );
	}

	@Override
	public boolean canUse( PlayerEntity player )
	{
		return !isLocked() || getMainTileEntity().getPlayersUsing() > 0;
	}

	@Override
	public void applyTrigger()
	{
		setPowered( true );
	}

	@Override
	public boolean unlockWith( ItemStack heldItem )
	{
		final Item item = heldItem.getItem();
		return item instanceof IKey ? ( (IKey)item ).unlock( heldItem, getLock(), false ) : false;
	}

	// Trigger enchantment related

	/**
	 * Returns if the chest is emitting redstone.
	 *
	 * @return True if powered
	 */
	public boolean isPowered()
	{
		if( isMain() )
			return EnchantmentHelper.getEnchantmentLevel( EnchantmentBetterStorage.TRIGGER.get(), getLock() ) > 0;
		return ( (TileEntityReinforcedChest)getMainTileEntity() ).isPowered();
	}

	/**
	 * Sets if the chest is emitting redstone.
	 * <p>
	 * Updates all nearby blocks to make sure they notice it.
	 *
	 * @param powered
	 *            True if powered
	 */
	public void setPowered( boolean powered )
	{
		if( !isMain() )
		{
			( (TileEntityReinforcedLocker)getMainTileEntity() ).setPowered( powered );
			return;
		}

		this.powered = powered;

		final Block block = getBlockState().getBlock();

		// Notify nearby blocks
		getWorld().notifyNeighborsOfStateChange( pos, block );

		// Notify nearby blocks of adjacent chest
		if( isConnected() )
			getWorld().notifyNeighborsOfStateChange( getConnected(), block );
	}

	@Override
	public HexKeyConfig getConfig()
	{
		return config;
	}

	@Override
	public void setConfig( HexKeyConfig config )
	{
		this.config = config;
		markDirty();
	}

	/*
	 * ==========================
	 * TileEntity synchronization
	 * ==========================
	 */

	@Override
	public CompoundNBT getUpdateTag()
	{
		final CompoundNBT nbt = super.getUpdateTag();

		if( !config.isEmpty() )
			nbt.put( "Config", config.serializeNBT() );
		if( !lock.isEmpty() )
			nbt.put( "lock", lock.serializeNBT() );

		return nbt;
	}

	@Override
	public void onDataPacket( NetworkManager network, SUpdateTileEntityPacket packet )
	{
		final CompoundNBT nbt = packet.getNbtCompound();
		if( nbt.contains( "Config" ) )
			config.deserializeNBT( nbt.getCompound( "Config" ) );
		else
			config = new HexKeyConfig();
		if( nbt.contains( "lock" ) )
		{
			final CompoundNBT lockNBT = (CompoundNBT)nbt.get( "lock" );
			lock = ItemStack.read( lockNBT );
		}
		else
			lock = ItemStack.EMPTY;
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		return new SUpdateTileEntityPacket( pos, 1, getUpdateTag() );
	}

	@Override
	public void handleUpdateTag( CompoundNBT nbt )
	{
		super.handleUpdateTag( nbt );

		if( nbt.contains( "Config" ) )
			config.deserializeNBT( nbt.getCompound( "Config" ) );
		else
			config = new HexKeyConfig();
		if( nbt.contains( "lock" ) )
		{
			final CompoundNBT lockNBT = (CompoundNBT)nbt.get( "lock" );
			lock = ItemStack.read( lockNBT );
		}
		else
			lock = ItemStack.EMPTY;
	}

	@Override
	public CompoundNBT write( CompoundNBT nbt )
	{
		if( !config.isEmpty() )
			nbt.put( "Config", config.serializeNBT() );
		if( !lock.isEmpty() )
		{
			final CompoundNBT lockNBT = new CompoundNBT();
			lock.write( lockNBT );
			nbt.put( "lock", lockNBT );
		}

		return super.write( nbt );
	}

	@Override
	public void read( CompoundNBT nbt )
	{
		if( nbt.contains( "Config" ) )
			config.deserializeNBT( nbt.getCompound( "Config" ) );
		else
			config = new HexKeyConfig();
		if( nbt.contains( "lock" ) )
		{
			final CompoundNBT lockNBT = (CompoundNBT)nbt.get( "lock" );
			lock = ItemStack.read( lockNBT );
		}
		else
			lock = ItemStack.EMPTY;

		super.read( nbt );
	}
}
