package greencity.controller;

import greencity.dto.achievement.AchievementRequestDto;
import greencity.dto.achievement.AchievementResponseDto;
import greencity.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/achievement")
@RequiredArgsConstructor
public class AchievementController {
    private final AchievementService achievementService;

    @PostMapping
    public ResponseEntity<AchievementResponseDto> addNewAchievement(@RequestBody AchievementRequestDto newAchievement) {
        return ResponseEntity.status(HttpStatus.CREATED).body(achievementService.createAchievement(newAchievement));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AchievementResponseDto> getAchievementById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.FOUND).body(achievementService.getAchievementById(id));
    }

    @GetMapping("/byConditions")
    public ResponseEntity<List<AchievementResponseDto>> getAllAchievementsByConditionsCoincidence(
        @RequestParam String text) {
        List<AchievementResponseDto> foundAchievements = achievementService
            .getAchievementsByConditionsContainingText(text);
        if (foundAchievements.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(List.of());
        }

        return ResponseEntity.status(HttpStatus.OK)
            .body(foundAchievements);
    }

    @GetMapping("/byTypeContaining")
    public ResponseEntity<List<AchievementResponseDto>> getAllAchievementsByTypeCoincidence(@RequestParam String text) {

        List<AchievementResponseDto> foundAchievements = achievementService
            .getAchievementsByTypeContainingText(text);
        if (foundAchievements.isEmpty())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(List.of());

        return ResponseEntity.status(HttpStatus.OK)
            .body(foundAchievements);
    }

    @GetMapping("/byRateInterval")
    public ResponseEntity<List<AchievementResponseDto>> getAllByRateInterval(@RequestParam Integer startRate,
        @RequestParam Integer endRate) {
        List<AchievementResponseDto> foundAchievements = achievementService
            .getAchievementsByRequiredRatesBetween(startRate, endRate);
        if (foundAchievements.isEmpty())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(List.of());

        return ResponseEntity.status(HttpStatus.OK)
            .body(foundAchievements);

    }

    @GetMapping("/byType")
    public ResponseEntity<AchievementResponseDto> getByType(@RequestParam String type) {

        return ResponseEntity.status(HttpStatus.OK).body(achievementService.getAchievementByType(type));
    }

    @GetMapping("/byRate")
    public ResponseEntity<AchievementResponseDto> getByRate(@RequestParam Integer requiredRate) {

        return ResponseEntity.status(HttpStatus.OK)
            .body(achievementService.getAchievementByRequiredRate(requiredRate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AchievementResponseDto> updAchievementById(@PathVariable Long id,
        @RequestBody AchievementRequestDto achievementToUpd) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(achievementService.updateAchievement(id, achievementToUpd));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delAchievement(@PathVariable Long id) {

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(achievementService.deleteAchievement(id));
    }

}
