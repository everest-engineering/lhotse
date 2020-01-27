package engineering.everest.lhotse.axon.command.validation;

public interface EmailAddressValidatableCommand extends ValidatableCommand {

    String getEmailAddress();
}
