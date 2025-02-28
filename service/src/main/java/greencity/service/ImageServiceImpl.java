package greencity.service;

import greencity.dto.event.ImageRequestDto;
import greencity.dto.event.ImageResponseDto;
import greencity.entity.Event;
import greencity.entity.Image;
import greencity.exception.exceptions.BadRequestException;
import greencity.repository.EventRepo;
import greencity.repository.ImageRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ImageServiceImpl implements ImageService {
    private ImageRepo imageRepo;
    private EventRepo eventRepo;
    private ModelMapper modelMapper;

    @Override
    public ImageResponseDto createImage(ImageRequestDto imageRequestDto) {
        if (imageRequestDto == null) {
            throw new BadRequestException("ImageRequestDto cannot be null");
        }

        Image image = modelMapper.map(imageRequestDto, Image.class);
        return modelMapper.map(imageRepo.save(image), ImageResponseDto.class);
    }

    @Override
    public ImageResponseDto updateImage(Long imageId, ImageRequestDto imageRequestDto) {
        if (imageRequestDto == null) {
            throw new BadRequestException("ImageRequestDto cannot be null");
        }

        Image image = imageRepo.findById(imageId).orElse(null);

        if (image != null) {
            image.setImagePath(imageRequestDto.getImagePath());
            return modelMapper.map(imageRepo.save(image), ImageResponseDto.class);
        } else {
            throw new EntityNotFoundException("Image not found with id: " + imageId);
        }
    }

    @Override
    public void deleteImage(Long imageId) {
        Image image = imageRepo.findById(imageId).orElse(null);

        if (image != null) {
            imageRepo.delete(image);
        } else {
            throw new EntityNotFoundException("Image not found with id: " + imageId);
        }
    }

    @Override
    public ImageResponseDto getImageById(Long id) {
        Image image = imageRepo.findById(id).orElse(null);

        if (image != null) {
            return modelMapper.map(image, ImageResponseDto.class);
        } else {
            throw new EntityNotFoundException("Image not found with id: " + id);
        }
    }

    @Override
    public List<ImageResponseDto> getImagesByEventId(Long eventId) {
        Event event = eventRepo.findById(eventId).orElse(null);

        if (event != null) {
            List<Image> images = imageRepo.findAllByEvent(event);
            List<ImageResponseDto> result =
                images.stream().map(image -> modelMapper.map(image, ImageResponseDto.class)).toList();
            return result;
        } else {
            throw new EntityNotFoundException("Event not found with id: " + eventId);
        }
    }
}
