package engineering.everest.lhotse.axon.command.validators;

import engineering.everest.lhotse.axon.command.AxonCommandExecutionExceptionFactory;
import engineering.everest.lhotse.axon.command.validation.FileStatusValidatableCommand;
import engineering.everest.starterkit.filestorage.FileService;
import org.axonframework.commandhandling.CommandExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileStatusValidatorTest {

    private static final UUID FILE_ID_1 = randomUUID();
    private static final UUID FILE_ID_2 = randomUUID();
    private static final FileStatusValidatableCommand VALIDATABLE_COMMAND = () -> Set.of(FILE_ID_1, FILE_ID_2);

    private FileStatusValidator fileStatusValidator;

    @Mock
    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileStatusValidator = new FileStatusValidator(fileService, new AxonCommandExecutionExceptionFactory());
    }

    @Test
    void validate_WillPassWhenAllFilesExist() {
        when(fileService.fileSizeInBytes(FILE_ID_1)).thenReturn(1234L);
        when(fileService.fileSizeInBytes(FILE_ID_2)).thenReturn(5678L);

        fileStatusValidator.validate(VALIDATABLE_COMMAND);
    }

    @Test
    void validate_WillFailWhenAnySingleFileCannotBeFound() {
        lenient().when(fileService.fileSizeInBytes(FILE_ID_1)).thenReturn(1234L);
        when(fileService.fileSizeInBytes(FILE_ID_2)).thenThrow(new NoSuchElementException("not here"));

        var exception = assertThrows(CommandExecutionException.class,
            () -> fileStatusValidator.validate(VALIDATABLE_COMMAND));
        assertEquals("FILE_DOES_NOT_EXIST", exception.getMessage());
    }
}
