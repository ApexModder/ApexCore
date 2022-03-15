package xyz.apex.forge.apexcore.lib.util;

import net.jodah.typetools.TypeResolver;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.GenericEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import xyz.apex.forge.apexcore.core.ApexCore;
import xyz.apex.java.utility.nullness.NonnullConsumer;

public final class EventBusHelper
{
	private static final boolean DEFAULT_RECEIVE_CANCELLED = false;
	private static final EventPriority DEFAULT_EVENT_PRIORITY = EventPriority.NORMAL;

	public static <T extends Event> void addListener(EventPriority priority, NonnullConsumer<T> consumer)
	{
		var eventType = getEventType(consumer);
		addListener(priority, DEFAULT_RECEIVE_CANCELLED, eventType, consumer);
	}

	public static <T extends Event> void addListener(EventPriority priority, boolean receiveCancelled, NonnullConsumer<T> consumer)
	{
		var eventType = getEventType(consumer);
		addListener(priority, receiveCancelled, eventType, consumer);
	}

	public static <T extends Event> void addListener(EventPriority priority, Class<T> eventType, NonnullConsumer<T> consumer)
	{
		addListener(priority, DEFAULT_RECEIVE_CANCELLED, eventType, consumer);
	}

	public static <T extends Event> void addListener(EventPriority priority, boolean receiveCancelled, Class<T> eventType, NonnullConsumer<T> consumer)
	{
		var eventBus = getEventBus(eventType);
		eventBus.addListener(priority, receiveCancelled, eventType, consumer);
	}

	public static <T extends Event> void addListener(boolean receiveCancelled, Class<T> eventType, NonnullConsumer<T> consumer)
	{
		addListener(DEFAULT_EVENT_PRIORITY, receiveCancelled, eventType, consumer);
	}

	public static <T extends Event> void addListener(boolean receiveCancelled, NonnullConsumer<T> consumer)
	{
		var eventType = getEventType(consumer);
		addListener(DEFAULT_EVENT_PRIORITY, receiveCancelled, eventType, consumer);
	}

	public static <T extends Event> void addListener(Class<T> eventType, NonnullConsumer<T> consumer)
	{
		addListener(DEFAULT_EVENT_PRIORITY, DEFAULT_RECEIVE_CANCELLED, eventType, consumer);
	}

	public static <T extends Event> void addListener(NonnullConsumer<T> consumer)
	{
		var eventType = getEventType(consumer);
		addListener(DEFAULT_EVENT_PRIORITY, DEFAULT_RECEIVE_CANCELLED, eventType, consumer);
	}

