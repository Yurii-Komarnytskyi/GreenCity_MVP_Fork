package greencity.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import greencity.constant.ErrorMessage;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.ImageUrlParseException;
import greencity.exception.exceptions.NotSavedException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@Service
public class AzureCloudStorageService implements FileService {
    private final String connectionString;
    private final String containerName;
    private final ModelMapper modelMapper;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * Constructor with parameters.
     */
    public AzureCloudStorageService(@Autowired PropertyResolver propertyResolver,
        ModelMapper modelMapper) {
        this.connectionString = propertyResolver.getProperty("azure.connection.string");
        this.containerName = propertyResolver.getProperty("azure.container.name");
        this.modelMapper = modelMapper;
    }

    /**
     * {@inheritDoc}
     */

    public String upload(MultipartFile multipartFile) {
        if (multipartFile.getSize() > MAX_FILE_SIZE) {
            String fileName = multipartFile.getOriginalFilename();
            String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
            if (fileExtension.equals("jpg") || fileExtension.equals("png")) {
                throw new MultipartException("Incorrect image size. Maximum allowed size is 10 MB");
            }
        }

        final String blob = UUID.randomUUID().toString();
        BlobClient client = containerClient()
            .getBlobClient(blob + multipartFile.getOriginalFilename());
        try {
            client.upload(new BufferedInputStream(multipartFile.getInputStream()), multipartFile.getSize());
        } catch (IOException e) {
            throw new NotSavedException(ErrorMessage.FILE_NOT_SAVED);
        }

        String contentType = null;
        try {
            contentType = Files.probeContentType(Path.of(Objects.requireNonNull(multipartFile.getOriginalFilename())));
        } catch (IOException e) {
            throw new NotSavedException("Failed to determine file content type");
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(contentType);
        client.setHttpHeaders(headers);

        return client.getBlobUrl();
    }

    private BlobContainerClient containerClient() {
        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
            .connectionString(connectionString).buildClient();
        return serviceClient.getBlobContainerClient(containerName);
    }

    /**
     * {@inheritDoc}
     */
    public MultipartFile convertToMultipartImage(String image) {
        try {
            return modelMapper.map(image, MultipartFile.class);
        } catch (Exception e) {
            throw new BadRequestException(ErrorMessage.MULTIPART_FILE_BAD_REQUEST + image);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String path) {
        String fileName;
        try {
            fileName = Paths.get(new URI(path).getPath()).getFileName().toString();
        } catch (URISyntaxException e) {
            throw new ImageUrlParseException(ErrorMessage.PARSING_URL_FAILED + path);
        }
        BlobClient client = containerClient().getBlobClient(fileName);
        if (Boolean.TRUE.equals(client.exists())) {
            client.delete();
        }
    }
}
