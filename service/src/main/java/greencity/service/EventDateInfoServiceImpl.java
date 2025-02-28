package greencity.service;

import greencity.dto.event.EventDateInfoRequestDto;
import greencity.dto.event.EventDateInfoResponseDto;
import greencity.dto.event.EventDateInfoUpdateDto;
import greencity.entity.Event;
import greencity.entity.EventDateInfo;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.EventDateInfoRepo;
import greencity.repository.EventRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventDateInfoServiceImpl implements EventDateInfoService {

    private final EventDateInfoRepo eventDateInfoRepo;
    private final EventRepo eventRepo;
    private final ModelMapper modelMapper;

    @Override
    public EventDateInfoResponseDto createEventDateInfo(Long eventId, EventDateInfoRequestDto requestDto) {
        Event event = eventRepo.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

        EventDateInfo eventDateInfo = modelMapper.map(requestDto, EventDateInfo.class);
        eventDateInfo.setEvent(event);

        EventDateInfo savedEventDateInfo = eventDateInfoRepo.save(eventDateInfo);

        return modelMapper.map(savedEventDateInfo, EventDateInfoResponseDto.class);
    }

    @Override
    @Transactional
    public EventDateInfoResponseDto updateEventDateInfo(Long id, EventDateInfoUpdateDto requestDto) {

        EventDateInfo existingInfo = eventDateInfoRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("EventDateInfo not found with id: " + id));

        existingInfo.setEventDate(requestDto.getEventDate());
        existingInfo.setAllDay(requestDto.getIsAllDay());
        existingInfo.setEventTimeStart(requestDto.getEventTimeStart());
        existingInfo.setEventTimeEnd(requestDto.getEventTimeEnd());
        existingInfo.setPlace(requestDto.getIsPlace());
        existingInfo.setOnline(requestDto.getIsOnline());
        existingInfo.setLocation(requestDto.getLocation());
        existingInfo.setUrl(requestDto.getUrl());

        eventDateInfoRepo.save(existingInfo);

        return modelMapper.map(existingInfo, EventDateInfoResponseDto.class);
    }

    @Override
    public void deleteEventDateInfo(Long id) {

    }

    @Override
    public List<EventDateInfoResponseDto> getEventDateInfoByEvent(Long eventId) {
        Event event = eventRepo.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

        List<EventDateInfo> eventDateInfos = eventDateInfoRepo.findByEvent(event);

        return eventDateInfos.stream()
            .map(eventDateInfo -> modelMapper.map(eventDateInfo, EventDateInfoResponseDto.class))
            .collect(Collectors.toList());
    }

    @Override
    public List<EventDateInfoResponseDto> getEventDateInfoByDateRange(LocalDate startDate, LocalDate endDate) {
        return List.of();
    }

    @Override
    public List<EventDateInfoResponseDto> getAllOnlineEvents() {
        return List.of();
    }

    @Override
    public List<EventDateInfoResponseDto> getAllOfflineEvents() {
        return List.of();
    }

    @Override
    public List<EventDateInfoResponseDto> getEventsByLocation(String location) {
        return List.of();
    }

    @Override
    public List<EventDateInfoResponseDto> getEventsByUrl(String url) {
        return List.of();
    }
}
