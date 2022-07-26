package engineering.everest.lhotse.api.rest.controllers;

import engineering.everest.lhotse.api.rest.annotations.AdminOrRegisteredUser;
import engineering.everest.lhotse.api.rest.annotations.RegisteredUser;
import engineering.everest.lhotse.api.rest.converters.DtoConverter;
import engineering.everest.lhotse.api.rest.responses.PhotoResponse;
import engineering.everest.lhotse.photos.services.PhotosReadService;
import engineering.everest.lhotse.photos.services.PhotosService;
import engineering.everest.starterkit.filestorage.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/photos")
@Api(tags = "Photos")
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
    @ApiOperation("List all photos belonging to the current user")
    @RegisteredUser
    public UUID uploadPhoto(@ApiIgnore Principal principal, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        var persistedFileId = fileService.transferToPermanentStore(uploadedFile.getOriginalFilename(),
            uploadedFile.getSize(), uploadedFile.getInputStream());
        return photosService.registerUploadedPhoto(UUID.fromString(principal.getName()), persistedFileId,
            uploadedFile.getOriginalFilename());
    }

    @GetMapping
    @ResponseStatus(OK)
    @ApiOperation("Retrieves a page of photos accessible to the authenticated user")
    @AdminOrRegisteredUser
    public List<PhotoResponse> getAllPhotos(@ApiIgnore Principal principal,
                                            @SortDefault(sort = "uploadTimestamp", direction = DESC)
                                            @PageableDefault(20) Pageable pageable) {
        return photosReadService.getAllPhotosForUser(UUID.fromString(principal.getName()), pageable).stream()
            .map(dtoConverter::convert)
            .collect(toList());
    }
}
