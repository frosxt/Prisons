package com.github.frosxt.prisoncore.scheduler.chain;

import com.github.frosxt.prisoncore.scheduler.api.TaskHandle;
import com.github.frosxt.prisoncore.scheduler.api.TaskOrchestrator;
import com.github.frosxt.prisoncore.scheduler.api.TaskSpec;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class AsyncActionChain {
    private final TaskOrchestrator orchestrator;
    private final List<ChainStep> steps;
    private Duration pendingDelay;
    private Consumer<Exception> errorHandler;
    private Runnable completionHandler;
    private final AtomicBoolean cancelled;
    private final AtomicReference<TaskHandle> currentTask;

    private AsyncActionChain(final TaskOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
        this.steps = new ArrayList<>();
        this.cancelled = new AtomicBoolean(false);
        this.currentTask = new AtomicReference<>();
    }

    public static AsyncActionChain create(final TaskOrchestrator orchestrator) {
        return new AsyncActionChain(orchestrator);
    }

    public AsyncActionChain then(final Runnable action) {
        addStep(action, ThreadContext.MAIN, 1);
        return this;
    }

    public AsyncActionChain thenAsync(final Runnable action) {
        addStep(action, ThreadContext.ASYNC, 1);
        return this;
    }

    public AsyncActionChain delay(final Duration duration) {
        this.pendingDelay = duration;
        return this;
    }

    public AsyncActionChain delayTicks(final long ticks) {
        this.pendingDelay = Duration.ofMillis(ticks * 50L);
        return this;
    }

    public AsyncActionChain repeat(final Runnable action, final int times) {
        addStep(action, ThreadContext.MAIN, times);
        return this;
    }

    public AsyncActionChain conditional(final Supplier<Boolean> condition, final Runnable action) {
        final Runnable wrapped = () -> {
            if (Boolean.TRUE.equals(condition.get())) {
                action.run();
            }
        };
        addStep(wrapped, ThreadContext.MAIN, 1);
        return this;
    }

    public AsyncActionChain onError(final Consumer<Exception> handler) {
        this.errorHandler = handler;
        return this;
    }

    public AsyncActionChain onComplete(final Runnable handler) {
        this.completionHandler = handler;
        return this;
    }

    public CompletableFuture<Void> execute() {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        if (steps.isEmpty()) {
            invokeCompletion();
            future.complete(null);
            return future;
        }
        executeStep(0, future);
        return future;
    }

    public void cancel() {
        cancelled.set(true);
        final TaskHandle handle = currentTask.get();
        if (handle != null && !handle.isCancelled()) {
            handle.cancel();
        }
    }

    private void addStep(final Runnable action, final ThreadContext context, final int repetitions) {
        steps.add(new ChainStep(action, pendingDelay, context, repetitions));
        pendingDelay = null;
    }

    private void executeStep(final int index, final CompletableFuture<Void> future) {
        if (cancelled.get()) {
            future.cancel(false);
            return;
        }

        if (index >= steps.size()) {
            invokeCompletion();
            future.complete(null);
            return;
        }

        final ChainStep step = steps.get(index);

        final Runnable stepExecution = () -> {
            if (cancelled.get()) {
                future.cancel(false);
                return;
            }

            try {
                for (int i = 0; i < step.repetitions(); i++) {
                    if (cancelled.get()) {
                        future.cancel(false);
                        return;
                    }
                    step.action().run();
                }
                executeStep(index + 1, future);
            } catch (final Exception exception) {
                invokeError(exception);
                future.completeExceptionally(exception);
            }
        };

        if (step.delay() != null && !step.delay().isZero()) {
            scheduleDelayed(step, stepExecution);
        } else {
            scheduleImmediate(step, stepExecution);
        }
    }

    private void scheduleDelayed(final ChainStep step, final Runnable execution) {
        final TaskSpec spec = TaskSpec.builder(execution)
                .delay(step.delay())
                .build();

        final TaskHandle handle = (step.context() == ThreadContext.ASYNC)
                ? orchestrator.io(spec)
                : orchestrator.mainThread(spec);

        currentTask.set(handle);
    }

    private void scheduleImmediate(final ChainStep step, final Runnable execution) {
        final TaskSpec spec = TaskSpec.builder(execution).build();

        final TaskHandle handle = (step.context() == ThreadContext.ASYNC)
                ? orchestrator.io(spec)
                : orchestrator.mainThread(spec);

        currentTask.set(handle);
    }

    private void invokeCompletion() {
        if (completionHandler != null) {
            completionHandler.run();
        }
    }

    private void invokeError(final Exception exception) {
        if (errorHandler != null) {
            errorHandler.accept(exception);
        }
    }
}
