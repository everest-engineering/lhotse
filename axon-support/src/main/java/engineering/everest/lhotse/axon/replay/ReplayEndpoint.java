package engineering.everest.lhotse.axon.replay;

import engineering.everest.lhotse.axon.replay.ReplayableEventProcessor.ListenerRegistry;
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

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@Endpoint(id = "replay")
public class ReplayEndpoint {

    private final AxonConfiguration axonConfiguration;
    private final List<ReplayCompletionAware> replayCompletionAwares;
    private final TaskExecutor taskExecutor;
    private final ConcurrentHashMap<ReplayableEventProcessor, ListenerRegistry> replayingProcessors;

    @Autowired
    public ReplayEndpoint(AxonConfiguration axonConfiguration,
                          List<ReplayCompletionAware> replayCompletionAwares,
                          TaskExecutor taskExecutor) {
        this.axonConfiguration = axonConfiguration;
        this.replayCompletionAwares = replayCompletionAwares;
        this.taskExecutor = taskExecutor;
        replayingProcessors = new ConcurrentHashMap<>();
    }

    @ReadOperation
    public Map<String, Object> status() {
        var statusMap = new HashMap<String, Object>();
        statusMap.put("ReplayableEventProcessors", getReplayableEventProcessors().size());
        int currentlyReplaying = replayingProcessors.size();
        statusMap.put("currentlyReplaying", currentlyReplaying);
        statusMap.put("isReplaying", currentlyReplaying > 0);
        return statusMap;
    }

    @WriteOperation
    public void startReplay(@Nullable Set<String> processingGroups,
                            @Nullable OffsetDateTime startTime) {
        var replayableEventProcessors = processingGroups == null
                ? getReplayableEventProcessors() : getReplayableEventProcessor(processingGroups);

        if (replayableEventProcessors.isEmpty()) {
            throw new IllegalStateException("No matching replayable event processors");
        }

        synchronized (this) {
            if (isReplaying()) {
                throw new IllegalStateException("Cannot start replay while an existing one is running");
            }
            EventStore eventStore = axonConfiguration.eventStore();
            TrackingToken startPosition = startTime == null
                    ? eventStore.createTailToken() : eventStore.createTokenAt(startTime.toInstant());

            ReplayMarkerEvent replayMarkerEvent = new ReplayMarkerEvent(randomUUID());
            replayableEventProcessors.forEach(p -> {
                replayingProcessors.put(p, p.registerReplayCompletionListener(this::onSingleProcessorReplayCompletion));
                p.startReplay(startPosition, replayMarkerEvent);
            });
            axonConfiguration.eventGateway().publish(replayMarkerEvent);
        }
    }

    @SuppressWarnings("PMD.CloseResource")
    private void onSingleProcessorReplayCompletion(ReplayableEventProcessor processor) {
        synchronized (this) {
            ListenerRegistry listenerRegistry = replayingProcessors.remove(processor);
            if (listenerRegistry == null) {
                LOGGER.warn("Processor not registered for replaying: {}", processor);
                return;
            }
            try {
                listenerRegistry.close();
            } catch (IOException e) {
                LOGGER.error("Cannot de-register listener for processor: {}", processor, e);
            }
            if (replayingProcessors.size() == 0) {
                LOGGER.info("Executing reset completion tasks");
                taskExecutor.execute(() -> replayCompletionAwares.forEach(t -> {
                    try {
                        t.replayCompleted();
                    } catch (Exception e) {
                        LOGGER.error("Error when running replay completion aware task", e);
                    }
                }));
            }
        }
    }

    private boolean isReplaying() {
        return replayingProcessors.size() > 0;
    }

    private List<ReplayableEventProcessor> getReplayableEventProcessors() {
        return axonConfiguration.eventProcessingConfiguration().eventProcessors().values().stream()
                .filter(e -> e instanceof ReplayableEventProcessor)
                .map(e -> (ReplayableEventProcessor) e)
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
