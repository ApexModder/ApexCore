package xyz.apex.minecraft.apexcore.common.lib.registry.builders;

import net.minecraft.world.item.Item;
import xyz.apex.minecraft.apexcore.common.lib.registry.RegistrarManager;
import xyz.apex.minecraft.apexcore.common.lib.registry.factories.ItemFactory;

public non-sealed class BuilderManagerImpl implements BuilderManager
{
    private final RegistrarManager registrarManager;

    protected BuilderManagerImpl(RegistrarManager registrarManager)
    {
        this.registrarManager = registrarManager;
    }

    @Override
    public final RegistrarManager getRegistrarManager()
    {
        return registrarManager;
    }

    @Override
    public final String getOwnerId()
    {
        return registrarManager.getOwnerId();
    }

    // region: Item
    @Override
    public final <P, T extends Item> ItemBuilder<P, T> item(P parent, String registrationName, ItemFactory<T> itemFactory)
    {
        return new ItemBuilder<>(parent, this, registrationName, itemFactory);
    }

    @Override
    public final <P> ItemBuilder<P, Item> item(P parent, String registrationName)
    {
        return item(parent, registrationName, Item::new);
    }

    @Override
    public final <T extends Item> ItemBuilder<BuilderManager, T> item(String registrationName, ItemFactory<T> itemFactory)
    {
        return item(this, registrationName, itemFactory);
    }

    @Override
    public final ItemBuilder<BuilderManager, Item> item(String registrationName)
    {
        return item(this, registrationName, Item::new);
    }
    // endregion
}
