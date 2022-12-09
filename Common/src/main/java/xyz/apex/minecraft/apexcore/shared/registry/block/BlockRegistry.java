package xyz.apex.minecraft.apexcore.shared.registry.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import xyz.apex.minecraft.apexcore.shared.registry.BasicRegistry;
import xyz.apex.minecraft.apexcore.shared.registry.ModdedRegistries;

import java.util.function.Function;
import java.util.function.Supplier;

public final class BlockRegistry extends BasicRegistry<Block>
{
    public static final ResourceKey<Registry<Block>> TYPE = Registries.BLOCK;

    private BlockRegistry(String modId)
    {
        super(TYPE, modId);
    }

    public <T extends Block> BlockBuilder<T> builder(String name, Function<BlockBehaviour.Properties, T> factory)
    {
        return new BlockBuilder<>(this, name, factory);
    }

    public BlockBuilder<Block> generic(String name)
    {
        return builder(name, Block::new);
    }

    @Override
    protected <R extends Block> BlockRegistryEntry<R> createRegistryEntry(ResourceKey<R> key, Supplier<R> supplier)
    {
        return new BlockRegistryEntry<>(this, key, supplier);
    }

    @Override
    public <R extends Block> BlockRegistryEntry<R> register(ResourceKey<Block> key, Supplier<R> factory)
    {
        return (BlockRegistryEntry<R>) super.register(key, factory);
    }

    @Override
    public <R extends Block> BlockRegistryEntry<R> register(String name, Supplier<R> factory)
    {
        return (BlockRegistryEntry<R>) super.register(name, factory);
    }

    @Nullable
    @Override
    public BlockRegistryEntry<Block> getEntry(ResourceKey<Block> key)
    {
        var entry = super.getEntry(key);
        return entry instanceof BlockRegistryEntry<Block> Block ? Block : null;
    }

    @Nullable
    @Override
    public BlockRegistryEntry<Block> getEntry(ResourceLocation id)
    {
        var entry = super.getEntry(id);
        return entry instanceof BlockRegistryEntry<Block> Block ? Block : null;
    }

    @Nullable
    @Override
    public BlockRegistryEntry<Block> getEntry(String name)
    {
        var entry = super.getEntry(name);
        return entry instanceof BlockRegistryEntry<Block> Block ? Block : null;
    }

    public static BlockRegistry create(String modId)
    {
        return ModdedRegistries.INSTANCE.getOrCreate(TYPE, modId, ModdedRegistries.Constructor.of(BlockRegistry::new));
    }
}
