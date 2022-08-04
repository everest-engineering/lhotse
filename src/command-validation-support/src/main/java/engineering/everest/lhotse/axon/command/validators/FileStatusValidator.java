package engineering.everest.lhotse.axon.command.validators;

import engineering.everest.lhotse.axon.command.validation.FileStatusValidatableCommand;
import engineering.everest.lhotse.axon.command.validation.Validates;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalStateException;
import engineering.everest.starterkit.filestorage.FileService;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

import static engineering.everest.lhotse.i18n.MessageKeys.FILE_DOES_NOT_EXIST;

@Component
public class FileStatusValidator implements Validates<FileStatusValidatableCommand> {

    private final FileService fileService;

    public FileStatusValidator(FileService fileService) {
        this.fileService = fileService;
    }

    @Override
    public void validate(FileStatusValidatableCommand validatable) {
        validatable.getFileIDs().forEach(fileId -> {
            try {
                fileService.fileSizeInBytes(fileId);
            } catch (NoSuchElementException e) {
                throwWrappedInCommandExecutionException(new TranslatableIllegalStateException(FILE_DOES_NOT_EXIST, fileId));
            }
        });
    }
}
