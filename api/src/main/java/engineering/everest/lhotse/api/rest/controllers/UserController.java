package engineering.everest.lhotse.api.rest.controllers;

import engineering.everest.lhotse.axon.common.domain.User;
import engineering.everest.starterkit.filestorage.FileService;
import engineering.everest.lhotse.users.services.UsersReadService;
import engineering.everest.lhotse.users.services.UsersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import engineering.everest.lhotse.api.rest.converters.DtoConverter;
import engineering.everest.lhotse.api.rest.requests.UpdateUserRequest;
import engineering.everest.lhotse.api.rest.responses.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@RestController
@RequestMapping("/api/user")
@Api(consumes = APPLICATION_JSON_VALUE, tags = "Users")
public class UserController {

    private final DtoConverter dtoConverter;
    private final UsersService usersService;
    private final FileService fileService;
    private final UsersReadService usersReadService;

    @Autowired
    public UserController(DtoConverter dtoConverter, UsersService usersService, FileService fileService,
                          UsersReadService usersReadService) {
        this.dtoConverter = dtoConverter;
        this.usersService = usersService;
        this.fileService = fileService;
        this.usersReadService = usersReadService;
    }

    @GetMapping
    @ApiOperation(produces = APPLICATION_JSON_VALUE, value = "Get currently authenticated user information")
    public UserResponse getUser(User user) {
        return dtoConverter.convert(user);
    }

    @PutMapping
    @ApiOperation("Update currently authenticated user information")
    public void updateUser(User user, @RequestBody UpdateUserRequest updateUserRequest) {
        usersService.updateUser(user.getId(), user.getId(),
                updateUserRequest.getEmail(), updateUserRequest.getDisplayName(), updateUserRequest.getPassword());
    }

    @PostMapping("/profile-photo")
    public void uploadProfilePhoto(User requestingUser,
                                   @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        var persistedFileId = fileService.transferToPermanentStore(uploadedFile.getOriginalFilename(), uploadedFile.getInputStream());
        usersService.storeProfilePhoto(requestingUser.getId(), persistedFileId);
    }

    @GetMapping("/profile-photo")
    public ResponseEntity<StreamingResponseBody> streamProfilePhoto(User requestingUser) {
        StreamingResponseBody streamingResponse = outputStream -> {
            try (var inputStream = usersReadService.getProfilePhotoStream(requestingUser.getId())) {
                inputStream.transferTo(outputStream);
            }
        };
        return ResponseEntity.ok()
                .contentType(APPLICATION_OCTET_STREAM)
                .body(streamingResponse);
    }

    @GetMapping(
            value = "/profile-photo/thumbnail",
            produces = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> streamProfilePhotoThumbnail(User requestingUser,
                                                                             @RequestParam int width,
                                                                             @RequestParam int height) {
        StreamingResponseBody streamingResponse = outputStream -> {
            try (var inputStream = usersReadService.getProfilePhotoThumbnailStream(requestingUser.getId(), width, height)) {
                inputStream.transferTo(outputStream);
            }
        };
        return ResponseEntity.ok()
                .contentType(APPLICATION_OCTET_STREAM)
                .body(streamingResponse);
    }
}
