package engineering.everest.lhotse.api.rest.controllers;

import engineering.everest.lhotse.api.rest.annotations.AdminOrRegisteredUser;
import engineering.everest.lhotse.api.rest.annotations.RegisteredUser;
import engineering.everest.lhotse.api.rest.converters.DtoConverter;
import engineering.everest.lhotse.api.rest.responses.PhotoResponse;
import engineering.everest.lhotse.photos.services.PhotosReadService;
import engineering.everest.lhotse.photos.services.PhotosService;
import engineering.everest.starterkit.filestorage.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@RestController
@RequestMapping("/api/photos")
@Tag(name = "Photos")
@SecurityRequirement(name = "bearerAuth")
public class PhotosController {

    private final DtoConverter dtoConverter;
    private final FileService fileService;
    private final PhotosService photosService;
    private final PhotosReadService photosReadService;

    public PhotosController(DtoConverter dtoConverter,
                            FileService fileService,
                            PhotosService photosService,
                            PhotosReadService photosReadService) {
        this.dtoConverter = dtoConverter;
        this.fileService = fileService;
        this.photosService = photosService;
        this.photosReadService = photosReadService;
    }

    @PostMapping
    @ResponseStatus(CREATED)
    @Operation(description = "List all photos belonging to the current user")
    @RegisteredUser
    public UUID uploadPhoto(@Parameter(hidden = true) Principal principal, @RequestParam("file") MultipartFile uploadedFile)
        throws IOException {
        var persistedFileId = fileService.transferToEphemeralStore(uploadedFile.getOriginalFilename(),
            uploadedFile.getSize(), uploadedFile.getInputStream());
        return photosService.registerUploadedPhoto(UUID.fromString(principal.getName()), persistedFileId,
            uploadedFile.getOriginalFilename());
    }

    @GetMapping
    @ResponseStatus(OK)
    @Operation(description = "Retrieves a page of photos accessible to the authenticated user")
    @AdminOrRegisteredUser
    public List<PhotoResponse> listPhotosForUser(@Parameter(hidden = true) Principal principal,
                                                 @SortDefault(sort = "uploadTimestamp", direction = DESC)
                                                 @PageableDefault(20) Pageable pageable) {
        return photosReadService.getAllPhotos(UUID.fromString(principal.getName()), pageable).stream()
            .map(dtoConverter::convert)
            .toList();
    }

    @GetMapping("/{photoId}")
    @AdminOrRegisteredUser
    public ResponseEntity<StreamingResponseBody> streamPhoto(@Parameter(hidden = true) Principal principal,
                                                             @PathVariable UUID photoId) {
        StreamingResponseBody streamingResponse = outputStream -> {
            try (var inputStream = photosReadService.streamPhoto(UUID.fromString(principal.getName()), photoId)) {
                inputStream.transferTo(outputStream);
            }
        };
        return ResponseEntity.ok()
            .contentType(APPLICATION_OCTET_STREAM)
            .body(streamingResponse);
    }

    @GetMapping(value = "/{photoId}/thumbnail", produces = APPLICATION_OCTET_STREAM_VALUE)
    @AdminOrRegisteredUser
    public ResponseEntity<StreamingResponseBody> streamPhotoThumbnail(@Parameter(hidden = true) Principal principal,
                                                                      @PathVariable UUID photoId,
                                                                      @RequestParam int width,
                                                                      @RequestParam int height) {
        StreamingResponseBody streamingResponse = outputStream -> {
            try (var inputStream = photosReadService.streamPhotoThumbnail(
                UUID.fromString(principal.getName()), photoId, width, height)) {
                inputStream.transferTo(outputStream);
            }
        };
        return ResponseEntity.ok()
            .contentType(APPLICATION_OCTET_STREAM)
            .body(streamingResponse);
    }
}
