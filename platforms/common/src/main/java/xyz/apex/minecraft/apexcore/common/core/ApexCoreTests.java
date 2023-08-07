package xyz.apex.minecraft.apexcore.common.core;

import com.mojang.blaze3d.vertex.PoseStack;
import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.apex.minecraft.apexcore.common.lib.client.renderer.ItemStackRenderer;
import xyz.apex.minecraft.apexcore.common.lib.helper.InteractionResultHelper;
import xyz.apex.minecraft.apexcore.common.lib.hook.MenuHooks;
import xyz.apex.minecraft.apexcore.common.lib.menu.EnhancedSlot;
import xyz.apex.minecraft.apexcore.common.lib.menu.SimpleContainerMenu;
import xyz.apex.minecraft.apexcore.common.lib.menu.SimpleContainerMenuScreen;
import xyz.apex.minecraft.apexcore.common.lib.registry.Registrar;
import xyz.apex.minecraft.apexcore.common.lib.registry.entry.BlockEntityEntry;
import xyz.apex.minecraft.apexcore.common.lib.registry.entry.MenuEntry;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static xyz.apex.minecraft.apexcore.common.lib.resgen.loot.EntityLootProvider.ENTITY_ON_FIRE;

@ApiStatus.Internal
public final class ApexCoreTests
{
    private static final String PROPERTY = "%s.test_elements.enabled".formatted(ApexCore.ID);
    public static final boolean ENABLED = BooleanUtils.toBoolean(System.getProperty(PROPERTY, "false"));
    public static final Marker MARKER = MarkerManager.getMarker("Tests");

