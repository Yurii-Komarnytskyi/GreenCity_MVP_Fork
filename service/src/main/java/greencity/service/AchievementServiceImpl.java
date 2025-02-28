package greencity.service;

import greencity.dto.achievement.AchievementRequestDto;
import greencity.dto.achievement.AchievementResponseDto;
import greencity.entity.Achievement;
import greencity.mapping.AchievementMapper;
import greencity.mapping.AchievementRequestDtoMapper;
import greencity.repository.AchievementRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepo achievementRepo;
    private final AchievementRequestDtoMapper achievementRequestDtoMapper;
    private final AchievementMapper achievementMapper;

    @Override
    public AchievementResponseDto createAchievement(AchievementRequestDto achievementRequestDto) {

        Achievement newAchievement = achievementRequestDtoMapper.convert(achievementRequestDto);

        return achievementMapper
            .convert(achievementRepo.save(newAchievement));
    }

    @Override
    public AchievementResponseDto getAchievementById(Long id) {

        Achievement foundAchievement = findAchievementByIdPrivate(id);

        return achievementMapper
            .convert(achievementRepo.save(foundAchievement));
    }

    @Override
    public AchievementResponseDto getAchievementByType(String type) {

        return achievementMapper.convert(achievementRepo.findByType(type.toLowerCase()));
    }

    @Override
    public List<AchievementResponseDto> getAchievementsByRequiredRatesBetween(Integer startRate, Integer endRate) {

        List<Achievement> foundAchievements = achievementRepo.findAllByRequiredRateBetween(startRate, endRate);
        return foundAchievements.stream().map(achievementMapper::convert).toList();
    }

    @Override
    public AchievementResponseDto getAchievementByRequiredRate(Integer requiredRate) {

        return achievementMapper.convert(achievementRepo.findByRequiredRate(requiredRate));
    }

    @Override
    public List<AchievementResponseDto> getAchievementsByConditionsContainingText(String text) {

        return achievementRepo.findByConditionsContainingIgnoreCase(text)
            .stream().map(achievementMapper::convert)
            .toList();
    }

    @Override
    public List<AchievementResponseDto> getAchievementsByTypeContainingText(String text) {

        return achievementRepo.findByTypeContainingIgnoreCase(text)
            .stream().map(achievementMapper::convert)
            .toList();
    }

    @Override
    public AchievementResponseDto updateAchievement(Long id, AchievementRequestDto achievementRequestDtoUpd) {

        Achievement achievementToUpd = findAchievementByIdPrivate(id);

        achievementToUpd.setConditions(achievementRequestDtoUpd.getConditions());
        achievementToUpd.setType(achievementToUpd.getType());
        achievementToUpd.setRequiredRate(achievementRequestDtoUpd.getRequiredUserRating());

        return achievementMapper
            .convert(achievementRepo.save(achievementToUpd));
    }

    @Override
    public String deleteAchievement(Long id) {

        Achievement achievementToDel = findAchievementByIdPrivate(id);
        String achievementType = achievementToDel.getType().toString();
        achievementRepo.delete(achievementToDel);
        return achievementType + " was successfully deleted! ";
    }

    private Achievement findAchievementByIdPrivate(Long id) {

        return achievementRepo.findById(id).orElseThrow(
            () -> new EntityNotFoundException("Achievement is not found"));
    }
}
