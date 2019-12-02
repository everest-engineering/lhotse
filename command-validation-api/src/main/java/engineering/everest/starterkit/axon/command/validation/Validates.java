package engineering.everest.starterkit.axon.command.validation;

public interface Validates<T extends ValidatableCommand> {

    void validate(T validatable);
}