	public static <T extends GenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter, EventPriority priority, NonnullConsumer<T> consumer)
	{
		var eventType = getEventType(consumer);
		addGenericListener(genericClassFilter, priority, DEFAULT_RECEIVE_CANCELLED, eventType, consumer);
	}

	public static <T extends GenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter, EventPriority priority, boolean receiveCancelled, NonnullConsumer<T> consumer)
	{
		var eventType = getEventType(consumer);
		addGenericListener(genericClassFilter, priority, receiveCancelled, eventType, consumer);
	}

	public static <T extends GenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter, EventPriority priority, Class<T> eventType, NonnullConsumer<T> consumer)
	{
		addGenericListener(genericClassFilter, priority, DEFAULT_RECEIVE_CANCELLED, eventType, consumer);
	}

	public static <T extends GenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter, EventPriority priority, boolean receiveCancelled, Class<T> eventType, NonnullConsumer<T> consumer)
	{
		var eventBus = getEventBus(eventType);
		eventBus.addGenericListener(genericClassFilter, priority, receiveCancelled, eventType, consumer);
	}

	public static <T extends GenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter, boolean receiveCancelled, Class<T> eventType, NonnullConsumer<T> consumer)
	{
		addGenericListener(genericClassFilter, DEFAULT_EVENT_PRIORITY, receiveCancelled, eventType, consumer);
	}

	public static <T extends GenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter, boolean receiveCancelled, NonnullConsumer<T> consumer)
	{
		var eventType = getEventType(consumer);
		addGenericListener(genericClassFilter, DEFAULT_EVENT_PRIORITY, receiveCancelled, eventType, consumer);
	}

	public static <T extends GenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter, Class<T> eventType, NonnullConsumer<T> consumer)
	{
		addGenericListener(genericClassFilter, DEFAULT_EVENT_PRIORITY, DEFAULT_RECEIVE_CANCELLED, eventType, consumer);
	}

	public static <T extends GenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter, NonnullConsumer<T> consumer)
	{
		var eventType = getEventType(consumer);
		addGenericListener(genericClassFilter, DEFAULT_EVENT_PRIORITY, DEFAULT_RECEIVE_CANCELLED, eventType, consumer);
	}

	private static <T extends ParallelDispatchEvent> NonnullConsumer<T> enqueueListener(NonnullConsumer<T> consumer)
	{
		return (T event) -> event.enqueueWork(() -> consumer.accept(event));
	}

	public static <T extends ParallelDispatchEvent> void addEnqueuedListener(EventPriority priority, boolean receiveCancelled, Class<T> eventType, NonnullConsumer<T> consumer)
	{
		var enqueuedListener = enqueueListener(consumer);
		addListener(priority, receiveCancelled, eventType, enqueuedListener);
	}

	public static <T extends ParallelDispatchEvent> void addEnqueuedListener(EventPriority priority, boolean receiveCancelled, NonnullConsumer<T> consumer)
	{
		var eventType = getEventType(consumer);
		addEnqueuedListener(priority, receiveCancelled, eventType, consumer);
	}

	public static <T extends ParallelDispatchEvent> void addEnqueuedListener(EventPriority priority, NonnullConsumer<T> consumer)
	{
		var eventType = getEventType(consumer);
		addEnqueuedListener(priority, DEFAULT_RECEIVE_CANCELLED, eventType, consumer);
	}

	public static <T extends ParallelDispatchEvent> void addEnqueuedListener(EventPriority priority, Class<T> eventType, NonnullConsumer<T> consumer)
	{
		addEnqueuedListener(priority, DEFAULT_RECEIVE_CANCELLED, eventType, consumer);
	}

	public static <T extends ParallelDispatchEvent> void addEnqueuedListener(boolean receiveCancelled, Class<T> eventType, NonnullConsumer<T> consumer)
	{
		addEnqueuedListener(DEFAULT_EVENT_PRIORITY, receiveCancelled, eventType, consumer);
	}

	public static <T extends ParallelDispatchEvent> void addEnqueuedListener(boolean receiveCancelled, NonnullConsumer<T> consumer)
	{
		var eventType = getEventType(consumer);
		addEnqueuedListener(DEFAULT_EVENT_PRIORITY, receiveCancelled, eventType, consumer);
	}

	public static <T extends ParallelDispatchEvent> void addEnqueuedListener(Class<T> eventType, NonnullConsumer<T> consumer)
	{
		addEnqueuedListener(DEFAULT_EVENT_PRIORITY, DEFAULT_RECEIVE_CANCELLED, eventType, consumer);
	}

	public static <T extends ParallelDispatchEvent> void addEnqueuedListener(NonnullConsumer<T> consumer)
	{
		var eventType = getEventType(consumer);
		addEnqueuedListener(DEFAULT_EVENT_PRIORITY, DEFAULT_RECEIVE_CANCELLED, eventType, consumer);
	}

	public static <T extends Event> Class<T> getEventType(NonnullConsumer<T> consumer)
	{
		var eventType = (Class<T>) TypeResolver.resolveRawArgument(NonnullConsumer.class, consumer.getClass());

		if((Class<?>) eventType == TypeResolver.Unknown.class)
		{
			ApexCore.LOGGER.error("Failed to resolve handler for \"{}\"", consumer.toString());
			throw new IllegalStateException("Failed to resolve consumer event type: " + consumer);
		}

		return eventType;
	}

	public static <T extends Event> boolean isModBusEvent(Class<T> eventType)
	{
		return IModBusEvent.class.isAssignableFrom(eventType);
	}

	public static <T extends Event> IEventBus getEventBus(Class<T> eventType)
	{
		if(isModBusEvent(eventType))
			return FMLJavaModLoadingContext.get().getModEventBus();
		return MinecraftForge.EVENT_BUS;
	}
}
