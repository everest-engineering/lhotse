package engineering.everest.lhotse.axon.replay;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.DisallowReplay;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.spring.config.AxonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.core.task.TaskExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@Endpoint(id = "replay")
public class ReplayEndpoint {

    private final AxonConfiguration axonConfiguration;
    private final List<ReplayCompletionAware> resetCompletionAwares;
    private final TaskExecutor taskExecutor;

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
        statusMap.put("switchingEventProcessors", getSwitchingEventProcessors().size());
        statusMap.put("isReplaying", isReplaying());
        return statusMap;
    }

    @WriteOperation
    public void startReplay(@Nullable Set<String> processingGroups,
                            @Nullable OffsetDateTime startTime) {
        synchronized (this) {
            if (isReplaying()) {
                throw new IllegalStateException("Cannot start replay while an existing one is running");
            }
            var switchingEventProcessors = processingGroups == null
                    ? getSwitchingEventProcessors() : getSwitchingEventProcessors(processingGroups);

            if (switchingEventProcessors.isEmpty()) {
                throw new IllegalStateException("No matching SwitchingEventProcessor");
            }

            EventStore eventStore = axonConfiguration.eventStore();
            var trackingToken = startTime == null
                    ? eventStore.createTailToken() : eventStore.createTokenAt(startTime.toInstant());

            ReplayMarkerEvent replayMarkerEvent = new ReplayMarkerEvent(randomUUID());
            switchingEventProcessors.forEach(p -> p.startReplay(trackingToken, replayMarkerEvent));
            axonConfiguration.eventGateway().publish(replayMarkerEvent);
        }
    }

    @EventHandler
    @DisallowReplay
    void on(ReplayMarkerEvent event) {
        if (!isReplaying()) {
            LOGGER.info("Ignoring replay marker event as we are not replaying: {}", event);
            return;
        }
        LOGGER.info("Handling replay marker event: {}", event);
        stopReplay();
    }

    private void stopReplay() {
        taskExecutor.execute(() -> {
            getSwitchingEventProcessors().stream()
                    .filter(SwitchingEventProcessor::isReplaying)
                    .forEach(SwitchingEventProcessor::stopReplay);
            resetCompletionAwares.forEach(ReplayCompletionAware::replayCompleted);
        });
    }

    private boolean isReplaying() {
        return getSwitchingEventProcessors().stream()
                .anyMatch(SwitchingEventProcessor::isReplaying);
    }

    private List<SwitchingEventProcessor> getSwitchingEventProcessors() {
        return axonConfiguration.eventProcessingConfiguration().eventProcessors().values().stream()
                .filter(e -> e instanceof SwitchingEventProcessor)
                .map(e -> (SwitchingEventProcessor) e)
                .collect(toList());
    }

    private List<SwitchingEventProcessor> getSwitchingEventProcessors(Set<String> processingGroups) {
        return processingGroups.stream()
                .map(e -> axonConfiguration.eventProcessingConfiguration()
                        .eventProcessorByProcessingGroup(e, SwitchingEventProcessor.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }
}
