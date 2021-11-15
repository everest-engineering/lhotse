package engineering.everest.lhotse.api.rest.controllers;

import engineering.everest.lhotse.api.rest.converters.DtoConverter;
import engineering.everest.lhotse.api.rest.requests.UpdateUserRequest;
import engineering.everest.lhotse.api.rest.responses.UserResponse;
import engineering.everest.lhotse.users.services.UsersReadService;
import engineering.everest.lhotse.users.services.UsersService;
import engineering.everest.starterkit.filestorage.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import springfox.documentation.annotations.ApiIgnore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.security.Principal;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@RestController
@RequestMapping("/api/user")
@Api(tags = "Users")
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
    @ApiOperation("Get currently authenticated user information")
    public UserResponse getUser(@ApiIgnore Principal principal) {
        return dtoConverter.convert(usersReadService.getById(UUID.fromString(principal.getName())));
    }

    @PutMapping
    @ApiOperation("Update currently authenticated user information")
    public void updateUser(@ApiIgnore Principal principal, @RequestBody UpdateUserRequest updateUserRequest) {
        var userId = UUID.fromString(principal.getName());
        usersService.updateUser(userId, userId, updateUserRequest.getEmail(),
                updateUserRequest.getDisplayName());
    }

    @PostMapping("/profile-photo")
    public void uploadProfilePhoto(@ApiIgnore Principal principal, @RequestParam("file") MultipartFile uploadedFile)
            throws IOException {
        var persistedFileId = fileService.transferToPermanentStore(uploadedFile.getOriginalFilename(),
                uploadedFile.getSize(), uploadedFile.getInputStream());
        usersService.storeProfilePhoto(UUID.fromString(principal.getName()), persistedFileId);
    }

    @GetMapping("/profile-photo")
    public ResponseEntity<StreamingResponseBody> streamProfilePhoto(@ApiIgnore Principal principal) {
        StreamingResponseBody streamingResponse = outputStream -> {
            try (var inputStream = usersReadService.getProfilePhotoStream(UUID.fromString(principal.getName()))) {
                inputStream.transferTo(outputStream);
            }
        };
        return ResponseEntity.ok()
                .contentType(APPLICATION_OCTET_STREAM)
                .body(streamingResponse);
    }

    @GetMapping(value = "/profile-photo/thumbnail", produces = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> streamProfilePhotoThumbnail(@ApiIgnore Principal principal,
            @RequestParam int width, @RequestParam int height) {
        StreamingResponseBody streamingResponse = outputStream -> {
            try (var inputStream = usersReadService.getProfilePhotoThumbnailStream(UUID.fromString(principal.getName()),
                    width, height)) {
                inputStream.transferTo(outputStream);
            }
        };
        return ResponseEntity.ok()
                .contentType(APPLICATION_OCTET_STREAM)
                .body(streamingResponse);
    }
}
