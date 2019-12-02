package engineering.everest.starterkit.axon.command.validation;

public interface UserUniqueEmailValidatableCommand extends ValidatableCommand {

    String getEmailAddress();
}
