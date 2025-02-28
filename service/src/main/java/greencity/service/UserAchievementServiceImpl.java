package greencity.service;

import greencity.dto.achievement.AchievementResponseDto;
import greencity.dto.userachievement.UserAchievementRequestDto;
import greencity.dto.userachievement.UserAchievementResponseDto;
import greencity.entity.Achievement;
import greencity.entity.User;
import greencity.entity.UserAchievement;
import greencity.exception.exceptions.*;
import greencity.mapping.AchievementMapper;
import greencity.mapping.UserAchievementMapper;
import greencity.repository.AchievementRepo;
import greencity.repository.UserAchievementRepo;
import greencity.repository.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class UserAchievementServiceImpl implements UserAchievementService {

    private final UserAchievementRepo userAchievementRepo;
    private final AchievementRepo achievementRepo;
    private final UserRepo userRepo;
    private final UserAchievementMapper userAchievementMapper;
    private final AchievementMapper achievementMapper;

    @Override
    @Transactional
    public UserAchievementResponseDto addUserAchievement(UserAchievementRequestDto userAchievementRequestDto) {

        User user = userRepo.findById(userAchievementRequestDto.getUserId())
            .orElseThrow(
                () -> new NotFoundException("Such a user is not found! "));
        Achievement achievement = achievementRepo.findById(userAchievementRequestDto.getAchievementId())
            .orElseThrow(
                () -> new NotFoundException("Such Achievement is not found !"));

        if (userAchievementRepo.findByUserAndAchievement(user.getId(), achievement.getId()).isPresent())
            throw new AchievementAlreadyExistsException("Such an achievement already exists for this user!");

        if (!checkWhetherAllPreviousAchievementsAreOpened(achievement, user))
            throw new AchievementUnlockingException(
                "You can't unlock this current Achievement before unlocking the previous ones! ");

        if (user.getRating() >= achievement.getRequiredRate()) {

            UserAchievement userAchievement = new UserAchievement();
            userAchievement.setAchievement(achievement);
            userAchievement.setUser(user);
            userAchievement.setIsActive(true);
            userAchievement = userAchievementRepo.save(userAchievement);

            return userAchievementMapper.convert(userAchievementRepo.save(userAchievement));
        }

        throw new NotEnoughRatingForAchievement("Not enough rating to get an Achievement " + achievement.getType());

    }

    @Override
    public List<UserAchievementResponseDto> getAllUserAchievements(Long userId) {

        User user = userRepo.findById(userId)
            .orElseThrow(
                () -> new NotFoundException("Such a user is not found! "));
        List<UserAchievement> userAchievements = userAchievementRepo.findByUser(user);
        /*
         * List<Achievement> achievementsForUser = userAchievements.stream()
         * .map(UserAchievement::getAchievement) .toList();
         * achievementsForUser.stream().map(achievementMapper::convert).toList();
         */

        return userAchievements.stream().map(userAchievementMapper::convert).toList();
    }

    @Override
    public UserAchievementResponseDto getUserAchievementByIds(UserAchievementRequestDto userAchievementRequestDto) {

        return userAchievementMapper.convert(userAchievementRepo
            .findByUserAndAchievement(userAchievementRequestDto.getUserId(),
                userAchievementRequestDto.getAchievementId())
            .orElseThrow(
                () -> new NotFoundException("Such a user doesn't have this achievement or" +
                    "a user doesn't exist ! ")));
    }

    private boolean checkWhetherAllPreviousAchievementsAreOpened(Achievement achievement, User user) {

        if (user == null || achievement == null) {
            throw new BadRequestException("Both user and achievement parameters must be provided");
        }

        List<UserAchievement> userAchievements = userAchievementRepo.findByUser(user);

        if (userAchievements.isEmpty()) {

            return achievement.getType().getRank() == 1;
        }

        List<Integer> userRanks = userAchievements.stream()
            .map(ua -> ua.getAchievement().getType().getRank())
            .sorted()
            .toList();

        if (userRanks.size() == 1) {
            int lastUnlockedRank = userRanks.get(0);
            return achievement.getType().getRank() - lastUnlockedRank == 1;
        }

        for (int i = 1; i < userRanks.size(); i++) {
            if (userRanks.get(i) - userRanks.get(i - 1) != 1) {
                return false;
            }
        }

        int lastUnlockedRank = userRanks.get(userRanks.size() - 1);
        int currentRank = achievement.getType().getRank();

        return (currentRank - lastUnlockedRank == 1);
    }

}
