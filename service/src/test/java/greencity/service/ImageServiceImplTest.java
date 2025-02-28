package greencity.service;

import greencity.dto.event.ImageRequestDto;
import greencity.dto.event.ImageResponseDto;
import greencity.entity.Event;
import greencity.entity.Image;
import greencity.entity.User;
import greencity.exception.exceptions.BadRequestException;
import greencity.repository.EventRepo;
import greencity.repository.ImageRepo;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImageServiceImplTest {
    @Mock
    private ImageRepo imageRepo;

    @Mock
    private EventRepo eventRepo;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ImageServiceImpl imageService;

    private Image image;
    private ImageRequestDto imageRequestDto;
    private ImageResponseDto imageResponseDto;
    private Event event;

    @BeforeEach
    void setUp() {
        image = new Image();
        image.setImagePath("Image path");
        image.setId(1L);

        imageRequestDto = new ImageRequestDto();
        imageRequestDto.setImagePath("Image path");

        imageResponseDto = new ImageResponseDto();
        imageResponseDto.setImagePath(imageRequestDto.getImagePath());
        imageResponseDto.setId(1L);

        event = new Event();
        event.setId(1L);
        event.setAuthor(new User());
    }

    @Test
    void createImageTest() {
        when(modelMapper.map(imageRequestDto, Image.class)).thenReturn(image);
        when(modelMapper.map(image, ImageResponseDto.class)).thenReturn(imageResponseDto);
        when(imageRepo.save(image)).thenReturn(image);

        ImageResponseDto result = imageService.createImage(imageRequestDto);

        Assertions.assertNotNull(result, "Result should not be null");
        Assertions.assertEquals(imageResponseDto.getImagePath(), result.getImagePath(), "Image paths should match");

        verify(modelMapper, times(1)).map(imageRequestDto, Image.class);
        verify(imageRepo, times(1)).save(image);
        verify(modelMapper, times(1)).map(image, ImageResponseDto.class);
    }

    @Test
    void createImageBadRequestTest() {
        imageRequestDto = null;

        Exception exception =
            Assertions.assertThrows(BadRequestException.class, () -> imageService.createImage(imageRequestDto));
        Assertions.assertEquals("ImageRequestDto cannot be null", exception.getMessage());

        verify(imageRepo, times(0)).save(any(Image.class));
    }

    @Test
    void updateImageTest() {
        imageRequestDto.setImagePath("New image path");

        when(imageRepo.findById(image.getId())).thenReturn(Optional.of(image));
        when(imageRepo.save(image)).thenReturn(image);
        when(modelMapper.map(image, ImageResponseDto.class)).thenReturn(imageResponseDto);

        ImageResponseDto result = imageService.updateImage(1L, imageRequestDto);

        Assertions.assertNotNull(result, "Result should not be null");
        Assertions.assertEquals(imageResponseDto.getImagePath(), result.getImagePath(), "Image paths should match");
        Assertions.assertEquals(imageResponseDto.getId(), result.getId(), "Image id should match");

        verify(modelMapper, times(1)).map(image, ImageResponseDto.class);
        verify(imageRepo, times(1)).save(image);
    }

    @Test
    void updateImageEntityNotFoundTest() {
        imageRequestDto.setImagePath("New image path");

        when(imageRepo.findById(image.getId())).thenReturn(Optional.empty());

        Exception exception =
            Assertions.assertThrows(EntityNotFoundException.class, () -> imageService.updateImage(1L, imageRequestDto));
        Assertions.assertEquals("Image not found with id: 1", exception.getMessage());

        verify(imageRepo, times(0)).save(any(Image.class));
    }

    @Test
    void deleteImageTest() {
        when(imageRepo.findById(image.getId())).thenReturn(Optional.of(image));
        doNothing().when(imageRepo).delete(image);

        imageService.deleteImage(image.getId());

        verify(imageRepo, times(1)).delete(image);
    }

    @Test
    void deleteImageEntityNotFoundTest() {
        when(imageRepo.findById(image.getId())).thenReturn(Optional.empty());

        Exception exception = Assertions.assertThrows(EntityNotFoundException.class,  () -> imageService.deleteImage(1L));
        Assertions.assertEquals("Image not found with id: 1", exception.getMessage());

        verify(imageRepo, times(0)).delete(image);
    }

    @Test
    void getImageByIdTest() {
        when(imageRepo.findById(image.getId())).thenReturn(Optional.of(image));
        when(modelMapper.map(image, ImageResponseDto.class)).thenReturn(imageResponseDto);

        ImageResponseDto result = imageService.getImageById(image.getId());

        Assertions.assertNotNull(result, "Result should not be null");
        Assertions.assertEquals(imageResponseDto.getImagePath(), result.getImagePath(), "Image paths should match");

        verify(imageRepo, times(1)).findById(image.getId());
    }

    @Test
    void getImageByIdEntityNotFoundTest() {
        when(imageRepo.findById(image.getId())).thenReturn(Optional.empty());

        Exception exception = Assertions.assertThrows(EntityNotFoundException.class,  () -> imageService.deleteImage(1L));
        Assertions.assertEquals("Image not found with id: 1", exception.getMessage());

        verify(imageRepo, times(1)).findById(1L);
    }

    @Test
    void getImagesByEventIdTest() {
        when(eventRepo.findById(event.getId())).thenReturn(Optional.of(event));
        when(imageRepo.findAllByEvent(event)).thenReturn(List.of(image));
        when(modelMapper.map(image, ImageResponseDto.class)).thenReturn(imageResponseDto);

        List<ImageResponseDto> result = imageService.getImagesByEventId(event.getId());

        Assertions.assertNotNull(result, "Result should not be null");
        Assertions.assertEquals(imageResponseDto.getImagePath(), result.get(0).getImagePath(), "Image paths should match");

        verify(imageRepo, times(1)).findAllByEvent(event);
    }

    @Test
    void getImagesByEventEntityNotFoundIdTest() {
        when(imageRepo.findById(image.getId())).thenReturn(Optional.empty());

        Exception exception = Assertions.assertThrows(EntityNotFoundException.class,  () -> imageService.deleteImage(1L));
        Assertions.assertEquals("Image not found with id: 1", exception.getMessage());

        verify(imageRepo, times(0)).findAllByEvent(any(Event.class));
    }
}
