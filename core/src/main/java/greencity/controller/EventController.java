package greencity.controller;

import greencity.annotations.CurrentUser;
import greencity.dto.event.*;
import greencity.service.EventService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@Validated
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody EventRequestDto eventRequestDto,
        @CurrentUser Principal currentUser, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        eventRequestDto.setAuthorEmail(currentUser.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(eventRequestDto));
    }

    @GetMapping("/myEvents")
    public ResponseEntity<EventProfilePreviewPageable> getEventsByUser(@CurrentUser Principal currentUser,
        @Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(eventService.getAllUserEvents(currentUser.getName(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEventById(@CurrentUser Principal currentUser, @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(eventService.getEventById(id, currentUser.getName()).get());
    }

    @GetMapping("/myEvents/past")
    public ResponseEntity<EventProfilePreviewPageable> getPastEventsByUser(@CurrentUser Principal currentUser,
        @Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(eventService.getAllUserPastEvents(currentUser.getName(), pageable));
    }

    @GetMapping("/myEvents/live")
    public ResponseEntity<EventProfilePreviewPageable> getLiveEventsByUser(@CurrentUser Principal currentUser,
        @Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(eventService.getAllUserLiveEvents(currentUser.getName(), pageable));
    }

    @GetMapping("/myEvents/upcoming")
    public ResponseEntity<EventProfilePreviewPageable> getUpcomingEventsByUser(@CurrentUser Principal currentUser,
        @Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(eventService.getAllUserUpcomingEvents(currentUser.getName(), pageable));
    }

    @GetMapping("/myEvents/status/{status}")
    public ResponseEntity<EventProfilePreviewPageable> getAllUserEventsByStatus(@CurrentUser Principal currentUser,
        @PathVariable String status,
        @Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(eventService.getAllUserEventsByStatus(currentUser.getName(), status, pageable));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<?> update(@Valid @RequestBody EventUpdateDto eventUpdateDto, BindingResult result,
        @CurrentUser Principal currentUser, @PathVariable Long eventId) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        return ResponseEntity.status(HttpStatus.OK)
            .body(eventService.updateEvent(eventId, eventUpdateDto, currentUser.getName()));
    }

    @GetMapping
    public ResponseEntity<EventProfilePreviewPageable> getAllEvents(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(eventService.getAllEventsPageable(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<EventProfilePreviewPageable> searchEventByTittle(
        @RequestParam(required = false, defaultValue = "") String title,
        @Parameter(hidden = true) @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(eventService.getEventsByTitle(title, pageable));
    }
}
