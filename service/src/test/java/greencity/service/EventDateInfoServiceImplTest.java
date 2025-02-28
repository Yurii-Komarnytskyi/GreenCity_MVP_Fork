package greencity.service;

import greencity.dto.event.EventDateInfoRequestDto;
import greencity.dto.event.EventDateInfoResponseDto;
import greencity.entity.Event;
import greencity.entity.EventDateInfo;
import greencity.repository.EventDateInfoRepo;
import greencity.repository.EventRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventDateInfoServiceImplTest {

    @Mock
    private EventDateInfoRepo eventDateInfoRepo;

    @Mock
    private EventRepo eventRepo;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private EventDateInfoServiceImpl eventDateInfoService;

    private Event event;
    private EventDateInfoRequestDto eventDateInfoRequestDto;
    private EventDateInfoResponseDto eventDateInfoResponseDto;
    private EventDateInfo eventDateInfo;

    @BeforeEach
    void setUp() {
        eventDateInfoRequestDto = EventDateInfoRequestDto.builder()
            .eventDate(LocalDate.now())
            .eventTimeStart(LocalDateTime.now())
            .eventTimeEnd(LocalDateTime.now().plusHours(1))
            .isAllDay(true)
            .isPlace(true)
            .isOnline(false)
            .location("Location")
            .url("http://example.com")
            .build();

        event = new Event();
        event.setId(1L);

        eventDateInfo = EventDateInfo.builder()
            .id(1L)
            .event(event)
            .eventDate(eventDateInfoRequestDto.getEventDate())
            .eventTimeStart(eventDateInfoRequestDto.getEventTimeStart())
            .eventTimeEnd(eventDateInfoRequestDto.getEventTimeEnd())
            .isAllDay(eventDateInfoRequestDto.getIsAllDay())
            .isPlace(eventDateInfoRequestDto.getIsPlace())
            .isOnline(eventDateInfoRequestDto.getIsOnline())
            .location(eventDateInfoRequestDto.getLocation())
            .url(eventDateInfoRequestDto.getUrl())
            .build();

        eventDateInfoResponseDto = EventDateInfoResponseDto.builder()
            .id(eventDateInfo.getId())
            .eventDate(eventDateInfo.getEventDate())
            .eventTimeStart(eventDateInfo.getEventTimeStart())
            .eventTimeEnd(eventDateInfo.getEventTimeEnd())
            .isAllDay(eventDateInfo.isAllDay())
            .isPlace(eventDateInfo.isPlace())
            .isOnline(eventDateInfo.isOnline())
            .location(eventDateInfo.getLocation())
            .url(eventDateInfo.getUrl())
            .build();
    }

    @Test
    void createEventDateInfo_ShouldSaveAndReturnEventDateInfoResponseDto() {
        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(modelMapper.map(eventDateInfoRequestDto, EventDateInfo.class)).thenReturn(eventDateInfo);
        when(eventDateInfoRepo.save(eventDateInfo)).thenReturn(eventDateInfo);
        when(modelMapper.map(eventDateInfo, EventDateInfoResponseDto.class)).thenReturn(eventDateInfoResponseDto);

        EventDateInfoResponseDto result = eventDateInfoService.createEventDateInfo(1L, eventDateInfoRequestDto);

        assertNotNull(result);
        assertEquals(eventDateInfo.getId(), result.getId());
        assertEquals(eventDateInfo.getEventDate(), result.getEventDate());
        assertEquals(eventDateInfo.getEventTimeStart(), result.getEventTimeStart());
        assertEquals(eventDateInfo.getEventTimeEnd(), result.getEventTimeEnd());
        assertEquals(eventDateInfo.isAllDay(), result.isAllDay());
        assertEquals(eventDateInfo.isPlace(), result.isPlace());
        assertEquals(eventDateInfo.isOnline(), result.isOnline());
        assertEquals(eventDateInfo.getLocation(), result.getLocation());
        assertEquals(eventDateInfo.getUrl(), result.getUrl());

        verify(eventRepo, times(1)).findById(1L);
        verify(eventDateInfoRepo, times(1)).save(eventDateInfo);
    }

    @Test
    void getEventDateInfoByEvent_ShouldReturnListOfEventDateInfoResponseDto() {
        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(eventDateInfoRepo.findByEvent(event)).thenReturn(List.of(eventDateInfo));
        when(modelMapper.map(eventDateInfo, EventDateInfoResponseDto.class)).thenReturn(eventDateInfoResponseDto);

        List<EventDateInfoResponseDto> result = eventDateInfoService.getEventDateInfoByEvent(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(eventDateInfo.getId(), result.get(0).getId());
        verify(eventRepo, times(1)).findById(1L);
        verify(eventDateInfoRepo, times(1)).findByEvent(event);
    }

    @Test
    void createEventDateInfo_ShouldThrowIllegalArgumentException_WhenEventNotFound() {
        when(eventRepo.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            eventDateInfoService.createEventDateInfo(1L, eventDateInfoRequestDto);
        });

        assertEquals("Event not found with id: 1", exception.getMessage());
        verify(eventRepo, times(1)).findById(1L);
    }
}
