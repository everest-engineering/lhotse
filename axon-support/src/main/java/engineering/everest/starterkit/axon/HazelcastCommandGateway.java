package engineering.everest.starterkit.axon;

import com.hazelcast.core.HazelcastInstance;
import engineering.everest.starterkit.axon.common.exceptions.RemoteCommandExecutionException;
import engineering.everest.starterkit.axon.config.AxonHazelcastConfig;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.Registration;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.modelling.command.AnnotationCommandTargetResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.axonframework.commandhandling.GenericCommandMessage.asCommandMessage;
import static org.axonframework.commandhandling.GenericCommandResultMessage.asCommandResultMessage;

@Component
public class HazelcastCommandGateway implements CommandGateway {

    private final HazelcastInstance hazelcastInstance;
    private final AnnotationCommandTargetResolver annotationCommandTargetResolver;

    @Autowired
    public HazelcastCommandGateway(HazelcastInstance hazelcastInstance,
                                   AnnotationCommandTargetResolver annotationCommandTargetResolver) {
        this.hazelcastInstance = hazelcastInstance;
        this.annotationCommandTargetResolver = annotationCommandTargetResolver;
    }

    @Override
    public <C, R> void send(C command, CommandCallback<? super C, ? super R> callback) {
        CompletableFuture<R> future = dispatchThroughHazelcastAndReturnFuture(command);
        future.thenApply(response -> {
            callback.onResult(asCommandMessage(command), asCommandResultMessage(response));
            return null;
        });
    }

    @Override
    public <R> CompletableFuture<R> send(Object command) {
        return dispatchThroughHazelcastAndReturnFuture(command);
    }

    @Override
    public <R> R sendAndWait(Object command) {
        CompletableFuture<R> future = dispatchThroughHazelcastAndReturnFuture(command);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RemoteCommandExecutionException(e);
        }
    }

    @Override
    public <R> R sendAndWait(Object command, long timeout, TimeUnit unit) {
        CompletableFuture<R> future = dispatchThroughHazelcastAndReturnFuture(command);
        try {
            return future.get(timeout, unit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RemoteCommandExecutionException(e);
        }
    }

    @Override
    public Registration registerDispatchInterceptor(MessageDispatchInterceptor<? super CommandMessage<?>> dispatchInterceptor) {
        throw new UnsupportedOperationException("Not invented here");
    }

    private <R> CompletableFuture<R> dispatchThroughHazelcastAndReturnFuture(Object command) {
        CompletableFuture<R> future = new CompletableFuture<>();
        AxonDistributableCommandCallback<R> callback = new AxonDistributableCommandCallback<>(future);
        hazelcastInstance.getExecutorService(AxonHazelcastConfig.AXON_COMMAND_DISPATCHER)
                .submitToKeyOwner(new AxonDistributableCommand<>(command), keyForCommand(command), callback);
        return future;
    }

    private String keyForCommand(Object command) {
        return annotationCommandTargetResolver.resolveTarget(asCommandMessage(command)).getIdentifier();
    }
}
