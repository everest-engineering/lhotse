package engineering.everest.lhotse.axon.replay;

public interface ReplayCompletionAware {

    default void replayCompleted() {}
}
