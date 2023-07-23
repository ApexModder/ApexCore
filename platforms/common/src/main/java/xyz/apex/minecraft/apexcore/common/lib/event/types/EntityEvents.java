package xyz.apex.minecraft.apexcore.common.lib.event.types;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.minecraft.apexcore.common.lib.event.Event;
import xyz.apex.minecraft.apexcore.common.lib.event.EventType;

@ApiStatus.NonExtendable
public interface EntityEvents
{
    EventType<JoinLevel> JOIN_LEVEL = EventType.create(listeners -> (entity, level) -> listeners.forEach(listener -> listener.handle(entity, level)));
    EventType<LeaveLevel> LEAVE_LEVEL = EventType.create(listeners -> (entity, level) -> listeners.forEach(listener -> listener.handle(entity, level)));
    EventType<LivingDeath> LIVING_DEATH = EventType.create(listeners -> (entity, source) -> listeners.stream().anyMatch(listener -> listener.handle(entity, source)));
    EventType<IsFakePlayer> IS_FAKE_PLAYER = EventType.create(listeners -> entity -> listeners.stream().anyMatch(listener -> listener.handle(entity)));
    EventType<AfterEntityFallOn> AFTER_ENTITY_FALL_ON = EventType.create(listeners -> (entity, level, pos, blockState) -> listeners.forEach(listener -> listener.handle(entity, level, pos, blockState)));

    @ApiStatus.Internal
    static void bootstrap()
    {
    }

    @FunctionalInterface
    @ApiStatus.NonExtendable
    interface JoinLevel extends Event
    {
        void handle(Entity entity, Level level);
    }

    @FunctionalInterface
    @ApiStatus.NonExtendable
    interface LeaveLevel extends Event
    {
        void handle(Entity entity, Level level);
    }

    @FunctionalInterface
    @ApiStatus.NonExtendable
    interface LivingDeath extends Event
    {
        boolean handle(LivingEntity entity, DamageSource source);
    }

    @FunctionalInterface
    @ApiStatus.NonExtendable
    interface IsFakePlayer extends Event
    {
        boolean handle(Entity entity);
    }

    @FunctionalInterface
    @ApiStatus.NonExtendable
    interface AfterEntityFallOn extends Event
    {
        void handle(Entity entity, Level level, BlockPos pos, BlockState blockState);
    }
}
