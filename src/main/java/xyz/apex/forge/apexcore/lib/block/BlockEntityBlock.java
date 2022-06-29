package xyz.apex.forge.apexcore.lib.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BlockEntityBlock<T extends BlockEntity> extends Block implements EntityBlock
{
	public BlockEntityBlock(BlockBehaviour.Properties properties)
	{
		super(properties);
	}

	protected abstract BlockEntityType<T> getBlockEntityType();

	@Nullable
	protected T getBlockEntity(BlockGetter level, BlockPos pos)
	{
		return getBlockEntityType().getBlockEntity(level, pos);
	}

	@Override
	public boolean triggerEvent(BlockState blockState, Level level, BlockPos pos, int event, int param)
	{
		T blockEntity = getBlockEntity(level, pos);
		return blockEntity != null && blockEntity.triggerEvent(event, param);
	}

	@Nullable
	public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos pos)
	{
		var blockEntity = getBlockEntity(level, pos);
		return blockEntity instanceof MenuProvider menuProvider ? menuProvider : null;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState blockState)
	{
		return getBlockEntityType().create(pos, blockState);
	}
}