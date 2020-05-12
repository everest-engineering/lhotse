package engineering.everest.lhotse.axon.replay;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.spring.config.AxonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.core.task.TaskExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@Endpoint(id = "replay")
public class ReplayEndpoint {

    private final AxonConfiguration axonConfiguration;
    private final List<ReplayCompletionAware> resetCompletionAwares;
    private final TaskExecutor taskExecutor;
    private final ConcurrentHashMap<ReplayableEventProcessor, Closeable> listenerRegistry = new ConcurrentHashMap<>();
    private final AtomicBoolean replaying = new AtomicBoolean();

    @Autowired

    public ReplayEndpoint(AxonConfiguration axonConfiguration,
                          List<ReplayCompletionAware> resetCompletionAwares,
                          TaskExecutor taskExecutor) {
        this.axonConfiguration = axonConfiguration;
        this.resetCompletionAwares = resetCompletionAwares;
        this.taskExecutor = taskExecutor;
    }

    @ReadOperation
    public Map<String, Object> status() {
        var statusMap = new HashMap<String, Object>();
        statusMap.put("switchingEventProcessors", getReplayableEventProcessors().size());
        statusMap.put("isReplaying", isReplaying());
        return statusMap;
    }

    @WriteOperation
    public void startReplay(@Nullable Set<String> processingGroups,
                            @Nullable OffsetDateTime startTime) {
        if (!replaying.compareAndSet(false, true)) {
            throw new IllegalStateException("Cannot start replay while an existing one is running");
        }
        try {
            var replayableEventProcessors = processingGroups == null
                    ? getReplayableEventProcessors() : getReplayableEventProcessor(processingGroups);

            if (replayableEventProcessors.isEmpty()) {
                throw new IllegalStateException("No matching replayable event processors");
            }

            EventStore eventStore = axonConfiguration.eventStore();
            final TrackingToken startPosition = startTime == null
                    ? eventStore.createTailToken() : eventStore.createTokenAt(startTime.toInstant());

            ReplayMarkerEvent replayMarkerEvent = new ReplayMarkerEvent(randomUUID());
            listenerRegistry.clear();
            replayableEventProcessors.forEach(p -> {
                listenerRegistry.put(p, p.registerReplayCompletionListener(this::onSingleProcessorReplayCompletion));
                p.startReplay(startPosition, replayMarkerEvent);
            });
            axonConfiguration.eventGateway().publish(replayMarkerEvent);
        } catch (Exception e) {
            replaying.set(false);
            throw e;
        }
    }

    private void onSingleProcessorReplayCompletion(ReplayableEventProcessor processor) {
        Closeable registry = listenerRegistry.remove(processor);
        if (registry == null) {
            throw new IllegalStateException(
                    "replay completion callback is called by a processor that is not part of the replay");
        }
        try {
            registry.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (listenerRegistry.size() == 0 && replaying.compareAndSet(true, false)) {
            taskExecutor.execute(() -> resetCompletionAwares.forEach(ReplayCompletionAware::replayCompleted));
        }
    }

    private boolean isReplaying() {
        return replaying.get();
    }

    private List<ReplayableEventProcessor> getReplayableEventProcessors() {
        return axonConfiguration.eventProcessingConfiguration().eventProcessors().values().stream()
                .filter(e -> e instanceof ReplayableEventProcessor)
                .map(e -> (SwitchingEventProcessor) e)
                .collect(toList());
    }

    private List<ReplayableEventProcessor> getReplayableEventProcessor(Set<String> processingGroups) {
        return processingGroups.stream()
                .map(e -> axonConfiguration.eventProcessingConfiguration()
                        .eventProcessorByProcessingGroup(e, ReplayableEventProcessor.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }
}
