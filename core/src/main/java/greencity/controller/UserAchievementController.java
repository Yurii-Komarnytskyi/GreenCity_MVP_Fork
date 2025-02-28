package greencity.controller;

import greencity.dto.achievement.AchievementResponseDto;
import greencity.dto.userachievement.UserAchievementRequestDto;
import greencity.dto.userachievement.UserAchievementResponseDto;
import greencity.service.UserAchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user-achievement")
@RequiredArgsConstructor
public class UserAchievementController {

    private final UserAchievementService userAchievementService;

    @PostMapping
    public ResponseEntity<UserAchievementResponseDto> addNewUserAchievement(
        @RequestBody UserAchievementRequestDto newUserAchievement) {

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userAchievementService.addUserAchievement(newUserAchievement));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<UserAchievementResponseDto>> getAllUserAchievements(@PathVariable Long userId) {

        List<UserAchievementResponseDto> foundUserAchievements = userAchievementService.getAllUserAchievements(userId);

        if (foundUserAchievements.isEmpty())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(List.of());

        return ResponseEntity.status(HttpStatus.OK).body(foundUserAchievements);
    }

    @GetMapping
    public ResponseEntity<UserAchievementResponseDto> getUserAchievement(@RequestParam Long userId,
        @RequestParam Long achievementId) {
        UserAchievementRequestDto userAchievementRequestDto = new UserAchievementRequestDto(userId, achievementId);

        return ResponseEntity.status(HttpStatus.OK)
            .body(userAchievementService.getUserAchievementByIds(userAchievementRequestDto));
    }

}
