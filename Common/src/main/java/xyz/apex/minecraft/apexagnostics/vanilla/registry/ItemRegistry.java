package xyz.apex.minecraft.apexagnostics.vanilla.registry;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public final class ItemRegistry extends BasicRegistry<Item>
{
    public static final ResourceKey<Registry<Item>> TYPE = RegistryKeys.ITEM;

    private ItemRegistry(String modId)
    {
        super(TYPE, modId);
    }

    @Override
    protected <R extends Item> ItemRegistryEntry<R> createRegistryEntry(ResourceKey<R> key, Supplier<R> supplier)
    {
        return new ItemRegistryEntry<>(this, key, supplier);
    }

    @Override
    public <R extends Item> ItemRegistryEntry<R> register(ResourceKey<Item> key, Supplier<R> factory)
    {
        return (ItemRegistryEntry<R>) super.register(key, factory);
    }

    @Override
    public <R extends Item> ItemRegistryEntry<R> register(String name, Supplier<R> factory)
    {
        return (ItemRegistryEntry<R>) super.register(name, factory);
    }

    @Nullable
    @Override
    public ItemRegistryEntry<Item> getEntry(ResourceKey<Item> key)
    {
        var entry = super.getEntry(key);
        return entry instanceof ItemRegistryEntry<Item> item ? item : null;
    }

    @Nullable
    @Override
    public ItemRegistryEntry<Item> getEntry(ResourceLocation id)
    {
        var entry = super.getEntry(id);
        return entry instanceof ItemRegistryEntry<Item> item ? item : null;
    }

    @Nullable
    @Override
    public ItemRegistryEntry<Item> getEntry(String name)
    {
        var entry = super.getEntry(name);
        return entry instanceof ItemRegistryEntry<Item> item ? item : null;
    }

    public static ItemRegistry create(String modId)
    {
        return ModdedRegistries.INSTANCE.getOrCreate(TYPE, modId, ModdedRegistries.Constructor.of(ItemRegistry::new));
    }
}
