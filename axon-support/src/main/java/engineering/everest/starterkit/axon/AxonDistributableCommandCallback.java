package engineering.everest.starterkit.axon;

import com.hazelcast.core.ExecutionCallback;

import java.util.concurrent.CompletableFuture;

class AxonDistributableCommandCallback<R> implements ExecutionCallback<R> {

    private final CompletableFuture<R> future;

    public AxonDistributableCommandCallback(CompletableFuture<R> future) {
        this.future = future;
    }

    @Override
    public void onResponse(R response) {
        future.complete(response);
    }

    @Override
    public void onFailure(Throwable t) {
        future.completeExceptionally(t);
    }
}
