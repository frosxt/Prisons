package com.github.frosxt.prisoncore.kernel.event;

import com.github.frosxt.prisoncore.api.event.DomainEvent;
import com.github.frosxt.prisoncore.api.event.DomainEventBus;
import com.github.frosxt.prisoncore.api.event.DomainEventHandler;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DefaultDomainEventBus implements DomainEventBus {
    private final Map<Class<?>, List<DomainEventHandler<?>>> handlers = new ConcurrentHashMap<>();
    private final ExecutorService asyncExecutor;
    private final Logger logger;

    public DefaultDomainEventBus(final Logger logger) {
        this.logger = logger;
        this.asyncExecutor = Executors.newCachedThreadPool(r -> {
            final Thread t = new Thread(r, "PrisonCore-EventBus");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void publish(final DomainEvent event) {
        final List<DomainEventHandler<?>> handlerList = handlers.get(event.getClass());
        if (handlerList == null) {
            return;
        }

        for (final DomainEventHandler handler : handlerList) {
            if (handler.async()) {
                asyncExecutor.submit(() -> {
                    try {
                        handler.handle(event);
                    } catch (final Exception e) {
                        logger.log(Level.WARNING, "[PrisonCore] Async event handler error", e);
                    }
                });
            } else {
                try {
                    handler.handle(event);
                } catch (final Exception e) {
                    logger.log(Level.WARNING, "[PrisonCore] Event handler error", e);
                }
            }
        }
    }

    @Override
    public <T extends DomainEvent> void subscribe(final Class<T> eventType, final DomainEventHandler<T> handler) {
        handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
        handlers.get(eventType).sort(Comparator.comparingInt(DomainEventHandler::priority));
    }

    @Override
    public <T extends DomainEvent> void unsubscribe(final Class<T> eventType, final DomainEventHandler<T> handler) {
        final List<DomainEventHandler<?>> list = handlers.get(eventType);
        if (list != null) {
            list.remove(handler);
        }
    }

    public void shutdown() {
        asyncExecutor.shutdown();
        try {
            if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
        } catch (final InterruptedException e) {
            asyncExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
