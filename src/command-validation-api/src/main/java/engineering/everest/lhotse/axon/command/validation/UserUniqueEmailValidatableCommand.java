package engineering.everest.lhotse.axon.command.validation;

public interface UserUniqueEmailValidatableCommand extends ValidatableCommand {

    String getEmailAddress();
}
