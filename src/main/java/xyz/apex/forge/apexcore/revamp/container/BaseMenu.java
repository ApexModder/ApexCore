package xyz.apex.forge.apexcore.revamp.container;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import xyz.apex.forge.apexcore.revamp.block.MultiBlockPattern;
import xyz.apex.forge.apexcore.revamp.block.entity.InventoryBlockEntity;
import xyz.apex.forge.apexcore.revamp.net.packet.SyncContainerPacket;

public class BaseMenu extends AbstractContainerMenu
{
	public final Player player;
	public final BlockPos pos;
	@Nullable public final BlockEntity blockEntity;

	public BaseMenu(@Nullable MenuType<? extends BaseMenu> menuType, int windowId, Inventory playerInventory, FriendlyByteBuf buffer)
	{
		super(menuType, windowId);

		player = playerInventory.player;

		pos = buffer.readBlockPos();
		blockEntity = player.level.getBlockEntity(pos);

	}

	@Nullable
	public final <BLOCK_ENTITY extends BlockEntity> BLOCK_ENTITY getBlockEntity(BlockEntityType<BLOCK_ENTITY> blockEntityType)
	{
		return blockEntity != null && blockEntity.getType() == blockEntityType ? (BLOCK_ENTITY) blockEntity : null;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex)
	{
		if(slots.isEmpty())
			return ItemStack.EMPTY;

		var stack = ItemStack.EMPTY;
		var slot = slots.get(slotIndex);

		if(slot.hasItem())
		{
			var stack1 = slot.getItem();
			stack = stack1.copy();
			IItemHandler itemHandler = null;

			if(blockEntity != null)
				itemHandler = blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).resolve().orElse(null);

			if(itemHandler != null && itemHandler.getSlots() > 0)
			{
				var maxIndex = itemHandler.getSlots();

				if(slotIndex < maxIndex)
				{
					if(!moveItemStackTo(stack1, maxIndex, slots.size(), true))
						return ItemStack.EMPTY;
				}
				else if(!moveItemStackTo(stack1, 0, maxIndex, false))
					return ItemStack.EMPTY;
			}
			else
			{
				if(!moveItemStackTo(stack1, 0, slots.size(), false))
					return ItemStack.EMPTY;
			}

			if(stack1.isEmpty())
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();
		}

		return stack;
	}

	@Override
	public boolean stillValid(Player player)
	{
		return player.isAlive() && player.containerMenu == this;
	}

	@Override
	public void broadcastChanges()
	{
		super.broadcastChanges();

		if(blockEntity != null)
			blockEntity.setChanged();

		if(blockEntity instanceof InventoryBlockEntity inventoryBlockEntity)
			SyncContainerPacket.sendToClient(inventoryBlockEntity);
	}

	public static void bindItemHandlerSlots(BaseMenu menu, IItemHandler itemHandler, int rows, int cols, int x, int y, MultiBlockPattern.QuadFunction<IItemHandler, Integer, Integer, Integer, SlotItemHandler> slotFactory)
	{
		for(var j = 0; j < rows; j++)
		{
			for(var k = 0; k < cols; k++)
			{
				menu.addSlot(slotFactory.apply(itemHandler, k + j * cols, x + k * 18, y + j * 18));
			}
		}
	}

	public static void bindItemHandlerSlots(BaseMenu menu, IItemHandler itemHandler, int rows, int cols, int x, int y)
	{
		bindItemHandlerSlots(menu, itemHandler, rows, cols, x, y, SlotItemHandler::new);
	}

	public static void bindPlayerInventory(BaseMenu menu)
	{
		bindPlayerInventory(menu, 8, 84);
	}

	public static void bindPlayerInventory(BaseMenu menu, int x, int y)
	{
		var playerInventory = menu.player.getInventory();

		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				menu.addSlot(new Slot(playerInventory, j + i * 9 + 9, x + j * 18, y + i * 18));
			}
		}

		for(int i = 0; i < 9; ++i)
		{
			menu.addSlot(new Slot(playerInventory, i, x + i * 18, y + 58));
		}
	}
}