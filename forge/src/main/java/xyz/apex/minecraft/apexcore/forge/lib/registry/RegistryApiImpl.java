package xyz.apex.minecraft.apexcore.forge.lib.registry;

import com.google.common.collect.*;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryManager;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.minecraft.apexcore.common.lib.registry.Registrar;
import xyz.apex.minecraft.apexcore.common.lib.registry.RegistryApi;
import xyz.apex.minecraft.apexcore.common.lib.registry.RegistryEntry;
import xyz.apex.minecraft.apexcore.common.lib.registry.RegistryEntryImpl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ApiStatus.Internal
public final class RegistryApiImpl implements RegistryApi
{
    private final Table<String, ResourceKey<? extends Registry<?>>, RegistryData<?>> registryDataTable = HashBasedTable.create();

    @Override
    public <T> void register(Registrar<T> registrar)
    {
        data(registrar.getOwnerId(), registrar.getRegistryType()).register();
    }

    @Override
    public <T, R extends T> RegistryEntry<R> register(Registrar<T> registrar, String registrationName, Supplier<R> registryEntryFactory)
    {
        return data(registrar.getOwnerId(), registrar.getRegistryType()).register(registrationName, registryEntryFactory);
    }

    @Override
    public <T> Optional<Holder<T>> getDelegate(ResourceKey<? extends Registry<T>> registryType, ResourceKey<T> registryKey)
    {
        var forgeRegistry = RegistryManager.ACTIVE.getRegistry(registryType);
        if(forgeRegistry != null) return forgeRegistry.getHolder(registryKey);
        return RegistryApi.findVanillaRegistry(registryType).flatMap(registry -> registry.getHolder(registryKey));
    }

    @Override
    public <T> void addListener(ResourceKey<? extends Registry<T>> registryType, String ownerId, Consumer<Registrar<T>> listener)
    {
        data(ownerId, registryType).addListener(listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, R extends T> void addListener(ResourceKey<? extends Registry<T>> registryType, String ownerId, ResourceLocation registryName, Consumer<R> listener)
    {
        data(ownerId, registryType).addListener(registryName, (Consumer<? super T>) listener);
    }

    @SuppressWarnings("unchecked")
    private <T> RegistryData<T> data(String ownerId, ResourceKey<? extends Registry<T>> registryType)
    {
        var data = registryDataTable.get(ownerId, registryType);

        if(data == null)
        {
            data = new RegistryData<>(ownerId, registryType);
            registryDataTable.put(ownerId, registryType, data);
        }

        return (RegistryData<T>) data;
    }

    private static final class RegistryData<T>
    {
        private final String ownerId;
        private final ResourceKey<? extends Registry<T>> registryType;
        private final Map<String, Pair<RegistryEntry<? extends T>, Supplier<? extends T>>> entries = Maps.newConcurrentMap();
        private final List<Consumer<Registrar<T>>> registrarListeners = Lists.newLinkedList();
        private final Multimap<ResourceLocation, Consumer<? super T>> registryEntryListeners = MultimapBuilder.hashKeys().linkedListValues().build();
        private boolean registered = false;

        private RegistryData(String ownerId, ResourceKey<? extends Registry<T>> registryType)
        {
            this.ownerId = ownerId;
            this.registryType = registryType;
        }

        public void register()
        {
            Validate.isTrue(ModLoadingContext.get().getActiveNamespace().equals(ownerId));
            var modBus = FMLJavaModLoadingContext.get().getModEventBus();
            modBus.addListener(EventPriority.HIGHEST, this::registerEntries);
            modBus.addListener(this::postListeners);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public <R extends T> RegistryEntry<R> register(String registrationName, Supplier<R> registryEntryFactory)
        {
            Validate.isTrue(!registered);
            var registryEntry = new RegistryEntryImpl<R>((Registrar) getRegistrar(), new ResourceLocation(ownerId, registrationName));
            Validate.isTrue(entries.put(registrationName, Pair.of(registryEntry, registryEntryFactory)) == null);
            return registryEntry;
        }

        public void addListener(Consumer<Registrar<T>> listener)
        {
            if(registered) listener.accept(getRegistrar());
            else registrarListeners.add(listener);
        }

        public void addListener(ResourceLocation registryName, Consumer<? super T> listener)
        {
            if(registered) listener.accept(getRegistrar().get(registryName).get());
            else registryEntryListeners.put(registryName, listener);
        }

        private Registrar<T> getRegistrar()
        {
            return Registrar.get(ownerId, registryType);
        }

        private void registerEntries(RegisterEvent event)
        {
            event.register(registryType, helper -> {
                Validate.isTrue(!registered);
                entries.forEach((registrationName, pair) -> {
                    LOGGER.info("[{}] Registered registry entry '{}' of type '{}'", ownerId, registrationName, registryType.location());
                    helper.register(registrationName, pair.getRight().get());
                });
                entries.clear();
                registered = true;
            });
        }

        private void postListeners(RegisterEvent event)
        {
            event.register(registryType, helper -> {
                var registrar = getRegistrar();

                registryEntryListeners.forEach((registrationName, listener) -> listener.accept(registrar.get(registrationName).get()));
                registryEntryListeners.clear();

                registrarListeners.forEach(listener -> listener.accept(registrar));
                registrarListeners.clear();
            });
        }
    }
}