    static void register()
    {
        if(!ENABLED)
            return;

        var messages = new String[] {
                "ApexCore Test Elements Enabled!",
                "Errors, Bugs and Glitches may arise, you are on your own.",
                "No support will be provided while Test Elements are Enabled!"
        };

        var maxLen = Stream.of(messages).mapToInt(String::length).max().orElse(1);
        var header = Strings.repeat('*', maxLen + 4);
        ApexCore.LOGGER.warn(MARKER, header);
        Stream.of(messages).map(str -> StringUtils.center(str, maxLen, ' ')).forEach(str -> ApexCore.LOGGER.warn(MARKER, "* {} *", str));
        ApexCore.LOGGER.warn(MARKER, header);

        var registrar = Registrar.create(ApexCore.ID);

        var testItem = registrar
                .object("test_item")
                .item()
                .defaultModel()
                .model((provider, lookup, entry) -> provider.withParent(
                        entry.getRegistryName().withPrefix("item/"),
                        provider.existingModel(new ResourceLocation("builtin/entity"))
                ))
                .renderer(() -> TestItemStackRenderer::new)
        .register();

        var testBlock = registrar
                .object("test_block")
                .block()
                .defaultBlockState((provider, lookup, entry) -> provider.withParent(
                        entry.getRegistryName().withPrefix("block/"),
                        "block/cube_all"
                ).texture("all", new ResourceLocation("block/debug")))
                .defaultItem()
        .register();

        var testEnchantment = registrar
                .object("test_enchantment")
                .enchantment(EnchantmentCategory.DIGGER)
                .equipmentSlots(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND)
        .register();

        var testMenu = registrar.<TestMenu, SimpleContainerMenuScreen<TestMenu>>menu("test_menu", TestMenu::new, () -> () -> SimpleContainerMenuScreen::new);

        var testEntity = registrar
                .object("test_entity")
                .entity(MobCategory.CREATURE, TestEntity::new)
                .attributes(TestEntity::attributes)
                .renderer(() -> () -> TestEntityRenderer::new)
                .defaultSpawnEgg(0xF2D7D5, 0xFFC300)
                .sized(.9F, .9F)
                .clientTrackingRange(10)
                .tag(EntityTypeTags.DISMOUNTS_UNDERWATER)
                .spawnPlacement(SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules)
                .lootTable((provider, entry) -> provider.add(entry.value(), LootTable
                        .lootTable()
                        .withPool(LootPool
                                .lootPool()
                                .setRolls(ConstantValue.exactly(1F))
                                .add(LootItem
                                        .lootTableItem(Items.PORKCHOP)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1F, 3F)))
                                        .apply(SmeltItemFunction
                                                .smelted()
                                                .when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, ENTITY_ON_FIRE))
                                        )
                                        .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0F, 1F)))
                                )
                        )
                ))
        .register();

        var testBlockWithEntity = registrar
                .object("test_block_with_entity")
                .block(properties -> new TestBlockWithEntity(properties, () -> BlockEntityEntry.cast(registrar.get(Registries.BLOCK_ENTITY_TYPE, "test_block_with_entity"))))
                .copyInitialPropertiesFrom(testBlock::value)
                .defaultBlockState((provider, lookup, entry) -> provider.withParent(
                        entry.getRegistryName().withPrefix("block/"),
                        "block/cube_all"
                ).texture("all", new ResourceLocation("block/debug2")))
                .defaultItem()
                .<TestBlockEntity>defaultBlockEntity((blockEntityType, pos, blockState) -> new TestBlockEntity(blockEntityType, pos, blockState, testMenu))
        .register();

        var creativeModeTab = registrar
                .creativeModeTab("test")
                .lang("en_us", "ApexCore - Test Elements")
                .icon(testBlock::asStack)
                .displayItems((parameters, output) -> {
                    output.accept(testItem);
                    output.accept(testBlock);
                    output.accept(testEnchantment.asStack(1));
                    output.accept(testEntity);
                    output.accept(testBlockWithEntity);
                })
        .register();

        registrar.register();
    }

    private static final class TestItemStackRenderer implements ItemStackRenderer
    {
        @Override
        public void render(ItemStack stack, ItemDisplayContext displayContext, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay)
        {
            var renderer = Minecraft.getInstance().getItemRenderer();
            var model = renderer.getItemModelShaper().getItemModel(Items.BARRIER);
            model = model == null ? renderer.getItemModelShaper().getModelManager().getMissingModel() : model;
            renderer.render(stack, displayContext, false, pose, buffer, packedLight, packedOverlay, model);
        }
    }

    private static final class TestEntity extends Animal
    {
        private TestEntity(EntityType<? extends TestEntity> entityType, Level level)
        {
            super(entityType, level);
        }

        @Override
        protected void registerGoals()
        {
            super.registerGoals();

            goalSelector.addGoal(0, new FloatGoal(this));
            goalSelector.addGoal(1, new PanicGoal(this, 1.25D));
            goalSelector.addGoal(3, new BreedGoal(this, 1D));
            goalSelector.addGoal(4, new TemptGoal(this, 1.2D, Ingredient.of(ItemTags.PIGLIN_LOVED), false));
            goalSelector.addGoal(5, new FollowParentGoal(this, 1.1D));
            goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1D));
            goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6F));
            goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        }

        @Override
        protected SoundEvent getAmbientSound()
        {
            return SoundEvents.PIG_AMBIENT;
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource source)
        {
            return SoundEvents.PIG_HURT;
        }

        @Override
        protected SoundEvent getDeathSound()
        {
            return SoundEvents.PIG_DEATH;
        }

        @Override
        protected void playStepSound(BlockPos pos, BlockState state)
        {
            playSound(SoundEvents.PIG_STEP, .15F, 1F);
        }

        @Nullable
        @Override
        public TestEntity getBreedOffspring(ServerLevel level, AgeableMob otherParent)
        {
            return (TestEntity) getType().create(level);
        }

        @Override
        public boolean isFood(ItemStack stack)
        {
            return stack.is(ItemTags.PIGLIN_LOVED);
        }

        @Override
        protected Vec3 getLeashOffset()
        {
            return new Vec3(0D, .6F * getEyeHeight(), getBbWidth() * .4F);
        }

        private static AttributeSupplier.Builder attributes()
        {
            return Mob.createMobAttributes()
                      .add(Attributes.MAX_HEALTH, 10D)
                      .add(Attributes.MOVEMENT_SPEED, .5D);
        }
    }

    private static final class TestEntityRenderer extends MobRenderer<TestEntity, PigModel<TestEntity>>
    {
        private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/pig/pig.png");

        public TestEntityRenderer(EntityRendererProvider.Context context)
        {
            super(context, new PigModel<>(context.bakeLayer(ModelLayers.PIG)), .7F);
        }

        @Override
        public ResourceLocation getTextureLocation(TestEntity entity)
        {
            return TEXTURE;
        }
    }

    private static final class TestBlockWithEntity extends BaseEntityBlock
    {
        private final Supplier<BlockEntityEntry<TestBlockEntity>> blockEntityType;

        private TestBlockWithEntity(Properties properties, Supplier<BlockEntityEntry<TestBlockEntity>> blockEntityType)
        {
            super(properties);

            this.blockEntityType = blockEntityType;
        }

        @Override
        public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
        {
            if(level.getBlockEntity(pos) instanceof TestBlockEntity blockEntity)
                return MenuHooks.get().openMenu(player, blockEntity.getDisplayName(), blockEntity, buffer -> buffer.writeBlockPos(pos));
            return InteractionResultHelper.BlockUse.noActionTaken();
        }

        @Nullable
        @Override
        public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos pos)
        {
            var blockEntity = blockEntityType.get().getBlockEntity(level, pos);
            return blockEntity == null ? null : blockEntity.createMenuProvider();
        }

        @Override
        public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack)
        {
            if(stack.hasCustomHoverName())
                blockEntityType.get().getBlockEntityOptional(level, pos).ifPresent(blockEntity -> blockEntity.setCustomName(stack.getHoverName()));
        }

        @Override
        public RenderShape getRenderShape(BlockState blockState)
        {
            return RenderShape.MODEL;
        }

        @Nullable
        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState blockState)
        {
            return blockEntityType.get().create(pos, blockState);
        }
    }

    private static final class TestBlockEntity extends BlockEntity implements Nameable, MenuConstructor
    {
        private final MenuEntry<TestMenu> menuEntry;
        @Nullable private Component customName = null;

        private TestBlockEntity(BlockEntityType<? extends TestBlockEntity> blockEntityType, BlockPos pos, BlockState blockState, MenuEntry<TestMenu> menuEntry)
        {
            super(blockEntityType, pos, blockState);

            this.menuEntry = menuEntry;
        }

        public void setCustomName(Component customName)
        {
            this.customName = customName;
            setChanged();
        }

        public MenuProvider createMenuProvider()
        {
            return MenuHooks.get().createMenuProvider(getDisplayName(), this, buffer -> buffer.writeBlockPos(worldPosition));
        }

        @Override
        public void load(CompoundTag tag)
        {
            customName = null;

            if(tag.contains("CustomName", Tag.TAG_STRING))
                customName = Component.Serializer.fromJson(tag.getString("CustomName"));
        }

        @Override
        protected void saveAdditional(CompoundTag tag)
        {
            if(customName != null)
                tag.putString("CustomName", Component.Serializer.toJson(customName));
        }

        @Override
        public boolean hasCustomName()
        {
            return customName != null;
        }

        @Nullable
        @Override
        public Component getCustomName()
        {
            return customName;
        }

        @Override
        public Component getName()
        {
            return getBlockState().getBlock().getName();
        }

        @Override
        public Component getDisplayName()
        {
            return customName == null ? getName() : getCustomName();
        }

        @Override
        public AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player)
        {
            return new TestMenu(menuEntry.value(), syncId, inventory);
        }
    }

    private static final class TestMenu extends SimpleContainerMenu
    {
        private TestMenu(MenuType<? extends TestMenu> menuType, int syncId, Inventory inventory)
        {
            super(menuType, syncId, inventory, new SimpleContainer(ItemStack.EMPTY));
        }

        private TestMenu(MenuType<? extends TestMenu> menuType, int syncId, Inventory inventory, FriendlyByteBuf buffer)
        {
            this(menuType, syncId, inventory);
        }

        @Override
        protected void bindSlots(Inventory playerInventory)
        {
            addSlot(new EnhancedSlot(container, 0, 80, 37));

            bindPlayerInventory(playerInventory, 8, 84, this::addSlot);
        }
    }
}
