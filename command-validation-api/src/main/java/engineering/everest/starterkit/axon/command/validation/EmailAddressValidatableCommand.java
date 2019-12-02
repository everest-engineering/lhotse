package engineering.everest.starterkit.axon.command.validation;

public interface EmailAddressValidatableCommand extends ValidatableCommand {

    String getEmailAddress();
}
