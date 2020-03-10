package engineering.everest.lhotse.axon.replay;

public interface ReplayAware {

    default void prepareForReplay() {
    }

    default void replayCompleted() {
    }
}
