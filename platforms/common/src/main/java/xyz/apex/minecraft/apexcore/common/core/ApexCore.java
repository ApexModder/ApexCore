package xyz.apex.minecraft.apexcore.common.core;

import com.google.errorprone.annotations.DoNotCall;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import xyz.apex.lib.Services;
import xyz.apex.minecraft.apexcore.common.lib.PhysicalSide;
import xyz.apex.minecraft.apexcore.common.lib.component.block.entity.types.BlockEntityComponentTypes;
import xyz.apex.minecraft.apexcore.common.lib.component.block.types.BlockComponentTypes;
import xyz.apex.minecraft.apexcore.common.lib.event.EventType;
import xyz.apex.minecraft.apexcore.common.lib.event.types.EntityEvents;
import xyz.apex.minecraft.apexcore.common.lib.hook.CreativeModeTabHooks;
import xyz.apex.minecraft.apexcore.common.lib.hook.EntityHooks;
import xyz.apex.minecraft.apexcore.common.lib.hook.GameRuleHooks;
import xyz.apex.minecraft.apexcore.common.lib.hook.MenuHooks;
import xyz.apex.minecraft.apexcore.common.lib.modloader.ModLoader;
import xyz.apex.minecraft.apexcore.common.lib.multiblock.MultiBlockTypes;
import xyz.apex.minecraft.apexcore.common.lib.network.NetworkManager;
import xyz.apex.minecraft.apexcore.common.lib.registry.AbstractRegistrar;
import xyz.apex.minecraft.apexcore.common.lib.registry.factory.MenuFactory;
import xyz.apex.minecraft.apexcore.common.lib.resgen.ProviderTypes;

import java.util.function.Supplier;

@ApiStatus.Internal
@ApiStatus.NonExtendable
public interface ApexCore
{
    String ID = "apexcore";
    Logger LOGGER = LogManager.getLogger();
    ApexCore INSTANCE = Services.singleton(ApexCore.class);

    ModLoader MOD_LOADER = Services.singleton(ModLoader.class);

    CreativeModeTabHooks CREATIVE_MODE_TAB_HOOKS = Services.singleton(CreativeModeTabHooks.class);
    EntityHooks ENTITY_HOOKS = Services.singleton(EntityHooks.class);
    GameRuleHooks GAME_RULE_HOOKS = Services.singleton(GameRuleHooks.class);
    MenuHooks MENU_HOOKS = Services.singleton(MenuHooks.class);

    @MustBeInvokedByOverriders
    default void bootstrap()
    {
        // common check for every platform
        // all fake players *should* extend the ServerPlayer class
        // forge & fabric register their own listeners to check instanceof directly on their fake player classes
        //
        // platform listeners are registered before this one
        // meaning the platform specific check should happen first before this one
        EntityEvents.IS_FAKE_PLAYER.addListener(entity -> entity instanceof ServerPlayer && entity.getClass() != ServerPlayer.class);
        ApexTags.bootstrap();
        EventType.bootstrap();
        BlockComponentTypes.bootstrap();
        BlockEntityComponentTypes.bootstrap();
        MultiBlockTypes.bootstrap();

        ApexCoreTests.register();

        registerGenerators();

        // tell listeners that ApexCore has been initialized
        Services.stream(ApexCoreLoaded.class).forEach(ApexCoreLoaded::onApexCoreLoaded);
    }

    private void registerGenerators()
    {
        var descriptionKey = "pack.%s.description".formatted(ID);

        ProviderTypes.LANGUAGES.addListener(ID, (provider, lookup) -> provider
                .enUS()
                    .add(descriptionKey, "ApexCore")
                .end()
        );

        ProviderTypes.registerDefaultMcMetaGenerator(ID, Component.translatable(descriptionKey));
    }

    PhysicalSide physicalSide();

    NetworkManager createNetworkManager(String ownerId);

    @DoNotCall
    @ApiStatus.Internal
    void register(AbstractRegistrar<?> registrar);

    @ApiStatus.Internal
    SpawnEggItem createSpawnEgg(Supplier<? extends EntityType<? extends Mob>> entityType, int backgroundColor, int highlightColor, Item.Properties properties);

    @ApiStatus.Internal
    <T extends AbstractContainerMenu> MenuType<T> createMenuType(MenuFactory<T> menuFactory, Supplier<MenuType<T>> selfSupplier);
}
