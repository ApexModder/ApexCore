package xyz.apex.minecraft.apexcore.common.lib.registry.builders;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import xyz.apex.minecraft.apexcore.common.lib.registry.entries.EnchantmentEntry;
import xyz.apex.minecraft.apexcore.common.lib.registry.factories.EnchantmentFactory;
import xyz.apex.minecraft.apexcore.common.lib.resgen.ProviderRegistryListener;
import xyz.apex.minecraft.apexcore.common.lib.resgen.ProviderType;
import xyz.apex.minecraft.apexcore.common.lib.resgen.ProviderTypes;
import xyz.apex.minecraft.apexcore.common.lib.resgen.tag.TagsProvider;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public final class EnchantmentBuilder<P, T extends Enchantment, M extends BuilderManager<M>> extends AbstractBuilder<P, Enchantment, T, EnchantmentEntry<T>, EnchantmentBuilder<P, T, M>, M>
{
    private final EnchantmentFactory<T> enchantmentFactory;
    private final EnchantmentCategory enchantmentCategory;
    private final Set<EquipmentSlot> equipmentSlots = EnumSet.noneOf(EquipmentSlot.class);
    private Enchantment.Rarity rarity = Enchantment.Rarity.COMMON;

    EnchantmentBuilder(P parent, M builderManager, String registrationName, EnchantmentCategory enchantmentCategory, EnchantmentFactory<T> enchantmentFactory)
    {
        super(parent, builderManager, Registries.ENCHANTMENT, registrationName, EnchantmentEntry::new);

        this.enchantmentCategory = enchantmentCategory;
        this.enchantmentFactory = enchantmentFactory;
    }

    /**
     * Sets the rarity for this enchantment.
     *
     * @param rarity Rarity for this enchantment.
     * @return This build instance.
     */
    public EnchantmentBuilder<P, T, M> rarity(Enchantment.Rarity rarity)
    {
        this.rarity = rarity;
        return self();
    }

    /**
     * Marks equipment slot as valid slot for this enchantment.
     *
     * @param equipmentSlot Valid equipment slot.
     * @return This builder instance.
     */
    public EnchantmentBuilder<P, T, M> equipmentSlot(EquipmentSlot equipmentSlot)
    {
        this.equipmentSlots.add(equipmentSlot);
        return self();
    }

    /**
     * Marks given equipment slots as valid for this enchantment.
     *
     * @param equipmentSlots Valid equipment slots.
     * @return This builder instance.
     */
    public EnchantmentBuilder<P, T, M> equipmentSlots(EquipmentSlot... equipmentSlots)
    {
        Collections.addAll(this.equipmentSlots, equipmentSlots);
        return self();
    }

    /**
     * Marks all armor equipment slots as valid for this enchantment.
     *
     * @return This builder instance.
     */
    public EnchantmentBuilder<P, T, M> armorSlots()
    {
        return equipmentSlots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
    }

    public EnchantmentBuilder<P, T, M> tag(TagKey<Enchantment>... tags)
    {
        return tag((provider, lookup, context) -> {
            for(var tag : tags)
            {
                provider.tag(tag).addElement(context.registryName());
            }
        });
    }

    public EnchantmentBuilder<P, T, M> tag(ProviderRegistryListener<TagsProvider<Enchantment>, Enchantment, T> listener)
    {
        return addProvider(ProviderTypes.ENCHANTMENT_TAGS, listener);
    }

    public EnchantmentBuilder<P, T, M> noTags()
    {
        return clearProvider(ProviderTypes.ENCHANTMENT_TAGS);
    }

    @Override
    protected String getDescriptionId(ProviderType.RegistryContext<Enchantment, T> context)
    {
        return context.value().getDescriptionId();
    }

    @Override
    protected T createObject()
    {
        var equipmentSlots = this.equipmentSlots.toArray(EquipmentSlot[]::new);
        return enchantmentFactory.create(rarity, enchantmentCategory, equipmentSlots);
    }
}
